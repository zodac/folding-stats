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

package me.zodac.folding.api.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Locale;

/**
 * Utility class used to convert date/time values.
 */
public final class DateTimeConverterUtils {

    private static final int FIRST_HOUR = 0;
    private static final int FIRST_MINUTE = 0;
    private static final int FIRST_SECOND = 0;
    private static final int LAST_HOUR = 23;
    private static final int LAST_MINUTE = 59;
    private static final int LAST_SECOND = 59;

    private DateTimeConverterUtils() {

    }

    /**
     * Get the {@link LocalDateTime} for the given values.
     *
     * <p>
     * Will set the day of the month to <b>1</b> and the time to <b>00:00:00</b>.
     *
     * @param year  the {@link Year}
     * @param month the {@link Month}
     * @return the {@link LocalDateTime}
     */
    public static LocalDateTime getLocalDateTimeOf(final Year year, final Month month) {
        return LocalDateTime.of(year.getValue(), month.getValue(), 1, 0, 0, 0);
    }

    /**
     * Get the {@link LocalDateTime} for the given values.
     *
     * @param year   the {@link Year}
     * @param month  the {@link Month}
     * @param day    the day of the {@link Month}
     * @param hour   the hour
     * @param minute the minute
     * @param second the second
     * @return the {@link LocalDateTime}
     */
    public static LocalDateTime getLocalDateTimeOf(final Year year, final Month month, final int day, final int hour, final int minute,
                                                   final int second) {
        return LocalDateTime.of(year.getValue(), month.getValue(), day, hour, minute, second);
    }

    /**
     * Convert the given {@link Timestamp} into a {@link LocalDateTime} in {@link ZoneOffset#UTC}.
     *
     * @param timestamp the {@link Timestamp} to convert
     * @return the {@link ZoneOffset#UTC} {@link LocalDateTime}
     */
    public static LocalDateTime toUtcLocalDateTime(final Timestamp timestamp) {
        return timestamp
            .toInstant()
            .atOffset(ZoneOffset.UTC)
            .toLocalDateTime();
    }

    /**
     * Gets the first {@link Timestamp} in {@link ZoneOffset#UTC} for the given date. The time will be <b>00:00:00</b>.
     *
     * @param year  the {@link Year}
     * @param month the {@link Month}
     * @param day   the day of the {@link Month}
     * @return the first {@link ZoneOffset#UTC} {@link Timestamp}
     */
    public static Timestamp getFirstTimestampOf(final Year year, final Month month, final int day) {
        return toTimestamp(LocalDateTime.of(year.getValue(), month.getValue(), day, FIRST_HOUR, FIRST_MINUTE, FIRST_SECOND));
    }

    /**
     * Gets the last {@link Timestamp} in {@link ZoneOffset#UTC} for the given date. The time will be <b>23:59:59</b>.
     *
     * @param year  the {@link Year}
     * @param month the {@link Month}
     * @param day   the day of the {@link Month}
     * @return the last {@link ZoneOffset#UTC} {@link Timestamp}
     */
    public static Timestamp getLastTimestampOf(final Year year, final Month month, final int day) {
        return toTimestamp(LocalDateTime.of(year.getValue(), month.getValue(), day, LAST_HOUR, LAST_MINUTE, LAST_SECOND));
    }

    /**
     * Convert the given {@link ChronoLocalDateTime} into a {@link Timestamp} in {@link ZoneOffset#UTC}.
     *
     * @param chronoLocalDateTime the {@link ChronoLocalDateTime} to convert
     * @return the {@link ZoneOffset#UTC} {@link Timestamp}
     */
    public static Timestamp toTimestamp(final ChronoLocalDateTime<LocalDate> chronoLocalDateTime) {
        return new Timestamp(chronoLocalDateTime.toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    /**
     * Formats the input {@link Month} with correct capitalisation, based on {@link Locale#UK}.
     *
     * <p>
     * <b>NOTE:</b> This should only be used for logging. Any values making it to the frontend should be formatted there,
     * otherwise we may end up formatting with the incorrect style.
     *
     * @param month the {@link Month} to be formatted
     * @return the formatted {@link Month}
     */
    public static String formatMonth(final Month month) {
        final String monthAsString = month.toString();
        return monthAsString.substring(0, 1).toUpperCase(Locale.UK) + monthAsString.substring(1).toLowerCase(Locale.UK);
    }
}
