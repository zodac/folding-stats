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

package net.zodac.folding.rest.exception;

import java.io.Serial;
import lombok.Getter;
import net.zodac.folding.rest.api.LoginCredentials;

/**
 * {@link Exception} to be thrown when a provided {@link LoginCredentials} is not valid.
 */
@Getter
public class InvalidLoginCredentialsException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5115765817637450011L;

    /**
     * The invalid {@link LoginCredentials}.
     */
    private final transient LoginCredentials loginCredentials;

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
}
