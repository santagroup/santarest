package com.santarest.http.client;

import com.santarest.http.ByteArrayBody;
import com.santarest.http.Header;
import com.santarest.http.HttpBody;
import com.santarest.http.Request;
import com.santarest.http.Response;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


/**
 * A {@link com.santarest.http.client.HttpClient} which uses an implementation of Apache's {@link HttpClient}.
 */
public class ApacheClient implements com.santarest.http.client.HttpClient {
    private static HttpClient createDefaultClient() {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT_MILLIS);
        HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT_MILLIS);
        return new DefaultHttpClient(params);
    }

    private final HttpClient client;

    /**
     * Creates an instance backed by {@link DefaultHttpClient}.
     */
    public ApacheClient() {
        this(createDefaultClient());
    }

    public ApacheClient(HttpClient client) {
        this.client = client;
    }

    @Override
    public Response execute(Request request) throws IOException {
        HttpUriRequest apacheRequest = createRequest(request);
        HttpResponse apacheResponse = execute(client, apacheRequest);
        return parseResponse(request.getUrl(), apacheResponse);
    }

    /**
     * Execute the specified {@code request} using the provided {@code client}.
     */
    protected HttpResponse execute(HttpClient client, HttpUriRequest request) throws IOException {
        return client.execute(request);
    }

    static HttpUriRequest createRequest(Request request) {
        if (request.getBody() != null) {
            return new GenericEntityHttpRequest(request);
        }
        return new GenericHttpRequest(request);
    }

    static Response parseResponse(String url, HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        int status = statusLine.getStatusCode();
        String reason = statusLine.getReasonPhrase();

        List<Header> headers = new ArrayList<Header>();
        String contentType = "application/octet-stream";
        for (org.apache.http.Header header : response.getAllHeaders()) {
            String name = header.getName();
            String value = header.getValue();
            if ("Content-Type".equalsIgnoreCase(name)) {
                contentType = value;
            }
            headers.add(new Header(name, value));
        }

        ByteArrayBody body = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            byte[] bytes = EntityUtils.toByteArray(entity);
            body = new ByteArrayBody(contentType, bytes);
        }

        return new Response(url, status, reason, headers, body);
    }

    private static class GenericHttpRequest extends HttpRequestBase {
        private final String method;

        public GenericHttpRequest(Request request) {
            method = request.getMethod();
            setURI(URI.create(request.getUrl()));

            // Add all headers.
            for (Header header : request.getHeaders()) {
                addHeader(new BasicHeader(header.getName(), header.getValue()));
            }
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    private static class GenericEntityHttpRequest extends HttpEntityEnclosingRequestBase {
        private final String method;

        GenericEntityHttpRequest(Request request) {
            super();
            method = request.getMethod();
            setURI(URI.create(request.getUrl()));

            // Add all headers.
            for (Header header : request.getHeaders()) {
                addHeader(new BasicHeader(header.getName(), header.getValue()));
            }

            // Add the content body.
            setEntity(new TypedOutputEntity(request.getBody()));
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    /**
     * Container class for passing an entire {@link HttpBody} as an {@link HttpEntity}.
     */
    static class TypedOutputEntity extends AbstractHttpEntity {
        final HttpBody requestBody;

        TypedOutputEntity(HttpBody requestBody) {
            this.requestBody = requestBody;
            setContentType(requestBody.mimeType());
        }

        @Override
        public boolean isRepeatable() {
            return true;
        }

        @Override
        public long getContentLength() {
            return requestBody.length();
        }

        @Override
        public InputStream getContent() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            requestBody.writeTo(out);
            return new ByteArrayInputStream(out.toByteArray());
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            requestBody.writeTo(out);
        }

        @Override
        public boolean isStreaming() {
            return false;
        }
    }
}
