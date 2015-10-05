package com.santarest.sample;

import com.santarest.annotations.Error;
import com.santarest.annotations.Path;
import com.santarest.annotations.Query;
import com.santarest.annotations.RequestHeader;
import com.santarest.annotations.Response;
import com.santarest.annotations.ResponseHeader;
import com.santarest.annotations.RestAction;
import com.santarest.annotations.Status;
import com.santarest.http.HttpBody;

import java.util.List;

@RestAction(value = "/repos/{owner}/{repo}/contributors",
        type = RestAction.Type.SIMPLE,
        method = RestAction.Method.GET)
public class ExampleAction extends BaseExampleAction {

    @Path("owner")
    Object ownerr;
    @Path("repo")
    Object repoo;
    @Query("repo")
    int query;
    @Query("repo2")
    boolean query2;
    @Query("repo3")
    double query3;

    @Response
    HttpBody responseBody;
    @Response
    List<Contributor> contributorss;
    @Response
    String string;
    @Response(404)
    String errorResponse404;
    @Response(401)
    ErrorMessage errorResponse401;

    @Status
    long status;
    @Status
    boolean success;

    @ResponseHeader("X-GitHub-Request-Id")
    String responseId;

    @RequestHeader("X-GitHub-Request-Id")
    String requestId;

    @Error
    Exception error;

    @Response
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
                ", status=" + status +
                '}';
    }
}
