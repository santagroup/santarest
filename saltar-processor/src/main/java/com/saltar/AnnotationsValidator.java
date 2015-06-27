package com.saltar;

import com.google.gson.reflect.TypeToken;
import com.saltar.annotations.Body;
import com.saltar.annotations.Field;
import com.saltar.annotations.FieldMap;
import com.saltar.annotations.Part;
import com.saltar.annotations.PartMap;
import com.saltar.annotations.Path;
import com.saltar.annotations.Query;
import com.saltar.annotations.QueryMap;
import com.saltar.annotations.RequestHeader;
import com.saltar.annotations.RequestHeaders;
import com.saltar.annotations.ResponseHeader;
import com.saltar.annotations.ResponseHeaders;
import com.saltar.annotations.SaltarAction;
import com.saltar.annotations.SaltarAction.Method;
import com.saltar.annotations.SaltarAction.Type;
import com.saltar.annotations.Status;
import com.saltar.http.Header;
import com.squareup.javapoet.TypeName;

import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Validate annotations compatibility for classes annotated with
 *
 * @see com.saltar.annotations.SaltarAction
 */
public class AnnotationsValidator implements Validator {
    private static final String PATH_FORMAT_DEFINITION = "{%s}";
    private static final Pattern PATH_PATTERN = Pattern.compile("[{](.*?)[}]");
    private final SaltarActionClass saltarAction;
    private final Messager messager;
    private final TypeElement baseElement;
    private final Method method;
    private final SaltarAction.Type requestType;

    public AnnotationsValidator(Messager messager, SaltarActionClass saltarAction) {
        this.saltarAction = saltarAction;
        method = saltarAction.getMethod();
        requestType = saltarAction.getRequestType();
        baseElement = saltarAction.getTypeElement();
        this.messager = messager;
    }

    @Override
    public void validate() {
        //TODO: validate annotations compatibility
        validateFields();
        validatePath();
        validateBody();
        validateTypes();
        validateQuantity();
        validateRequestTypes();
    }

    private void validateFields() {
        for (Element element : saltarAction.getAllAnnotatedMembers()) {
            if (element.getKind() != ElementKind.FIELD) continue;
            boolean hasPrivateModifier = element.getModifiers().contains(Modifier.PRIVATE);
            boolean hasStaticModifier = element.getModifiers().contains(Modifier.STATIC);
            if (hasStaticModifier || hasPrivateModifier) {
                printMessage("Annotated fields must not be private or static.", element);
            }
        }
    }

    private void validatePath() {

        if (StringUtils.isEmpty(saltarAction.getPath())) {
            printMessage("Path in @%s for class %s is null or empty! That's not allowed", baseElement,
                    SaltarAction.class.getSimpleName(), baseElement.getQualifiedName().toString());
        }

        //Validate that annotated with Path variables exists in path of SaltarAction
        List<Element> pathAnnotations = saltarAction.getAnnotatedElements(Path.class);
        for (Element element : pathAnnotations) {
            Path annotation = element.getAnnotation(Path.class);
            String formatedPath = String.format(PATH_FORMAT_DEFINITION, annotation.value());
            if (saltarAction.getPath().contains(formatedPath)) continue;
            printMessage("%s annotated variable doesn't exist in your path", element, Path.class.getName());
        }

        //Validate that specified variable in path, has specified right annotated variable in class
        Matcher matcher = PATH_PATTERN.matcher(saltarAction.getPath());
        while (matcher.find()) {
            boolean hasAnnotatedVariable = false;
            String group = matcher.group(1);
            for (Element element : pathAnnotations) {
                Path annotation = element.getAnnotation(Path.class);
                if (annotation.value().equals(group)) {
                    hasAnnotatedVariable = true;
                    break;
                }
            }
            if (!hasAnnotatedVariable) {
                printMessage("Annotate varaible with %s annotation with value \"%s\"", baseElement, Path.class.getName(), group);
            }
        }
    }

    private void validateBody() {
        List<Element> annotations = saltarAction.getAnnotatedElements(Body.class);
        if (annotations.isEmpty()) return;
        Element element = annotations.get(0);

        if (method.hasBody()) return;

        List<String> methodNames = new ArrayList<String>();
        for (Method method : Method.values()) {
            if (!method.hasBody()) continue;
            methodNames.add(method.name());
        }
        printMessage("It's possible to use %s only with %s methods ", element, Body.class.getName(), methodNames.toString());
    }

    private void validateTypes() {
        java.lang.reflect.Type mapWithStrings = new TypeToken<Map<String, String>>() {
        }.getType();
        java.lang.reflect.Type listWithHeader = new TypeToken<List<Header>>() {
        }.getType();
        validateType(Query.class, String.class);
        validateType(Field.class, String.class);
        validateType(QueryMap.class, mapWithStrings);
        validateType(FieldMap.class, mapWithStrings);
        validateType(RequestHeader.class, String.class);
        validateType(ResponseHeader.class, String.class);
        validateType(ResponseHeaders.class, mapWithStrings, listWithHeader, Header[].class);
        validateType(RequestHeaders.class, mapWithStrings, listWithHeader, Header[].class);
        validateType(Status.class, Boolean.class, Integer.class, Long.class, String.class, boolean.class, int.class, long.class);
    }

    private void validateQuantity() {
        validateQuantity(Body.class, 1);
        validateQuantity(Field.class, 1);
        validateQuantity(FieldMap.class, 1);
        validateQuantity(Part.class, 1);
        validateQuantity(PartMap.class, 1);
    }

    private void validateRequestTypes() {
        validateRequestType(Body.class, Type.SIMPLE);
        validateRequestType(Field.class, Type.FORM_URL_ENCODED);
        validateRequestType(FieldMap.class, Type.FORM_URL_ENCODED);
        validateRequestType(Part.class, Type.MULTIPART);
        validateRequestType(PartMap.class, Type.MULTIPART);
    }

    private void validateQuantity(Class annotationClass, int maxQuantity) {
        List<Element> annotations = saltarAction.getAnnotatedElements(annotationClass);
        if (annotations.size() > maxQuantity) {
            printMessage("There are more then one field annotated with %s", annotations.get(maxQuantity), annotationClass.getName());
        }
    }

    private void validateRequestType(Class annotationClass, Type... requestTypes) {
        String bodyName = annotationClass.getSimpleName();
        List<Type> typesList = Arrays.asList(requestTypes);
        for (Element element : baseElement.getEnclosedElements()) {
            if (element.getAnnotation(annotationClass) == null) continue;
            if (!typesList.contains(this.requestType)) {
                printMessage("It's possible to use %s only with %s RequestType ", element, bodyName, requestType.name());
            }
        }
    }

    private void validateType(Class annotationClass, java.lang.reflect.Type... types) {
        for (Element element : saltarAction.getAllAnnotatedMembers()) {
            Annotation annotation = element.getAnnotation(annotationClass);
            if (annotation == null) continue;
            List<String> typeNames = new ArrayList<String>();
            for (java.lang.reflect.Type type : types) {
                typeNames.add(type.getTypeName());
            }
            if (typeNames.contains(TypeName.get(element.asType()).toString())) {
                continue;
            }
            printMessage("Fields annotated with %s should one from these types %s", element, annotation.toString(), typeNames.toString());
        }
    }

    private void printMessage(String message, Element e, String... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, args), e);
    }

}
