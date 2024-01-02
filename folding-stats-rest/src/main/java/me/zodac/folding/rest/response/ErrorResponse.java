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

package me.zodac.folding.rest.response;

/**
 * Simple POJO used for REST responses where an error message is required. Allows us to have a JSON payload with a name value, instead of the response
 * body simply being a {@link String}.
 *
 * <p>
 * When using {@link com.google.gson.Gson}, the response will be in the form:
 * <pre>
 *     {
 *         "error": "My error message here"
 *     }
 * </pre>
 *
 * @param error the error message
 */
public record ErrorResponse(String error) {

    /**
     * Creates an {@link ErrorResponse}.
     *
     * @param error the error message
     * @return the created {@link ErrorResponse}
     */
    public static ErrorResponse create(final String error) {
        return new ErrorResponse(error);
    }
}
