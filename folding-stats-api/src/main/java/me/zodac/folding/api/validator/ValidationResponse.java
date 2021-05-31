package me.zodac.folding.api.validator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Simple POJO defining the result of a validation check.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class ValidationResponse {

    private Object invalidObject;
    private Collection<String> errors;

    /**
     * Validation was successful.
     *
     * @return a {@link ValidationResponse} with no invalid object and an empty {@link Collection} of errors
     */
    public static ValidationResponse success() {
        return new ValidationResponse(null, Collections.emptyList());
    }

    /**
     * Validation failed due to one or more errors.
     *
     * @param invalidObject the {@link Object} that failed validation
     * @param errors        a {@link Collection} of the validation errors for the {@link Object}
     * @return a {@link ValidationResponse} with the invalid object and a {@link Collection} of errors
     */
    public static ValidationResponse failure(final Object invalidObject, final Collection<String> errors) {
        return new ValidationResponse(invalidObject, errors);
    }

    /**
     * Validation failed due to a null object.
     *
     * @return a {@link ValidationResponse} with the null object a single error
     */
    public static ValidationResponse nullObject() {
        return new ValidationResponse(null, List.of("Payload is null"));
    }

    public boolean isInvalid() {
        return !errors.isEmpty();
    }
}