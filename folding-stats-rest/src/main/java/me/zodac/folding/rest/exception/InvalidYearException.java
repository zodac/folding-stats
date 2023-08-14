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
import java.time.Year;
import lombok.Getter;

/**
 * {@link Exception} to be thrown when a provided {@link String} year is not a valid {@link Year}.
 */
@Getter
public class InvalidYearException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5445226246538685833L;

    /**
     * The invalid year.
     */
    private final String year;

    /**
     * Basic constructor.
     *
     * @param year      the invalid year
     * @param cause the cause {@link Throwable}
     */
    public InvalidYearException(final String year, final Throwable cause) {
        super(cause);
        this.year = year;
    }
}
