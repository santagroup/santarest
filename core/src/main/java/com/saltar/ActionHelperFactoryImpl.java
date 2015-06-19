package com.saltar;

/**
 * Created by dirong on 6/18/15.
 */
public class ActionHelperFactoryImpl implements Saltar.ActionHelperFactory {

    @Override
    public Saltar.ActionHelper make(Class actionClass) {
        if(actionClass == ExampleAction.class){
            return new ExampleActionHelper();
        }
        return null;
    }

}
