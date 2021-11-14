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

package me.zodac.folding.api.tc.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Convenience methods to assist with the validation logic.
 */
final class ValidationUtils {

    private static final Set<String> VALID_URL_SCHEMES = Set.of("http", "https");

    private ValidationUtils() {

    }

    /**
     * Checks if the provided {@link String} is <code>null</code>, and if it is not, that it is a valid URL.
     *
     * @param input the {@link String} to check
     * @return <code>true</code> if the {@link String} is either empty, or a valid URL
     */
    static boolean isBlankOrValidUrl(final String input) {
        if (isBlankString(input)) {
            return true;
        }

        return isValidUrl(input);
    }

    /**
     * Checks if the provided {@link String} is <code>null</code>, or {@link String#isBlank()}.
     *
     * @param input the {@link String} to check
     * @return <code>true</code> if the {@link String} is either <code>null</code> or {@link String#isBlank()}
     */
    static boolean isBlankString(final String input) {
        return input == null || input.isBlank();
    }

    private static boolean isValidUrl(final String input) {
        final URI uri;
        try {
            uri = new URI(input);
        } catch (final URISyntaxException e) {
            return false;
        }

        final String scheme = uri.getScheme();
        return scheme != null && VALID_URL_SCHEMES.contains(scheme);
    }
}
