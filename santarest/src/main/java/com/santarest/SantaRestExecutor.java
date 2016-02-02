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
    private final ActionState<A> state;
    private ConnectableObservable<ActionState<A>> cachedPipeline;

    private final Func1<A, Observable<A>> observableFactory;

    SantaRestExecutor(A action, Func1<A, Observable<A>> observableFactory) {
        this.state = new ActionState<A>(action);
        this.observableFactory = observableFactory;
        this.signal = PublishSubject.create();
        createCachedPipeline();
    }

    private void createCachedPipeline() {
        this.cachedPipeline = signal.replay(1);
        this.cachedPipeline.connect();
    }

    public Observable<ActionState<A>> observe() {
        return signal.asObservable();
    }

    public Observable<ActionState<A>> observeWithReplay() {
        return cachedPipeline.asObservable();
    }

    public Observable<A> observeActions(){
        return observe()
                .compose(new StateToValue<A>());

    }

    public Observable<A> observeActionsWithReplay(){
        return observeWithReplay()
                .compose(new StateToValue<A>());
    }

    public void clearReplays() {
        createCachedPipeline();
    }

    public void execute() {
        createJob().subscribe();
    }

    public Observable<ActionState<A>> createJob() {
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