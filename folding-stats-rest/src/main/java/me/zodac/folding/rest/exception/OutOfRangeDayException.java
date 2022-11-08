/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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
