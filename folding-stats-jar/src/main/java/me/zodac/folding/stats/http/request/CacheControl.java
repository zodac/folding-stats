/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.stats.http.request;

import me.zodac.folding.rest.api.header.RestHeader;

/**
 * Values for {@link RestHeader#CACHE_CONTROL} REST headers.
 */
public enum CacheControl {

    /**
     * The {@code no-cache} value.
     */
    NO_CACHE("no-cache"),

    /**
     * The {@code no-store} value.
     */
    NO_STORE("no-store");

    private final String headerValue;

    /**
     * Constructs a {@link CacheControl} with the header value as a {@link String}.
     *
     * @param headerValue the {@link CacheControl} value as a {@link String}
     */
    CacheControl(final String headerValue) {
        this.headerValue = headerValue;
    }

    /**
     * The value of the {@link CacheControl}.
     *
     * @return the {@link CacheControl} value
     */
    public String headerValue() {
        return headerValue;
    }
}
