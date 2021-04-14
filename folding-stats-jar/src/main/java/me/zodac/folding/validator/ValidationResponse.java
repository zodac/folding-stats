package me.zodac.folding.validator;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class ValidationResponse implements Serializable {

    private static final long serialVersionUID = -8159619110977167992L;

    private Object invalidObject;
    private List<String> errors;

    public ValidationResponse() {

    }

    private ValidationResponse(final Object invalidObject, final List<String> errors) {
        this.invalidObject = invalidObject;
        this.errors = errors;
    }

    public static ValidationResponse success() {
        return new ValidationResponse(null, Collections.emptyList());
    }

    public static ValidationResponse failure(final Object invalidObject, final List<String> errors) {
        return new ValidationResponse(invalidObject, errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public Object getInvalidObject() {
        return invalidObject;
    }

    public void setInvalidObject(final Object invalidObject) {
        this.invalidObject = invalidObject;
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    public void setErrors(final List<String> errors) {
        this.errors = List.copyOf(errors);
    }
}