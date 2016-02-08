package com.santarest.ws.client;

import com.santarest.SantaRestException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dirong on 2/8/16.
 */
public abstract class WSClient {

    public final static int DEFAULT_PORT = 443;
    public final static Scheme DEFAULT_SCHEME = Scheme.NONE;
    private List<Handler> handlers = new LinkedList<Handler>();

    public enum Scheme {
        WS, WSS, HTTP, HTTPS,
        NONE //same as in baseUrl
    }

    private final int port;
    private Scheme scheme;

    public WSClient(int port, Scheme scheme) {
        this.port = port;
        this.scheme = scheme;
    }

    public WSClient() {
        this(DEFAULT_PORT, DEFAULT_SCHEME);
    }

    public abstract void connect();

    public abstract void disconnect();

    public abstract void send(String message);

    public abstract boolean isConnected();

    final protected void onConnect(){
        for (Handler handler : handlers) {
            handler.onConnect();
        }
    }

    final protected void onDisconnect(int code, String reason){
        for (Handler handler : handlers) {
            handler.onDisconnect(code, reason);
        }
    }

    final protected void onMessage(String message){
        for (Handler handler : handlers) {
            handler.onMessage(message);
        }
    }

    final protected void onMessage(byte[] data){
        for (Handler handler : handlers) {
            handler.onMessage(data);
        }
    }

    final protected void onError(Exception error){
        for (Handler handler : handlers) {
            handler.onError(error);
        }
    }

    final public int port() {
        return port;
    }

    final public Scheme scheme() {
        return scheme;
    }

    final public void addHandler(Handler handler){
        if(handler == null)
            throw new IllegalArgumentException("WSClient.Handler == null");
        handlers.add(handler);
    }

    final public void removeHandler(Handler handler){
        if(handler == null)
            throw new IllegalArgumentException("WSClient.Handler == null");
        handlers.remove(handler);
    }

    public interface Handler{

        void onConnect();

        void onMessage(String message);

        void onMessage(byte[] data);

        void onDisconnect(int code, String reason);

        void onError(Exception error);
    }
}
