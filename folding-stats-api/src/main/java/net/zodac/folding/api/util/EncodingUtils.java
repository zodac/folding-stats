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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Utility class to help with encoding or decoding {@link String}s for authentication.
 */
public final class EncodingUtils {

    /**
     * Prefix defining the <b>Basic</b> authentication scheme.
     */
    public static final String BASIC_AUTHENTICATION_SCHEME = "Basic ";
    private static final Pattern BASIC_AUTHENTICATION_PATTERN = Pattern.compile(BASIC_AUTHENTICATION_SCHEME);

    private static final String DECODED_USERNAME_PASSWORD_DELIMITER = ":";
    private static final Pattern DECODED_USERNAME_PASSWORD_DELIMITER_PATTERN = Pattern.compile(DECODED_USERNAME_PASSWORD_DELIMITER);

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
        return !authorizationPayload.contains(BASIC_AUTHENTICATION_SCHEME);
    }

    /**
     * Decodes the authorization payload for the {@value #BASIC_AUTHENTICATION_SCHEME} authentication scheme.
     *
     * @param authorizationPayload the authorization value to decode
     * @return the {@link DecodedLoginCredentials}
     * @throws IllegalArgumentException thrown if the input is not a valid {@link Base64} {@link String}
     */
    public static DecodedLoginCredentials decodeBasicAuthentication(final String authorizationPayload) {
        if (!authorizationPayload.startsWith(BASIC_AUTHENTICATION_SCHEME)) {
            throw new IllegalArgumentException(String.format("Cannot decode input that does not start with: '%s'", BASIC_AUTHENTICATION_SCHEME));
        }

        final String encodedUserNameAndPassword = BASIC_AUTHENTICATION_PATTERN.split(authorizationPayload, 2)[1];
        return decodeAuthentication(encodedUserNameAndPassword);
    }

    private static DecodedLoginCredentials decodeAuthentication(final String encodedUserNameAndPassword) {
        final String decodedUserNameAndPassword = new String(Base64.getDecoder().decode(encodedUserNameAndPassword), StandardCharsets.ISO_8859_1);

        if (!decodedUserNameAndPassword.contains(DECODED_USERNAME_PASSWORD_DELIMITER)) {
            throw new IllegalArgumentException(String.format("Decoded input does not contain: '%s'", DECODED_USERNAME_PASSWORD_DELIMITER));
        }

        final String[] userNameAndPasswordTokens = DECODED_USERNAME_PASSWORD_DELIMITER_PATTERN.split(decodedUserNameAndPassword, 2);
        return new DecodedLoginCredentials(userNameAndPasswordTokens[0], userNameAndPasswordTokens[1]);
    }
}
