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

package me.zodac.folding.api.tc.stats;

/**
 * Utility to convert/update stats classes with one another.
 */
public final class StatsConverter {

    private StatsConverter() {

    }

    /**
     * Converts an instance of {@link UserTcStats} to {@link OffsetTcStats}.
     *
     * @param userTcStats existing {@link UserTcStats}
     * @return the created {@link OffsetTcStats}
     */
    public static OffsetTcStats toOffsetStats(final UserTcStats userTcStats) {
        return OffsetTcStats.create(userTcStats.points(), userTcStats.multipliedPoints(), userTcStats.units());
    }
}
