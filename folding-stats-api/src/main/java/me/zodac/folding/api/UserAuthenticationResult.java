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

package me.zodac.folding.api;

import java.util.Collections;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * POJO defining the authentication response for a system user/password, and the roles for that user if successful.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserAuthenticationResult {

    private static final String ADMIN_ROLE = "admin";

    private final boolean userExists;
    private final boolean passwordMatch;
    private final Set<String> userRoles;

    /**
     * User does not exist {@link UserAuthenticationResult}.
     *
     * @return the {@link UserAuthenticationResult}
     */
    public static UserAuthenticationResult userDoesNotExist() {
        return new UserAuthenticationResult(false, false, Collections.emptySet());
    }

    /**
     * User exists, but invalid password provided {@link UserAuthenticationResult}.
     *
     * @return the {@link UserAuthenticationResult}
     */
    public static UserAuthenticationResult invalidPassword() {
        return new UserAuthenticationResult(true, false, Collections.emptySet());
    }

    /**
     * Successful {@link UserAuthenticationResult} with retrieved user roles.
     *
     * @param userRoles the roles of the user that has been successfully logged in
     * @return the {@link UserAuthenticationResult}
     */
    public static UserAuthenticationResult success(final Set<String> userRoles) {
        return new UserAuthenticationResult(true, true, userRoles);
    }

    /**
     * Checks if the {@code userRoles} of the {@link UserAuthenticationResult} contain the 'admin' user role.
     *
     * @return <code>true</code> if the {@code userRoles} contains the 'admin' user role
     */
    public boolean isAdmin() {
        return userRoles.contains(ADMIN_ROLE);
    }
}
