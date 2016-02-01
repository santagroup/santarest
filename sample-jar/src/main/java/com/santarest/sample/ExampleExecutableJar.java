package com.santarest.sample;

/**
 * Created by vladla on 11/13/15.
 */
public class ExampleExecutableJar {

    public static void main(String... str) {
        SamplesRunner samplesRunner = new SamplesRunner();
        samplesRunner.registerEvents();
        samplesRunner.runTests();
    }

}
