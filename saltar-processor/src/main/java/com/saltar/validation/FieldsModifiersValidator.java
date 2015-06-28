package com.saltar.validation;

import com.saltar.SaltarActionClass;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

/**
 * Created by dirong on 6/28/15.
 */
public class FieldsModifiersValidator implements Validator<SaltarActionClass> {
    @Override
    public Set<ValidationError> validate(SaltarActionClass value) {
        Set<ValidationError> messages = new HashSet<ValidationError>();
        for (Element element : value.getAllAnnotatedMembers()) {
            if (element.getKind() != ElementKind.FIELD) continue;
            boolean hasPrivateModifier = element.getModifiers().contains(Modifier.PRIVATE);
            boolean hasStaticModifier = element.getModifiers().contains(Modifier.STATIC);
            if (hasStaticModifier || hasPrivateModifier) {
                messages.add(new ValidationError("Annotated fields must not be private or static.", element));
            }
        }
        return messages;
    }
}
