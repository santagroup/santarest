package com.saltar;

import com.saltar.callback.ActionPoster;
import com.saltar.callback.Callback;
import com.saltar.client.HttpClient;
import com.saltar.converter.Converter;
import com.saltar.http.Request;
import com.saltar.http.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class Saltar {

    final static String HELPERS_FACTORY_CLASS_SIMPLE_NAME = "ActionHelperFactoryImpl";
    private final static String HELPERS_FACTORY_CLASS_NAME = Saltar.class.getPackage().getName() + "." + HELPERS_FACTORY_CLASS_SIMPLE_NAME;

    private final String serverUrl;
    private final HttpClient client;
    private final Executor executor;
    private final Executor callbackExecutor;
    private final RequestInterceptor requestInterceptor;
    private final Converter converter;
    private final ActionPoster actionPoster;

    private final Map<Class, ActionHelper> actionHelperCache = new HashMap<Class, ActionHelper>();
    private ActionHelperFactory actionHelperFactory;

    private Saltar(Builder builder) {
        this.serverUrl = builder.serverUrl;
        this.client = builder.client;
        this.executor = builder.executor;
        this.callbackExecutor = builder.callbackExecutor;
        this.requestInterceptor = builder.requestInterceptor;
        this.converter = builder.converter;
        this.actionPoster = builder.actionPoster;
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

    public <A> A executeAction(A action) {
        ActionHelper<A> helper = getActionHelper(action.getClass());
        if (helper == null) {
            throw new IllegalArgumentException("Action object should be annotated by @SaltarAction");
        }
        Request request = helper.createRequest(action, new RequestBuilder(serverUrl, converter));
        try {
            Response response = client.execute(request);
            action = helper.fillResponse(action, response, converter);
        } catch (Exception error) {
            action = helper.fillError(action, error);
        }
        return action;
    }

    public <A> void sendAction(final A action, Callback<A> callback) {
        CallbackWrapper<A> callbackWrapper = new CallbackWrapper<A>(actionPoster, callback);
        executor.execute(new CallbackRunnable<A>(action, callbackWrapper, callbackExecutor) {
            @Override
            protected void doExecuteAction(A action) {
                executeAction(action);
            }
        });
    }

    public <A> void sendAction(A action) {
        sendAction(action, null);
    }

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

    public static interface ActionHelper<T> {
        Request createRequest(T action, RequestBuilder requestBuilder);

        T fillResponse(T action, Response response, Converter converter);

        T fillError(T action, Throwable error);
    }

    static interface ActionHelperFactory {
        ActionHelper make(Class actionClass);
    }

    /**
     * Intercept every request before it is executed in order to add additional data.
     */
    public static interface RequestInterceptor {
        /**
         * Called for every request. Add data using methods on the supplied {@link RequestFacade}.
         */
        void intercept(Request request);


        /**
         * A {@link RequestInterceptor} which does no modification of requests.
         */
        RequestInterceptor NONE = new RequestInterceptor() {
            @Override
            public void intercept(Request request) {
                // Do nothing.
            }
        };
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

        /**
         * A request interceptor for adding data to every request.
         */
        public Builder setRequestInterceptor(RequestInterceptor requestInterceptor) {
            if (requestInterceptor == null) {
                throw new NullPointerException("Request interceptor may not be null.");
            }
            this.requestInterceptor = requestInterceptor;
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
         * Create the {@link Saltar} instance.
         */
        public Saltar build() {
            if (serverUrl == null) {
                throw new IllegalArgumentException("Server url may not be null.");
            }
            fillDefaults();
            return new Saltar(this);
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
            if (converter == null) {
                converter = Defaults.getConverter();
            }
            if (callbackExecutor == null) {
                callbackExecutor = Defaults.getDefaultCallbackExecutor();
            }
            if (requestInterceptor == null) {
                requestInterceptor = RequestInterceptor.NONE;
            }
            if (actionPoster == null) {
                actionPoster = Defaults.getDefualtActionPoster();
            }
        }
    }
}
