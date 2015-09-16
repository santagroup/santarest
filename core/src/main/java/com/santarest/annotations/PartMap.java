package com.santarest.annotations;

import com.santarest.http.MultipartRequestBody;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Name and value parts of a multi-part request
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface PartMap {
    /**
     * The {@code Content-Transfer-Encoding} of this part.
     */
    String encoding() default MultipartRequestBody.DEFAULT_TRANSFER_ENCODING;
}
