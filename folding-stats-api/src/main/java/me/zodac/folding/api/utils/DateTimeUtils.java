package me.zodac.folding.api.utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Utility class with convenient date/time-based functions.
 */
public final class DateTimeUtils {

    private DateTimeUtils() {

    }

    /**
     * Calculates the number of {@link ChronoUnit}s between the {@link #currentUtcDateTime()} and the start of next {@link Month}.
     *
     * @param chronoUnit the {@link ChronoUnit} of the response
     * @return the number of {@link ChronoUnit}s until <code>00:00:000</code> {@link ZoneOffset#UTC} of the 1st of next {@link Month}
     */
    public static int untilNextMonthUtc(final ChronoUnit chronoUnit) {
        final int currentMonth = currentUtcMonth().getValue();
        final int nextMonth = currentMonth == 12 ? 1 : currentMonth + 1;
        final int currentYear = currentUtcYear().getValue();
        final int nextMonthYear = currentMonth == 12 ? currentYear + 1 : currentYear;

        final String nextMonthAsDate = String.format("%s-%02d-01T00:00:00Z", nextMonthYear, nextMonth);

        return (int) chronoUnit.between(
                Instant.now().atZone(ZoneOffset.UTC),
                Instant.parse(nextMonthAsDate).atZone(ZoneOffset.UTC)
        );
    }

    /**
     * Formats the input {@link Month} with correct capitalisation, based on {@link Locale#UK}.
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

    /**
     * Get the {@link Timestamp} in {@link ZoneOffset#UTC} for the given values
     *
     * @param year   the {@link Year}
     * @param month  the {@link Month}
     * @param day    the day of the {@link Month}
     * @param hour   the hour
     * @param minute the minute
     * @param second the second
     * @return the {@link ZoneOffset#UTC} {@link Timestamp}
     */
    public static Timestamp getTimestampOf(final Year year, final Month month, final int day, final int hour, final int minute, final int second) {
        return new Timestamp(LocalDateTime.of(year.getValue(), month.getValue(), day, hour, minute, second).toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    /**
     * Get the current {@link Timestamp} in {@link ZoneOffset#UTC}.
     *
     * @return the current {@link ZoneOffset#UTC} {@link Timestamp}
     */
    public static Timestamp currentUtcTimestamp() {
        return new Timestamp(currentUtcDateTime().toInstant().toEpochMilli());
    }

    /**
     * Get the current {@link Month} in {@link ZoneOffset#UTC}.
     *
     * @return the current {@link ZoneOffset#UTC} {@link Month}
     */
    public static Month currentUtcMonth() {
        return currentUtcDateTime().toLocalDate().getMonth();
    }

    /**
     * Get the current {@link Year} in {@link ZoneOffset#UTC}.
     *
     * @return the current {@link ZoneOffset#UTC} {@link Year}
     */
    public static Year currentUtcYear() {
        return Year.now(ZoneOffset.UTC);
    }

    /**
     * Get the current {@link OffsetDateTime} in {@link ZoneOffset#UTC}.
     *
     * @return the current {@link ZoneOffset#UTC} {@link OffsetDateTime}
     */
    public static OffsetDateTime currentUtcDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
