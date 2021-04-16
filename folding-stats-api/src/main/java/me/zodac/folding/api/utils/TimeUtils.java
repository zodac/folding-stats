package me.zodac.folding.api.utils;

import java.sql.Timestamp;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;

/**
 * Utility class with convenient time-based functions.
 */
public class TimeUtils {

    private TimeUtils() {

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