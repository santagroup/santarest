package com.santarest.sample;

import com.santarest.ActionStateSubscriber;
import com.santarest.SantaRest;
import com.santarest.SantaRestExecutor;
import com.santarest.sample.model.User;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by dirong on 2/4/16.
 */
public class SimpleService {

    private static final String API_URL = "https://api.github.com";

    public static void main(String... args) {
        SantaRest gitHubAPI = new SantaRest.Builder()
                .setServerUrl(API_URL)
                .build();

        SantaRestExecutor<UsersAction> usersExecutor = gitHubAPI.createExecutor(UsersAction.class);
        SantaRestExecutor<UserReposAction> userReposExecutor = gitHubAPI.createExecutor(UserReposAction.class);

        usersExecutor.observeActions()
                .filter(BaseAction::isSuccess)
                .subscribe(usersAction -> {
                    System.out.println("received " + usersAction);
                });


        usersExecutor.createObservable(new UsersAction())
                .filter(state -> state.action.isSuccess())
                .flatMap(state -> Observable.<User>from(state.action.response).first())
                .flatMap(user -> userReposExecutor.createObservable(new UserReposAction(user.getLogin())))
                .subscribe(new ActionStateSubscriber<UserReposAction>()
                        .onFail(throwable -> System.out.println("repos request error " + throwable))
                        .onSuccess(action -> System.out.println("repos request finished " + action)));
    }
}
