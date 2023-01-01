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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EncodingUtils}.
 */
class EncodingUtilsTest {

    @Test
    void whenEncodingBasicAuthentication_givenValidUserNamePasswordSupplied_thenAuthenticationIsEncoded() {
        final String userName = "userName";
        final String password = "password";

        final String result = EncodingUtils.encodeBasicAuthentication(userName, password);

        assertThat(result)
            .isEqualTo("Basic dXNlck5hbWU6cGFzc3dvcmQ=");
    }

    @Test
    void whenCheckingIfNotBasicAuthentication_givenInputIsNull_thenTrueIsReturned() {
        final boolean result = EncodingUtils.isInvalidBasicAuthentication(null);
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenCheckingIfNotBasicAuthentication_givenInputIsNotBasicAuthentication_thenTrueIsReturned() {
        final boolean result = EncodingUtils.isInvalidBasicAuthentication("invalid");
        assertThat(result)
            .isTrue();
    }

    @Test
    void whenCheckingIfNotBasicAuthentication_givenInputIsBasicAuthentication_thenFalseIsReturned() {
        final boolean result = EncodingUtils.isInvalidBasicAuthentication("Basic dXNlck5hbWU6cGFzc3dvcmQ=");
        assertThat(result)
            .isFalse();
    }

    @Test
    void whenDecodingBasicAuthentication_givenInputIsNull_thenExceptionIsThrown() {
        assertThatThrownBy(() -> EncodingUtils.decodeBasicAuthentication(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot decode null");
    }

    @Test
    void whenDecodingBasicAuthentication_givenInputIsNotBasicAuthentication_thenExceptionIsThrown() {
        assertThatThrownBy(() -> EncodingUtils.decodeBasicAuthentication("invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot decode input that does not start with: 'Basic '");
    }

    @Test
    void whenDecodingBasicAuthentication_givenInputHasInvalidEncoding_thenExceptionIsThrown() {
        final String encodedInput = encode("NonBasic", "userName:password");
        assertThatThrownBy(() -> EncodingUtils.decodeBasicAuthentication(encodedInput))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot decode input that does not start with: 'Basic '");
    }

    @Test
    void whenDecodingBasicAuthentication_givenInputIsValid_thenDecodedUsernameAndPasswordIsReturned() {
        final String encodedInput = encode("Basic", "userName:password");
        final Map<String, String> result = EncodingUtils.decodeBasicAuthentication(encodedInput);

        assertThat(result)
            .containsExactlyEntriesOf(
                Map.of(
                    EncodingUtils.DECODED_PASSWORD_KEY, "password",
                    EncodingUtils.DECODED_USERNAME_KEY, "userName"
                )
            );
    }

    @Test
    void whenDecodingAuthentication_givenInputIsNull_thenExceptionIsThrown() {
        assertThatThrownBy(() -> EncodingUtils.decodeBasicAuthentication(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenDecodingAuthentication_givenInputIsBlank_thenExceptionIsThrown() {
        assertThatThrownBy(() -> EncodingUtils.decodeBasicAuthentication("invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot decode input that does not start with: 'Basic '");
    }

    @Test
    void whenDecodingAuthentication_givenInputDoesNotHaveValidDelimiter_thenExceptionIsThrown() {
        final String encodedInput = encode("Basic", "userName+password");

        assertThatThrownBy(() -> EncodingUtils.decodeBasicAuthentication(encodedInput))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Decoded input does not contain: ':'");
    }

    private static String encode(final String prefix, final String input) {
        final byte[] usernameAndPassword = input.getBytes(StandardCharsets.ISO_8859_1);
        return prefix + " " + Base64
            .getEncoder()
            .encodeToString(usernameAndPassword);
    }
}
