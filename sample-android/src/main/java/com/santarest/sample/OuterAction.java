package com.santarest.sample;

import com.santarest.annotations.RestAction;

public class OuterAction {

    @RestAction("/events/")
    public static class InnerAction {
    }
}
