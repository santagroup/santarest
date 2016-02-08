package com.santarest;

import com.santarest.converter.Converter;
import com.santarest.ws.client.WSClient;

import java.io.IOException;

import rx.functions.Action1;

/**
 * Created by dirong on 2/8/16.
 */
public class WSActionAdapter implements ActionAdapter {

    private final WSClient client;
    private final Converter converter;
    private final String baseUrl;

    private WSActionAdapter(WSClient client, Converter converter, String baseUrl) {
        this.client = client;
        this.converter = converter;
        this.baseUrl = baseUrl;
    }

    static WSActionAdapter create(WSClient client, Converter converter, String baseUrl) {
        return new WSActionAdapter(client, converter, baseUrl);
    }

    @Override
    public <A> void send(final A action, final Action1<A> callback) throws IOException {
        client.addHandler(new WSClient.Handler() {
            @Override
            public void onConnect() {

            }

            @Override
            public void onMessage(String message) {}

            @Override
            public void onMessage(byte[] data) {}

            @Override
            public void onDisconnect(int code, String reason) {}

            @Override
            public void onError(Exception error) {}
        });
        if(!client.isConnected()){
            client.connect();
        }
    }
}
