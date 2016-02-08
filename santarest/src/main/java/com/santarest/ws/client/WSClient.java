package com.santarest.ws.client;

/**
 * Created by dirong on 2/8/16.
 */
public abstract class WSClient {

    public final static int DEFAULT_PORT = 443;
    public final static Scheme DEFAULT_SCHEME = Scheme.NONE;

    public enum Scheme {
        WS, WSS, HTTP, HTTPS,
        NONE //same as in baseUrl
    }

    private final int port;
    private Scheme scheme;

    public WSClient(int port, Scheme scheme) {
        this.port = port;
    }

    public WSClient() {
        this(DEFAULT_PORT, DEFAULT_SCHEME);
    }

    abstract void connect();

    abstract void disconnect();

    public int port() {
        return port;
    }

    public Scheme getScheme() {
        return scheme;
    }
}
