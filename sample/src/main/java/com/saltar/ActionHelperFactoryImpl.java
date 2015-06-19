package com.saltar;

import com.saltar.sample.ExampleAction;
import com.saltar.sample.ExampleActionHelper;

/**
 * Created by dirong on 6/18/15.
 */
class ActionHelperFactoryImpl implements Saltar.ActionHelperFactory {

    @Override
    public Saltar.ActionHelper make(Class actionClass) {
        if(actionClass == ExampleAction.class){
            return new ExampleActionHelper();
        }
        return null;
    }

}
