package com.saltar;

import com.saltar.annotations.SaltarAction;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ClassValidator implements Validator {

    private final Element saltarElement;
    private final TypeElement typeElement;

    public ClassValidator(Element saltarElement) {
        this.saltarElement = saltarElement;
        typeElement = (TypeElement) saltarElement;
    }

    @Override
    public void validate() throws IllegalAccessException {

        String className = SaltarAction.class.getSimpleName();
        if (typeElement.getKind() != ElementKind.CLASS) {
            throw new IllegalAccessException(String.format("Only classes can be annotated with @%s", className));
        }
        String annotatedClassName = typeElement.getQualifiedName().toString();
        if (!typeElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new IllegalAccessException(String.format("The class %s is not public.", annotatedClassName));
        }

        // Check if it's an abstract class
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new IllegalAccessException(String.format("The class %s is abstract. You can't annotate abstract classes with @%s",
                    annotatedClassName, className));
        }
    }
}