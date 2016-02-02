package com.santarest;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;

final public class SantaRestExecutor<A> {

    private final PublishSubject<ActionState<A>> signal;
    private ConnectableObservable<ActionState<A>> cachedSignal;

    private final Func1<A, Observable<A>> observableFactory;

    SantaRestExecutor(Func1<A, Observable<A>> observableFactory) {
        this.observableFactory = observableFactory;
        this.signal = PublishSubject.create();
        createCachedPipeline();
    }

    private void createCachedPipeline() {
        this.cachedSignal = signal.replay(1);
        this.cachedSignal.connect();
    }

    public Observable<ActionState<A>> observe() {
        return signal.asObservable();
    }

    public Observable<ActionState<A>> observeWithReplay() {
        return cachedSignal.asObservable();
    }

    public Observable<A> observeActions(){
        return observe()
                .compose(new StateToAction<A>());

    }

    public Observable<A> observeActionsWithReplay(){
        return observeWithReplay()
                .compose(new StateToAction<A>());
    }

    public void clearReplays() {
        createCachedPipeline();
    }

    public void execute(A action) {
        createJob(action).subscribe();
    }

    public Observable<ActionState<A>> createJob(A action) {
        final ActionState<A> state = new ActionState<A>(action);
        return Observable.defer(new Func0<Observable<A>>() {
            @Override
            public Observable<A> call() {
                return observableFactory.call(state.action);
            }
        }).flatMap(new Func1<A, Observable<ActionState<A>>>() {
            @Override
            public Observable<ActionState<A>> call(A a) {
                return Observable.just(state.status(ActionState.Status.FINISH));
            }
        }).doOnSubscribe(new Action0() {
            @Override
            public void call() {
                signal.onNext(state.status(ActionState.Status.START));
            }
        }).onErrorReturn(new Func1<Throwable, ActionState<A>>() {
            @Override
            public ActionState<A> call(Throwable throwable) {
                return state.error(throwable);
            }
        }).doOnNext(new Action1<ActionState<A>>() {
            @Override
            public void call(ActionState<A> state) {
                signal.onNext(state);
            }
        });
    }
}