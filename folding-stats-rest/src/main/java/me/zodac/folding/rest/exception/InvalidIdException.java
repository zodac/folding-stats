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

package me.zodac.folding.rest.exception;

import java.io.Serial;

/**
 * {@link Exception} to be thrown when a provided {@link String} ID is not a valid {@link Integer}.
 */
public class InvalidIdException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4712566298212173158L;

    /**
     * The invalid ID.
     */
    private final String id;

    /**
     * Basic constructor.
     *
     * @param id        the invalid ID
     * @param throwable the cause {@link Throwable}
     */
    public InvalidIdException(final String id, final Throwable throwable) {
        super(throwable);
        this.id = id;
    }

    /**
     * The invalid ID.
     *
     * @return the invalid ID
     */
    public String getId() {
        return id;
    }
}
