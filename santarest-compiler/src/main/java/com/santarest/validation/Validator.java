package com.santarest.validation;

import java.util.Set;

public interface Validator<T> {
    Set<ValidationError> validate(T value);
}
