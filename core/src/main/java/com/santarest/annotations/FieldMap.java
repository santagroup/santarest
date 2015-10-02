package com.santarest.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Named key/value pairs for a form-encoded request.
 * <p/>
 * Field values may be {@code null} which will omit them from the request body.
 * <p/>
 * @see Field
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface FieldMap {
}
