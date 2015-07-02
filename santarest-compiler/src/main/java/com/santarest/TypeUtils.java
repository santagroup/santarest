package com.santarest;

import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.TypeName;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;

public class TypeUtils {
    public static boolean isMapString(Element element) {
        TypeToken<Map<String, String>> mapStringToken = new TypeToken<Map<String, String>>() {
        };
        return equalTypes(element, mapStringToken);
    }

    public static boolean equalTypes(Element element, TypeToken token) {
        return equalTypes(element, token.getType());
    }

    public static boolean equalTypes(Element element, Type type) {
        return TypeName.get(element.asType()).toString().equals(TypeName.get(type).toString());
    }

    public static boolean containsType(Element element, Type... classes) {
        for (Type clazz : classes) {
            if (equalTypes(element, clazz)) {
                return true;
            }
        }
        return false;
    }
}
