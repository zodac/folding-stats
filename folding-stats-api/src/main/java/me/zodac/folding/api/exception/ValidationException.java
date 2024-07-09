/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.api.exception;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.ResponsePojo;

/**
 * Exception thrown when an object being validated fails a validation constraint.
 */
@Getter
public class ValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3819940876269728413L;

    /**
     * The actual failure to be returned.
     */
    private final transient ValidationFailure validationFailure;

    /**
     * Constructor with failing {@link ResponsePojo} and a single error.
     *
     * @param invalidObject the {@link ResponsePojo} that failed validation
     * @param message       the error
     */
    public ValidationException(final ResponsePojo invalidObject, final String message) {
        super(message);
        validationFailure = new ValidationFailure(invalidObject, List.of(message));
    }

    /**
     * Constructor with failing {@link RequestPojo} and a single error.
     *
     * @param invalidObject the {@link RequestPojo} that failed validation
     * @param message       the error
     */
    public ValidationException(final RequestPojo invalidObject, final String message) {
        super(message);
        validationFailure = new ValidationFailure(invalidObject, List.of(message));
    }

    /**
     * Constructor with failing {@link RequestPojo}, a single error and the cause {@link Throwable}.
     *
     * @param invalidObject the {@link RequestPojo} that failed validation
     * @param message       the error
     * @param cause         the cause {@link Throwable}
     */
    public ValidationException(final RequestPojo invalidObject, final String message, final Throwable cause) {
        super(message, cause);
        validationFailure = new ValidationFailure(invalidObject, List.of(message));
    }

    /**
     * Constructor with failing {@link RequestPojo} and multiple errors.
     *
     * @param invalidObject the {@link RequestPojo} that failed validation
     * @param errors        a {@link Collection} of the errors
     */
    public ValidationException(final RequestPojo invalidObject, final Collection<String> errors) {
        super();
        validationFailure = new ValidationFailure(invalidObject, errors);
    }

    /**
     * Failure POJO used for REST response.
     */
    public record ValidationFailure(Object invalidObject, Collection<String> errors) {

    }
}
