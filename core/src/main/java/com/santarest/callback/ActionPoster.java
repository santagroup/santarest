package com.santarest.callback;

/**
 * Created by dirong on 6/23/15.
 */
public interface ActionPoster {

    void post(Object action);

    void subscribe(Object subscriber);

    void unsubscribe(Object subscriber);

}
