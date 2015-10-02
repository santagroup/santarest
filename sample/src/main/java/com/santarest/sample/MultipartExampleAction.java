package com.santarest.sample;

import com.santarest.annotations.Error;
import com.santarest.annotations.Part;
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
import com.santarest.http.ByteArrayBody;
import com.santarest.http.Header;
import com.santarest.http.HttpBody;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestAction(value = "/repos/{owner}/{repo}/contributors",
        type = RestAction.Type.MULTIPART,
        method = RestAction.Method.POST)
public class MultipartExampleAction extends BaseExampleAction {

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
    @RequestHeader("repo")
    Object requestHeader;

    @Part("file")
    File file;
    @Part("string")
    String stringPart;
    @Part("byte")
    byte[] bytes;
    @Part("byte")
    ByteArrayBody arrayBody;

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

    @Status
    long status;
    @Status
    boolean success;

    @ResponseHeader("X-GitHub-Request-Id")
    String requestId;

    @Error
    Exception error;

    @Response
    String errorResponse;

    public MultipartExampleAction(String owner, String repo) {
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
