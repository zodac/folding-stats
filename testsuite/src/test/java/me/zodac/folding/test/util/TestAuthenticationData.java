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

package me.zodac.folding.test.util;

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
