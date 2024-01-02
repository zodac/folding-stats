/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link NumberUtils}.
 */
class NumberUtilsTest {

    @Test
    void whenFormattingNumberWithCommas_givenLongWithFewerThanFourDigits_thenInputIsReturnedUnchanged() {
        final long input = 100L;
        final String result = NumberUtils.formatWithCommas(input);
        assertThat(result)
            .isEqualTo("100");
    }

    @Test
    void whenFormattingNumberWithCommas_givenIntWithFewerThanFourDigits_thenInputIsFormatted() {
        final int input = 100;
        final String result = NumberUtils.formatWithCommas(input);
        assertThat(result)
            .isEqualTo("100");
    }

    @Test
    void whenFormattingNumberWithCommas_givenLong_thenInputIsFormatted() {
        final long input = 1_000L;
        final String result = NumberUtils.formatWithCommas(input);
        assertThat(result)
            .isEqualTo("1,000");
    }

    @Test
    void whenFormattingNumberWithCommas_givenInt_thenInputIsFormatted() {
        final int input = 1_000;
        final String result = NumberUtils.formatWithCommas(input);
        assertThat(result)
            .isEqualTo("1,000");
    }

    @Test
    void whenFormattingNumberWithCommas_givenLargeLong_thenInputIsFormatted() {
        final String result = NumberUtils.formatWithCommas(Long.MAX_VALUE);
        assertThat(result)
            .isEqualTo("9,223,372,036,854,775,807");
    }

    @Test
    void whenFormattingNumberWithCommas_givenLargeInt_thenInputIsFormatted() {
        final String result = NumberUtils.formatWithCommas(Integer.MAX_VALUE);
        assertThat(result)
            .isEqualTo("2,147,483,647");
    }

    @Test
    void whenFormattingNumberWithCommas_givenNegativeInt_thenInputIsFormatted() {
        final long input = -1_000L;
        final String result = NumberUtils.formatWithCommas(input);
        assertThat(result)
            .isEqualTo("-1,000");
    }

    @Test
    void whenFormattingNumberWithCommas_givenNegativeLong_thenInputIsFormatted() {
        final int input = -1_000;
        final String result = NumberUtils.formatWithCommas(input);
        assertThat(result)
            .isEqualTo("-1,000");
    }
}
