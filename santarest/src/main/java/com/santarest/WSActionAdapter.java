package com.santarest;

import com.santarest.converter.Converter;
import com.santarest.ws.client.WSClient;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.functions.Action1;

/**
 * Created by dirong on 2/8/16.
 */
public class WSActionAdapter implements ActionAdapter {

    private final WSClient client;
    private final Converter converter;
    private final String url;

    private final Collection<PendingAction> pendingActions;

    private WSActionAdapter(WSClient client, Converter converter, String baseUrl) {
        this.client = client;
        this.converter = converter;
        this.url = convertURL(baseUrl, client);
        this.client.addHandler(handler);
        this.pendingActions = new CopyOnWriteArrayList<PendingAction>();
    }

    static WSActionAdapter create(WSClient client, Converter converter, String baseUrl) {
        return new WSActionAdapter(client, converter, baseUrl);
    }

    @Override
    public <A> void send(A action, Action1<A> callback) throws IOException {
        PendingAction<A> pendingAction = new PendingAction<A>(action, callback);
        pendingActions.add(pendingAction);
        if (!client.isConnected()) {
            client.connect(url);
        }
    }

    private void send(PendingAction pendingAction) throws IOException {
        send(pendingAction.action, pendingAction.callback);
    }

    private final WSClient.Handler handler = new WSClient.Handler() {
        @Override
        public void onConnect() {
            for (PendingAction action : pendingActions) {
                try {
                    send(action);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onMessage(String message) {

        }

        @Override
        public void onMessage(byte[] data) {

        }

        @Override
        public void onDisconnect(int code, String reason) {

        }

        @Override
        public void onError(Exception error) {

        }
    };

    private static class PendingAction<A>{
        private A action;
        private Action1<A> callback;
        private boolean sent;

        public PendingAction(A action, Action1<A> callback) {
            this.action = action;
            this.callback = callback;
        }
    }

    private static String convertURL(String baseUrl, WSClient client) {
        String schemeName = client.scheme().name().toLowerCase();
        if (client.scheme() == WSClient.Scheme.NONE
                || baseUrl.startsWith(schemeName)) {
            return baseUrl;
        }
        StringBuilder sb = new StringBuilder(schemeName);
        sb.append(baseUrl.substring(baseUrl.indexOf("://")));
        return sb.toString();
    }
}
