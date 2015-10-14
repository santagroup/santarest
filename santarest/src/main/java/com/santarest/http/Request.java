package com.santarest.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class Request {

    private final String method;
    private final String url;
    private final List<Header> headers;
    private final HttpBody body;

    public Request(String method, String url, List<Header> headers, HttpBody body) {
        if (method == null) {
            throw new NullPointerException("Method must not be null.");
        }
        if (url == null) {
            throw new NullPointerException("URL must not be null.");
        }
        this.method = method;
        this.url = url;

        if (headers == null) {
            this.headers = Collections.emptyList();
        } else {
            this.headers = Collections.unmodifiableList(new ArrayList<Header>(headers));
        }

        this.body = body;
    }

    /**
     * HTTP method verb.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Target URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns an unmodifiable list of headers, never {@code null}.
     */
    public List<Header> getHeaders() {
        return headers;
    }

    /**
     * Returns the request body or {@code null}.
     */
    public HttpBody getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Request{" +
                "body=" + body +
                ", headers=" + headers +
                ", url='" + url + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
