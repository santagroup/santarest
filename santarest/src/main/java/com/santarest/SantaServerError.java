package com.santarest;

import com.santarest.http.Request;
import com.santarest.http.Response;

/**
 * Created by dirong on 11/6/15.
 */
public class SantaServerError extends Exception {

    private final int code;
    private final String reason;
    private final Request request;
    private final Response response;
    private final Object action;

    public SantaServerError(Object action, Request request, Response response) {
        super("HTTP ERROR: " + response.getStatus() + " " + response.getReason());
        this.code = response.getStatus();
        this.reason = response.getReason();
        this.request = request;
        this.response = response;
        this.action = action;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public Object getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "SantaServerError{" +
                "code=" + code +
                ", reason='" + reason + '\'' +
                ", request=" + request +
                ", response=" + response +
                ", action=" + action +
                '}';
    }
}
