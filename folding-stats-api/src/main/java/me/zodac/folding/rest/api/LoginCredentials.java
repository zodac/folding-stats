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

package me.zodac.folding.rest.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.util.EncodingUtils;


/**
 * Simple POJO defining {@link java.util.Base64}-encoded credentials. The credentials are created by:
 * <ol>
 *     <li>Concatenate the username and password, with a {@code :} as delimiter (i.e., 'username:password')</li>
 *     <li>Encode the result with {@link java.util.Base64}</li>
 *     <li>Prefix the encoded result with "Basic " (note the space)</li>
 * </ol>
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class LoginCredentials {

    private String encodedUserNameAndPassword;

    /**
     * Creates an instance of {@link LoginCredentials} given a {@link java.util.Base64}-encoded username/password credentials.
     *
     * <p>
     * If the input does not start with {@link EncodingUtils#BASIC_AUTHENTICATION_SCHEME}, it will be prefixed.
     *
     * @param encodedUserNameAndPassword the {@link java.util.Base64}-encoded username/password
     * @return the {@link LoginCredentials} for the given credentials
     */
    public static LoginCredentials createWithBasicAuthentication(final String encodedUserNameAndPassword) {
        if (encodedUserNameAndPassword.startsWith(EncodingUtils.BASIC_AUTHENTICATION_SCHEME)) {
            return new LoginCredentials(encodedUserNameAndPassword);
        }

        return new LoginCredentials(EncodingUtils.BASIC_AUTHENTICATION_SCHEME + encodedUserNameAndPassword);
    }
}
