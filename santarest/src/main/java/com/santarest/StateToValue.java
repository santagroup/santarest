package com.santarest;

import rx.Observable;
import rx.functions.Func1;

class StateToValue<A> implements Observable.Transformer<ActionState<A>, A> {
    @Override
    public Observable<A> call(Observable<ActionState<A>> jobObservable) {
        return jobObservable.flatMap(new Func1<ActionState<A>, Observable<A>>() {
            @Override
            public Observable<A> call(ActionState<A> state) {
                switch (state.status) {
                    case START:
                        return Observable.never();
                    case FINISH:
                        return Observable.just(state.action);
                    case FAIL:
                        return Observable.error(state.error);
                    default:
                        throw new IllegalArgumentException("Job status is unknown");
                }
            }
        });
    }
}