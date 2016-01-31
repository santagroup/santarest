package com.santarest;

/**
 * Created by dirong on 6/23/15.
 */
public abstract class ActionPoster {

    protected abstract void post(Object action);

    public abstract void subscribe(Object subscriber);

    public abstract void unsubscribe(Object subscriber);

}
