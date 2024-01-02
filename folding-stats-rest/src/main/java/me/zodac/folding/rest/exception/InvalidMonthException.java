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

package me.zodac.folding.rest.exception;

import java.io.Serial;
import java.time.Month;
import lombok.Getter;

/**
 * {@link Exception} to be thrown when a provided {@link String} month is not a valid {@link Month}.
 */
@Getter
public class InvalidMonthException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 210062796728375018L;

    /**
     * The invalid month.
     */
    private final String month;

    /**
     * Basic constructor.
     *
     * @param month     the invalid month
     * @param cause the cause {@link Throwable}
     */
    public InvalidMonthException(final String month, final Throwable cause) {
        super(cause);
        this.month = month;
    }
}
