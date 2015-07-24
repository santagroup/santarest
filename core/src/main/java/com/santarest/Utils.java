package com.santarest;

import com.santarest.http.ByteArrayBody;
import com.santarest.http.ByteBody;
import com.santarest.http.Request;
import com.santarest.http.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

final class Utils {
    private static final int BUFFER_SIZE = 0x1000;

    /**
     * Creates a {@code byte[]} from reading the entirety of an {@link InputStream}. May return an
     * empty array but never {@code null}.
     * <p/>
     * Copied from Guava's {@code ByteStreams} class.
     */
    static byte[] streamToBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (stream != null) {
            byte[] buf = new byte[BUFFER_SIZE];
            int r;
            while ((r = stream.read(buf)) != -1) {
                baos.write(buf, 0, r);
            }
        }
        return baos.toByteArray();
    }

    /**
     * Conditionally replace a {@link Request} with an identical copy whose body is backed by a
     * byte[] rather than an input stream.
     */
    static Request readBodyToBytesIfNecessary(Request request) throws IOException {
        ByteBody body = request.getBody();
        if (body == null || body instanceof ByteArrayBody) {
            return request;
        }

        String bodyMime = body.mimeType();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        body.writeTo(baos);
        body = new ByteArrayBody(bodyMime, baos.toByteArray());

        return new Request(request.getMethod(), request.getUrl(), request.getHeaders(), body);
    }

    /**
     * Conditionally replace a {@link Response} with an identical copy whose body is backed by a
     * byte[] rather than an input stream.
     */
    static Response readBodyToBytesIfNecessary(Response response) throws IOException {
        ByteBody body = response.getBody();
        if (body == null || body instanceof ByteArrayBody) {
            return response;
        }

        String bodyMime = body.mimeType();
        InputStream is = body.in();
        try {
            byte[] bodyBytes = Utils.streamToBytes(is);
            body = new ByteArrayBody(bodyMime, bodyBytes);

            return replaceResponseBody(response, body);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    static Response replaceResponseBody(Response response, ByteBody body) {
        return new Response(response.getUrl(), response.getStatus(), response.getReason(),
                response.getHeaders(), body);
    }

    private Utils() {
        // No instances.
    }
}
