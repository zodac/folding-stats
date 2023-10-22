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

package me.zodac.folding.stats.http.response;

import java.util.Comparator;

/**
 * Response returned from {@link me.zodac.folding.stats.http.request.UnitsUrlBuilder} for a user/passkey.
 *
 * <p>
 * Expected response:
 * <pre>
 *     [
 *         {
 *             "finished":21260, (Value we are interested in)
 *             "expired":60,
 *             "active":1
 *         },
 *         {
 *             "finished":512,
 *             "expired":4,
 *             "active":0
 *         }
 *     ]
 * </pre>
 */
record UnitsApiInstance(int finished, int expired, int active) implements Comparable<UnitsApiInstance> {

    @Override
    public int compareTo(final UnitsApiInstance o) {
        return Comparator.comparingInt(UnitsApiInstance::finished)
            .thenComparingInt(UnitsApiInstance::active)
            .thenComparingInt(UnitsApiInstance::expired)
            .compare(o, this);
    }
}
