/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.rest.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * {@link Exception} to be thrown when a provided {@link String} day is not a valid {@link Integer}.
 */
@Getter
public class InvalidDayException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6466120653086944738L;

    /**
     * The invalid day.
     */
    private final transient String day;

    /**
     * Basic constructor.
     *
     * @param day       the invalid day
     * @param cause the cause {@link Throwable}
     */
    public InvalidDayException(final String day, final Throwable cause) {
        super(cause);
        this.day = day;
    }
}
