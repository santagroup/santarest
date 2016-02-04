package com.santarest.sample;

import com.santarest.ActionState;
import com.santarest.ActionStateSubscriber;
import com.santarest.SantaRest;
import com.santarest.SantaRestExecutor;
import com.santarest.sample.model.User;

import rx.Observable;

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
                .filter(state -> state.status == ActionState.Status.FINISH)
                .filter(state -> state.action.isSuccess())
                .flatMap(state -> Observable.from(state.action.response).first())
                .cast(User.class)
                .flatMap(user -> userReposExecutor.createObservable(new UserReposAction(user.getLogin())))
                .subscribe(new ActionStateSubscriber<UserReposAction>()
                        .onStart(() -> System.out.println("repos request start"))
                        .onFail(throwable -> System.out.println("repos request fault"))
                        .onFinish(action -> System.out.println("repos request finished " + action)));

    }
}
