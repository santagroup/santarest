package com.santarest;

import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;

public class ActionStateSubscriber<A> extends Subscriber<ActionState<A>> {

    private Action1<A> onSuccess;
    private Action1<Throwable> onFail;
    private Action1<A> onError;
    private Action0 onStart;
    private Action1<ActionState<A>> beforeEach;
    private Action1<ActionState<A>> afterEach;

    public ActionStateSubscriber<A> onSuccess(Action1<A> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    public ActionStateSubscriber<A> onError(Action1<A> onError) {
        this.onError = onError;
        return this;
    }

    public ActionStateSubscriber<A> onFail(Action1<Throwable> onError) {
        this.onFail = onError;
        return this;
    }

    public ActionStateSubscriber<A> onStart(Action0 onProgress) {
        this.onStart = onProgress;
        return this;
    }

    public ActionStateSubscriber<A> beforeEach(Action1<ActionState<A>> onEach) {
        this.beforeEach = onEach;
        return this;
    }

    public ActionStateSubscriber<A> afterEach(Action1<ActionState<A>> afterEach) {
        this.afterEach = afterEach;
        return this;
    }

    @Override public void onNext(ActionState<A> state) {
        if (beforeEach != null) beforeEach.call(state);
        switch (state.status) {
            case START:
                if (onStart != null) onStart.call();
                break;
            case SUCCESS:
                if (onSuccess != null) onSuccess.call(state.action);
                break;
            case FAIL:
                if (onFail != null) onFail.call(state.throwable);
                break;
            case ERROR:
                if (onError != null) onError.call(state.action);
                break;
        }
        if (afterEach != null) afterEach.call(state);
    }

    @Override public void onCompleted() { }

    @Override
    public void onError(Throwable e) {
        if (onFail != null) onFail.call(e);
    }
}