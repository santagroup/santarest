package com.santarest.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for request body filling
 * Simple Example:
 * <pre>
 * @Body
 * Map<String, Object> body = new HashMap<>();
 * @Body
 * SomeObject someObjectName;
 * </pre>
 * This map is serialized to json
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface Body {

//    int[] value() default {}; //TODO: add ports config

}
