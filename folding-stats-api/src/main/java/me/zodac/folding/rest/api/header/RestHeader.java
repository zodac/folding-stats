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

package me.zodac.folding.rest.api.header;

/**
 * Set of REST headers and their name.
 */
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
