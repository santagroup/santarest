package com.santarest.callback;

import com.santarest.ActionPoster;
import com.squareup.otto.Bus;

/**
 * Created by dirong on 6/24/15.
 */
public class OttoPoster extends ActionPoster {

    private final Bus bus = new Bus();

    @Override
    protected void post(Object action) {
        bus.post(action);
    }

    @Override
    public void subscribe(Object subscriber) {
        bus.register(subscriber);
    }

    @Override
    public void unsubscribe(Object subscriber) {
        bus.unregister(subscriber);
    }
}
