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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
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
            ResponseHeader annotation = element.getAnnotation(ResponseHeader.class);
            builder.addStatement("requestBuilder.addHeader($S, action.$L)", annotation.value(), element.toString());
        }
        for (Element element : actionClass.getAnnotatedElements(RequestHeaders.class)) {
            String mapTypeName = new TypeToken<Map<String, String>>() {}.getType().getTypeName();
            if (TypeName.get(element.asType()).toString().equals(mapTypeName)) {
                builder.beginControlFlow("if (action.$L != null)", element);
                builder.beginControlFlow("for ($T headerName : action.$L.keySet())", String.class, element);
                builder.addStatement("requestBuilder.addHeader(headerName, action.$L.get(headerName))",element);
                builder.endControlFlow();
                builder.endControlFlow();
            }
        }
        for (Element element : actionClass.getAnnotatedElements(RequestHeaders.class)) {
            String listTypeName = new TypeToken<List<Header>>() {}.getType().getTypeName();
            if (TypeName.get(element.asType()).toString().equals(listTypeName)) {
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
        addHeadersMapField(builder);
        addHeadersListField(builder);
        addHeaderFields(builder);
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

    private void addBasicHeadersMap(MethodSpec.Builder builder) {
        builder.addStatement("$T<$T, $T> $L = new $T<$T, $T>()", HashMap.class, String.class, String.class, BASE_HEADERS_MAP, HashMap.class, String.class, String.class);
        builder.beginControlFlow("for ($T header : response.getHeaders())", Header.class);
        builder.addStatement("$L.put(header.getName(), header.getValue())", BASE_HEADERS_MAP);
        builder.endControlFlow();
    }

    private void addHeadersMapField(MethodSpec.Builder builder) {
        String headersHashMapField = actionClass.getFieldName(new TypeToken<HashMap<String, String>>() {
        }, ResponseHeaders.class);
        String headersMapField = actionClass.getFieldName(new TypeToken<Map<String, String>>() {
        }, ResponseHeaders.class);
        if (StringUtils.isEmpty(headersHashMapField) && StringUtils.isEmpty(headersMapField)) {
            return;
        }
        if (!StringUtils.isEmpty(headersHashMapField)) {
            builder.addStatement("action.$L = $L", headersHashMapField, BASE_HEADERS_MAP);
        }
        if (!StringUtils.isEmpty(headersMapField)) {
            builder.addStatement("action.$L = $L", headersMapField, BASE_HEADERS_MAP);
        }

    }

    private void addHeaderFields(MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(ResponseHeader.class)) {
            ResponseHeader annotation = element.getAnnotation(ResponseHeader.class);
            builder.addStatement("action.$L = $L.get($S)", element.toString(), BASE_HEADERS_MAP, annotation.value());
        }
    }

    private void addStatusField(MethodSpec.Builder builder) {
        String statusIntField = actionClass.getFieldName(int.class, Status.class);
        if (!StringUtils.isEmpty(statusIntField)) {
            builder.addStatement("action.$L = response.getStatus()", statusIntField);
        }

        String statusBooleanField = actionClass.getFieldName(boolean.class, Status.class);
        if (!StringUtils.isEmpty(statusBooleanField)) {
            builder.addStatement("action.$L = response.getStatus() >= 200 && response.getStatus() < 300", statusBooleanField);
        }
    }
}