package com.santarest.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Named pair for a form-encoded request.
 * <p/>
 * Values are converted to strings using {@link String#valueOf(Object)} and then form URL encoded.
 * {@code null} values are ignored. Passing a {@link java.util.List List} or array will result in a
 * field pair for each non-{@code null} item.
 *
 * @see FieldMap
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface Field {
    String value();
}
