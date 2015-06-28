package com.saltar.validation;

import com.saltar.SaltarActionClass;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

/**
 * Created by dirong on 6/28/15.
 */
public class AnnotationTypesValidator implements Validator<SaltarActionClass> {

    private final Class annotationClass;
    private final Type[] types;

    public AnnotationTypesValidator(Class annotationClass, Type... types) {
        this.annotationClass = annotationClass;
        this.types = types;
    }

    @Override
    public Set<ValidationError> validate(SaltarActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        for (Element element : value.getAllAnnotatedMembers()) {
            Annotation annotation = element.getAnnotation(annotationClass);
            if (annotation == null) continue;
            List<String> typeNames = new ArrayList<String>();
            for (java.lang.reflect.Type type : types) {
                typeNames.add(TypeName.get(type).toString());
            }
            if (typeNames.contains(TypeName.get(element.asType()).toString())) {
                continue;
            }
            errors.add(new ValidationError("Fields annotated with %s should one from these types %s", element, annotation.toString(), typeNames.toString()));
        }
        return errors;
    }
}
