/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.santarest.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An HTTP response.
 * <p/>
 * When used directly as a data type for an interface method, the response body is buffered to a
 * {@code byte[]}. Annotate the method with {@link retrofit.http.Streaming @Streaming} for an
 * unbuffered stream from the network.
 */
public final class Response {
    private final String url;
    private final int status;
    private final String reason;
    private final List<Header> headers;
    private final ResponseBody body;

    public Response(String url, int status, String reason, List<Header> headers, ResponseBody body) {
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
     * Status line code.
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
    public ResponseBody getBody() {
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
