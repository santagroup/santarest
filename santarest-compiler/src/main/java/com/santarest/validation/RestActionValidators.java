package com.santarest.validation;

import com.santarest.CollectionTypes;
import com.santarest.RestActionClass;
import com.santarest.annotations.Body;
import com.santarest.annotations.Error;
import com.santarest.annotations.Field;
import com.santarest.annotations.FieldMap;
import com.santarest.annotations.Part;
import com.santarest.annotations.PartMap;
import com.santarest.annotations.QueryMap;
import com.santarest.annotations.RequestHeaders;
import com.santarest.annotations.ResponseHeader;
import com.santarest.annotations.ResponseHeaders;
import com.santarest.annotations.RestAction;
import com.santarest.annotations.RestAction.Type;
import com.santarest.annotations.Status;
import com.santarest.http.Header;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.santarest.CollectionTypes.TYPE_COLLECTION_WITH_HEADER;
import static com.santarest.CollectionTypes.TYPE_LIST_WITH_HEADER;
import static com.santarest.CollectionTypes.TYPE_MAP_WITH_STRING_KEYS;

/**
 * Validate annotations compatibility for classes annotated with
 *
 * @see RestAction
 */
public class RestActionValidators implements Validator<RestActionClass> {

    private final List<Validator<RestActionClass>> validators;

    public RestActionValidators() {
        validators = new ArrayList<Validator<RestActionClass>>();
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
        validators.add(new AnnotationTypesValidator(QueryMap.class, TYPE_MAP_WITH_STRING_KEYS));
        validators.add(new AnnotationTypesValidator(FieldMap.class, TYPE_MAP_WITH_STRING_KEYS));
        validators.add(new AnnotationTypesValidator(ResponseHeader.class, String.class));
        validators.add(new AnnotationTypesValidator(ResponseHeaders.class, CollectionTypes.TYPE_MAP_WITH_STRINGS, TYPE_LIST_WITH_HEADER, TYPE_COLLECTION_WITH_HEADER, Header[].class));
        validators.add(new AnnotationTypesValidator(RequestHeaders.class, TYPE_MAP_WITH_STRING_KEYS, TYPE_LIST_WITH_HEADER, TYPE_COLLECTION_WITH_HEADER, Header[].class));
        validators.add(new AnnotationTypesValidator(Status.class, Boolean.class, Integer.class, Long.class, String.class, boolean.class, int.class, long.class));
        validators.add(new AnnotationTypesValidator(Error.class, Throwable.class, Exception.class));
        validators.add(new AnnotationQuantityValidator(Body.class, 1));
        validators.add(new AnnotationQuantityValidator(Field.class, 1));
        validators.add(new AnnotationQuantityValidator(FieldMap.class, 1));
        validators.add(new AnnotationQuantityValidator(Part.class, 1));
        validators.add(new AnnotationQuantityValidator(PartMap.class, 1));

    }

    @Override
    public Set<ValidationError> validate(RestActionClass value) {
        Set<ValidationError> errors = new HashSet<ValidationError>();
        for (Validator<RestActionClass> validator : validators) {
            errors.addAll(validator.validate(value));
        }
        return errors;
    }
}
