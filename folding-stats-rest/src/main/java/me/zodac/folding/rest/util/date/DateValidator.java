/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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
 * Utility class that validates whether a {@link Year}, {@link Month} and {@code day} are valid to simply represent a date.
 */
final class DateValidator {

    private static final Logger LOGGER = LogManager.getLogger();

    private DateValidator() {

    }

    /**
     * Validates that the input {@code day} is a valid day for the given {@link Month} and {@link Year}.
     *
     * @param year  the input {@link String} to parse into a {@link Year}
     * @param month the input {@link String} to parse into a {@link Month}
     * @param day   the input {@link String} to parse into a valid {@code day}
     * @return the {@code day} as an {@code int}
     * @throws InvalidDayException    thrown if the input {@code day} is not a valid {@link Integer}
     * @throws OutOfRangeDayException thrown if the input {@code day} is a valid {@code int}, but not valid for the {@link Year}/{@link Month}
     */
    static int validateDay(final Year year, final Month month, final String day) {
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

    /**
     * Validates that the input {@link String} is a valid {@link Month}.
     *
     * @param month the input {@link String} to parse into a {@link Month}
     * @return the parsed {@link Month}
     * @throws InvalidMonthException thrown if the input {@code month} is not a valid {@link Month}
     */
    static Month validateMonth(final String month) {
        try {
            final int monthAsInt = Integer.parseInt(month);
            return Month.of(monthAsInt);
        } catch (final NumberFormatException | DateTimeException e) {
            LOGGER.trace("Error parsing month '{}'", month, e);
            throw new InvalidMonthException(month, e);
        }
    }

    /**
     * Validates that the input {@link String} is a valid {@link Year}.
     *
     * @param year the input {@link String} to parse into a {@link Year}
     * @return the parsed {@link Year}
     * @throws InvalidYearException thrown if the input {@code year} is not a valid {@link Year}
     */
    static Year validateYear(final String year) {
        try {
            return Year.parse(year);
        } catch (final DateTimeParseException e) {
            LOGGER.trace("Error parsing year '{}'", year, e);
            throw new InvalidYearException(year, e);
        }
    }
}
