package com.saltar.validation;

import com.saltar.SaltarActionClass;
import com.saltar.annotations.Body;
import com.saltar.annotations.SaltarAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

/**
 * Created by dirong on 6/28/15.
 */
public class BodyValidator implements Validator<SaltarActionClass> {
    @Override
    public Set<ValidationError> validate(SaltarActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        List<Element> annotations = value.getAnnotatedElements(Body.class);
        if (annotations.isEmpty()) return errors;
        Element element = annotations.get(0);

        if (value.getMethod().hasBody()) return errors;

        List<String> methodNames = new ArrayList<String>();
        for (SaltarAction.Method method : SaltarAction.Method.values()) {
            if (!method.hasBody()) continue;
            methodNames.add(method.name());
        }
        errors.add(new ValidationError(String.format("It's possible to use %s only with %s methods ", element, Body.class.getName(), methodNames.toString()), value.getTypeElement()));
        return errors;
    }
}
