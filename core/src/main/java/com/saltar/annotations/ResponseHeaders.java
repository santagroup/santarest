package com.saltar.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by dirong on 6/19/15.
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface ResponseHeaders {
}
