package com.saltar;


import com.google.gson.reflect.TypeToken;
import com.saltar.annotations.Path;
import com.saltar.annotations.RequestHeader;
import com.saltar.annotations.RequestHeaders;
import com.saltar.annotations.ResponseHeader;
import com.saltar.annotations.ResponseHeaders;
import com.saltar.annotations.SaltarAction;
import com.saltar.annotations.Status;
import com.saltar.converter.Converter;
import com.saltar.http.Header;
import com.saltar.http.Request;
import com.saltar.http.Response;
import com.saltar.http.ResponseBody;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

public class HelperGenerator implements Generator {
    private static final String BASE_HEADERS_MAP = "headers";
    private final SaltarActionClass actionClass;
    private final Filer filer;
    private final Elements elementUtils;

    public HelperGenerator(SaltarActionClass actionClass, Filer filer, Elements elementUtils) {
        this.actionClass = actionClass;
        this.filer = filer;
        this.elementUtils = elementUtils;
    }

    @Override
    public void generate() throws IllegalAccessException {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(actionClass.getHelperName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(Saltar.ActionHelper.class);

        classBuilder.addMethod(createRequestMethod().build());
        classBuilder.addMethod(createFillResponseMethod().build());
        classBuilder.addMethod(createFillErrorMethod().build());
        try {
            JavaFile.builder(actionClass.getPackageName(), classBuilder.build()).build().writeTo(filer);
        } catch (IOException e) {
            throw new IllegalAccessException(e.getMessage());
        }
    }

    private MethodSpec.Builder createFillErrorMethod() {
        return MethodSpec.methodBuilder("fillError")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(actionClass.getTypeElement().asType()))
                .addParameter(Object.class, "objectAction")
                .addParameter(Throwable.class, "error")
                .addStatement("return ($T) objectAction", actionClass.getTypeElement());
    }

