/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.rest.exception;

import java.io.Serial;
import org.springframework.http.HttpStatus;

/**
 * {@link Exception} thrown when an {@link HttpStatus#UNAUTHORIZED} request has been made.
 */
public class UnauthorizedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5640864796327254860L;

    /**
     * Basic constructor.
     */
    public UnauthorizedException() {
        super("Unauthorized access");
    }

    /**
     * Constructor taking in a cause {@link Throwable}.
     *
     * @param cause the cause {@link Throwable}
     */
    public UnauthorizedException(final Throwable cause) {
        super("Unauthorized access: " + cause.getMessage(), cause);
    }
}
