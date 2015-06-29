package com.saltar.validation;

import com.saltar.annotations.SaltarAction;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ClassValidator implements Validator<Element> {

    private final static String SALTAR_ACTION_CLASS = SaltarAction.class.getSimpleName();

    @Override
    public Set<ValidationError> validate(Element value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        TypeElement typeElement = (TypeElement) value;
        if (typeElement.getKind() != ElementKind.CLASS) {
            errors.add(new ValidationError(String.format("Only classes can be annotated with @%s", SALTAR_ACTION_CLASS), value));
        }
        String annotatedClassName = typeElement.getQualifiedName().toString();
        if (!typeElement.getModifiers().contains(Modifier.PUBLIC)) {
            errors.add(new ValidationError(String.format("The class %s is not public.", annotatedClassName), value));
        }
        // Check if it's an abstract class
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            errors.add(new ValidationError(String.format("The class %s is abstract. You can't annotate abstract classes with @%s",
                    annotatedClassName, SALTAR_ACTION_CLASS), value));
        }
        return Collections.emptySet();
    }
}