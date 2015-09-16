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
 * Simple Example:
 * <pre>
 * &#64;FormUrlEncoded
 * &#64;POST("/things")
 * void things(@FieldMap Map&lt;String, String&gt; fields);
 * }
 * </pre>
 * Calling with {@code foo.things(ImmutableMap.of("foo", "bar", "kit", "kat")} yields a request
 * body of {@code foo=bar&kit=kat}.
 *
 * @see FormUrlEncoded
 * @see Field
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface FieldMap {
}
