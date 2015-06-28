package com.saltar.validation;

import com.google.gson.reflect.TypeToken;
import com.saltar.SaltarActionClass;
import com.saltar.annotations.*;
import com.saltar.annotations.Error;
import com.saltar.annotations.SaltarAction.Type;
import com.saltar.http.Header;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validate annotations compatibility for classes annotated with
 *
 * @see com.saltar.annotations.SaltarAction
 */
public class SaltarActionValidators implements Validator<SaltarActionClass> {

    private final static java.lang.reflect.Type TYPE_MAP_WITH_STRINGS = new TypeToken<Map<String, String>>() {
    }.getType();
    private final static java.lang.reflect.Type TYPE_LIST_WITH_HEADER = new TypeToken<List<Header>>() {
    }.getType();


    private final List<Validator<SaltarActionClass>> validators;

    public SaltarActionValidators() {
        validators = new ArrayList<Validator<SaltarActionClass>>();
        //general rules
        validators.add(new FieldsModifiersValidator());
        validators.add(new PathValidator());
        validators.add(new BodyValidator());
        validators.add(new RequestTypeValidator(Body.class, Type.SIMPLE));
        validators.add(new RequestTypeValidator(Field.class, Type.FORM_URL_ENCODED));
        validators.add(new RequestTypeValidator(FieldMap.class, Type.FORM_URL_ENCODED));
        validators.add(new RequestTypeValidator(Part.class, Type.MULTIPART));
        validators.add(new RequestTypeValidator(PartMap.class, Type.MULTIPART));
        //annotation rules
        validators.add(new AnnotationTypesValidator(Query.class, String.class));
        validators.add(new AnnotationTypesValidator(Field.class, String.class));
        validators.add(new AnnotationTypesValidator(QueryMap.class, TYPE_MAP_WITH_STRINGS));
        validators.add(new AnnotationTypesValidator(FieldMap.class, TYPE_MAP_WITH_STRINGS));
        validators.add(new AnnotationTypesValidator(RequestHeader.class, String.class));
        validators.add(new AnnotationTypesValidator(ResponseHeader.class, String.class));
        validators.add(new AnnotationTypesValidator(ResponseHeaders.class, TYPE_MAP_WITH_STRINGS, TYPE_LIST_WITH_HEADER, Header[].class));
        validators.add(new AnnotationTypesValidator(RequestHeaders.class, TYPE_MAP_WITH_STRINGS, TYPE_LIST_WITH_HEADER, Header[].class));
        validators.add(new AnnotationTypesValidator(Status.class, Boolean.class, Integer.class, Long.class, String.class, boolean.class, int.class, long.class));
        validators.add(new AnnotationTypesValidator(Error.class, Throwable.class, Exception.class));
        validators.add(new AnnotationQuantityValidator(Body.class, 1));
        validators.add(new AnnotationQuantityValidator(Field.class, 1));
        validators.add(new AnnotationQuantityValidator(FieldMap.class, 1));
        validators.add(new AnnotationQuantityValidator(Part.class, 1));
        validators.add(new AnnotationQuantityValidator(PartMap.class, 1));
    }

    @Override
    public Set<ValidationError> validate(SaltarActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        for (Validator<SaltarActionClass> validator : validators) {
            errors.addAll(validator.validate(value));
        }
        return errors;
    }
}
