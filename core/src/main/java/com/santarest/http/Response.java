package com.santarest.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Response {
    private final String url;
    private final int status;
    private final String reason;
    private final List<Header> headers;
    private final HttpBody body;

    public Response(String url, int status, String reason, List<Header> headers, HttpBody body) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (status < 200) {
            throw new IllegalArgumentException("Invalid status code: " + status);
        }
        if (reason == null) {
            throw new IllegalArgumentException("reason == null");
        }
        if (headers == null) {
            throw new IllegalArgumentException("headers == null");
        }

        this.url = url;
        this.status = status;
        this.reason = reason;
        this.headers = Collections.unmodifiableList(new ArrayList<Header>(headers));
        this.body = body;
    }

    /**
     * Request URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Status code.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns true if the code is in [200..300), which means the request was
     * successfully received, understood, and accepted.
     */
    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }

    /**
     * Status line reason phrase.
     */
    public String getReason() {
        return reason;
    }

    /**
     * An unmodifiable collection of headers.
     */
    public List<Header> getHeaders() {
        return headers;
    }

    /**
     * Response body. May be {@code null}.
     */
    public HttpBody getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Response{" +
                "url='" + url + '\'' +
                ", status=" + status +
                ", reason='" + reason + '\'' +
                ", headers=" + headers +
                ", body=" + body +
                '}';
    }
}
