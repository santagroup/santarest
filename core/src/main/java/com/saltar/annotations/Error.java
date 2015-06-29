package com.saltar.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by dirong on 6/28/15.
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface Error {
}
