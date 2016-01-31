package com.santarest.callback;

import com.santarest.ActionPoster;

import de.greenrobot.event.EventBus;

/**
 * Created by dirong on 6/23/15.
 */
public class EventBusPoster extends ActionPoster {

    private final EventBus eventBus;

    public EventBusPoster(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public EventBusPoster() {
        this(new EventBus());
    }

    @Override
    protected void post(Object action) {
        eventBus.post(action);
    }

    @Override
    public void subscribe(Object subscriber) {
        eventBus.register(subscriber);
    }

    @Override
    public void unsubscribe(Object subscriber) {
        eventBus.unregister(subscriber);
    }
}
