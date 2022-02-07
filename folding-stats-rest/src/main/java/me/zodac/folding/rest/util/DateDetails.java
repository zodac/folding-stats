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

package me.zodac.folding.rest.util;

import java.time.DateTimeException;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import me.zodac.folding.rest.exception.InvalidDayException;
import me.zodac.folding.rest.exception.InvalidMonthException;
import me.zodac.folding.rest.exception.InvalidYearException;
import me.zodac.folding.rest.exception.OutOfRangeDayException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * POJO containing a {@link Year}, {@link Month} and {@code day} to simply represent a date.
 */
public record DateDetails(Year year, Month month, int day) {

    private static final int INVALID_DAY = -1;
    private static final Logger LOGGER = LogManager.getLogger();

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
        final Year parsedYear = parseYear(year);
        final Month parsedMonth = parseMonth(month);
        final int parsedDay = parseDay(parsedYear, parsedMonth, day);
        return new DateDetails(parsedYear, parsedMonth, parsedDay);
    }

    /**
     * Parses the input {@code year} {@link String} into a valid {@link Year}, {@link Month} and {@link Integer} day.
     *
     * @param year the input {@link String} to parse into a {@link Year}
     * @return the {@link DateDetails}
     * @throws InvalidYearException thrown if the input {@code year} is not a valid {@link Year}
     */
    public static DateDetails of(final String year) {
        final Year parsedYear = parseYear(year);
        return new DateDetails(parsedYear, null, INVALID_DAY);
    }

    /**
     * Parses the input {@code year} and {@code month} {@link String}s into a valid {@link Year} and {@link Month}.
     *
     * @param year  the input {@link String} to parse into a {@link Year}
     * @param month the input {@link String} to parse into a {@link Month}
     * @return the {@link DateDetails}
     * @throws InvalidYearException  thrown if the input {@code year} is not a valid {@link Year}
     * @throws InvalidMonthException thrown if the input {@code month} is not a valid {@link Month}
     */
    public static DateDetails of(final String year, final String month) {
        final Year parsedYear = parseYear(year);
        final Month parsedMonth = parseMonth(month);
        return new DateDetails(parsedYear, parsedMonth, INVALID_DAY);
    }

    private static Year parseYear(final String year) {
        try {
            return Year.parse(year);
        } catch (final DateTimeParseException e) {
            LOGGER.trace("Error parsing year '{}'", year, e);
            throw new InvalidYearException(year, e);
        }
    }

    private static Month parseMonth(final String month) {
        try {
            final int monthAsInt = Integer.parseInt(month);
            return Month.of(monthAsInt);
        } catch (final NumberFormatException | DateTimeException e) {
            LOGGER.trace("Error parsing month '{}'", month, e);
            throw new InvalidMonthException(month, e);
        }
    }

    private static int parseDay(final Year year, final Month month, final String day) {
        final int yearAsInt = year.getValue();
        final int monthAsInt = month.getValue();

        try {
            final int dayAsInt = Integer.parseInt(day);

            final YearMonth date = YearMonth.of(yearAsInt, monthAsInt);
            if (!date.isValidDay(dayAsInt)) {
                throw new OutOfRangeDayException(yearAsInt, monthAsInt, dayAsInt);
            }

            return dayAsInt;
        } catch (final NumberFormatException e) {
            LOGGER.trace("Error parsing day '{}'", day, e);
            throw new InvalidDayException(day, e);
        }
    }
}
