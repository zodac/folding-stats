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

package me.zodac.folding.api.exception;

import java.io.Serial;
import lombok.Getter;

/**
 * {@link Exception} for errors when connecting to an external service, or when an unexpected response is returned.
 */
@Getter
public class ExternalConnectionException extends Exception {

    @Serial
    private static final long serialVersionUID = 2084075114898438910L;

    /**
     * The URL that was unable to be connected to.
     */
    private final String url;

    /**
     * Constructor taking in the failing URL and an error message.
     *
     * @param url     the URL which was unable to be connected to
     * @param message the error message
     */
    public ExternalConnectionException(final String url, final String message) {
        super(message);
        this.url = url;
    }

    /**
     * Constructor taking in the failing URL, an error message and a cause {@link Throwable}.
     *
     * @param url       the URL which was unable to be connected to
     * @param message   the error message
     * @param cause the cause {@link Throwable}
     */
    public ExternalConnectionException(final String url, final String message, final Throwable cause) {
        super(message, cause);
        this.url = url;
    }
}
