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

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DateTimeUtils}.
 */
class DateTimeUtilsTest {

    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2020, 11, 15, 5, 30);
    private static final Supplier<OffsetDateTime> TEST_TIME_SUPPLIER = () -> OffsetDateTime.of(TEST_TIME, ZoneOffset.UTC);
    private static final DateTimeUtils DATE_TIME_UTILS = DateTimeUtils.createWithSupplier(TEST_TIME_SUPPLIER);

    @Test
    void whenGetCurrentUtcLocalDateTime_thenCurrentUtcLocalDateTimeIsReturned() {
        final LocalDateTime currentUtcDateTime = DATE_TIME_UTILS.currentUtcLocalDateTime();

        assertThat(currentUtcDateTime)
            .isEqualTo(TEST_TIME);
    }

    @Test
    void whenGetCurrentUtcTimestamp_thenCurrentUtcTimestampIsReturned() {
        final Timestamp currentUtcTimestamp = DATE_TIME_UTILS.currentUtcTimestamp();
        final Timestamp expected = new Timestamp(Instant.parse("2020-11-15T05:30:00.00Z").toEpochMilli());

        assertThat(currentUtcTimestamp)
            .isEqualTo(expected);
    }

    @Test
    void whenGetCurrentUtcYear_thenCurrentUtcYearIsReturned() {
        final Year currentUtcYear = DATE_TIME_UTILS.currentUtcYear();

        assertThat(currentUtcYear)
            .isEqualTo(Year.of(TEST_TIME.getYear()));
    }

    @Test
    void whenGetCurrentUtcMonth_thenCurrentUtcMonthIsReturned() {
        final Month currentUtcMonth = DATE_TIME_UTILS.currentUtcMonth();

        assertThat(currentUtcMonth)
            .isEqualTo(TEST_TIME.getMonth());
    }

    @Test
    void whenGetUntilNextMonthUtc_givenChronoUnitOfSeconds_andCurrentMonthIsNotDecember_thenCorrectCountIsReturned() {
        final LocalDateTime testTime = LocalDateTime.of(2020, 10, 15, 5, 30);
        final Supplier<OffsetDateTime> testTimeSupplier = () -> OffsetDateTime.of(testTime, ZoneOffset.UTC);
        final DateTimeUtils dateTimeUtils = DateTimeUtils.createWithSupplier(testTimeSupplier);

        final long actual = dateTimeUtils.untilNextMonthUtc(ChronoUnit.SECONDS);

        assertThat(actual)
            .isEqualTo(1_449_000L);
    }

    @Test
    void whenGetUntilNextMonthUtc_givenChronoUnitOfMilliseconds_andCurrentMonthIsNotDecember_thenCorrectCountIsReturned() {
        final LocalDateTime testTime = LocalDateTime.of(2020, 10, 15, 5, 30);
        final Supplier<OffsetDateTime> testTimeSupplier = () -> OffsetDateTime.of(testTime, ZoneOffset.UTC);
        final DateTimeUtils dateTimeUtils = DateTimeUtils.createWithSupplier(testTimeSupplier);

        final long actual = dateTimeUtils.untilNextMonthUtc(ChronoUnit.MILLIS);

        assertThat(actual)
            .isEqualTo(1_449_000_000L);
    }

    @Test
    void whenGetUntilNextMonthUtc_givenChronoUnitOfSeconds_andCurrentMonthIsDecember_thenCorrectCountIsReturned() {
        final LocalDateTime testTime = LocalDateTime.of(2020, 12, 15, 5, 30);
        final Supplier<OffsetDateTime> testTimeSupplier = () -> OffsetDateTime.of(testTime, ZoneOffset.UTC);
        final DateTimeUtils dateTimeUtils = DateTimeUtils.createWithSupplier(testTimeSupplier);

        final long actual = dateTimeUtils.untilNextMonthUtc(ChronoUnit.SECONDS);

        assertThat(actual)
            .isEqualTo(1_449_000L);
    }

    @Test
    void whenCreateWithoutSupplier_givenGetCurrentUtcLocalDateTime_thenReturnedValueIsCloseToCurrentTime_andNotTestTime() {
        final DateTimeUtils dateTimeUtils = DateTimeUtils.create();
        final LocalDateTime currentUtcDateTime = dateTimeUtils.currentUtcLocalDateTime();
        final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // Get the diff between the returned current time, and the actual current time
        // Since there will be a small delay between executions, we can't compare the times directly, so we count the seconds in between
        final long diffInSeconds = ChronoUnit.SECONDS.between(currentUtcDateTime, now);

        assertThat(diffInSeconds)
            .isLessThan(5L);

        assertThat(currentUtcDateTime)
            .isNotEqualTo(TEST_TIME);
    }
}
