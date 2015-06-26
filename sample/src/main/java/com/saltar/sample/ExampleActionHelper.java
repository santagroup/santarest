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
        requestBuilder.addPathParam("owner", action.ownerr);
        requestBuilder.addPathParam("repo", action.repoo);
        return requestBuilder.build();
    }

    @Override
    public ExampleAction fillResponse(ExampleAction action, Response response, Converter converter){
        action.status = response.getStatus();
        action.success = response.getStatus() >= 200 && response.getStatus() < 300;
        Type type = new TypeToken<List<ExampleAction.Contributor>>(){}.getType();
        action.contributorss = (List<ExampleAction.Contributor>) converter.fromBody(response.getBody(), type);
        action.responseBodys = response.getBody();
        action.headersMaps = new HashMap<String, String>();
        for (Header header : response.getHeaders()) {
            action.headersMaps.put(header.getName(), header.getValue());
            if("X-GitHub-Request-Id".equals(header.getName())){
                action.requestId = header.getValue();
            }
        }
        action.headerss = response.getHeaders();
        return action;
    }

    @Override
    public ExampleAction fillError(ExampleAction action, Throwable error) {
        return action;
    }

}
