package com.saltar;

import com.google.gson.reflect.TypeToken;
import com.saltar.annotations.SaltarAction;
import com.saltar.annotations.SaltarAction.Method;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class SaltarActionClass {
    private final Method method;
    private final List<String> headers;
    private final String path;
    private final SaltarAction.Type requestType;
    private final TypeElement typeElement;
    private final Elements elementUtils;
    private final List<Element> allMembers;

    public SaltarActionClass(Elements elementUtils, TypeElement typeElement) throws IllegalAccessException {
        SaltarAction annotation = typeElement.getAnnotation(SaltarAction.class);
        this.typeElement = typeElement;
        this.elementUtils = elementUtils;
        method = annotation.value();
        headers = Arrays.asList(annotation.headers());
        path = annotation.path();
        requestType = annotation.type();

        allMembers = new ArrayList<Element>();
        for (Element element : elementUtils.getAllMembers(typeElement)) {
            if (element.getKind() == ElementKind.FIELD) {
                allMembers.add(element);
            }
        }
    }

    public Method getMethod() {
        return method;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getPath() {
        return path;
    }

    public SaltarAction.Type getRequestType() {
        return requestType;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getHelperName() {
        return getTypeElement().getSimpleName() + "Helper";
    }

    public String getName() {
        return getTypeElement().getSimpleName().toString();
    }

    public String getPackageName() {
        Name qualifiedName = elementUtils.getPackageOf(getTypeElement()).getQualifiedName();
        return qualifiedName.toString();
    }

    public List<Element> getAnnotatedElements(Class annotationClass) {
        ArrayList<Element> annotatedElements = new ArrayList<Element>();
        for (Element element : elementUtils.getAllMembers(typeElement)) {
            Annotation annotation = element.getAnnotation(annotationClass);
            if (annotation != null) {
                annotatedElements.add(element);
            }
        }
        return annotatedElements;
    }

    public String getFieldName(Class fieldAnnotation) {
        return getFieldNameExcluded(fieldAnnotation);
    }


    public TypeMirror getFieldType(Class fieldAnnotation, Class... excluded) {
        for (Element element : allMembers) {
            Annotation annotation = element.getAnnotation(fieldAnnotation);
            if (annotation == null || isExcluded(element, excluded)) continue;
            return element.asType();
        }
        return null;
    }

    public String getFieldNameExcluded(Class fieldAnnotation, Class... excluded) {
        for (Element element : allMembers) {
            Annotation annotation = element.getAnnotation(fieldAnnotation);
            if (annotation == null || isExcluded(element, excluded)) continue;
            return element.getSimpleName().toString();
        }
        return null;
    }

    private boolean isExcluded(Element element, Class[] excludeClasses) {
        if (excludeClasses == null || excludeClasses.length == 0) {
            return false;
        }
        for (Class clazz : excludeClasses) {
            if (element.asType().toString().equals(clazz.getName())) {
                return true;
            }
        }
        return false;
    }

    public String getFieldName(Class fieldClass, Class fieldAnnotation) {
        for (Element element : allMembers) {
            Annotation annotation = element.getAnnotation(fieldAnnotation);
            if (annotation == null) continue;
            if (element.asType().toString().equals(fieldClass.getName())) {
                return element.getSimpleName().toString();
            }
        }
        return null;
    }

    public String getFieldName(TypeToken typeToken, Class fieldAnnotation) {
        for (Element element : allMembers) {
            Annotation annotation = element.getAnnotation(fieldAnnotation);
            if (annotation == null) continue;
            String typeClassName = element.asType().toString().replaceAll(" ", "");
            String tokenClassName = typeToken.toString().replaceAll(" ", "");
            if (typeClassName.equals(tokenClassName)) {
                return element.getSimpleName().toString();
            }
        }
        return null;
    }
}
