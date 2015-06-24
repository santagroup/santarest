package com.saltar.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.saltar.Saltar;
import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity {

    private Saltar saltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        saltar = new Saltar.Builder()
                .setServerUrl("https://api.github.com")
                .build();
    }

    @Subscribe
    public void onExampleAction(ExampleAction action) {
        System.out.println(action);
    }

    @Override
    protected void onResume() {
        super.onResume();
        saltar.subscribe(this);
        saltar.sendAction(new ExampleAction("square", "retrofit"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        saltar.unsubscribe(this);
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
