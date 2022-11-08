/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class to assist with encoding or decoding {@link String}s for authentication.
 */
public final class EncodingUtils {

    /**
     * Key for the {@code username} value returned by {@link #decodeAuthentication(String)} and {@link #decodeBasicAuthentication(String)}.
     */
    public static final String DECODED_USERNAME_KEY = "userName";

    /**
     * Key for the {@code password} value returned by {@link #decodeAuthentication(String)} and {@link #decodeBasicAuthentication(String)}.
     */
    public static final String DECODED_PASSWORD_KEY = "password";

    /**
     * Prefix defining the <b>Basic</b> authentication scheme.
     */
    public static final String BASIC_AUTHENTICATION_SCHEME = "Basic ";

    private static final String DECODED_USERNAME_PASSWORD_DELIMITER = ":";
    private static final Pattern BASIC_AUTHENTICATION_PATTERN = Pattern.compile(BASIC_AUTHENTICATION_SCHEME);

    private EncodingUtils() {

    }

    /**
     * Encodes the provided {@code userName} and {@code password} for basic authentication. Takes the {@code userName}
     * and {@code password}, concatenates them with a <b>:</b> as separator, then encodes in {@link Base64}. This is then
     * prefixed by the {@link String} "Basic " (note the trailing space).
     *
     * @param userName the username
     * @param password the password
     * @return the {@link Base64}-encoded authentication {@link String}
     */
    public static String encodeBasicAuthentication(final String userName, final String password) {
        final byte[] usernameAndPassword = (userName + DECODED_USERNAME_PASSWORD_DELIMITER + password).getBytes(StandardCharsets.ISO_8859_1);
        return BASIC_AUTHENTICATION_SCHEME + Base64
            .getEncoder()
            .encodeToString(usernameAndPassword);
    }

    /**
     * Checks if the authorization payload contains the {@value BASIC_AUTHENTICATION_SCHEME} authentication scheme.
     *
     * @param authorizationPayload the value to check
     * @return {@code true} if the value is <b>not</b> a basic authentication payload
     */
    public static boolean isInvalidBasicAuthentication(final String authorizationPayload) {
        return authorizationPayload == null || !authorizationPayload.contains(BASIC_AUTHENTICATION_SCHEME);
    }

    /**
     * Decodes the authorization payload for the {@value #BASIC_AUTHENTICATION_SCHEME} authentication scheme.
     *
     * @param authorizationPayload the authorization value to decode
     * @return a {@link Map} with two keys, the {@link #DECODED_USERNAME_KEY} and {@link #DECODED_PASSWORD_KEY}
     * @throws IllegalArgumentException thrown if the input is not a valid {@link Base64} {@link String}
     */
    public static Map<String, String> decodeBasicAuthentication(final String authorizationPayload) {
        if (authorizationPayload == null) {
            throw new IllegalArgumentException("Cannot decode null");
        }

        if (!authorizationPayload.startsWith(BASIC_AUTHENTICATION_SCHEME)) {
            throw new IllegalArgumentException(String.format("Cannot decode input that does not start with: '%s'", BASIC_AUTHENTICATION_SCHEME));
        }

        final String encodedUserNameAndPassword = BASIC_AUTHENTICATION_PATTERN.split(authorizationPayload)[1];
        return decodeAuthentication(encodedUserNameAndPassword);
    }

    private static Map<String, String> decodeAuthentication(final String encodedUserNameAndPassword) {
        final String decodedUserNameAndPassword = new String(Base64.getDecoder().decode(encodedUserNameAndPassword), StandardCharsets.ISO_8859_1);

        if (!decodedUserNameAndPassword.contains(DECODED_USERNAME_PASSWORD_DELIMITER)) {
            throw new IllegalArgumentException(String.format("Decoded input does not contain: '%s'", DECODED_USERNAME_PASSWORD_DELIMITER));
        }

        final String[] userNameAndPasswordTokens = decodedUserNameAndPassword.split(DECODED_USERNAME_PASSWORD_DELIMITER, 2);

        return Map.of(
            DECODED_USERNAME_KEY, userNameAndPasswordTokens[0],
            DECODED_PASSWORD_KEY, userNameAndPasswordTokens[1]
        );
    }
}
