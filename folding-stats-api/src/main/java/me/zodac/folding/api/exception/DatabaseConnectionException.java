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
     * @param message   the error message
     * @param throwable the cause {@link Throwable}
     */
    public DatabaseConnectionException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
