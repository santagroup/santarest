package com.saltar;


import com.google.gson.reflect.TypeToken;
import com.saltar.annotations.Path;
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
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * g
 */
public class HelperGenerator implements Generator {
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
        try {
            JavaFile.builder(actionClass.getPackageName(), classBuilder.build()).build().writeTo(filer);
        } catch (IOException e) {
            throw new IllegalAccessException(e.getMessage());
        }
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

        for (Element element : actionClass.getAnnotatedElements(Path.class)) {
            String path = element.getAnnotation(Path.class).value();
            String name = element.getSimpleName().toString();
            if (StringUtils.isEmpty(path)) {
                path = name;
            }
            builder.addStatement("requestBuilder.addPathParam($S, action.$L)", path, name);
        }
        return builder.addStatement("return requestBuilder.build()");
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
        addHeadersMapField(builder);
        addHeadersListField(builder);
        builder.addStatement("return action");
        return builder;
    }

    private void addConverter(MethodSpec.Builder builder) {
        String fieldName = actionClass.getFieldNameExcluded(com.saltar.annotations.Response.class, ResponseBody.class);
        if (StringUtils.isEmpty(fieldName)) {
            return;
        }
        TypeMirror type = actionClass.getFieldType(com.saltar.annotations.Response.class, ResponseBody.class);
        builder.addStatement("$T type = new $T<$T>(){}.getType()", Type.class, TypeToken.class, type);
        builder.addStatement("action.$L = ($T) converter.fromBody(response.getBody(), type)", fieldName, type);
    }

    private void addHeadersListField(MethodSpec.Builder builder) {
        String headersArrayListField = actionClass.getFieldName(new TypeToken<ArrayList<Header>>() {
        }, ResponseHeaders.class);
        String headersListField = actionClass.getFieldName(new TypeToken<List<Header>>() {
        }, ResponseHeaders.class);
        if (StringUtils.isEmpty(headersArrayListField) && StringUtils.isEmpty(headersListField)) {
            return;
        }
        if (!StringUtils.isEmpty(headersArrayListField)) {
            builder.addStatement("action.$L = response.getHeaders()", headersArrayListField);
        }
        if (!StringUtils.isEmpty(headersListField)) {
            builder.addStatement("action.$L = response.getHeaders()", headersListField);
        }
    }

    private void addBodyField(MethodSpec.Builder builder) {
        String bodyField = actionClass.getFieldName(ResponseBody.class, com.saltar.annotations.Response.class);
        if (!StringUtils.isEmpty(bodyField)) {
            builder.addStatement("action.$L = response.getBody()", bodyField);
        }
    }

    private void addHeadersMapField(MethodSpec.Builder builder) {
        String headersHashMapField = actionClass.getFieldName(new TypeToken<HashMap<String, String>>() {
        }, ResponseHeaders.class);
        String headersMapField = actionClass.getFieldName(new TypeToken<Map<String, String>>() {
        }, ResponseHeaders.class);
        builder.addStatement("$T<$T, $T> headers = new $T<$T, $T>()", HashMap.class, String.class, String.class, HashMap.class, String.class, String.class);
        if (StringUtils.isEmpty(headersHashMapField) && StringUtils.isEmpty(headersMapField)) {
            return;
        }
        if (!StringUtils.isEmpty(headersHashMapField)) {
            builder.addStatement("action.$L = headers", headersHashMapField);
        }
        if (!StringUtils.isEmpty(headersMapField)) {
            builder.addStatement("action.$L = headers", headersMapField);
        }
        builder.beginControlFlow("for ($T header : response.getHeaders())", Header.class);
        builder.addStatement("headers.put(header.getName(), header.getValue())");
        builder.endControlFlow();
    }

    private void addStatusField(MethodSpec.Builder builder) {
        String statusField = actionClass.getFieldName(Status.class);
        if (!StringUtils.isEmpty(statusField)) {
            builder.addStatement("action.$L = response.getStatus()", statusField);
        }
    }
}