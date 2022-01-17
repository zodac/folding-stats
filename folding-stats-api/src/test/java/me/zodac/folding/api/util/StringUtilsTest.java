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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StringUtils}.
 */
class StringUtilsTest {

    @Test
    void whenIsBlankOrValidUrl_givenInputIsNull_thenTrueIsReturned() {
        final boolean result = StringUtils.isBlankOrValidUrl(null);
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsBlankOrValidUrl_givenInputIsEmptyString_thenTrueIsReturned() {
        final boolean result = StringUtils.isBlankOrValidUrl("");
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsBlankOrValidUrl_givenInputIsValidHttpUrl_thenTrueIsReturned() {
        final boolean result = StringUtils.isBlankOrValidUrl("http://www.google.com");
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsBlankOrValidUrl_givenInputIsValidHttpsUrl_thenTrueIsReturned() {
        final boolean result = StringUtils.isBlankOrValidUrl("https://www.google.com");
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsBlankOrValidUrl_givenInputIsValidFileUrl_thenFalseIsReturned() {
        final boolean result = StringUtils.isBlankOrValidUrl("file://www.google.com");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsBlankOrValidUrl_givenInputIsInvalidUrl_thenFalseIsReturned() {
        final boolean result = StringUtils.isBlankOrValidUrl("invalidUrl");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsBlankString_givenInputIsNull_thenTrueIsReturned() {
        final boolean result = StringUtils.isBlank(null);
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsBlankString_givenInputIsEmptyString_thenTrueIsReturned() {
        final boolean result = StringUtils.isBlank("");
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsBlankString_givenInputIsStringOfWhitespace_thenTrueIsReturned() {
        final boolean result = StringUtils.isBlank("    ");
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsBlankString_givenInputIsValidString_thenFalseIsReturned() {
        final boolean result = StringUtils.isBlank("validString");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsNotBlankString_givenInputIsNull_thenFalseIsReturned() {
        final boolean result = StringUtils.isNotBlank(null);
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsNotBlankString_givenInputIsEmptyString_thenFalseIsReturned() {
        final boolean result = StringUtils.isNotBlank("");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsNotBlankString_givenInputIsStringOfWhitespace_thenFalseIsReturned() {
        final boolean result = StringUtils.isNotBlank("    ");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsNotBlankString_givenInputIsValidString_thenTrueIsReturned() {
        final boolean result = StringUtils.isNotBlank("validString");
        assertThat(result)
            .isTrue();
    }
}
