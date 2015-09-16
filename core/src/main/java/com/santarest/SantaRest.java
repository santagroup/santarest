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

public class SantaRest {

    final static String HELPERS_FACTORY_CLASS_SIMPLE_NAME = "ActionHelperFactoryImpl";
    private final static String HELPERS_FACTORY_CLASS_NAME = SantaRest.class.getPackage().getName() + "." + HELPERS_FACTORY_CLASS_SIMPLE_NAME;

    private final String serverUrl;
    private final HttpClient client;
    private final Executor executor;
    private final Executor callbackExecutor;
    private final RequestInterceptor requestInterceptor;
    private final List<ResponseListener> responseListeners;
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
        this.requestInterceptor = builder.requestInterceptor;
        this.responseListeners = builder.responseListeners;
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
            throw new RuntimeException(e);
        }
    }

    /**
     * Request will be performed in executing thread and return action with filled response fields.
     *
     * @param action any object annotated with
     * @see com.santarest.annotations.RestAction
     */
    public <A> A executeAction(A action) {
        ActionHelper<A> helper = getActionHelper(action.getClass());
        if (helper == null) {
            throw new IllegalArgumentException("Action object should be annotated by " + RestAction.class.getName());
        }
        RequestBuilder builder = new RequestBuilder(serverUrl, converter);
        requestInterceptor.intercept(builder);
        Request request = helper.createRequest(action, builder);
        try {
            String nameActionForlog = action.getClass().getSimpleName();
            logger.log("Start executing request %s", nameActionForlog);
            Response response = client.execute(request);
            logger.log("Received response of %s", nameActionForlog);
            action = helper.fillResponse(action, response, converter);
            for (ResponseListener interceptor : responseListeners) {
                interceptor.onResponseReceived(action, request, response);
            }
            logger.log("Filled response of %s using helper %s", nameActionForlog, helper.getClass().getSimpleName());
        } catch (Exception error) {
            logger.error("Failed action %s executing: %s", action.getClass().getSimpleName(), error.getMessage());
            action = helper.fillError(action, error);
        }
        return action;
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
            protected void doExecuteAction(A action) {
                executeAction(action);
            }
        });
    }

    /**
     * Subscribe to receiving filled actions after server response. Posting actions to subscriber is realised by ActionPoster
     *
     * @param subscriber
     * @see ActionPoster
     */
    public void subscribe(Object subscriber) {
        if (actionPoster != null) {
            actionPoster.subscribe(subscriber);
        }
    }

    public void unsubscribe(Object subscriber) {
        if (actionPoster != null) {
            actionPoster.unsubscribe(subscriber);
        }
    }

    private ActionHelper getActionHelper(Class actionClass) {
        ActionHelper helper = actionHelperCache.get(actionClass);
        if (helper == null) {
            synchronized (this) {
                helper = actionHelperFactory.make(actionClass);
                actionHelperCache.put(actionClass, helper);
            }
        }
        return helper;
    }

    public interface ActionHelper<T> {
        Request createRequest(T action, RequestBuilder requestBuilder);

        T fillResponse(T action, Response response, Converter converter);

        T fillError(T action, Throwable error);
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
                doExecuteAction(action);
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(action);
                    }
                });
            } catch (final Exception e) {
                callbackExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFail(action, e);
                    }
                });
            }
        }

        protected abstract void doExecuteAction(A action);
    }


    public static class Builder {
        private String serverUrl;
        private HttpClient client;
        private Executor executor;
        private Executor callbackExecutor;
        private RequestInterceptor requestInterceptor;
        private List<ResponseListener> responseListeners = new ArrayList<ResponseListener>();
        private Converter converter;
        private ActionPoster actionPoster;

        /**
         * API URL.
         */
        public Builder setServerUrl(String serverUrl) {
            if (serverUrl == null || serverUrl.trim().length() == 0) {
                throw new NullPointerException("Endpoint may not be blank.");
            }
            this.serverUrl = serverUrl;
            return this;
        }

        /**
         * The HTTP client used for requests.
         */
        public Builder setClient(HttpClient client) {
            if (client == null) {
                throw new NullPointerException("Client provider may not be null.");
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
                throw new NullPointerException("HTTP executor may not be null.");
            }
            this.executor = httpExecutor;
            return this;
        }

        public Builder setCallbackExecutor(Executor callbackExecutor) {
            if (callbackExecutor == null) {
                throw new NullPointerException("HTTP executor may not be null.");
            }
            this.callbackExecutor = callbackExecutor;
            return this;
        }

        public Builder setRequestInterceptor(RequestInterceptor requestInterceptor) {
            if (requestInterceptor == null) {
                throw new NullPointerException("Request interceptor may not be null.");
            }
            this.requestInterceptor = requestInterceptor;
            return this;
        }

        public Builder addResponseInterceptors(ResponseListener responseListener) {
            if (responseListener == null) {
                throw new NullPointerException("Request interceptor may not be null.");
            }
            this.responseListeners.add(responseListener);
            return this;
        }

        /**
         * The converter used for serialization and deserialization of objects.
         */
        public Builder setConverter(Converter converter) {
            if (converter == null) {
                throw new NullPointerException("Converter may not be null.");
            }
            this.converter = converter;
            return this;
        }

        public Builder setActionPoster(ActionPoster actionPoster) {
            if (actionPoster == null) {
                throw new NullPointerException("ActionPoster may not be null.");
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
            if (requestInterceptor == null) {
                requestInterceptor = Defaults.getDefaultRequestInterceptor();
            }
        }
    }
}
