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
