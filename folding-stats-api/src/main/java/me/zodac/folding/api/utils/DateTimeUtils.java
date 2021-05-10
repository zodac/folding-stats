package me.zodac.folding.api.utils;

import java.sql.Timestamp;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.Locale;

/**
 * Utility class with convenient time-based functions.
 */
public class DateTimeUtils {

    private DateTimeUtils() {

    }

    /**
     * Formats the input {@link Month} with correct capitalisation, based on {@link Locale#UK}.
     * <p>
     * <b>NOTE:</b> This should only be used for logging. Any values making it to the frontend should be formatted there,
     * otherwise we may end up formatting with the incorrect style.
     *
     * @param month the {@link Month} to be formatted
     * @return the formatted number
     */
    public static String formatMonth(final Month month) {
        final String monthAsString = month.toString();
        return monthAsString.substring(0, 1).toUpperCase(Locale.UK) + monthAsString.substring(1).toLowerCase(Locale.UK);
    }


    /**
     * Get the current {@link Timestamp} in UTC.
     *
     * @return the current UTC {@link Month}
     */
    public static Timestamp getCurrentUtcTimestamp() {
        return new Timestamp(getCurrentUtcDateTime().toInstant().toEpochMilli());
    }

    /**
     * Get the current {@link Month} in UTC.
     *
     * @return the current UTC {@link Month}
     */
    public static Month getCurrentUtcMonth() {
        return getCurrentUtcDateTime().toLocalDate().getMonth();
    }

    /**
     * Get the current {@link Year} in UTC.
     *
     * @return the current UTC {@link Year}
     */
    public static Year getCurrentUtcYear() {
        return Year.now(ZoneOffset.UTC);
    }

    private static OffsetDateTime getCurrentUtcDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
