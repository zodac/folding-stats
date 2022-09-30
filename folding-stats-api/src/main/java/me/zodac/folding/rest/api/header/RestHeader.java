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

package me.zodac.folding.rest.api.header;

/**
 * Set of REST headers and their name.
 */
// TODO: Needed in this module?
public enum RestHeader {

    /**
     * The {@code Authorization} header.
     */
    AUTHORIZATION("Authorization"),

    /**
     * The {@code Cache-Control} header.
     */
    CACHE_CONTROL("Cache-Control"),

    /**
     * The {@code Content-Type} header.
     */
    CONTENT_TYPE("Content-Type"),

    /**
     * The {@code ETag} header.
     */
    ETAG("eTag"),

    /**
     * The {@code If-None-Match} header.
     */
    IF_NONE_MATCH("If-None-Match"),

    /**
     * The {@code X-Total-Count} header.
     */
    TOTAL_COUNT("X-Total-Count");

    private final String headerName;

    /**
     * Constructs a {@link RestHeader}.
     *
     * @param headerName the {@link RestHeader} name
     */
    RestHeader(final String headerName) {
        this.headerName = headerName;
    }

    /**
     * The {@link RestHeader} name.
     *
     * @return the name of the {@link RestHeader}
     */
    public String headerName() {
        return headerName;
    }
}
