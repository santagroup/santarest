package com.saltar;

import com.saltar.client.HttpClient;
import com.saltar.converter.Converter;
import com.saltar.http.Request;
import com.saltar.http.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class Saltar {

    private String serverUrl;
    private HttpClient client;
    private Executor executor;
    private RequestInterceptor requestInterceptor;
    private Converter converter;

    private final Map<Class, ActionHelper> actionHelperCache = new HashMap<Class, ActionHelper>();
    private ActionHelperFactory actionHelperFactory;

    private Saltar(Builder builder) {
        this.serverUrl = builder.serverUrl;
        this.client = builder.client;
        this.executor = builder.executor;
        this.requestInterceptor = builder.requestInterceptor;
        this.converter = builder.converter;
        loadRequestCreatorProvider();
    }

    private void loadRequestCreatorProvider() {
        try {
            Class<? extends ActionHelperFactory> clazz
                    = (Class<? extends ActionHelperFactory>) Class.forName("com.saltar.ActionHelperFactoryImpl");
            actionHelperFactory = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("It failed to create the Saltar");//TODO: add sаltar exception
        }
    }

    public <A> A executeAction(A action) {
        Class actionClass = action.getClass();
        ActionHelper<A> helper = actionHelperCache.get(actionClass);
        if (helper == null) {
            synchronized (this) {
                helper = actionHelperFactory.make(action.getClass());
                actionHelperCache.put(actionClass, helper);
            }
        }
        Request request = helper.createRequest(action, new RequestBuilder(serverUrl, converter));
        Response response = invokeRequest(request);
        action = helper.fillResponse(action, response, converter);
        return action;
    }

    private Response invokeRequest(Request request) {
        try {
            Response response = client.execute(request);
            return response;
        } catch (IOException e) {
            throw new RuntimeException("invoke request exception");//TODO: add sаltar exception
        }
    }

    public static interface ActionHelper<T> {
        Request createRequest(T action, RequestBuilder requestBuilder);

        T fillResponse(T action, Response response, Converter converter);
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


    public static class Builder {
        private String serverUrl;
        private HttpClient client;
        private Executor executor;
        private RequestInterceptor requestInterceptor;
        private Converter converter;

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

        /**
         * Create the {@link Saltar} instance.
         */
        public Saltar build() {
            if (serverUrl == null) {
                throw new IllegalArgumentException("Endpoint may not be null.");
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
//            if (callbackExecutor == null) {
//                callbackExecutor = Platform.make().defaultCallbackExecutor();
//            }
            if (requestInterceptor == null) {
                requestInterceptor = RequestInterceptor.NONE;
            }
        }
    }
}
