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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

/**
 * Convenience class with convenient date/time-based functions.
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
     * Required to allow for testing since we cannot reliable unit test functions that use the actual current time, and instead need to supply a test
     * time that can be asserted against.
     *
     * <p>
     * Should not be used outside of tests unless absolutely necessary.
     *
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
     * Calculates the number of {@link ChronoUnit}s between the {@link #currentUtcDateTime()} and the start of next {@link Month}.
     *
     * @param chronoUnit the {@link ChronoUnit} of the response
     * @return the number of {@link ChronoUnit}s until {@code 00:00:000} {@link ZoneOffset#UTC} of the 1st of next {@link Month}
     */
    public long untilNextMonthUtc(final ChronoUnit chronoUnit) {
        final int currentMonth = currentUtcMonth().getValue();
        final int nextMonth = currentMonth == 12 ? 1 : (currentMonth + 1);
        final int currentYear = currentUtcYear().getValue();
        final int nextMonthYear = currentMonth == 12 ? (currentYear + 1) : currentYear;

        final String nextMonthAsDate = String.format("%s-%02d-01T00:00:00Z", nextMonthYear, nextMonth);

        return chronoUnit.between(
            currentUtcDateTime().toInstant(),
            Instant.parse(nextMonthAsDate).atZone(ZoneOffset.UTC)
        );
    }

    private OffsetDateTime currentUtcDateTime() {
        return timeSupplier.get();
    }
}
