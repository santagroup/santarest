package com.santarest.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Any success or failed response data.
 * Acceptable types
 * @see com.santarest.http.HttpBody
 * @see String
 * and any other type, which can be parsed with converter
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Response {

    /**
     * HTTP status code of sever response.
     */
    int value() default 0;/*for all statuses*/
}
