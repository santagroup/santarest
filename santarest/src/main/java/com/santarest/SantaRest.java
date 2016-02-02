package com.santarest;

import com.santarest.annotations.RestAction;
import com.santarest.client.HttpClient;
import com.santarest.converter.Converter;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.santarest.utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

public class SantaRest {

    final static String HELPERS_FACTORY_CLASS_SIMPLE_NAME = "ActionHelperFactoryImpl";
    private final static String HELPERS_FACTORY_CLASS_NAME = SantaRest.class.getPackage().getName() + "." + HELPERS_FACTORY_CLASS_SIMPLE_NAME;

    private final String serverUrl;
    private final HttpClient client;
    private final List<RequestInterceptor> requestInterceptors;
    private final List<ResponseListener> responseInterceptors;
    private final Converter converter;
    private final Logger logger;

    private final Map<Class, ActionHelper> actionHelperCache = new HashMap<Class, ActionHelper>();
    private ActionHelperFactory actionHelperFactory;

    private SantaRest(Builder builder) {
        this.serverUrl = builder.serverUrl;
        this.client = builder.client;
        this.requestInterceptors = builder.requestInterceptors;
        this.responseInterceptors = builder.responseInterceptors;
        this.converter = builder.converter;
        this.logger = Defaults.getLogger();
        loadActionHelperFactory();
    }

    private void loadActionHelperFactory() {
        try {
            Class<? extends ActionHelperFactory> clazz
                    = (Class<? extends ActionHelperFactory>) Class.forName(HELPERS_FACTORY_CLASS_NAME);
            actionHelperFactory = clazz.newInstance();
        } catch (Exception e) {
            //do nothing. actionHelperFactory will be checked on run action
        }
    }

    /**
     * Request will be performed in executing thread and return action with filled response fields.
     *
     * @param action any object annotated with
     * @see com.santarest.annotations.RestAction
     */
    private <A> A runAction(A action) throws IOException {
        final ActionHelper<A> helper = getActionHelper(action.getClass());
        if (helper == null) {
            throw new SantaRestException("Action object should be annotated by " + RestAction.class.getName() + " or check dependence of santarest-compiler");
        }
        RequestBuilder builder = new RequestBuilder(serverUrl, converter);
        builder = helper.fillRequest(builder, action);
        for (RequestInterceptor requestInterceptor : requestInterceptors) {
            requestInterceptor.intercept(builder);
        }
        Request request = builder.build();
        String nameActionForlog = action.getClass().getSimpleName();
        logger.log("Start executing request %s", nameActionForlog);
        Response response = client.execute(request);
        logger.log("Received response of %s", nameActionForlog);
        action = helper.onResponse(action, response, converter);
        for (ResponseListener listener : responseInterceptors) {
            listener.onResponseReceived(action, request, response);
        }
        logger.log("Filled response of %s using helper %s", nameActionForlog, helper.getClass().getSimpleName());
        return action;
    }

    public <A> Observable<A> createObservable(final A action) {
        return Observable
                .create(new CallOnSubscribe<A>(new Callable<A>() {
                    @Override
                    public A call() throws Exception {
                        return runAction(action);
                    }
                }));
    }

    public <A> SantaRestExecutor<A> createExecutor(Scheduler subscribeOn, Scheduler observeOn) {
        return new SantaRestExecutor<A>(new Func1<A, Observable<A>>() {
            @Override
            public Observable<A> call(A action) {
                return createObservable(action);
            }
        }).observeOn(observeOn).subscribeOn(subscribeOn);
    }

    public <A> SantaRestExecutor<A> createExecutor(){
        return createExecutor(null, null);
    }

    final private static class CallOnSubscribe<A> implements Observable.OnSubscribe<A> {

        private final Callable<A> func;

        CallOnSubscribe(Callable<A> func) {
            this.func = func;
        }

        @Override
        public void call(Subscriber<? super A> subscriber) {
            try {
                A action = func.call();
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(action);
                }
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

    private ActionHelper getActionHelper(Class actionClass) {
        ActionHelper helper = actionHelperCache.get(actionClass);
        if (helper == null && actionHelperFactory != null) {
            synchronized (this) {
                helper = actionHelperFactory.make(actionClass);
                actionHelperCache.put(actionClass, helper);
            }
        }
        return helper;
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

    public static class Builder {
        private String serverUrl;
        private HttpClient client;
        private List<RequestInterceptor> requestInterceptors = new ArrayList<RequestInterceptor>();
        private List<ResponseListener> responseInterceptors = new ArrayList<ResponseListener>();
        private Converter converter;

        /**
         * API URL.
         */
        public Builder setServerUrl(String serverUrl) {
            if (serverUrl == null || serverUrl.trim().length() == 0) {
                throw new IllegalArgumentException("Endpoint may not be blank.");
            }
            this.serverUrl = serverUrl;
            return this;
        }

        /**
         * The HTTP client used for requests.
         */
        public Builder setClient(HttpClient client) {
            if (client == null) {
                throw new IllegalArgumentException("Client provider may not be null.");
            }
            this.client = client;
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
            if (serverUrl == null) {
                throw new IllegalArgumentException("Server url may not be null.");
            }
            fillDefaults();
            return new SantaRest(this);
        }

        private void fillDefaults() {
            if (converter == null) {
                converter = Defaults.getConverter();
            }
            if (client == null) {
                client = Defaults.getClient();
            }
        }
    }
}
