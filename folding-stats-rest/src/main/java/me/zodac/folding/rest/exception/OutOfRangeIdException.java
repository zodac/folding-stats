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
 * {@link Exception} to be thrown when a provided ID is a valid {@link Integer}, but not in a valid range.
 */
public class OutOfRangeIdException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1956391236596546606L;

    /**
     * The out of range ID.
     */
    private final int id;

    /**
     * Basic constructor.
     *
     * @param id the out of range ID
     */
    public OutOfRangeIdException(final int id) {
        super();
        this.id = id;
    }

    /**
     * The out of range ID.
     *
     * @return the out of range ID
     */
    public int getId() {
        return id;
    }
}
