/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

package me.zodac.folding.api;

import java.util.Set;

/**
 * POJO defining the authentication result for a system user/password, and the roles for that user if successful.
 *
 * @param userExists    does the request user exist
 * @param passwordMatch does the request password match the persisted password
 * @param userRoles     {@link Set} of the user roles
 */
public record UserAuthenticationResult(boolean userExists, boolean passwordMatch, Set<String> userRoles) {

    private static final String ADMIN_ROLE = "admin";

    /**
     * User does not exist {@link UserAuthenticationResult}.
     *
     * @return the {@link UserAuthenticationResult}
     */
    public static UserAuthenticationResult userDoesNotExist() {
        return new UserAuthenticationResult(false, false, Set.of());
    }

    /**
     * User exists, but invalid password provided {@link UserAuthenticationResult}.
     *
     * @return the {@link UserAuthenticationResult}
     */
    public static UserAuthenticationResult invalidPassword() {
        return new UserAuthenticationResult(true, false, Set.of());
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
     * @return {@code true} if the {@code userRoles} contains the 'admin' user role
     */
    public boolean isAdmin() {
        return userRoles.contains(ADMIN_ROLE);
    }
}
