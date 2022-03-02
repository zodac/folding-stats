/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.api.exception;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Exception thrown when an object being validated fails a validation constraint.
 */
public class ValidationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3819940876269728413L;

    /**
     * The actual failure to be returned.
     */
    private final ValidationFailure validationFailure;

    /**
     * Constructor with failing object and a single errors.
     *
     * @param invalidObject the {@link Object} that failed validation
     * @param error         the error
     */
    public ValidationException(final Object invalidObject, final String error) {
        super();
        validationFailure = new ValidationFailure(invalidObject, List.of(error));
    }

    /**
     * Constructor with failing object and multiple errors.
     *
     * @param invalidObject the {@link Object} that failed validation
     * @param errors        a {@link Collection} of the errors
     */
    public ValidationException(final Object invalidObject, final Collection<String> errors) {
        super();
        validationFailure = new ValidationFailure(invalidObject, errors);
    }

    /**
     * The {@link ValidationFailure}.
     *
     * @return the {@link ValidationFailure}
     */
    public ValidationFailure getValidationFailure() {
        return validationFailure;
    }

    /**
     * Failure object used for REST response.
     */
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    public static class ValidationFailure {

        private Object invalidObject;
        private Collection<String> errors;
    }
}