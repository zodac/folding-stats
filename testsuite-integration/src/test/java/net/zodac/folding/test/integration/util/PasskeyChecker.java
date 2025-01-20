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

package net.zodac.folding.test.integration.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

/**
 * Utility class to check if a passkey is correctly hidden or not.
 */
public final class PasskeyChecker {

    private static final String MASK_CHARACTER = "*";
    private static final Pattern ALPHABET_PATTERN = Pattern.compile("[a-zA-Z]");

    private PasskeyChecker() {

    }

    /**
     * Validates that the provided {@code passkey} is correctly hidden.
     *
     * <p>
     * The first 8 characters should not contain the {@code *} character, while the remaining 24 characters should not contain any alphanumeric
     * characters.
     *
     * @param passkey the passkey to validate
     */
    public static void assertPasskeyIsHidden(final String passkey) {
        final int lengthOfUnmaskedPasskey = 8;
        final String firstPartOfPasskey = passkey.substring(0, lengthOfUnmaskedPasskey);
        final String secondPartOfPasskey = passkey.substring(lengthOfUnmaskedPasskey);

        assertThat(firstPartOfPasskey)
            .as("Expected the first 8 characters of the passkey to be shown")
            .doesNotContain(MASK_CHARACTER);

        assertThat(secondPartOfPasskey)
            .as("Expected the remaining 24 characters of the passkey to be masked")
            .doesNotContainPattern(ALPHABET_PATTERN);
    }

    /**
     * Validates that the provided {@code passkey} is correctly shown.
     *
     * <p>
     * There should be no {@code *} character in the passkey.
     *
     * @param passkey the passkey to validate
     */
    public static void assertPasskeyIsShown(final String passkey) {
        assertThat(passkey)
            .as("Expected the passkey to not be masked")
            .doesNotContain(MASK_CHARACTER);
    }
}
