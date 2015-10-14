package com.santarest.converter;

import com.santarest.http.HttpBody;

import java.lang.reflect.Type;

public interface Converter {

    /**
     * Convert an HTTP response body to a concrete object of the specified type.
     *
     * @param body HTTP response body.
     * @param type Target object type.
     * @return Instance of {@code type} which will be cast by the caller.
     */
    Object fromBody(HttpBody body, Type type);

    /**
     * Convert an object to an appropriate representation for HTTP transport.
     *
     * @param object Object instance to convert.
     * @return Representation of the specified object as bytes.
     */
    HttpBody toBody(Object object);
}
