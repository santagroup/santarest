package com.santarest.sample;

import com.santarest.annotations.Status;

/**
 * Created by dirong on 7/1/15.
 */
public class BaseExampleAction {

    @Status
    boolean success;

    public boolean isSuccess() {
        return success;
    }
}
