package com.santarest;


import com.google.gson.reflect.TypeToken;
import com.santarest.annotations.Body;
import com.santarest.annotations.Error;
import com.santarest.annotations.ErrorResponse;
import com.santarest.annotations.Field;
import com.santarest.annotations.FieldMap;
import com.santarest.annotations.Path;
import com.santarest.annotations.Query;
import com.santarest.annotations.QueryMap;
import com.santarest.annotations.RequestHeader;
import com.santarest.annotations.RequestHeaders;
import com.santarest.annotations.ResponseHeader;
import com.santarest.annotations.ResponseHeaders;
import com.santarest.annotations.RestAction;
import com.santarest.annotations.Status;
import com.santarest.converter.Converter;
import com.santarest.http.Header;
import com.santarest.http.Request;
import com.santarest.http.Response;
import com.santarest.http.ResponseBody;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang.StringUtils;

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
    public void generate(ArrayList<RestActionClass> actionClasses) {
        for (RestActionClass restActionClass : actionClasses) {
            generate(restActionClass);
        }
    }

    private void generate(RestActionClass actionClass) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(actionClass.getHelperName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(SantaRest.ActionHelper.class), actionClass.getTypeName()));

        classBuilder.addMethod(createRequestMethod(actionClass));
        classBuilder.addMethod(createFillResponseMethod(actionClass));
        classBuilder.addMethod(createFillErrorMethod(actionClass));
        saveClass(actionClass.getPackageName(), classBuilder.build());
    }

    private MethodSpec createFillErrorMethod(RestActionClass actionClass) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fillError")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(actionClass.getTypeName())
                .addParameter(actionClass.getTypeName(), "action")
                .addParameter(Throwable.class, "error");
        for (Element element : actionClass.getAnnotatedElements(Error.class)) {
            String fieldAddress = getFieldAddress(actionClass, element);
            if (TypeUtils.containsType(element, Throwable.class)) {
                builder.addStatement(fieldAddress + " = error", element);
            } else if (TypeUtils.containsType(element, Exception.class)) {
                builder.addStatement(fieldAddress + " = ($T) error", element, Exception.class);
            }
        }
        builder.addStatement("return action");
        return builder.build();
    }

    private MethodSpec createRequestMethod(RestActionClass actionClass) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("createRequest")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Request.class)
                .addParameter(TypeName.get(actionClass.getTypeElement().asType()), "action")
                .addParameter(RequestBuilder.class, "requestBuilder")
                .addStatement("requestBuilder.setMethod($T.$L)", RestAction.Method.class, actionClass.getMethod())
                .addStatement("requestBuilder.setRequestType($T.$L)", RestAction.Type.class, actionClass.getRequestType())
                .addStatement("requestBuilder.setPath($S)", actionClass.getPath());
        addPathParams(actionClass, builder);
        addRequestHeaders(actionClass, builder);
        addRequestFields(actionClass, builder);
        addRequestQueries(actionClass, builder);
        addRequestBody(actionClass, builder);
        builder.addStatement("return requestBuilder.build()");
        return builder.build();
    }

    //TODO: replace logic like in retrofit
    private void addRequestFields(RestActionClass actionClass, MethodSpec.Builder builder) {
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

    private void addRequestQueries(RestActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(Query.class)) {
            Query annotation = element.getAnnotation(Query.class);
            builder.addStatement("requestBuilder.addQueryParam($S, action.$L, $L, $L)", annotation.value(), element, annotation.encodeName(), annotation.encodeValue());
        }

        for (Element element : actionClass.getAnnotatedElements(QueryMap.class)) {
            QueryMap annotation = element.getAnnotation(QueryMap.class);
            if (TypeUtils.isMapString(element)) {
                builder.beginControlFlow("for ($T queryName : action.$L.keySet())", String.class, element);
                builder.addStatement("requestBuilder.addQueryParam(queryName, action.$L.get(queryName)), $L, $L", element, annotation.encodeNames(), annotation.encodeValues());
                builder.endControlFlow();
            }
        }
    }

    private void addRequestBody(RestActionClass actionClass, MethodSpec.Builder builder){
        for (Element element : actionClass.getAnnotatedElements(Body.class)) {
            builder.addStatement("requestBuilder.setBody(action.$L)", element);
            break;
        }
    }

    private void addRequestHeaders(RestActionClass actionClass, MethodSpec.Builder builder) {
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

    private void addPathParams(RestActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(Path.class)) {
            Path param = element.getAnnotation(Path.class);
            String path = param.value();
            String name = element.getSimpleName().toString();
            if (StringUtils.isEmpty(path)) {
                path = name;
            }
            boolean encode = param.encode();
            builder.addStatement("requestBuilder.addPathParam($S, action.$L, $L)", path, name, encode);
        }
    }

    private MethodSpec createFillResponseMethod(RestActionClass actionClass) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("fillResponse")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ClassName.get(actionClass.getTypeElement().asType()))
                .addParameter(actionClass.getTypeName(), "action")
                .addParameter(Response.class, "response")
                .addParameter(Converter.class, "converter");

        addStatusField(actionClass, builder);
        addResponses(actionClass, builder);
        addBasicHeadersMap(actionClass, builder);
        addResponseHeaders(actionClass, builder);
        builder.addStatement("return action");
        return builder.build();
    }

    private void addResponses(RestActionClass actionClass, MethodSpec.Builder builder) {
        List<Element> errorsElements = actionClass.getAnnotatedElements(ErrorResponse.class);
        List<Element> successElements = actionClass.getAnnotatedElements(com.santarest.annotations.Response.class);
        if(!errorsElements.isEmpty()){
            builder.beginControlFlow("if(response.isSuccessful())");
        }
        for (Element element : successElements) {
            addResponseStatements(actionClass, builder, element);
        }
        if(!errorsElements.isEmpty()){
            builder.nextControlFlow("else");
            for (Element element : errorsElements) {
                addResponseStatements(actionClass, builder, element);
            }
            builder.endControlFlow();
        }
    }

    private void addResponseStatements(RestActionClass actionClass, MethodSpec.Builder builder, Element element){
        String fieldAddress = getFieldAddress(actionClass, element);
        if (TypeUtils.equalTypes(element, ResponseBody.class)) {
            builder.addStatement(fieldAddress + " = response.getBody()", element);
        } else if (TypeUtils.equalTypes(element, String.class)){
            builder.addStatement(fieldAddress + " = response.getBody().toString()", element);
        }else {
            builder.addStatement(fieldAddress + " = ($T) converter.fromBody(response.getBody(), new $T<$T>(){}.getType())", element, element.asType(), TypeToken.class, element.asType());
        }
    }

    private void addBasicHeadersMap(RestActionClass actionClass, MethodSpec.Builder builder) {
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

    private void addResponseHeaders(RestActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(ResponseHeader.class)) {
            ResponseHeader annotation = element.getAnnotation(ResponseHeader.class);
            String fieldAddress = getFieldAddress(actionClass, element);
            builder.addStatement(fieldAddress + " = $L.get($S)", element.toString(), BASE_HEADERS_MAP, annotation.value());
        }

        TypeToken<List<Header>> listType = new TypeToken<List<Header>>() {
        };
        for (Element element : actionClass.getAnnotatedElements(ResponseHeaders.class)) {
            String fieldAddress = getFieldAddress(actionClass, element);
            if (TypeUtils.isMapString(element)) {
                builder.addStatement(fieldAddress+" = $L", element, BASE_HEADERS_MAP);
            } else if (TypeUtils.equalTypes(element, listType)) {
                builder.addStatement(fieldAddress + " = response.getHeaders()", element);
            } else if (TypeUtils.equalTypes(element, Header[].class)) {
                builder.addStatement(fieldAddress + " = new $T[response.getHeaders().size()]", element, Header.class);
                builder.addStatement(fieldAddress + " = response.getHeaders().toArray(action.$L)", element, element);
            }
        }
    }

    private void addStatusField(RestActionClass actionClass, MethodSpec.Builder builder) {
        for (Element element : actionClass.getAnnotatedElements(Status.class)) {
            String fieldAddress = getFieldAddress(actionClass, element);
            if (TypeUtils.containsType(element, Boolean.class, boolean.class)) {
                builder.addStatement(fieldAddress + " = response.isSuccessful()", element);
            } else if (TypeUtils.containsType(element, Integer.class, int.class, long.class)) {
                builder.addStatement(fieldAddress + " = ($T) response.getStatus()", element, element.asType());
            } else if (TypeUtils.equalTypes(element, String.class)) {
                builder.addStatement(fieldAddress + " = Integer.toString(response.getStatus())", element);
            } else if (TypeUtils.containsType(element, Long.class)) {
                builder.addStatement(fieldAddress + " = (long) response.getStatus()", element);
            }
        }
    }

    private static String getFieldAddress(RestActionClass actionClass, Element element){
        String address;
        if(actionClass.getTypeElement().equals(element.getEnclosingElement())){
            address = "action.$L";
        }else{
            address = String.format("((%s)action).$L", element.getEnclosingElement());
        }
        return address;
    }

}