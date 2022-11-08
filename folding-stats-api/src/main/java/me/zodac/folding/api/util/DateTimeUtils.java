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

package me.zodac.folding.api.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.function.Supplier;

/**
 * Convenience class with convenient date/time-based functions.
 *
 * @param timeSupplier {@link Supplier} defining how the time should be configured for the {@link DateTimeUtils}
 */
public record DateTimeUtils(Supplier<OffsetDateTime> timeSupplier) {

    /**
     * Creates a {@link DateTimeUtils} with a default {@link Supplier} for the current UTC time.
     *
     * @return the created {@link DateTimeUtils}
     */
    public static DateTimeUtils create() {
        return new DateTimeUtils(() -> OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Creates a {@link DateTimeUtils} with the provided {@link Supplier} for the current UTC time.
     *
     * <p>
     * Required to allow for testing since we cannot consistently execute unit test functions that use the actual current time, and instead need to
     * supply a test time that can be asserted against.
     *
     * <p>
     * Should not be used outside of tests unless absolutely necessary.
     *
     * @param timeSupplier {@link Supplier} defining how the time should be configured for the {@link DateTimeUtils}
     * @return the created {@link DateTimeUtils}
     */
    static DateTimeUtils createWithSupplier(final Supplier<OffsetDateTime> timeSupplier) {
        return new DateTimeUtils(timeSupplier);
    }

    /**
     * Get the current {@link LocalDateTime} in {@link ZoneOffset#UTC}.
     *
     * @return the current {@link ZoneOffset#UTC} {@link LocalDateTime}
     */
    public LocalDateTime currentUtcLocalDateTime() {
        return currentUtcDateTime().toLocalDateTime();
    }

    /**
     * Get the current {@link Timestamp} in {@link ZoneOffset#UTC}.
     *
     * @return the current {@link ZoneOffset#UTC} {@link Timestamp}
     */
    public Timestamp currentUtcTimestamp() {
        return new Timestamp(currentUtcDateTime().toInstant().toEpochMilli());
    }

    /**
     * Get the current {@link Year} in {@link ZoneOffset#UTC}.
     *
     * @return the current {@link ZoneOffset#UTC} {@link Year}
     */
    public Year currentUtcYear() {
        return Year.from(currentUtcDateTime());
    }

    /**
     * Get the current {@link Month} in {@link ZoneOffset#UTC}.
     *
     * @return the current {@link ZoneOffset#UTC} {@link Month}
     */
    public Month currentUtcMonth() {
        return currentUtcDateTime().toLocalDate().getMonth();
    }

    /**
     * Calculates the number of {@link TemporalUnit}s between the {@link #currentUtcDateTime()} and the start of next {@link Month}.
     *
     * @param temporalUnit the {@link TemporalUnit} of the response
     * @return the number of {@link TemporalUnit}s until {@code 00:00:000} {@link ZoneOffset#UTC} of the 1st of next {@link Month}
     */
    public long untilNextMonthUtc(final TemporalUnit temporalUnit) {
        final int currentMonth = currentUtcMonth().getValue();
        final int nextMonth = currentMonth == 12 ? 1 : (currentMonth + 1);
        final int currentYear = currentUtcYear().getValue();
        final int nextMonthYear = currentMonth == 12 ? (currentYear + 1) : currentYear;

        final String nextMonthAsDate = String.format("%s-%02d-01T00:00:00Z", nextMonthYear, nextMonth);

        return temporalUnit.between(
            currentUtcDateTime().toInstant(),
            Instant.parse(nextMonthAsDate).atZone(ZoneOffset.UTC)
        );
    }

    /**
     * Checks if the current {@link OffsetDateTime} day in {@link ZoneOffset#UTC} is the last day of the month.
     *
     * @return {@code true} if the current day is the last day of the month.
     */
    public boolean isLastDayOfMonth() {
        final int currentDayOfMonth = currentUtcDateTime().getDayOfMonth();
        final int lastDayOfMonth = currentUtcDateTime().toLocalDate()
            .with(TemporalAdjusters.lastDayOfMonth())
            .getDayOfMonth();

        return lastDayOfMonth == currentDayOfMonth;
    }

    private OffsetDateTime currentUtcDateTime() {
        return timeSupplier.get();
    }
}
