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

package me.zodac.folding.api.exception;

/**
 * {@link Exception} for errors when connecting to an external service, or when an unexpected response is returned.
 */
public class ExternalConnectionException extends Exception {

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
     * @param throwable the cause {@link Throwable}
     */
    public ExternalConnectionException(final String url, final String message, final Throwable throwable) {
        super(message, throwable);
        this.url = url;
    }

    /**
     * The URL that could not be connected to, causing the {@link ExternalConnectionException} to be thrown.
     *
     * @return the failing URL
     */
    public String getUrl() {
        return url;
    }
}
