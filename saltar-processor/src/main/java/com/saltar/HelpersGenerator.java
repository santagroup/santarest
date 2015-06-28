package com.saltar;


import com.google.gson.reflect.TypeToken;
import com.saltar.annotations.Error;
import com.saltar.annotations.Field;
import com.saltar.annotations.FieldMap;
import com.saltar.annotations.Path;
import com.saltar.annotations.Query;
import com.saltar.annotations.QueryMap;
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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class HelpersGenerator extends Generator {
    static final String HELPER_SUFFIX = "Helper";
    private static final String BASE_HEADERS_MAP = "headers";

    public HelpersGenerator(Filer filer) {
        super(filer);
    }

    @Override
    public void generate(ArrayList<SaltarActionClass> actionClasses) {
        for (SaltarActionClass saltarActionClass : actionClasses) {
            generate(saltarActionClass);
        }
    }

    private void generate(SaltarActionClass actionClass) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(actionClass.getHelperName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Saltar.ActionHelper.class), actionClass.getTypeName()));

        classBuilder.addMethod(createRequestMethod(actionClass));
        classBuilder.addMethod(createFillResponseMethod(actionClass));
        classBuilder.addMethod(createFillErrorMethod(actionClass));
        saveClass(actionClass.getPackageName(), classBuilder.build());
    }

    private MethodSpec createFillErrorMethod(SaltarActionClass actionClass) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fillError")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(actionClass.getTypeName())
                .addParameter(actionClass.getTypeName(), "action")
                .addParameter(Throwable.class, "error");
        for (Element element : actionClass.getAnnotatedElements(Error.class)) {
            if (TypeUtils.containsType(element, Throwable.class)) {
                builder.addStatement("action.$L = error", element);
            } else if (TypeUtils.containsType(element, Exception.class)) {
                builder.addStatement("action.$L = ($T) error", element, Exception.class);
            }
        }
        builder.addStatement("return action");
        return builder.build();
    }

    private MethodSpec createRequestMethod(SaltarActionClass actionClass) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("createRequest")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Request.class)
                .addParameter(TypeName.get(actionClass.getTypeElement().asType()), "action")
                .addParameter(RequestBuilder.class, "requestBuilder")
                .addStatement("requestBuilder.setMethod($T.$L)", SaltarAction.Method.class, actionClass.getMethod())
                .addStatement("requestBuilder.setRequestType($T.$L)", SaltarAction.Type.class, actionClass.getRequestType())
                .addStatement("requestBuilder.setPath($S)", actionClass.getPath());
        addPathVariables(actionClass, builder);
        addRequestHeaders(actionClass, builder);
        addRequestFields(actionClass, builder);
        addRequestQueries(actionClass, builder);
        builder.addStatement("return requestBuilder.build()");
        return builder.build();
    }

    private void addRequestFields(SaltarActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(Field.class)) {
            Field annotation = element.getAnnotation(Field.class);
            builder.addStatement("requestBuilder.addField($S, action.$L)", annotation.value(), element);
        }

        for (Element element : actionClass.getAnnotatedElements(FieldMap.class)) {
            if (TypeUtils.isMapString(element)) {
                builder.beginControlFlow("for ($T fieldName : action.$L.keySet())", String.class, element);
                builder.addStatement("requestBuilder.addField(fieldName, action.$L.get(fieldName))", element);
                builder.endControlFlow();
            }
        }
    }

    private void addRequestQueries(SaltarActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(Query.class)) {
            Query annotation = element.getAnnotation(Query.class);
            builder.addStatement("requestBuilder.addQueryParam($S, action.$L)", annotation.value(), element);
        }

        for (Element element : actionClass.getAnnotatedElements(QueryMap.class)) {
            if (TypeUtils.isMapString(element)) {
                builder.beginControlFlow("for ($T queryName : action.$L.keySet())", String.class, element);
                builder.addStatement("requestBuilder.addQueryParam(queryName, action.$L.get(queryName))", element);
                builder.endControlFlow();
            }
        }
    }

    private void addRequestHeaders(SaltarActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(RequestHeader.class)) {
            RequestHeader annotation = element.getAnnotation(RequestHeader.class);
            builder.addStatement("requestBuilder.addHeader($S, action.$L)", annotation.value(), element);
        }
        TypeToken listType = new TypeToken<List<Header>>() {
        };
        for (Element element : actionClass.getAnnotatedElements(RequestHeaders.class)) {
            if (TypeUtils.isMapString(element)) {
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

    private void addPathVariables(SaltarActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(Path.class)) {
            String path = element.getAnnotation(Path.class).value();
            String name = element.getSimpleName().toString();
            if (StringUtils.isEmpty(path)) {
                path = name;
            }
            builder.addStatement("requestBuilder.addPathParam($S, action.$L)", path, name);
        }
    }

    private MethodSpec createFillResponseMethod(SaltarActionClass actionClass) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fillResponse")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ClassName.get(actionClass.getTypeElement().asType()))
                .addParameter(actionClass.getTypeName(), "action")
                .addParameter(Response.class, "response")
                .addParameter(Converter.class, "converter");

        addStatusField(actionClass, builder);
        addConverter(actionClass, builder);
        addBodyField(actionClass, builder);
        addBasicHeadersMap(actionClass, builder);
        addResponseHeaders(actionClass, builder);
        builder.addStatement("return action");
        return builder.build();
    }

    private void addConverter(SaltarActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(com.saltar.annotations.Response.class)) {
            if (TypeUtils.equalTypes(element, ResponseBody.class)) continue;

            builder.addStatement("$T type = new $T<$T>(){}.getType()", Type.class, TypeToken.class, element.asType());
            builder.addStatement("action.$L = ($T) converter.fromBody(response.getBody(), type)", element, element.asType());
        }
    }

    private void addBodyField(SaltarActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(com.saltar.annotations.Response.class)) {
            if (TypeUtils.equalTypes(element, ResponseBody.class)) {
                builder.addStatement("action.$L = response.getBody()", element);
            }
        }
    }

    private void addBasicHeadersMap(SaltarActionClass actionClass, MethodSpec.Builder builder) {
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

    private void addResponseHeaders(SaltarActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(ResponseHeader.class)) {
            ResponseHeader annotation = element.getAnnotation(ResponseHeader.class);
            builder.addStatement("action.$L = $L.get($S)", element.toString(), BASE_HEADERS_MAP, annotation.value());
        }

        TypeToken<List<Header>> listType = new TypeToken<List<Header>>() {
        };
        for (Element element : actionClass.getAnnotatedElements(ResponseHeaders.class)) {
            if (TypeUtils.isMapString(element)) {
                builder.addStatement("action.$L = $L", element, BASE_HEADERS_MAP);
            } else if (TypeUtils.equalTypes(element, listType)) {
                builder.addStatement("action.$L = response.getHeaders()", element);
            } else if (TypeUtils.equalTypes(element, Header[].class)) {
                builder.addStatement("action.$L = new $T[response.getHeaders().size()]", element, Header.class);
                builder.addStatement("action.$L = response.getHeaders().toArray(action.$L)", element, element);
            }
        }
    }

    private void addStatusField(SaltarActionClass actionClass, MethodSpec.Builder builder) {
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