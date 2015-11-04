package com.santarest;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;

abstract class RXOnSubscribe<A> implements Observable.OnSubscribe<A> {

    private final A action;

    RXOnSubscribe(A action) {
        this.action = action;
    }

    @Override
    public void call(Subscriber<? super A> subscriber) {
        try {
            doAction(action);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(action);
            }
        } catch (final Exception e) {
            Exceptions.throwIfFatal(e);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(e);
            }
        }
    }

    protected abstract void doAction(A action);
}