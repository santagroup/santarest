package com.santarest;

import com.santarest.annotations.RestAction;
import com.santarest.annotations.WSAction;
import com.santarest.converter.Converter;
import com.santarest.http.client.HttpClient;
import com.santarest.ws.client.WSClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dirong on 2/8/16.
 */
public class ActionAdapterFactory {

    private final String baseUrl;
    private final Converter converter;
    private final HttpClient httpClient;
    private final WSClient wsClient;

    private final Map<Class, ActionAdapter> adaptersCache = new HashMap<Class, ActionAdapter>();

    public ActionAdapterFactory(String baseUrl, Converter converter, HttpClient httpClient, WSClient wsClient) {
        this.baseUrl = baseUrl;
        this.converter = converter;
        this.httpClient = httpClient;
        this.wsClient = wsClient;
    }

    public ActionAdapter make(Class actionClass) {
        if (actionClass.getAnnotation(RestAction.class) != null) {
            return getAdapter(RestAction.class);
        }
        if (actionClass.getAnnotation(WSAction.class) != null) {
            return getAdapter(WSAction.class);
        }
        return null;
    }

    private ActionAdapter create(Class annotationClass) {
        if (annotationClass == RestAction.class) {
            return HttpActionAdapter.create(httpClient, converter, baseUrl);
        }
        if (annotationClass == WSAction.class) {
            return WSActionAdapter.create(wsClient, converter, baseUrl);
        }
        return null;
    }

    private ActionAdapter getAdapter(Class annotationClass) {
        ActionAdapter adapter = adaptersCache.get(annotationClass);
        if (adapter == null) {
            synchronized (adaptersCache) {
                adapter = create(annotationClass);
                if (adapter != null) {
                    adaptersCache.put(annotationClass, adapter);
                }
            }
        }
        return adapter;
    }

}
