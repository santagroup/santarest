package com.santarest;

import com.santarest.annotations.RestAction;
import com.santarest.converter.Converter;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.santarest.http.client.HttpClient;
import com.santarest.utils.Logger;
import com.santarest.ws.client.WSClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action1;
import rx.functions.Func1;

public class SantaRest {

    final static String HELPERS_FACTORY_CLASS_SIMPLE_NAME = "ActionHelperFactoryImpl";

    private final List<RequestInterceptor> requestInterceptors;
    private final List<ResponseListener> responseInterceptors;
    private final Converter converter;
    private final Logger logger;
    private final ActionAdapterFactory actionAdapterFactory;

    private SantaRest(Builder builder) {
        this.requestInterceptors = builder.requestInterceptors;
        this.responseInterceptors = builder.responseInterceptors;
        this.converter = builder.converter;
        this.logger = Defaults.getLogger();
        this.actionAdapterFactory = new ActionAdapterFactory(builder.baseUrl, builder.converter, builder.httpClient, builder.wsClient);
    }

    private <A> void sendAction(A action, Action1<A> callback) throws IOException {
        ActionAdapter adapter = actionAdapterFactory.make(action.getClass());
        if(adapter == null){
            throw new SantaRestException("Action object should be annotated by " + RestAction.class.getName() + " or check dependence of santarest-compiler");
        }
        adapter.send(action, callback);
    }

    public <A> Observable<A> createObservable(final A action) {
        return Observable
                .create(new CallOnSubscribe<A>(new Callback<Action1<A>>() {
                    @Override
                    public void call(Action1<A> callback) throws IOException {
                        sendAction(action, callback);
                    }
                }));
    }

    public <A> SantaRestExecutor<A> createExecutor(Class<A> actionClass, Scheduler scheduler) {
        return new SantaRestExecutor<A>(new Func1<A, Observable<A>>() {
            @Override
            public Observable<A> call(A action) {
                return createObservable(action);
            }
        }).scheduler(scheduler);
    }

    public <A> SantaRestExecutor<A> createExecutor(Class<A> actionClass) {
        return createExecutor(actionClass, null);
    }

    final private static class CallOnSubscribe<A> implements Observable.OnSubscribe<A> {

        private final Callback<Action1<A>> func;

        CallOnSubscribe(Callback<Action1<A>> func) {
            this.func = func;
        }

        @Override
        public void call(final Subscriber<? super A> subscriber) {
            try {
                func.call(new Action1<A>() {
                    @Override
                    public void call(A action) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(action);
                        }
                    }
                });
            } catch (final Exception e) {
                Exceptions.throwIfFatal(e);
                if (e instanceof SantaRestException) {
                    throw (SantaRestException) e;
                }
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }
    }

    public interface ActionHelper<T> {
        RequestBuilder fillRequest(RequestBuilder requestBuilder, T action);

        T onResponse(T action, Response response, Converter converter);
    }

    interface ActionHelperFactory {
        ActionHelper make(Class actionClass);
    }

    /**
     * Intercept every request before it is executed.
     */
    public interface RequestInterceptor {
        /**
         * Called for every request. You can add your data to builder before create request
         *
         * @param request
         */
        void intercept(RequestBuilder request);
    }

    /**
     * Intercept every response.
     */
    public interface ResponseListener<A> {
        /**
         * Called for every get response. You can get data from response after invoke it
         */
        void onResponseReceived(A action, Request request, Response response);

    }

    private interface Callback<A> {
        void call(A value) throws IOException;
    }

    public static class Builder {
        private String baseUrl;
        private HttpClient httpClient;
        private WSClient wsClient;
        private List<RequestInterceptor> requestInterceptors = new ArrayList<RequestInterceptor>();
        private List<ResponseListener> responseInterceptors = new ArrayList<ResponseListener>();
        private Converter converter;

        /**
         * API URL.
         */
        public Builder setBaseUrl(String baseUrl) {
            if (baseUrl == null || baseUrl.trim().length() == 0) {
                throw new IllegalArgumentException("baseUrl may not be blank.");
            }
            if(!baseUrl.contains("://")){
                throw new IllegalArgumentException("baseUrl may not be without scheme.");
            }
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setHttpClient(HttpClient httpClient) {
            if (httpClient == null) {
                throw new IllegalArgumentException("Client provider may not be null.");
            }
            this.httpClient = httpClient;
            return this;
        }

        public Builder setWSClient(WSClient wsClient) {
            if (httpClient == null) {
                throw new IllegalArgumentException("Client provider may not be null.");
            }
            this.wsClient = wsClient;
            return this;
        }

        public Builder addRequestInterceptor(RequestInterceptor requestInterceptor) {
            if (requestInterceptor == null) {
                throw new IllegalArgumentException("Request interceptor may not be null.");
            }
            this.requestInterceptors.add(requestInterceptor);
            return this;
        }

        public Builder addResponseInterceptor(ResponseListener responseListener) {
            if (responseListener == null) {
                throw new IllegalArgumentException("Request interceptor may not be null.");
            }
            this.responseInterceptors.add(responseListener);
            return this;
        }

        /**
         * The converter used for serialization and deserialization of objects.
         *
         * @see com.santarest.converter.GsonConverter
         */
        public Builder setConverter(Converter converter) {
            if (converter == null) {
                throw new IllegalArgumentException("Converter may not be null.");
            }
            this.converter = converter;
            return this;
        }

        /**
         * Create the {@link SantaRest} instance.
         */
        public SantaRest build() {
            if (baseUrl == null) {
                throw new IllegalArgumentException("Endpoint may not be null.");
            }
            fillDefaults();
            return new SantaRest(this);
        }

        private void fillDefaults() {
            if (converter == null) {
                converter = Defaults.getConverter();
            }
            if (httpClient == null) {
                httpClient = Defaults.getClient();
            }
            //TODO: add wsClient filling
        }
    }
}
