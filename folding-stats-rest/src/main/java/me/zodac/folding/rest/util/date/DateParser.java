/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.rest.util.date;

import java.time.Month;
import java.time.Year;
import me.zodac.folding.rest.exception.InvalidDayException;
import me.zodac.folding.rest.exception.InvalidMonthException;
import me.zodac.folding.rest.exception.InvalidYearException;
import me.zodac.folding.rest.exception.OutOfRangeDayException;

/**
 * POJO containing a {@link Year}, {@link Month} and {@code day} to simply represent a date.
 */
public final class DateParser {

    private DateParser() {

    }

    /**
     * Parses the input {@code year}, {@code month} and {@code day} {@link String}s into a valid {@link Year}, {@link Month} and {@link Integer} day.
     *
     * <p>
     * Also validates that the {@code day} is a valid day for the given {@link Year}/{@link Month}.
     *
     * @param year  the input {@link String} to parse into a {@link Year}
     * @param month the input {@link String} to parse into a {@link Month}
     * @param day   the input {@link String} to parse into a valid {@code day}
     * @return the {@link DateDetails}
     * @throws InvalidYearException   thrown if the input {@code year} is not a valid {@link Year}
     * @throws InvalidMonthException  thrown if the input {@code month} is not a valid {@link Month}
     * @throws InvalidDayException    thrown if the input {@code day} is not a valid {@link Integer}
     * @throws OutOfRangeDayException thrown if the input {@code day} is a valid {@link Integer}, but not valid for the {@link Year}/{@link Month}
     */
    public static DateDetails of(final String year, final String month, final String day) {
        final Year parsedYear = DateValidator.validateYear(year);
        final Month parsedMonth = DateValidator.validateMonth(month);
        final int parsedDay = DateValidator.validateDay(parsedYear, parsedMonth, day);
        return new DateDetails(parsedYear, parsedMonth, parsedDay);
    }

    /**
     * Parses the input {@code year} and {@code month} {@link String}s into a valid {@link Year} and {@link Month}.
     *
     * @param year  the input {@link String} to parse into a {@link Year}
     * @param month the input {@link String} to parse into a {@link Month}
     * @return the {@link DateParser}
     * @throws InvalidYearException  thrown if the input {@code year} is not a valid {@link Year}
     * @throws InvalidMonthException thrown if the input {@code month} is not a valid {@link Month}
     */
    public static MonthDetails of(final String year, final String month) {
        final Year parsedYear = DateValidator.validateYear(year);
        final Month parsedMonth = DateValidator.validateMonth(month);
        return new MonthDetails(parsedYear, parsedMonth);
    }

    /**
     * Parses the input {@code year} {@link String}s into a valid {@link Year}.
     *
     * @param year the input {@link String} to parse into a {@link Year}
     * @return the {@link DateParser}
     * @throws InvalidYearException thrown if the input {@code year} is not a valid {@link Year}
     */
    public static YearDetails of(final String year) {
        final Year parsedYear = DateValidator.validateYear(year);
        return new YearDetails(parsedYear);
    }
}
