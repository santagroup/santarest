package com.saltar.validation;

import com.saltar.SaltarActionClass;
import com.saltar.annotations.SaltarAction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

/**
 * Created by dirong on 6/28/15.
 */
public class RequestTypeValidator implements Validator<SaltarActionClass> {

    private final Class annotationClass;
    private final SaltarAction.Type[] requestTypes;

    public RequestTypeValidator(Class annotationClass, SaltarAction.Type... requestTypes) {
        this.annotationClass = annotationClass;
        this.requestTypes = requestTypes;
    }

    @Override
    public Set<ValidationError> validate(SaltarActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        String bodyName = annotationClass.getSimpleName();
        List<SaltarAction.Type> typesList = Arrays.asList(requestTypes);
        for (Element element : value.getTypeElement().getEnclosedElements()) {
            if (element.getAnnotation(annotationClass) == null) continue;
            if (!typesList.contains(value.getRequestType())) {
                errors.add(new ValidationError("It's possible to use %s only with %s request types ", element, bodyName, typesList.toString()));
            }
        }
        return errors;
    }
}
