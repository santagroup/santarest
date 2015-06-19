package com.saltar.sample;

import com.google.gson.reflect.TypeToken;
import com.saltar.RequestBuilder;
import com.saltar.Saltar;
import com.saltar.annotations.SaltarAction;
import com.saltar.converter.Converter;
import com.saltar.http.Header;
import com.saltar.http.Request;
import com.saltar.http.Response;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dirong on 6/18/15.
 */
public class ExampleActionHelper implements Saltar.ActionHelper<ExampleAction> {

    @Override
    public Request createRequest(ExampleAction action, RequestBuilder requestBuilder) {
        requestBuilder.setMethod(SaltarAction.Method.GET);
        requestBuilder.setRequestType(SaltarAction.Type.SIMPLE);
        requestBuilder.setPath("/repos/{owner}/{repo}/contributors");
        requestBuilder.addPathParam("owner", action.owner);
        requestBuilder.addPathParam("repo", action.repo);
        return requestBuilder.build();
    }

    @Override
    public ExampleAction fillResponse(ExampleAction action, Response response, Converter converter){
        action.status = response.getStatus();
        Type type = new TypeToken<List<ExampleAction.Contributor>>(){}.getType();
        action.contributors = (List<ExampleAction.Contributor>) converter.fromBody(response.getBody(), type);
        action.responseBody = response.getBody();
        action.headersMap = new HashMap<String, String>();
        for (Header header : response.getHeaders()) {
            action.headersMap.put(header.getName(), header.getValue());
        }
        action.headers = response.getHeaders();
        return action;
    }

}
