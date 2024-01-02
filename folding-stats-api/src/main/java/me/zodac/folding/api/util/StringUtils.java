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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import org.checkerframework.nullaway.checker.nullness.qual.Nullable;

/**
 * Convenience methods to assist with {@link String}s.
 */
public final class StringUtils {

    private static final Set<String> VALID_URL_SCHEMES = Set.of("http", "https");

    private StringUtils() {

    }

    /**
     * Checks if the provided {@link String} is {@code null}, and if it is not, that it is a valid URL.
     *
     * <p>
     * <b>NOTE:</b> Only supports <b>HTTP</b> or <b>HTTPS</b> URLs.
     *
     * @param input the {@link String} to check
     * @return {@code true} if the {@link String} is either empty, or a valid URL
     */
    public static boolean isBlankOrValidUrl(final @Nullable String input) {
        return isBlank(input)
            ||
            isValidUrl(input);
    }

    /**
     * Checks if the provided {@link String} is {@code null}, or {@link String#isBlank()}.
     *
     * @param input the {@link String} to check
     * @return {@code true} if the {@link String} is either {@code null} or {@link String#isBlank()}
     */
    public static boolean isBlank(final @Nullable String input) {
        return input == null || input.isBlank();
    }

    /**
     * Checks if the provided {@link String} is not {@code null}, and is not {@link String#isBlank()}.
     *
     * @param input the {@link String} to check
     * @return {@code true} if the {@link String} is neither {@code null} nor {@link String#isBlank()}
     */
    public static boolean isNotBlank(final @Nullable String input) {
        return !isBlank(input);
    }

    /**
     * Checks if the two provided {@link String}s are not {@code null}, and are not {@link String#isBlank()}.
     *
     * @param first  the first {@link String} to check
     * @param second the second {@link String} to check
     * @return {@code true} if the {@link String}s are neither {@code null} nor {@link String#isBlank()}
     */
    public static boolean isNeitherBlank(final @Nullable String first, @Nullable final String second) {
        return isNotBlank(first) && isNotBlank(second);
    }

    private static boolean isValidUrl(final @Nullable String input) {
        final URI uri;
        try {
            uri = new URI(input);
        } catch (final URISyntaxException e) {
            return false;
        }

        final String scheme = uri.getScheme();
        return scheme != null && VALID_URL_SCHEMES.contains(scheme.toLowerCase(Locale.UK));
    }

    /**
     * Unescapes a HTML {@link String}.
     *
     * <p>
     * For example, {@code hello%20world} becomes {@code hello world}.
     *
     * @param input the {@link String} to unescape
     * @return the unescaped {@link String}, or a blank {@link String} if the input is {@code null}
     */
    public static String unescapeHtml(final @Nullable String input) {
        if (input == null) {
            return "";
        }

        return URLDecoder.decode(input, StandardCharsets.UTF_8);
    }
}