    private MethodSpec.Builder createRequestMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("createRequest")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Request.class)
                .addParameter(Object.class, "objectAction")
                .addParameter(RequestBuilder.class, "requestBuilder")
                .addStatement("$T action = ($T) objectAction", actionClass.getTypeElement().asType(), actionClass.getTypeElement().asType())
                .addStatement("requestBuilder.setMethod($T.$L)", SaltarAction.Method.class, actionClass.getMethod())
                .addStatement("requestBuilder.setRequestType($T.$L)", SaltarAction.Type.class, actionClass.getRequestType())
                .addStatement("requestBuilder.setPath($S)", actionClass.getPath());

        addPathVariables(builder);
        addRequestHeaders(builder);
        return builder.addStatement("return requestBuilder.build()");
    }

    private void addRequestHeaders(MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(RequestHeader.class)) {
            RequestHeader annotation = element.getAnnotation(RequestHeader.class);
            builder.addStatement("requestBuilder.addHeader($S, action.$L)", annotation.value(), element.toString());
        }
        TypeToken mapType = new TypeToken<Map<String, String>>() {
        };
        TypeToken listType = new TypeToken<List<Header>>() {
        };
        for (Element element : actionClass.getAnnotatedElements(RequestHeaders.class)) {
            if (TypeUtils.equalTypes(element, mapType)) {
                builder.beginControlFlow("if (action.$L != null)", element);
                builder.beginControlFlow("for ($T headerName : action.$L.keySet())", String.class, element);
                builder.addStatement("requestBuilder.addHeader(headerName, action.$L.get(headerName))", element);
                builder.endControlFlow();
                builder.endControlFlow();
            } else if (TypeUtils.equalTypes(element, listType) || TypeUtils.equalTypes(element, Header[].class)) {
                builder.beginControlFlow("if (action.$L != null)", element);
                builder.beginControlFlow("for ($T header : action.$L)", Header.class, element);
                builder.addStatement("requestBuilder.addHeader(header.getName(), header.getValue())");
                builder.endControlFlow();
                builder.endControlFlow();
            }
        }
    }

    private void addPathVariables(MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(Path.class)) {
            String path = element.getAnnotation(Path.class).value();
            String name = element.getSimpleName().toString();
            if (StringUtils.isEmpty(path)) {
                path = name;
            }
            builder.addStatement("requestBuilder.addPathParam($S, action.$L)", path, name);
        }
    }

    private MethodSpec.Builder createFillResponseMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fillResponse")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ClassName.get(actionClass.getTypeElement().asType()))
                .addParameter(Object.class, "objectAction")
                .addParameter(Response.class, "response")
                .addParameter(Converter.class, "converter")
                .addStatement("$T action = ($T) objectAction", actionClass.getTypeElement().asType(), actionClass.getTypeElement().asType());

        addStatusField(builder);
        addConverter(builder);
        addBodyField(builder);
        addBasicHeadersMap(builder);
        addResponseHeaders(builder);
        builder.addStatement("return action");
        return builder;
    }

    private void addConverter(MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(com.saltar.annotations.Response.class)) {
            if (TypeUtils.equalTypes(element, ResponseBody.class)) continue;

            builder.addStatement("$T type = new $T<$T>(){}.getType()", Type.class, TypeToken.class, element.asType());
            builder.addStatement("action.$L = ($T) converter.fromBody(response.getBody(), type)", element, element.asType());
        }
    }

    private void addBodyField(MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(com.saltar.annotations.Response.class)) {
            if (TypeUtils.equalTypes(element, ResponseBody.class)) {
                builder.addStatement("action.$L = response.getBody()", element);
            }
        }
    }

    private void addBasicHeadersMap(MethodSpec.Builder builder) {
        boolean hasResponseHeader = !actionClass.getAnnotatedElements(ResponseHeader.class).isEmpty();
        boolean hasResponseHeaders = !actionClass.getAnnotatedElements(ResponseHeaders.class).isEmpty();
        if (!hasResponseHeader && !hasResponseHeaders) {
            return;
        }
        builder.addStatement("$T<$T, $T> $L = new $T<$T, $T>()", HashMap.class, String.class, String.class, BASE_HEADERS_MAP, HashMap.class, String.class, String.class);
        builder.beginControlFlow("for ($T header : response.getHeaders())", Header.class);
        builder.addStatement("$L.put(header.getName(), header.getValue())", BASE_HEADERS_MAP);
        builder.endControlFlow();
    }

    private void addResponseHeaders(MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(ResponseHeader.class)) {
            ResponseHeader annotation = element.getAnnotation(ResponseHeader.class);
            builder.addStatement("action.$L = $L.get($S)", element.toString(), BASE_HEADERS_MAP, annotation.value());
        }

        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>() {
        };
        TypeToken<List<Header>> listType = new TypeToken<List<Header>>() {
        };
        for (Element element : actionClass.getAnnotatedElements(ResponseHeaders.class)) {
            if (TypeUtils.equalTypes(element, mapType)) {
                builder.addStatement("action.$L = $L", element, BASE_HEADERS_MAP);
            } else if (TypeUtils.equalTypes(element, listType)) {
                builder.addStatement("action.$L = response.getHeaders()", element);
            } else if (TypeUtils.equalTypes(element, Header[].class)) {
                builder.addStatement("action.$L = new $T[response.getHeaders().size()]", element, Header.class);
                builder.addStatement("action.$L = response.getHeaders().toArray(action.$L)", element, element);
            }
        }
    }

    private void addStatusField(MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(Status.class)) {
            if (TypeUtils.containsType(element, Boolean.class, boolean.class)) {
                builder.addStatement("action.$L = response.getStatus() >= 200 && response.getStatus() < 300", element);
            } else if (TypeUtils.containsType(element, Integer.class, int.class, long.class)) {
                builder.addStatement("action.$L = ($T) response.getStatus()", element, element.asType());
            } else if (TypeUtils.equalTypes(element, String.class)) {
                builder.addStatement("action.$L = Integer.toString(response.getStatus())", element);
            } else if (TypeUtils.containsType(element, Long.class)) {
                builder.addStatement("action.$L = (long) response.getStatus()", element);
            }
        }
    }
}