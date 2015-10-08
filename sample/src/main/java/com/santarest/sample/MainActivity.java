package com.santarest.sample;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;

import com.santarest.RequestBuilder;
import com.santarest.SantaRest;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.squareup.otto.Subscribe;

import java.io.File;

import static android.provider.MediaStore.Images.Media.insertImage;

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
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.abc_ab_share_pack_mtrl_alpha);
        String path = insertImage(getContentResolver(), bm, "test.jpg", "test.jpg");
        File file = new File(getRealPathFromURI(Uri.parse(path)));
        uploadFileServer.sendAction(new UploadFileAction(file));
        githubRest.sendAction(new ExampleAction("square", "otto"));
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
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