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

package me.zodac.folding.rest.api.exception;

import java.io.Serial;

/**
 * Application {@link Exception} used when an error occurs sending a REST request.
 */
public class FoldingRestException extends Exception {

    @Serial
    private static final long serialVersionUID = -3883148353675655633L;

    /**
     * Constructor taking in an error message.
     *
     * @param message the error message
     */
    public FoldingRestException(final String message) {
        super(message);
    }

    /**
     * Constructor taking in an error message and a cause {@link Throwable}.
     *
     * @param message the error message
     * @param cause   the cause {@link Throwable}
     */
    public FoldingRestException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
