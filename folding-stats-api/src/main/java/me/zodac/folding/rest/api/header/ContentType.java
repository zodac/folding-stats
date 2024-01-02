/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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
 * Values for {@link RestHeader#CONTENT_TYPE} REST headers.
 */
public enum ContentType {

    /**
     * The {@code application/json} value.
     */
    JSON("application/json"),

    /**
     * The {@code plain/text} value.
     */
    TEXT("plain/text");

    private final String contentTypeValue;

    /**
     * Constructs a {@link ContentType} with the header value as a {@link String}.
     *
     * @param contentTypeValue the {@link ContentType} value as a {@link String}
     */
    ContentType(final String contentTypeValue) {
        this.contentTypeValue = contentTypeValue;
    }

    /**
     * The value of the {@link ContentType}.
     *
     * @return the {@link ContentType} value
     */
    public String contentTypeValue() {
        return contentTypeValue;
    }
}
