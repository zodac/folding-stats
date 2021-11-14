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

package me.zodac.folding.rest.endpoint.util;

import javax.ws.rs.core.Response;
import me.zodac.folding.api.tc.validation.ValidationFailureType;
import me.zodac.folding.rest.response.Responses;
import me.zodac.folding.api.tc.validation.ValidationResult;

/**
 * Utility class used to convert a {@link ValidationResult} into a {@link Response} for REST endpoints.
 */
public final class ValidationFailureResponseMapper {

    private ValidationFailureResponseMapper() {

    }

    /**
     * Map the input {@link ValidationResult} to a REST {@link Response}. Extracts the {@link ValidationFailureType} and
     * decides on the best {@link Response}.
     * <p>
     * Currently supports:
     * <ul>
     *     <li>{@link ValidationFailureType#BAD_REQUEST}</li>
     *     <li>{@link ValidationFailureType#CONFLICT}</li>
     * </ul>
     *
     * @param validationResult the input {@link ValidationResult} to map
     * @return the mapped {@link Response}
     */
    public static Response map(final ValidationResult<?> validationResult) {
        switch (validationResult.getValidationFailureType()) {
            case BAD_REQUEST:
                return Responses.badRequest(validationResult.getValidationFailure());
            case CONFLICT:
                return Responses.conflict(validationResult.getValidationFailure());
            case NONE:
            default:
                throw new IllegalStateException(String.format("Cannot map a REST response for validation result: %s", validationResult));
        }
    }
}