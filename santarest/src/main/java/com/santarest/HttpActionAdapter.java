package com.santarest;

import com.santarest.converter.Converter;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.santarest.http.client.HttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import rx.functions.Action1;

/**
 * Created by dirong on 2/8/16.
 */
class HttpActionAdapter implements ActionAdapter {

    private final static String HELPERS_FACTORY_CLASS_NAME = SantaRest.class.getPackage().getName() + "." + SantaRest.HELPERS_FACTORY_CLASS_SIMPLE_NAME;

    private SantaRest.ActionHelperFactory actionHelperFactory;
    private final Map<Class, SantaRest.ActionHelper> actionHelperCache = new HashMap<Class, SantaRest.ActionHelper>();

    private final HttpClient client;
    private final Converter converter;
    private final String serverUrl;

    static HttpActionAdapter create(HttpClient client, Converter converter, String serverUrl) {
        return new HttpActionAdapter(client, converter, serverUrl);
    }

    private HttpActionAdapter(HttpClient client, Converter converter, String serverUrl) {
        this.client = client;
        this.converter = converter;
        this.serverUrl = serverUrl;
        loadActionHelperFactory();
    }

    public <A> void send(A action, Action1<A> callback) throws IOException {
        final SantaRest.ActionHelper<A> helper = getActionHelper(action.getClass());
        if (helper == null) {
            throw new SantaRestException("Something was happened with code generator. Check dependence of santarest-compiler");
        }
        RequestBuilder builder = new RequestBuilder(serverUrl, converter);
        builder = helper.fillRequest(builder, action);
//        for (RequestInterceptor requestInterceptor : requestInterceptors) {
//            requestInterceptor.intercept(builder);
//        }
        Request request = builder.build();
//        String nameActionForlog = action.getClass().getSimpleName();
//        logger.log("Start executing request %s", nameActionForlog);
        Response response = client.execute(request);
//        logger.log("Received response of %s", nameActionForlog);
        action = helper.onResponse(action, response, converter);
//        for (ResponseListener listener : responseInterceptors) {
//            listener.onResponseReceived(action, request, response);
//        }
//        logger.log("Filled response of %s using helper %s", nameActionForlog, helper.getClass().getSimpleName());
        if (!response.isSuccessful()) { //throw exception to change action state
            throw new SantaHTTPException();
        }
        callback.call(action);
    }

    private SantaRest.ActionHelper getActionHelper(Class actionClass) {
        SantaRest.ActionHelper helper = actionHelperCache.get(actionClass);
        if (helper == null && actionHelperFactory != null) {
            synchronized (actionHelperFactory) {
                helper = actionHelperFactory.make(actionClass);
                actionHelperCache.put(actionClass, helper);
            }
        }
        return helper;
    }

    private void loadActionHelperFactory() {
        try {
            Class<? extends SantaRest.ActionHelperFactory> clazz
                    = (Class<? extends SantaRest.ActionHelperFactory>) Class.forName(HELPERS_FACTORY_CLASS_NAME);
            actionHelperFactory = clazz.newInstance();
        } catch (Exception e) {
            //do nothing. actionHelperFactory will be checked on run action
        }
    }

}
