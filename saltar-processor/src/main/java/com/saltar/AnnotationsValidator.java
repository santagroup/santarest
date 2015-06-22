package com.saltar;

import com.saltar.annotations.SaltarAction;

import org.apache.commons.lang.StringUtils;

/**
 * Validate annotations compatibility for classes annotated with
 *
 * @see com.saltar.annotations.SaltarAction
 */
public class AnnotationsValidator implements Validator {
    private final SaltarActionClass saltarAction;

    public AnnotationsValidator(SaltarActionClass saltarAction) {
        this.saltarAction = saltarAction;
    }

    @Override
    public void validate() throws IllegalAccessException {
        //TODO: validate annotations compatibility
        if (StringUtils.isEmpty(saltarAction.getPath())) {
            throw new IllegalArgumentException(
                    String.format("Path in @%s for class %s is null or empty! That's not allowed",
                            SaltarAction.class.getSimpleName(), saltarAction.getTypeElement().getQualifiedName().toString()));
        }

    }
}
