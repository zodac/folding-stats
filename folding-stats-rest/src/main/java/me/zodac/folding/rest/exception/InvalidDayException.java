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
 * {@link Exception} to be thrown when a provided {@link String} day is not a valid {@link Integer}.
 */
public class InvalidDayException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6466120653086944738L;

    /**
     * The invalid day.
     */
    private final String day;

    /**
     * Basic constructor.
     *
     * @param day       the invalid day
     * @param throwable the cause {@link Throwable}
     */
    public InvalidDayException(final String day, final Throwable throwable) {
        super(throwable);
        this.day = day;
    }

    /**
     * The invalid day.
     *
     * @return the invalid day
     */
    public String getDay() {
        return day;
    }
}
