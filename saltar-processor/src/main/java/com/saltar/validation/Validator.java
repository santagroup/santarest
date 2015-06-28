package com.saltar.validation;

import java.util.Set;

public interface Validator<T> {
    Set<ValidationError> validate(T value);
}
