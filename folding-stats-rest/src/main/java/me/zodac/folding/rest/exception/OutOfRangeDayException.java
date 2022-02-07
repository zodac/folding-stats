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
 * {@link Exception} to be thrown when a provided {@link String} day is not valid for the month/year.
 */
public class OutOfRangeDayException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2315415240865681293L;

    /**
     * The year for the day.
     */
    private final int year;

    /**
     * The month for the day.
     */
    private final int month;

    /**
     * The out of range day.
     */
    private final int day;

    /**
     * Basic constructor.
     *
     * @param year  the year for the day
     * @param month the month for the day
     * @param day   the invalid day for the given year/month
     */
    public OutOfRangeDayException(final int year, final int month, final int day) {
        super();
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * The year of the out of range day.
     *
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * The month of the out of range day.
     *
     * @return the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * The invalid day.
     *
     * @return the invalid day
     */
    public int getDay() {
        return day;
    }
}
