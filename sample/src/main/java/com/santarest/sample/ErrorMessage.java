package com.santarest.sample;

import com.google.gson.annotations.Expose;

//Model object
public class ErrorMessage {
    @Expose
    String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }
}
