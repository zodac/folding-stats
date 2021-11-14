/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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
