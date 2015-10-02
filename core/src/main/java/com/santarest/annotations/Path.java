package com.santarest.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for request filling
 * Simple Example:
 * <pre>
 * @RestAction(value = "/repos/{repo}/contributors")
 * public class ExampleAction {
 *    @Path("repo") Object repoName;
 * }
 * </pre>
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface Path {
    String value();

    boolean encode() default true;
}
