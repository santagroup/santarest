package com.santarest.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by dirong on 2/8/16.
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface SocketAction {

    String value() default "";
}
