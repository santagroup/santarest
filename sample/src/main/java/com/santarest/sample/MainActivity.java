package com.santarest.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.santarest.RequestBuilder;
import com.santarest.SantaRest;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity {

    private SantaRest githubRest;
    private SantaRest uploadFileServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.santarest.sample.R.layout.activity_main);
        githubRest = new SantaRest.Builder()
                .setServerUrl("https://api.github.com")
                .setRequestInterceptor(new SantaRest.RequestInterceptor() {
                    @Override
                    public void intercept(RequestBuilder request) {
                        request.addHeader("test", "test");
                    }
                })
                .addResponseInterceptors(new SantaRest.ResponseListener() {
                    @Override
                    public void onResponseReceived(Object action, Request request, Response response) {
                        System.out.println(request);
                        System.out.println(response);
                    }

                })
                .build();
        uploadFileServer = new SantaRest.Builder()
                .setServerUrl("http://posttestserver.com")
                .setRequestInterceptor(new SantaRest.RequestInterceptor() {
                    @Override
                    public void intercept(RequestBuilder request) {
                        request.addHeader("test", "test");
                    }
                })
                .addResponseInterceptors(new SantaRest.ResponseListener() {
                    @Override
                    public void onResponseReceived(Object action, Request request, Response response) {
                        System.out.println(response);
                    }
                })
                .build();
        uploadFileServer.sendAction(new UploadFileAction());
        githubRest.sendAction(new ExampleAction("square", "otto"));
        githubRest.sendAction(new OuterAction.InnerAction());
    }


    @Override
    protected void onResume() {
        super.onResume();
        githubRest.subscribe(this);
        uploadFileServer.subscribe(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        githubRest.unsubscribe(this);
        uploadFileServer.unsubscribe(this);
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