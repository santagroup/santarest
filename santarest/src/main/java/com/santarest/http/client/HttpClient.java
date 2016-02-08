package com.santarest.http.client;


import com.santarest.http.Request;
import com.santarest.http.Response;

import java.io.IOException;

/**
 * Abstraction of an HTTP client which can execute {@link Request Requests}. This class must be
 * thread-safe as invocation may happen from multiple threads simultaneously.
 */
public interface HttpClient {

    int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s

    /**
     * Synchronously execute an HTTP represented by {@code request} and encapsulate all response data
     * into a {@link Response} instance.
     * <p/>
     * Note: If the request has a body, its length and mime type will have already been added to the
     * header list as {@code Content-Length} and {@code Content-Type}, respectively. Do NOT alter
     * these values as they might have been set as a result of an application-level configuration.
     */
    Response execute(Request request) throws IOException;
}
