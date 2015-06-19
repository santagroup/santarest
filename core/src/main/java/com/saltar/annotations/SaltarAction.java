package com.saltar.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by dirong on 6/18/15.
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface SaltarAction {

    Method value() default Method.GET;

    String path() default "";

    Type type() default Type.SIMPLE;

    String[] headers() default {};

    public enum Type {
        /**
         * No content-specific logic required.
         */
        SIMPLE,
        /**
         * Multi-part request body.
         */
        MULTIPART,
        /**
         * Form URL-encoded request body.
         */
        FORM_URL_ENCODED
    }

    public enum Method {
        GET(false), POST(true), PUT(true), DELETE(false), HEAD(false), PATCH(true);

        private boolean hasBody;

        Method(boolean hasBody) {
            this.hasBody = hasBody;
        }

        public boolean hasBody() {
            return hasBody;
        }
    }
}
