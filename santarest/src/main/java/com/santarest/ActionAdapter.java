package com.santarest;

import java.io.IOException;

import rx.functions.Action1;

/**
 * Created by dirong on 2/8/16.
 */
public interface ActionAdapter {

    <A> void send(A action, Action1<A> callback) throws IOException;
}
