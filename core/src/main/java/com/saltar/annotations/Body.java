package com.saltar.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Use this annotation on a service method param when you want to directly control the request body
 * of a POST/PUT request (instead of sending in as request parameters or form-style request
 * body). If the value of the parameter implements {@link retrofit.mime.TypedOutput TypedOutput},
 * the request body will be written exactly as specified by
 * {@link retrofit.mime.TypedOutput#writeTo(java.io.OutputStream)}. If the value does not implement
 * TypedOutput, the object will be serialized using the {@link retrofit.RestAdapter RestAdapter}'s
 * {@link retrofit.converter.Converter Converter} and the result will be set directly as the
 * request body.
 * <p>
 * Body parameters may not be {@code null}.
 *
 * @author Eric Denman (edenman@squareup.com)
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface Body {
}
