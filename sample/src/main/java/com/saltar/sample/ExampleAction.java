package com.saltar.sample;

import com.saltar.annotations.Path;
import com.saltar.annotations.Response;
import com.saltar.annotations.ResponseHeaders;
import com.saltar.annotations.SaltarAction;
import com.saltar.annotations.SaltarAction.Type;
import com.saltar.annotations.Status;
import com.saltar.http.Header;
import com.saltar.http.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Created by dirong on 6/18/15.
 */
@SaltarAction(path = "/repos/{owner}/{repo}/contributors", type = Type.SIMPLE, headers = {}, value = SaltarAction.Method.PATCH)
public class ExampleAction {

    @Path("owner")
    String ownerr;
    @Path("repo")
    String repoo;
    @Response
    ResponseBody responseBodys;
    @Response
    List<Contributor> contributorss;
    @ResponseHeaders
    Map<String, String> headersMaps;
    @ResponseHeaders
    List<Header> headerss;

    @Status
    int statuss;

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

    public List<Header> getHeaders() {
        return headerss;
    }

    public static class Contributor {
        String login;
        int contributions;
    }
}
