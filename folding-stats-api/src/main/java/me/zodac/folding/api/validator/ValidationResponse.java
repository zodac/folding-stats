package me.zodac.folding.api.validator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.ResponsePojo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Simple POJO defining the result of a validation check.
 *
 * @param <E> the type of the validated object, if it is valid
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class ValidationResponse<E extends ResponsePojo> {

    private Object invalidObject;
    private Object conflictingObject;
    private E output;
    private ValidationResult validationResult;
    private Collection<String> errors;

    /**
     * Validation was successful.
     *
     * @param output the output {@link ResponsePojo} after validation
     * @param <E>    the output type of the validated object
     * @return a {@link ValidationResponse} with no invalid object and an empty {@link Collection} of errors
     */
    public static <E extends ResponsePojo> ValidationResponse<E> success(final E output) {
        return new ValidationResponse<>(null, null, output, ValidationResult.SUCCESSFUL_VALIDATION, Collections.emptyList());
    }

    /**
     * Validation failed due to one or more errors.
     *
     * @param invalidObject the {@link Object} that failed validation
     * @param errors        a {@link Collection} of the validation errors for the {@link Object}
     * @param <E>           the output type of the validated object
     * @return a {@link ValidationResponse} with the invalid object and a {@link Collection} of errors
     */
    public static <E extends ResponsePojo> ValidationResponse<E> failure(final Object invalidObject, final Collection<String> errors) {
        return new ValidationResponse<>(invalidObject, null, null, ValidationResult.FAILURE_ON_VALIDATION, errors);
    }

    /**
     * Validation failed due to a null object.
     *
     * @param <E> the output type of the validated object
     * @return a {@link ValidationResponse} with the null object a single error
     */
    public static <E extends ResponsePojo> ValidationResponse<E> nullObject() {
        return new ValidationResponse<>(null, null, null, ValidationResult.FAILURE_ON_VALIDATION, List.of("Payload is null"));
    }

    /**
     * Validation failed due to a conflict with an existing {@link Object}.
     *
     * @param invalidObject         the {@link Object} that failed validation
     * @param conflictingObject     the conflicting {@link Object}
     * @param conflictingAttributes a {@link Collection} of the conflicting fields
     * @param <E>                   the output type of the validated object
     * @return a {@link ValidationResponse} with the conflicting {@link Object}
     */
    public static <E extends ResponsePojo> ValidationResponse<E> conflictingWith(final Object invalidObject, final Object conflictingObject, final Collection<String> conflictingAttributes) {
        final Collection<String> errors = List.of(String.format("Payload conflicts with an existing object on: %s", conflictingAttributes));
        return new ValidationResponse<>(invalidObject, conflictingObject, null, ValidationResult.FAILURE_DUE_TO_CONFLICT, errors);
    }

    /**
     * Validation failed due to a conflict with an existing {@link Object} using this {@link Object}.
     *
     * @param invalidObject     the {@link Object} that failed validation
     * @param conflictingObject the conflicting {@link Object}
     * @param <E>               the output type of the validated object
     * @return a {@link ValidationResponse} with the conflicting {@link Object}
     */
    public static <E extends ResponsePojo> ValidationResponse<E> usedBy(final Object invalidObject, final Object conflictingObject) {
        return new ValidationResponse<>(invalidObject, conflictingObject, null, ValidationResult.FAILURE_DUE_TO_CONFLICT, List.of("Payload conflicts with an existing object"));
    }

    /**
     * Checks if it was a failed validation.
     *
     * @return <code>true</code> if the validation failed
     */
    public boolean isInvalid() {
        return validationResult != ValidationResult.SUCCESSFUL_VALIDATION;
    }
}