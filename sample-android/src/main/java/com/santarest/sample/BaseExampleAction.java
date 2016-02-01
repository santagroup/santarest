package com.santarest.sample;

import com.santarest.annotations.Status;

/**
 * This action class was created to show,
 * that action helper will be generated to fill the
 * annotated variables of super class too.
 */
public class BaseExampleAction {

    @Status
    boolean success;

    public boolean isSuccess() {
        return success;
    }
}
