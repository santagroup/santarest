package com.santarest;

import com.santarest.annotations.RestAction;
import com.santarest.callback.ActionPoster;
import com.santarest.callback.Callback;
import com.santarest.client.HttpClient;
import com.santarest.converter.Converter;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.santarest.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;

public class SantaRest {

    final static String HELPERS_FACTORY_CLASS_SIMPLE_NAME = "ActionHelperFactoryImpl";
    private final static String HELPERS_FACTORY_CLASS_NAME = SantaRest.class.getPackage().getName() + "." + HELPERS_FACTORY_CLASS_SIMPLE_NAME;

    private final String serverUrl;
    private final HttpClient client;
    private final Executor executor;
    private final Executor callbackExecutor;
    private final List<RequestInterceptor> requestInterceptors;
    private final List<ResponseListener> responseInterceptors;
    private final Converter converter;
    private final ActionPoster actionPoster;
    private final Logger logger;

    private final Map<Class, ActionHelper> actionHelperCache = new HashMap<Class, ActionHelper>();
    private ActionHelperFactory actionHelperFactory;

    private SantaRest(Builder builder) {
        this.serverUrl = builder.serverUrl;
        this.client = builder.client;
        this.executor = builder.executor;
        this.callbackExecutor = builder.callbackExecutor;
        this.requestInterceptors = builder.requestInterceptors;
        this.responseInterceptors = builder.responseInterceptors;
        this.converter = builder.converter;
        this.actionPoster = builder.actionPoster;
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
    public <A> A runAction(A action) {
        ActionHelper<A> helper = getActionHelper(action.getClass());
        if (helper == null) {
            throw new SantaRestException("Action object should be annotated by " + RestAction.class.getName() + " or check dependence of santarest-compiler");
        }
        RequestBuilder builder = new RequestBuilder(serverUrl, converter);
        builder = helper.fillRequest(builder, action);
        for (RequestInterceptor requestInterceptor : requestInterceptors) {
            requestInterceptor.intercept(builder);
        }
        Request request = builder.build();
        try {
            String nameActionForlog = action.getClass().getSimpleName();
            logger.log("Start executing request %s", nameActionForlog);
            Response response = client.execute(request);
            logger.log("Received response of %s", nameActionForlog);
            action = helper.onResponse(action, response, converter);
            for (ResponseListener listener : responseInterceptors) {
                listener.onResponseReceived(action, request, response);
            }
            logger.log("Filled response of %s using helper %s", nameActionForlog, helper.getClass().getSimpleName());
        } catch (Exception error) {
            logger.error("Failed action %s executing", action.getClass().getSimpleName());
            for (StackTraceElement element : error.getStackTrace()) {
                logger.error("%s", element.toString());
            }
            action = helper.onError(action, error);
        }
        return action;
    }

    public <A> Observable<A> createObservable(final A... actions) {
        return Observable.from(actions)
                .doOnNext(new Action1<A>() {
                    @Override
                    public void call(A a) {
                        runAction(a);
                    }
                });
    }

    public <A> SantaRestExecutor<A> createExecutor(final A... actions) {
        return new SantaRestExecutor<A>(new Func0<Observable<A>>() {
            @Override
            public Observable<A> call() {
                return createObservable(actions);
            }
        });
    }

    /**
     * Request will be performed in working thread
     *
     * @param action any object annotated with
     * @see com.santarest.annotations.RestAction
     */
    public <A> void sendAction(A action) {
        sendAction(action, null);
    }

    /**
     * Request will be performed in working thread
     *
     * @param action any object annotated with
     * @see com.santarest.annotations.RestAction
     */
    public <A> void sendAction(final A action, Callback<A> callback) {
        CallbackWrapper<A> callbackWrapper = new CallbackWrapper<A>(actionPoster, callback);
        executor.execute(new CallbackRunnable<A>(action, callbackWrapper, callbackExecutor) {
            @Override
            protected void doAction(A action) {
                runAction(action);
            }
        });
    }

    /**
     * Subscribe to receiving filled actions after server response.
     * Posting actions to subscriber is realised by ActionPoster
     *
     * @param subscriber
     * @see ActionPoster
     */
    public void subscribe(Object subscriber) {
        if (actionPoster != null) {
            actionPoster.subscribe(subscriber);
        }
    }

    /**
     * Should be used for unsubscribing objects, which shouldn't anymore receive events.
     *
     * @param subscriber
     * @see ActionPoster
     */
    public void unsubscribe(Object subscriber) {
        if (actionPoster != null) {
            actionPoster.unsubscribe(subscriber);
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

        T onError(T action, Throwable error);
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

    private static class CallbackWrapper<A> implements Callback<A> {

        private final ActionPoster actionPoster;
        private final Callback<A> callback;

        private CallbackWrapper(ActionPoster actionPoster, Callback<A> callback) {
            this.actionPoster = actionPoster;
            this.callback = callback;
        }

        @Override
        public void onSuccess(A a) {
            if (callback != null) {
                callback.onSuccess(a);
            }
            if (actionPoster != null) {
                actionPoster.post(a);
            }
        }

        @Override
        public void onFail(A action, Exception error) {
            if (callback != null) {
                callback.onFail(action, error);
            }
            if (actionPoster != null) {
                actionPoster.post(action);
            }
        }
    }

    private static abstract class CallbackRunnable<A> implements Runnable {
        private final Callback<A> callback;
        private final Executor callbackExecutor;
        private final A action;

        private CallbackRunnable(A action, Callback<A> callback, Executor callbackExecutor) {
            this.action = action;
            this.callback = callback;
            this.callbackExecutor = callbackExecutor;
        }

        @Override
        public final void run() {
            try {
                doAction(action);
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(action);
                    }
                });
            } catch (SantaRestException e) {
                throw e;
            } catch (final Exception e) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFail(action, e);
                    }
                });
            }
        }

        protected abstract void doAction(A action);
    }


    public static class Builder {
        private String serverUrl;
        private HttpClient client;
        private Executor executor;
        private Executor callbackExecutor;
        private List<RequestInterceptor> requestInterceptors = new ArrayList<RequestInterceptor>();
        private List<ResponseListener> responseInterceptors = new ArrayList<ResponseListener>();
        private Converter converter;
        private ActionPoster actionPoster;

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

        /**
         * Executors used for asynchronous HTTP client downloads and callbacks.
         *
         * @param httpExecutor Executor on which HTTP client calls will be made.
         */
        public Builder setExecutor(Executor httpExecutor) {
            if (httpExecutor == null) {
                throw new IllegalArgumentException("HTTP executor may not be null.");
            }
            this.executor = httpExecutor;
            return this;
        }

        public Builder setCallbackExecutor(Executor callbackExecutor) {
            if (callbackExecutor == null) {
                throw new IllegalArgumentException("HTTP executor may not be null.");
            }
            this.callbackExecutor = callbackExecutor;
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
         * For example action poster wrapper of buses
         *
         * @see de.greenrobot.event.EventBus
         * @see com.squareup.otto.Bus
         */
        public Builder setActionPoster(ActionPoster actionPoster) {
            if (actionPoster == null) {
                throw new IllegalArgumentException("ActionPoster may not be null.");
            }
            this.actionPoster = actionPoster;
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
            if (executor == null) {
                executor = Defaults.getDefaultHttpExecutor();
            }
            if (callbackExecutor == null) {
                callbackExecutor = Defaults.getDefaultCallbackExecutor();
            }
            if (actionPoster == null) {
                actionPoster = Defaults.getDefualtActionPoster();
            }
        }
    }
}
