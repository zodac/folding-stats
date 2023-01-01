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

package me.zodac.folding.db.postgres;

import java.io.Serial;

/**
 * {@link RuntimeException} used when an error occurs opening or closing a database connection.
 *
 * <p>
 * In general, I'm not a fan of {@link RuntimeException}s since I think they should be handled at some level, same as
 * a checked {@link Exception}. However, to keep method signatures clean for others who might be reading this codebase,
 * I'll live with it.
 *
 * <p>
 * It does mean we need to ensure that our boundaries (schedules EJBs, REST request handlers, etc.) are at least handling
 * generic {@link Exception}s, or even handling this one explicitly.
 */
public class DatabaseConnectionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2662806281406745266L;

    /**
     * Constructor taking in an error message and a cause {@link Throwable}.
     *
     * @param message the error message
     * @param cause   the cause {@link Throwable}
     */
    public DatabaseConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
