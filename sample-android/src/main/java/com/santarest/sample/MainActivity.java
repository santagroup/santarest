package com.santarest.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.santarest.ActionStateSubscriber;
import com.santarest.RequestBuilder;
import com.santarest.SantaRest;
import com.santarest.SantaRestExecutor;
import com.santarest.http.Request;
import com.santarest.http.Response;

import rx.functions.Action1;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;

public class MainActivity extends ActionBarActivity {

    private SantaRest githubRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.santarest.sample.R.layout.activity_main);
        RxJavaPlugins.getInstance().registerObservableExecutionHook(new AndroidLogHook());
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
//                        System.out.println(request);
//                        System.out.println(response);
                    }

                })
                .build();

        SantaRestExecutor<ExampleAction> restExecutor = githubRest.createExecutor();
        restExecutor.subscribeOn(Schedulers.io());
        restExecutor.observeActions().subscribe(new Action1<ExampleAction>() {
            @Override
            public void call(ExampleAction exampleAction) {
                System.out.println("exampleAction = [" + exampleAction + "]");
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        restExecutor.observe().subscribe(new ActionStateSubscriber<ExampleAction>()
                .onFinish(new Action1<ExampleAction>() {
                    @Override
                    public void call(ExampleAction exampleAction) {
                        System.out.println("exampleAction = [" + exampleAction + "]");
                    }
                })
                .onFail(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
        restExecutor.observeActions().subscribe(new Action1<ExampleAction>() {
            @Override
            public void call(ExampleAction exampleAction) {
                System.out.println(exampleAction);
            }
        });
        restExecutor.execute(new ExampleAction("santagroup", "santarest"));
        restExecutor.observeActionsWithReplay().subscribe(new Action1<ExampleAction>() {
            @Override
            public void call(ExampleAction exampleAction) {
                System.out.println(exampleAction);
            }
        });
        restExecutor.execute(new ExampleAction("techery", "presenta"));
    }
}