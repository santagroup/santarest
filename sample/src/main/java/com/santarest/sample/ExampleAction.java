package com.santarest.sample;

import com.santarest.annotations.*;
import com.santarest.annotations.Error;
import com.santarest.http.Header;
import com.santarest.http.ResponseBody;

import java.util.List;
import java.util.Map;

@RestAction(value = "/repos/{owner}/{repo}/contributors",
              type = RestAction.Type.SIMPLE,
              method = RestAction.Method.GET)
public class ExampleAction extends BaseExampleAction{

    @Path("owner")
    String ownerr;
    @Path("repo")
    String repoo;
    @Response
    ResponseBody responseBodys;
    @Response
    List<Contributor> contributorss;
    @Response
    String string;
    @ResponseHeaders
    Map<String, String> headersMaps;
    @RequestHeaders
    List<Header> requestHeaders;
    @Status
    long status;

    @Status
    boolean success;

//    @RequestHeader("X-GitHub-Request-Id")
//    String requestIdRequest;
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

    public ResponseBody getResponseBody() {
        return responseBodys;
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
                ", responseBody=" + responseBodys +
                ", headersMap=" + headersMaps +
                ", headers=" + requestHeaders +
                ", status=" + status +
                '}';
    }
}
