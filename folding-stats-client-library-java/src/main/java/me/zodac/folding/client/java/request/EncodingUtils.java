package me.zodac.folding.client.java.request;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * {@link UtilityClass} to assist with encoding or decoding {@link String}s for authentication.
 */
@UtilityClass
final class EncodingUtils {

    /**
     * Encodes the provided {@code userName} and {@code password} for basic authentication. Takes the {@code userName}
     * and {@code password}, concatenates them with a <b>:</b> as separator, then encodes in {@link Base64}. This is then
     * prefixed by the{@link String} "Basic " (note the trailing space).
     *
     * @param userName the user name
     * @param password the password
     * @return the {@link Base64}-encoded authentication {@link String}
     */
    static String encodeAuthentication(final String userName, final String password) {
        return "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes(StandardCharsets.ISO_8859_1));
    }
}
