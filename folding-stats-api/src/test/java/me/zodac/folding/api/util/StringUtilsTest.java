/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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
    void whenIsBlankOrValidUrl_givenInputIsInvalidUrlScheme_thenFalseIsReturned() {
        final boolean result = StringUtils.isBlankOrValidUrl("file://www.google.com");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsBlankOrValidUrl_givenInputIsInvalidUrl_thenFalseIsReturned() {
        final boolean result = StringUtils.isBlankOrValidUrl("http://www.google.com/q?s=^query");
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

    @Test
    void whenIsNeitherBlank_givenFirstInputIsNotBlank_andSecondInputIsNotBlank_thenTrueIsReturned() {
        final boolean result = StringUtils.isNeitherBlank("value", "value");
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsNeitherBlank_givenFirstInputIsBlank_andSecondInputIsBlank_thenFalseIsReturned() {
        final boolean result = StringUtils.isNeitherBlank(null, null);
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsNeitherBlank_givenFirstInputIsNotBlank_andSecondInputIsBlank_thenFalseIsReturned() {
        final boolean result = StringUtils.isNeitherBlank("value", null);
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsNeitherBlank_givenFirstInputIsBlank_andSecondInputIsNotBlank_thenFalseIsReturned() {
        final boolean result = StringUtils.isNeitherBlank(null, "value");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsEqualSafe_givenFirstInputIsNull_andSecondIsNull_thenTrueIsReturned() {
        final boolean result = StringUtils.isEqualSafe(null, null);
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsEqualSafe_givenFirstInputIsNull_andSecondIsNotNull_thenFalseIsReturned() {
        final boolean result = StringUtils.isEqualSafe(null, "value");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsEqualSafe_givenFirstInputIsNotNull_andSecondIsNull_thenFalseIsReturned() {
        final boolean result = StringUtils.isEqualSafe("value", null);
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenIsEqualSafe_givenFirstInputIsNull_andSecondIsNull_andTheyAreTheSame_thenTrueIsReturned() {
        final boolean result = StringUtils.isEqualSafe("value", "value");
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenIsEqualSafe_givenFirstInputIsNotNull_andSecondIsNotNull_andTheyAreDifferent_thenFalseIsReturned() {
        final boolean result = StringUtils.isEqualSafe("value", "value2");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenUnescapeHtml_givenEscapedHtml_thenUnescapedHtmlIsReturned() {
        final String result = StringUtils.unescapeHtml("hello%20world");
        assertThat(result)
            .isEqualTo("hello world");
    }

    @Test
    void whenUnescapeHtml_givenUnescapedHtml_thenInputIsReturned() {
        final String result = StringUtils.unescapeHtml("hello world");
        assertThat(result)
            .isEqualTo("hello world");
    }

    @Test
    void whenUnescapeHtml_givenNullInput_thenEmptyStringIsReturned() {
        final String result = StringUtils.unescapeHtml(null);
        assertThat(result)
            .isEmpty();
    }
}
