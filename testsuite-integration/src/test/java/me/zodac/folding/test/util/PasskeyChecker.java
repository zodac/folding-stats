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

package me.zodac.folding.test.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

/**
 * Utility class to check if a passkey is correctly hidden or not.
 */
public final class PasskeyChecker {

    private PasskeyChecker() {

    }

    /**
     * Validates that the provided {@code passkey} is correctly hidden.
     *
     * <p>
     * The first 8 characters should not contain the <code>*</code> character, while the remaining 24 characters should not contain any alphanumeric
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
            .doesNotContain("*");

        assertThat(secondPartOfPasskey)
            .as("Expected the remaining 24 characters of the passkey to be masked")
            .doesNotContainPattern(Pattern.compile("[a-zA-Z]"));
    }

    /**
     * Validates that the provided {@code passkey} is correctly shown.
     *
     * <p>
     * There should be no <code>*</code> character in the passkey.
     *
     * @param passkey the passkey to validate
     */
    public static void assertPasskeyIsShown(final String passkey) {
        assertThat(passkey)
            .as("Expected the passkey to not be masked")
            .doesNotContain("*");
    }
}
