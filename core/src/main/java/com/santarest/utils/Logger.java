package com.santarest.utils;

/**
 * Created by dirong on 9/16/15.
 */
public interface Logger {

    public void log(String message, String... args);

    public void error(String message, String... args);
}
