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

/**
 * Simple wrapper to hold the URL for a points or units REST request.
 *
 * @param url the URL
 */
public record StatsRequestUrl(String url) {

    /**
     * Creates a {@link StatsRequestUrl} based on the provided URL.
     *
     * @param statsRequestUrl the URL
     * @return the created {@link StatsRequestUrl}
     */
    public static StatsRequestUrl create(final CharSequence statsRequestUrl) {
        return new StatsRequestUrl(statsRequestUrl.toString());
    }
}
