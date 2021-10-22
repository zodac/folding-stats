package me.zodac.folding.rest.validator;

import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.conflict;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.ResponsePojo;

/**
 * Simple POJO defining the result of a validation check.
 *
 * @param <E> the type of the validated object, if it is valid
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class ValidationResult<E extends ResponsePojo> {

    private final boolean successful;
    private final E output;
    private final Response failureResponse;
    private final ValidationResponse validationResponse;

    /**
     * Checks if the {@link ValidationResult} failed and does not contain a validated object.
     *
     * @return <code>true</code> if the {@link ValidationResult} does not contain a valid object
     */
    public boolean isFailure() {
        return !successful;
    }

    public Collection<String> getErrors() {
        return validationResponse == null ? Collections.emptyList() : validationResponse.getErrors();
    }

    /**
     * Validation was successful.
     *
     * @param output the output {@link ResponsePojo} after validation
     * @param <E>    the output type of the validated object
     * @return a {@link ValidationResult} with no invalid object and an empty {@link Collection} of errors
     */
    public static <E extends ResponsePojo> ValidationResult<E> success(final E output) {
        return new ValidationResult<>(true, output, null, null);
    }

    /**
     * Validation failed due to one or more errors.
     *
     * @param invalidObject the {@link Object} that failed validation
     * @param errors        a {@link Collection} of the validation errors for the {@link Object}
     * @param <E>           the output type of the validated object
     * @return a {@link ValidationResult} with the invalid object and a {@link Collection} of errors
     */
    public static <E extends ResponsePojo> ValidationResult<E> failure(final Object invalidObject, final Collection<String> errors) {
        final ValidationFailureResponse response = new ValidationFailureResponse(invalidObject, errors);
        return new ValidationResult<>(false, null, badRequest(response), response);
    }

    /**
     * Validation failed due to a null payload.
     *
     * @param <E> the output type of the validated object
     * @return a {@link ValidationResult} with the null object
     */
    public static <E extends ResponsePojo> ValidationResult<E> nullObject() {
        final ValidationFailureResponse response = new ValidationFailureResponse(null, List.of("Payload is null"));
        return new ValidationResult<>(false, null, badRequest(response), response);
    }

    /**
     * Validation failed due to the {@link RequestPojo} conflicting with an existing {@link ResponsePojo}.
     *
     * @param invalidObject         the {@link ResponsePojo} that failed validation
     * @param conflictingObject     the conflicting {@link ResponsePojo}
     * @param conflictingAttributes a {@link Collection} of the conflicting fields
     * @param <E>                   the output type of the validated object
     * @return a {@link ValidationResult} with the conflicting {@link Object}
     */
    public static <E extends ResponsePojo> ValidationResult<E> conflictingWith(final RequestPojo invalidObject,
                                                                               final ResponsePojo conflictingObject,
                                                                               final Collection<String> conflictingAttributes) {
        final ConflictResponse response = new ConflictResponse(
            invalidObject,
            List.of(conflictingObject),
            List.of(String.format("Payload conflicts with an existing object on: %s", conflictingAttributes))
        );
        return new ValidationResult<>(false, null, conflict(response), response);
    }

    /**
     * Validation failed due to a conflict with existing {@link ResponsePojo}s using this {@link ResponsePojo}.
     *
     * @param invalidObject the {@link ResponsePojo} that failed validation
     * @param usedBy        the {@link Collection} of conflicting entities
     * @param <E>           the output type of the validated object
     * @return a {@link ValidationResult} with the conflicting {@link Object}
     */
    public static <E extends ResponsePojo> ValidationResult<E> usedBy(final ResponsePojo invalidObject, final Collection<?> usedBy) {
        final ConflictResponse response = new ConflictResponse(invalidObject, usedBy, List.of("Payload is used by an existing object"));
        return new ValidationResult<>(false, null, conflict(response), response);
    }

    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    public static class ValidationFailureResponse implements ValidationResponse {

        private Object invalidObject;
        private Collection<String> errors;
    }

    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    public static class ConflictResponse implements ValidationResponse {

        private Object invalidObject;
        private Collection<?> conflictingObjects;
        private Collection<String> errors;
    }
}