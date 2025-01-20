/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.api.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DateTimeConverterUtils}.
 */
class DateTimeConverterUtilsTest {

    @Test
    void whenGetLocalDateTime_givenYearAndMonth_thenValidLocalDateTimeIsReturned() {
        final LocalDateTime actual = DateTimeConverterUtils.getLocalDateTimeOf(Year.of(2020), Month.DECEMBER);
        final LocalDateTime expected = LocalDateTime.of(2020, 12, 1, 0, 0, 0);

        assertThat(actual)
            .isEqualTo(expected);
    }

    @Test
    void whenGetLocalDateTime_givenFullDateAndTime_thenValidLocalDateTimeIsReturned() {
        final LocalDateTime actual = DateTimeConverterUtils.getLocalDateTimeOf(Year.of(2020), Month.DECEMBER, 3, 15, 27, 55);
        final LocalDateTime expected = LocalDateTime.of(2020, 12, 3, 15, 27, 55, 0);

        assertThat(actual)
            .isEqualTo(expected);
    }

    @Test
    void whenConvertDateToUtcLocalDateTime_givenDate_thenUtcLocalDateTimeIsReturned() {
        final long currentTimeInMilliseconds = System.currentTimeMillis();
        final Timestamp date = new Timestamp(currentTimeInMilliseconds);

        final LocalDateTime actual = DateTimeConverterUtils.toUtcLocalDateTime(date);
        final LocalDateTime expected = Instant.ofEpochMilli(currentTimeInMilliseconds)
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime();

        assertThat(actual)
            .isEqualTo(expected);
    }

    @Test
    void whenGetFirstTimeStamp_givenFullDate_thenFirstTimestampIsReturned() {
        final Timestamp actual = DateTimeConverterUtils.getFirstTimestampOf(Year.of(2020), Month.DECEMBER, 3);
        final Timestamp expected = new Timestamp(Instant.parse("2020-12-03T00:00:00.00Z").toEpochMilli());

        assertThat(actual)
            .isEqualTo(expected);
    }

    @Test
    void whenGetLastTimeStamp_givenFullDate_thenFirstTimestampIsReturned() {
        final Timestamp actual = DateTimeConverterUtils.getLastTimestampOf(Year.of(2020), Month.DECEMBER, 3);
        final Timestamp expected = new Timestamp(Instant.parse("2020-12-03T23:59:59.00Z").toEpochMilli());

        assertThat(actual)
            .isEqualTo(expected);
    }

    @Test
    void whenGetTimeStamp_givenLocalDateTime_thenValidTimestampIsReturned() {
        final Timestamp actual = DateTimeConverterUtils.toTimestamp(LocalDateTime.of(2020, Month.DECEMBER, 3, 15, 27, 55));
        final Timestamp expected = new Timestamp(Instant.parse("2020-12-03T15:27:55.00Z").toEpochMilli());

        assertThat(actual)
            .isEqualTo(expected);
    }

    @Test
    void whenFormatMonth_givenValidMonth_thenCorrectFormatIsReturned() {
        final String actual = DateTimeConverterUtils.formatMonth(Month.APRIL);

        assertThat(actual)
            .isEqualTo("April");
    }
}
