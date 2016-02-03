package com.santarest.sample.network;

import com.santarest.annotations.Query;
import com.santarest.annotations.Response;
import com.santarest.annotations.RestAction;
import com.santarest.sample.model.User;

import java.util.ArrayList;

/**
 * Created by dirong on 2/3/16.
 */
@RestAction("/users")
public class UsersAction extends BaseAction{

    @Query("since")
    final int since = 0;

    @Response
    ArrayList<User> response;

    public ArrayList<User> getResponse() {
        return response;
    }
}
