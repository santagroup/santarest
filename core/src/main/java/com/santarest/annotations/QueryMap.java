package com.santarest.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Query parameter keys and values appended to the URL.
 *
 * @see Query
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface QueryMap {
    /**
     * Specifies whether parameter names (keys in the map) are URL encoded.
     */
    boolean encodeNames() default false;

    /**
     * Specifies whether parameter values (values in the map) are URL encoded.
     */
    boolean encodeValues() default true;
}
