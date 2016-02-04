package com.santarest.sample;

import com.santarest.annotations.Path;
import com.santarest.annotations.Response;
import com.santarest.annotations.RestAction;
import com.santarest.sample.model.Repository;

import java.util.ArrayList;

/**
 * Created by dirong on 2/4/16.
 */
@RestAction("/users/{login}/repos")
public class UserReposAction extends BaseAction {

    @Path("login")
    final String login;

    @Response
    ArrayList<Repository> repositories;

    public UserReposAction(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public ArrayList<Repository> getRepositories() {
        return repositories;
    }

    @Override
    public String toString() {
        return "UserReposAction{" +
                "login='" + login + '\'' +
                ", repositories=" + repositories +
                '}';
    }
}
