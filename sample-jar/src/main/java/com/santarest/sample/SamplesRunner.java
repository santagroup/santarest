package com.santarest.sample;

import com.santarest.RequestBuilder;
import com.santarest.SantaRest;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.squareup.otto.Subscribe;

import java.util.concurrent.Executors;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by vladla on 11/13/15.
 */
public class SamplesRunner {

    private SantaRest githubRest;
    private SantaRest uploadFileServer;

    public SamplesRunner() {
        githubRest = new SantaRest.Builder()
                .setServerUrl("https://api.github.com")
                .addRequestInterceptor(new SantaRest.RequestInterceptor() {
                    @Override
                    public void intercept(RequestBuilder request) {
                        request.addHeader("test", "test");
                    }
                })
                .addResponseInterceptor(new SantaRest.ResponseListener() {
                    @Override
                    public void onResponseReceived(Object action, Request request, Response response) {
                        System.out.println(request);
                        System.out.println(response);
                    }

                })
                .build();
        uploadFileServer = new SantaRest.Builder()
                .setServerUrl("http://posttestserver.com")
                .addRequestInterceptor(new SantaRest.RequestInterceptor() {
                    @Override
                    public void intercept(RequestBuilder request) {
                        request.addHeader("test", "test");
                    }
                })
                .addResponseInterceptor(new SantaRest.ResponseListener() {
                    @Override
                    public void onResponseReceived(Object action, Request request, Response response) {
                        System.out.println(response);
                    }
                })
                .build();
    }

    public void runTests() {
        uploadFileServer.sendAction(new UploadFileAction());
        githubRest.sendAction(new ExampleAction("square", "otto"));
        githubRest.sendAction(new OuterAction.InnerAction());
        githubRest.createActionObservable(new ExampleAction("santagroup", "santarest"))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .subscribe(new Action1<ExampleAction>() {
                    @Override
                    public void call(ExampleAction exampleAction) {
                        System.out.println(exampleAction);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    public void registerEvents() {
        githubRest.subscribe(this);
        uploadFileServer.subscribe(this);
    }

    public void unregisterEvents() {
        githubRest.unsubscribe(this);
        uploadFileServer.unsubscribe(this);
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onExampleAction(ExampleAction action) {
        System.out.println(action);
        System.out.println(action.success);
        System.out.println(action.isSuccess());
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onUploadFileAction(UploadFileAction action) {
        System.out.println(action);
        System.out.println(action.success);
        System.out.println("response = " + action.getResponse());
    }

}
