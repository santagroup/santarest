package com.santarest;

import com.santarest.annotations.RestAction;
import com.santarest.annotations.RestAction.Method;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class RestActionClass {
    private final Method method;
    private final String path;
    private final RestAction.Type requestType;
    private final TypeElement typeElement;
    private final Elements elementUtils;
    private final List<Element> allAnnotatedMembers;
    private final HashMap<Class, List<Element>> allAnnotatedMembersMap;

    public RestActionClass(Elements elementUtils, TypeElement typeElement) {
        RestAction annotation = typeElement.getAnnotation(RestAction.class);
        this.typeElement = typeElement;
        this.elementUtils = elementUtils;
        method = annotation.method();
        path = annotation.value();
        requestType = annotation.type();

        allAnnotatedMembersMap = new HashMap<Class, List<Element>>();
        allAnnotatedMembers = new ArrayList<Element>();
        List<Class> libraryAnnotations = getLibraryAnnotations();

        for (Element element : elementUtils.getAllMembers(typeElement)) {
            for (Class libraryAnnotation : libraryAnnotations) {
                if (element.getKind() != ElementKind.FIELD || element.getAnnotation(libraryAnnotation) == null) {
                    continue;
                }
                allAnnotatedMembers.add(element);

                if (!allAnnotatedMembersMap.containsKey(libraryAnnotation)) {
                    allAnnotatedMembersMap.put(libraryAnnotation, new ArrayList<Element>());
                }
                allAnnotatedMembersMap.get(libraryAnnotation).add(element);
            }
        }
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public RestAction.Type getRequestType() {
        return requestType;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public TypeName getTypeName() {
        return TypeName.get(getTypeElement().asType());
    }

    public String getHelperName() {
        return getTypeElement().getSimpleName() + HelpersGenerator.HELPER_SUFFIX;
    }

    public String getFullHelperName() {
        return getPackageName() + "." + getTypeElement().getSimpleName() + HelpersGenerator.HELPER_SUFFIX;
    }

    public String getPackageName() {
        Name qualifiedName = elementUtils.getPackageOf(getTypeElement()).getQualifiedName();
        return qualifiedName.toString();
    }

    public List<Element> getAnnotatedElements(Class annotationClass) {
        List<Element> elements = allAnnotatedMembersMap.get(annotationClass);
        if (elements == null) return Collections.emptyList();
        return elements;
    }

    public List<Element> getAllAnnotatedMembers() {
        return allAnnotatedMembers;
    }

    private List<Class> getLibraryAnnotations() {
        String annotationPackage = RestAction.class.getPackage().getName();
        PackageElement packageElement = elementUtils.getPackageElement(annotationPackage);
        List<Class> libraryAnnotation = new ArrayList<Class>();
        for (Element element : packageElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.ANNOTATION_TYPE) continue;
            try {
                libraryAnnotation.add(Class.forName(element.asType().toString()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return libraryAnnotation;
    }
}