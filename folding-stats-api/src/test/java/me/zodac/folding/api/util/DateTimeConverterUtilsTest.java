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
    void whenGetTimeStamp_givenFullDateAndTime_thenValidTimestampIsReturned() {
        final Timestamp actual = DateTimeConverterUtils.getTimestampOf(Year.of(2020), Month.DECEMBER, 3, 15, 27, 55);
        final Timestamp expected = new Timestamp(Instant.parse("2020-12-03T15:27:55.00Z").toEpochMilli());

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
