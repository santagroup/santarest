package com.santarest;

import com.santarest.annotations.RestAction;
import com.santarest.converter.Converter;
import com.santarest.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dirong on 2/8/16.
 */
public class ActionAdapterFactory {

    private final String baseUrl;
    private final Converter converter;
    private final HttpClient httpClient;

    private final Map<Class, ActionAdapter> adaptersCache = new HashMap<Class, ActionAdapter>();

    public ActionAdapterFactory(String baseUrl, Converter converter, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.converter = converter;
        this.httpClient = httpClient;
    }

    public ActionAdapter make(Class actionClass) {
        if (actionClass.getAnnotation(RestAction.class) != null) {
            return getAdapter(RestAction.class);
        }
        //TODO: add get adapter for annotation SocketAction
        return null;
    }

    private ActionAdapter create(Class annotationClass) {
        if (annotationClass == RestAction.class) {
            return HttpActionAdapter.create(httpClient, converter, baseUrl);
        }
        //TODO: add initialisation socket adapter
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
