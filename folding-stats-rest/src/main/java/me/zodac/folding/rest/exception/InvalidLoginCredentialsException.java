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

package me.zodac.folding.rest.exception;

import java.io.Serial;
import me.zodac.folding.rest.api.LoginCredentials;

/**
 * {@link Exception} to be thrown when a provided {@link LoginCredentials} is not valid.
 */
public class InvalidLoginCredentialsException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5115765817637450011L;

    /**
     * The invalid {@link LoginCredentials}.
     */
    private final LoginCredentials loginCredentials;

    /**
     * Basic constructor.
     *
     * @param loginCredentials the invalid {@link LoginCredentials}
     */
    public InvalidLoginCredentialsException(final LoginCredentials loginCredentials) {
        super();
        this.loginCredentials = loginCredentials;
    }

    /**
     * Basic constructor.
     *
     * @param loginCredentials the invalid {@link LoginCredentials}
     * @param cause        the cause {@link Throwable}
     */
    public InvalidLoginCredentialsException(final LoginCredentials loginCredentials, final Throwable cause) {
        super(cause);
        this.loginCredentials = loginCredentials;
    }

    /**
     * The invalid {@link LoginCredentials}.
     *
     * @return the invalid {@link LoginCredentials}
     */
    public LoginCredentials getLoginCredentials() {
        return loginCredentials;
    }
}
