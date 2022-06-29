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
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenDecodingBasicAuthentication_givenInputIsNotBasicAuthentication_thenExceptionIsThrown() {
        assertThatThrownBy(() -> EncodingUtils.decodeBasicAuthentication("invalid"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenDecodingBasicAuthentication_givenInputHasInvalidEncoding_thenExceptionIsThrown() {
        final String encodedInput = encode("NonBasic", "userName:password");
        assertThatThrownBy(() -> EncodingUtils.decodeBasicAuthentication(encodedInput))
            .isInstanceOf(IllegalArgumentException.class);
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
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void whenDecodingAuthentication_givenInputDoesNotHaveValidDelimiter_thenExceptionIsThrown() {
        final String encodedInput = encode("Basic", "userName+password");

        assertThatThrownBy(() -> EncodingUtils.decodeBasicAuthentication(encodedInput))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private static String encode(final String prefix, final String input) {
        final byte[] usernameAndPassword = input.getBytes(StandardCharsets.ISO_8859_1);
        return prefix + " " + Base64
            .getEncoder()
            .encodeToString(usernameAndPassword);
    }
}
