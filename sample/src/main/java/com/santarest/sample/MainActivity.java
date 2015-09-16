package com.santarest.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.santarest.RequestBuilder;
import com.santarest.SantaRest;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity {

    private SantaRest santaRest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.santarest.sample.R.layout.activity_main);
        santaRest = new SantaRest.Builder()
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

    }

    @Subscribe
    public void onExampleAction(ExampleAction action) {
        System.out.println(action);
        System.out.println(action.success);
        System.out.println(action.isSuccess());
    }

    @Override
    protected void onResume() {
        super.onResume();
        santaRest.subscribe(this);
        santaRest.sendAction(new ExampleAction("square", "retrofit"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        santaRest.unsubscribe(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
