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

package net.zodac.folding.test.integration.util;

import net.zodac.folding.api.tc.User;

/**
 * Simple POJO defining the stats for a user to be persisted in a test DB for stats-based tests.
 * Used since the normal stubbed endpoints insert stats with the timestamp based on the time of execution, whereas for
 * historic stats, we want to specify the timestamps.
 */
// Replace usage with UserTcStats?
public record DummyStats(int userId, String timestamp, long points, long multipliedPoints, int units) {

    /**
     * Creates an instance of {@link DummyStats}.
     *
     * @param userId           the {@link User} ID
     * @param timestamp        the {@link java.sql.Timestamp} the stats were retrieved, as a {@link String}
     * @param points           the points
     * @param multipliedPoints the multiplied points
     * @param units            the units
     * @return the created {@link DummyStats}
     */
    public static DummyStats create(final int userId, final String timestamp, final long points, final long multipliedPoints, final int units) {
        return new DummyStats(userId, timestamp, points, multipliedPoints, units);
    }

    @Override
    public String toString() {
        return String.format("(%s, '%s', %s, %s, %s)", userId, timestamp, points, multipliedPoints, units);
    }
}
