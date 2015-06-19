package com.saltar.sample;

import com.saltar.annotations.Path;
import com.saltar.annotations.Response;
import com.saltar.annotations.ResponseHeaders;
import com.saltar.annotations.SaltarAction;
import com.saltar.annotations.Status;
import com.saltar.http.Header;
import com.saltar.http.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Created by dirong on 6/18/15.
 */
@SaltarAction(path = "/repos/{owner}/{repo}/contributors")
public class ExampleAction {

    @Path("owner")
    String owner;
    @Path("repo")
    String repo;
    @Response
    List<Contributor> contributors;
    @Response
    ResponseBody responseBody;
    @ResponseHeaders
    Map<String, String> headersMap;
    @ResponseHeaders
    List<Header> headers;
    @Status
    int status;

    public ExampleAction(String owner, String repo) {
        this.owner = owner;
        this.repo = repo;
    }

    public List<Contributor> getContributors() {
        return contributors;
    }

    public ResponseBody getResponseBody() {
        return responseBody;
    }

    public Map<String, String> getHeadersMap() {
        return headersMap;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public static class Contributor {
        String login;
        int contributions;
    }
}
