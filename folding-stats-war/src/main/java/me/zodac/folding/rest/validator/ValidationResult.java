/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

    private final boolean success;
    private final E output;
    private final Response failureResponse;
    private final ValidationFailure validationFailure;

    /**
     * Checks if the {@link ValidationResult} failed and does not contain a validated object.
     *
     * @return <code>true</code> if the {@link ValidationResult} does not contain a valid object
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Retrieves the errors from the {@code validationFailure}.
     *
     * @return the {@link Collection} of error {@link String}s from the {code validationFailure}, or {@link Collections#emptyList()}
     */
    public Collection<String> getErrors() {
        return validationFailure == null ? Collections.emptyList() : validationFailure.getErrors();
    }

    /**
     * Validation was successful.
     *
     * @param output the output {@link ResponsePojo} after validation
     * @param <E>    the output type of the validated object
     * @return a {@link ValidationResult} with no invalid object and an empty {@link Collection} of errors
     */
    public static <E extends ResponsePojo> ValidationResult<E> successful(final E output) {
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
        final ConstraintFailure response = new ConstraintFailure(invalidObject, errors);
        return new ValidationResult<>(false, null, badRequest(response), response);
    }

    /**
     * Validation failed due to a null payload.
     *
     * @param <E> the output type of the validated object
     * @return a {@link ValidationResult} with the null object
     */
    public static <E extends ResponsePojo> ValidationResult<E> nullObject() {
        final ConstraintFailure response = new ConstraintFailure(null, List.of("Payload is null"));
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
        final ConflictFailure response = new ConflictFailure(
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
        final ConflictFailure response = new ConflictFailure(invalidObject, usedBy, List.of("Payload is used by an existing object"));
        return new ValidationResult<>(false, null, conflict(response), response);
    }

    /**
     * Implementation of {@link ValidationFailure} used when the input payload fails a constraint check.
     */
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    public static class ConstraintFailure implements ValidationFailure {

        private Object invalidObject;
        private Collection<String> errors;
    }

    /**
     * Implementation of {@link ValidationFailure} used when the input payload conflicts with another object.
     */
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    public static class ConflictFailure implements ValidationFailure {

        private Object invalidObject;
        private Collection<?> conflictingObjects;
        private Collection<String> errors;
    }
}