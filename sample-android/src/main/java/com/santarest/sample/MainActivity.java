package com.santarest.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;

import com.santarest.RequestBuilder;
import com.santarest.SantaRest;
import com.santarest.SantaRestExecutor;
import com.santarest.http.Request;
import com.santarest.http.Response;

import java.util.concurrent.Executor;

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
//                        System.out.println(request);
//                        System.out.println(response);
                    }

                })
                .build();
        githubRest.createActionObservable(new ExampleAction("santagroup", "santarest"))
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
                          System.out.println("subscribed " + exampleAction);
                      }
                  }, new Action1<Throwable>() {
                      @Override
                      public void call(Throwable throwable) {
                          throwable.printStackTrace();
                      }
                  });

        SantaRestExecutor<ExampleAction> restExecutor = githubRest.createExecutor(new ExampleAction("santagroup", "santarest"));
        restExecutor.connect().subscribe(new Action1<ExampleAction>() {
            @Override
            public void call(ExampleAction exampleAction) {
                System.out.println(exampleAction);
            }
        });
        restExecutor.connect().subscribe(new Action1<ExampleAction>() {
            @Override
            public void call(ExampleAction exampleAction) {
                System.out.println(exampleAction);
            }
        });
        restExecutor.execute();
        restExecutor.connectWithCache().subscribe(new Action1<ExampleAction>() {
            @Override
            public void call(ExampleAction exampleAction) {
                System.out.println(exampleAction);
            }
        });
        restExecutor.execute();
    }
}