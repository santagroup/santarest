package com.santarest.validation;

import com.santarest.RestActionClass;
import com.santarest.annotations.Body;
import com.santarest.annotations.Field;
import com.santarest.annotations.Part;
import com.santarest.annotations.ResponseHeader;
import com.santarest.annotations.RestAction;
import com.santarest.annotations.RestAction.Type;
import com.santarest.annotations.Status;
import com.santarest.http.ByteArrayBody;
import com.santarest.http.FileBody;
import com.santarest.http.FormUrlEncodedRequestBody;
import com.santarest.http.HttpBody;
import com.santarest.http.MultipartRequestBody;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        validators.add(new RequestTypeValidator(Part.class, Type.MULTIPART));
        //annotation rules
        validators.add(new AnnotationTypesValidator(ResponseHeader.class, String.class));
        validators.add(new AnnotationTypesValidator(Status.class, Boolean.class, Integer.class, Long.class, String.class, boolean.class, int.class, long.class));
        validators.add(new AnnotationTypesValidator(Part.class, File.class, byte[].class, String.class, HttpBody.class,
                ByteArrayBody.class, MultipartRequestBody.class, FormUrlEncodedRequestBody.class, FileBody.class));
        validators.add(new AnnotationQuantityValidator(Body.class, 1));
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
