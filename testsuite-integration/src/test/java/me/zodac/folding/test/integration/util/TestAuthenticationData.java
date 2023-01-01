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

package me.zodac.folding.test.integration.util;

/**
 * Simple {@link Enum} containing the username and password data for test execution. Using default details that should not be used in production.
 */
public enum TestAuthenticationData {

    /**
     * A valid user with the 'admin' role.
     */
    ADMIN_USER("root", "shroot"),

    /**
     * A valid user with the 'read-only' role.
     */
    READ_ONLY_USER("root2", "shroot2"),

    /**
     * An invalid user with an invalid username.
     */
    INVALID_USERNAME("invalid", "shroot"),

    /**
     * An invalid user with an invalid password.
     */
    INVALID_PASSWORD("root", "invalid");

    private final String userName;
    private final String password;

    /**
     * Constructs a {@link TestAuthenticationData}.
     *
     * @param userName the username
     * @param password the password
     */
    TestAuthenticationData(final String userName, final String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     * Retrieves the username.
     *
     * @return the username
     */
    public String userName() {
        return userName;
    }

    /**
     * Retrieves the password.
     *
     * @return the password
     */
    public String password() {
        return password;
    }
}
