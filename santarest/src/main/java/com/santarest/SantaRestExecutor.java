package com.santarest;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;

final public class SantaRestExecutor<A> {

    private final PublishSubject<A> pipeline;
    private ConnectableObservable<A> cachedPipeline;

    private final Func0<Observable<A>> observableFactory;

    SantaRestExecutor(Func0<Observable<A>> observableFactory) {
        this.observableFactory = observableFactory;
        this.pipeline = PublishSubject.create();
        createCachedPipeline();
    }

    private void createCachedPipeline() {
        this.cachedPipeline = pipeline.replay(1);
        this.cachedPipeline.connect();
    }

    public Observable<A> connect() {
        return pipeline.asObservable();
    }

    public Observable<A> connectWithCache() {
        return cachedPipeline.asObservable();
    }

    public void clearCache() {
        createCachedPipeline();
    }

    public void execute() {
        createInternally().subscribe();
    }

    private Observable<A> createInternally() {
        return Observable.defer(new Func0<Observable<A>>() {
            @Override
            public Observable<A> call() {
                return observableFactory.call();
            }
        }).doOnNext(new Action1<A>() {
            @Override
            public void call(A action) {
                pipeline.onNext(action);
            }
        });
    }

}