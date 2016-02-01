package com.santarest.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

import com.santarest.RequestBuilder;
import com.santarest.SantaRest;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.squareup.otto.Subscribe;

import java.util.concurrent.Executor;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends ActionBarActivity {

    private SantaRest githubRest;
    private SantaRest uploadFileServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.santarest.sample.R.layout.activity_main);
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
        uploadFileServer.sendAction(new UploadFileAction());
        githubRest.observeActions()
                .ofType(ExampleAction.class)
                .subscribe(new Action1<ExampleAction>() {
                    @Override
                    public void call(ExampleAction exampleAction) {
                        System.out.println("exampleAction = [" + exampleAction + "]");
                    }
                });
        githubRest.sendAction(new ExampleAction("square", "otto"));
        githubRest.sendAction(new OuterAction.InnerAction());
        githubRest.createObservable(new ExampleAction("santagroup", "santarest"))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(new Executor() {
                    Handler handler = new Handler();

                    @Override
                    public void execute(Runnable command) {
                        handler.post(command);
                    }
                }))
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


    @Override
    protected void onResume() {
        super.onResume();
        githubRest.getActionPoster().subscribe(this);
        uploadFileServer.getActionPoster().subscribe(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        githubRest.getActionPoster().unsubscribe(this);
        uploadFileServer.getActionPoster().unsubscribe(this);
    }

    @Subscribe
    public void onExampleAction(ExampleAction action) {
        System.out.println(action);
        System.out.println(action.success);
        System.out.println(action.isSuccess());
    }

    @Subscribe
    public void onUploadFileAction(UploadFileAction action) {
        System.out.println(action);
        System.out.println(action.success);
        System.out.println("response = " + action.getResponse());
    }
}