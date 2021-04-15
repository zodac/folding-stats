package me.zodac.folding.api.utils;

import java.sql.Timestamp;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TimeUtils {

    private TimeUtils() {

    }

    public static Timestamp getCurrentUtcTimestamp() {
        return new Timestamp(getCurrentUtcDateTime().toInstant().toEpochMilli());
    }

    public static Month getCurrentUtcMonth() {
        return getCurrentUtcDateTime().toLocalDate().getMonth();
    }

    private static OffsetDateTime getCurrentUtcDateTime() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
