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

    @Test
    void whenIsLastDayOfMonth_given28thFeburary_andNotLeapYear_thenTrueIsReturned() {
        final LocalDateTime testTime = LocalDateTime.of(2019, 2, 28, 0, 0);
        final Supplier<OffsetDateTime> testTimeSupplier = () -> OffsetDateTime.of(testTime, ZoneOffset.UTC);
        final DateTimeUtils dateTimeUtils = DateTimeUtils.createWithSupplier(testTimeSupplier);

        final boolean actual = dateTimeUtils.isLastDayOfMonth();
        assertThat(actual)
            .isTrue();
    }

    @Test
    void whenIsLastDayOfMonth_given28thFeburary_andLeapYear_thenFalseIsReturned() {
        final LocalDateTime testTime = LocalDateTime.of(2020, 2, 28, 0, 0);
        final Supplier<OffsetDateTime> testTimeSupplier = () -> OffsetDateTime.of(testTime, ZoneOffset.UTC);
        final DateTimeUtils dateTimeUtils = DateTimeUtils.createWithSupplier(testTimeSupplier);

        final boolean actual = dateTimeUtils.isLastDayOfMonth();
        assertThat(actual)
            .isFalse();
    }
}
