package com.santarest.utils;

public interface Logger {

    void log(String message, String... args);

    void error(String message, String... args);
}
