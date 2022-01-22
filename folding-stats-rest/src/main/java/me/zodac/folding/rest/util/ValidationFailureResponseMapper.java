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

package me.zodac.folding.rest.util;

import me.zodac.folding.api.tc.validation.ValidationResult;
import me.zodac.folding.rest.response.Responses;
import org.springframework.http.ResponseEntity;

/**
 * Utility class used to convert a {@link ValidationResult} into a {@link ResponseEntity} for REST endpoints.
 */
public final class ValidationFailureResponseMapper {

    private ValidationFailureResponseMapper() {

    }

    /**
     * Map the input {@link ValidationResult} to a REST {@link ResponseEntity}. Extracts the
     * {@link me.zodac.folding.api.tc.validation.ValidationFailureType} and decides on the best {@link ResponseEntity}.
     *
     * <p>
     * Currently supports:
     * <ul>
     *     <li>{@link me.zodac.folding.api.tc.validation.ValidationFailureType#BAD_REQUEST}</li>
     *     <li>{@link me.zodac.folding.api.tc.validation.ValidationFailureType#CONFLICT}</li>
     * </ul>
     *
     * @param validationResult the input {@link ValidationResult} to map
     * @return the mapped {@link ResponseEntity}
     */
    public static ResponseEntity<String> map(final ValidationResult<?> validationResult) {
        return switch (validationResult.validationFailureType()) { // NOPMD - SwitchStmtsShouldHaveDefault false positive
            case BAD_REQUEST -> Responses.badRequest(validationResult.validationFailure());
            case CONFLICT -> Responses.conflict(validationResult.validationFailure());
            default -> throw new IllegalStateException(String.format("Cannot map a REST response for validation result: %s", validationResult));
        };
    }
}