package com.saltar;

import com.saltar.annotations.SaltarAction;
import com.saltar.annotations.SaltarAction.Method;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class SaltarActionClass {
    private final Method method;
    private final List<String> headers;
    private final String path;
    private final SaltarAction.Type requestType;
    private final TypeElement typeElement;
    private final Elements elementUtils;
    private final List<Element> allAnnotatedMembers;

    public SaltarActionClass(Elements elementUtils, TypeElement typeElement) throws IllegalAccessException {
        SaltarAction annotation = typeElement.getAnnotation(SaltarAction.class);
        this.typeElement = typeElement;
        this.elementUtils = elementUtils;
        method = annotation.value();
        headers = Arrays.asList(annotation.headers());
        path = annotation.path();
        requestType = annotation.type();


        allAnnotatedMembers = new ArrayList<Element>();
        for (Element element : elementUtils.getAllMembers(typeElement)) {
            if (element.getKind() == ElementKind.FIELD && annotatedWithLibraryAnnotation(element)) {
                allAnnotatedMembers.add(element);
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
        for (Element element : allAnnotatedMembers) {
            Annotation annotation = element.getAnnotation(annotationClass);
            if (annotation != null) {
                annotatedElements.add(element);
            }
        }
        return annotatedElements;
    }

    public List<Element> getAllAnnotatedMembers() {
        return allAnnotatedMembers;
    }

    private boolean annotatedWithLibraryAnnotation(Element specifiedElement) {
        String annotationPackage = SaltarAction.class.getPackage().getName();
        PackageElement packageElement = elementUtils.getPackageElement(annotationPackage);
        for (Element element : packageElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.ANNOTATION_TYPE) continue;
            try {
                Class aClass = Class.forName(element.asType().toString());
                if (specifiedElement.getAnnotation(aClass) != null) {
                    return true;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
