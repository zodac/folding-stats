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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link StringUtils}.
 */
class StringUtilsTest {

    @ParameterizedTest
    @CsvSource({
        "http://www.google.com,true",               // Valid HTTP URL
        "https://www.google.com,true",              // Valid HTTPS URL
        "file://www.google.com,false",              // Invalid URL protocol
        "http://www.google.com/q?s=^query,false",   // Invalid URL
        "'',true",                                  // Blank URL
        ",true",                                    // Null URL
    })
    void testIsBlankOrValidUrl(final String input, final boolean expected) {
        final boolean result = StringUtils.isBlankOrValidUrl(input);
        assertThat(result)
            .isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "validString,false",    // Valid String
        "'     ',true",         // Blank, multiple whitespaces
        "'',true",              // Blank
        ",true",                // Null
    })
    void testIsBlank(final String input, final boolean expected) {
        final boolean result = StringUtils.isBlank(input);
        assertThat(result)
            .isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "validString,true", // Valid String
        "'     ',false",    // Blank, multiple whitespaces
        "'',false",         // Blank
        ",false",           // Null
    })
    void testIsNotBlank(final String input, final boolean expected) {
        final boolean result = StringUtils.isNotBlank(input);
        assertThat(result)
            .isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "value,value,true", // Both not blank
        "value,,false",     // First not blank, second null
        ",value,false",     // First null, second not blank
        ",,false",          // Both null
    })
    void testIsNeitherBlank(final String first, final String second, final boolean expected) {
        final boolean result = StringUtils.isNeitherBlank(first, second);
        assertThat(result)
            .isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "hello%20world,hello world",    // Escaped HTML
        "hello world,hello world",      // Unescaped HTML
        ",''",                          // Null
    })
    void testUnescapeHtml(final String input, final String expected) {
        final String result = StringUtils.unescapeHtml(input);
        assertThat(result)
            .isEqualTo(expected);
    }
}
