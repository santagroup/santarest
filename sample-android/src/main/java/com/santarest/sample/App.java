package com.santarest.sample;

import android.app.Application;
import android.content.Context;

import com.santarest.SantaRest;
import com.santarest.SantaRestExecutor;
import com.santarest.sample.network.UserReposAction;
import com.santarest.sample.network.UsersAction;
import com.santarest.sample.tools.AndroidLogHook;

import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;

/**
 * Created by dirong on 2/3/16.
 */
public class App extends Application {

    private static final String API_URL = "https://api.github.com";

    private SantaRest gitHubAPI;

    private SantaRestExecutor<UsersAction> usersExecutor;

    private SantaRestExecutor<UserReposAction> userReposExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        RxJavaPlugins.getInstance().registerObservableExecutionHook(new AndroidLogHook());
    }

    public SantaRest getGitHubAPI() {
        if (gitHubAPI == null) {
            gitHubAPI = new SantaRest.Builder()
                    .setServerUrl(API_URL)
                    .addRequestInterceptor(request -> request.addHeader("test", "test"))
                    .build();
        }
        return gitHubAPI;
    }

    public SantaRestExecutor<UsersAction> getUsersExecutor() {
        if (usersExecutor == null) {
            usersExecutor = getGitHubAPI().createExecutor(UsersAction.class)
                    .scheduler(Schedulers.io());
        }
        return usersExecutor;
    }

    public SantaRestExecutor<UserReposAction> getUserReposExecutor() {
        if (userReposExecutor == null) {
            userReposExecutor = getGitHubAPI().createExecutor(UserReposAction.class)
                    .scheduler(Schedulers.io());
        }
        return userReposExecutor;
    }

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }
}
