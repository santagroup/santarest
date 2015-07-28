package com.santarest.sample;

import com.santarest.annotations.Error;
import com.santarest.annotations.ErrorResponse;
import com.santarest.annotations.FieldMap;
import com.santarest.annotations.Path;
import com.santarest.annotations.Query;
import com.santarest.annotations.QueryMap;
import com.santarest.annotations.RequestHeader;
import com.santarest.annotations.RequestHeaders;
import com.santarest.annotations.Response;
import com.santarest.annotations.ResponseHeader;
import com.santarest.annotations.ResponseHeaders;
import com.santarest.annotations.RestAction;
import com.santarest.annotations.Status;
import com.santarest.http.Header;
import com.santarest.http.HttpBody;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestAction(value = "/repos/{owner}/{repo}/contributors",
        type = RestAction.Type.FORM_URL_ENCODED,
        method = RestAction.Method.GET)
public class ExampleAction extends BaseExampleAction {

    @Path("owner")
    Object ownerr;
    @Path("repo")
    Object repoo;
    @Query("repo")
    Object query;
    @RequestHeader("repo")
    Object requestHeader;

    @Response
    HttpBody responseBody;
    @Response
    List<Contributor> contributorss;
    @Response
    String string;

    @ResponseHeaders
    Map<String, String> headersMaps;
    @ResponseHeaders
    Header[] headersMaps2;
    @ResponseHeaders
    Collection<Header> headersMaps3;
    @ResponseHeaders
    List<Header> headersMaps4;

    @RequestHeaders
    List<Header> requestHeaders;
    @RequestHeaders
    Collection<Header> requestHeaders2;
    @RequestHeaders
    Map<String, Object> requestHeaders3;
    @RequestHeaders
    Header[] requestHeaders4;

    @QueryMap
    Map<String, Object> queryMap;

    @FieldMap
    Map<String, Object> fieldMap;

    @Status
    long status;

    @Status
    boolean success;

    @ResponseHeader("X-GitHub-Request-Id")
    String requestId;

    @Error
    Exception error;

    @ErrorResponse
    String errorResponse;

    public ExampleAction(String owner, String repo) {
        this.ownerr = owner;
        this.repoo = repo;
    }

    public List<Contributor> getContributors() {
        return contributorss;
    }

    public HttpBody getResponseBody() {
        return responseBody;
    }

    public Map<String, String> getHeadersMap() {
        return headersMaps;
    }


    public String getRequestId() {
        return requestId;
    }

    public static class Contributor {
        String login;
        int contributions;
    }

    @Override
    public String toString() {
        return "ExampleAction{" +
                "owner='" + ownerr + '\'' +
                ", repo='" + repoo + '\'' +
                ", contributors=" + contributorss +
                ", responseBody=" + responseBody +
                ", headersMap=" + headersMaps +
                ", headers=" + requestHeaders +
                ", status=" + status +
                '}';
    }
}
