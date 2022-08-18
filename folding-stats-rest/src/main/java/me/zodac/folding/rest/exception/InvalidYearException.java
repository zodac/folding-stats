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
import java.time.Year;

/**
 * {@link Exception} to be thrown when a provided {@link String} year is not a valid {@link Year}.
 */
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

    /**
     * The invalid year.
     *
     * @return the invalid year
     */
    public String getYear() {
        return year;
    }
}
