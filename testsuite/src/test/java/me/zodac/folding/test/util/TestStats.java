/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.test.util;

/**
 * Simple POJO defining the stats for a user to be persisted in a test DB for stats-based tests.
 * Used since the normal stubbed endpoints insert stats with the timestamp based on the time of execution, whereas for
 * historic stats, we want to specify the timestamps.
 */
public final class TestStats {

    private final transient int userId;
    private final transient String timestamp;
    private final transient long points;
    private final transient long multipliedPoints;
    private final transient int units;

    private TestStats(final int userId, final String timestamp, final long points, final long multipliedPoints, final int units) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.points = points;
        this.multipliedPoints = multipliedPoints;
        this.units = units;
    }

    /**
     * Creates an instance of {@link TestStats}.
     *
     * @param userId           the {@link me.zodac.folding.api.tc.User} ID
     * @param timestamp        the {@link java.sql.Timestamp} the stats were retrieved, as a {@link String}
     * @param points           the points
     * @param multipliedPoints the multiplied points
     * @param units            the units
     * @return the created {@link TestStats}
     */
    public static TestStats create(final int userId, final String timestamp, final long points, final long multipliedPoints, final int units) {
        return new TestStats(userId, timestamp, points, multipliedPoints, units);
    }

    @Override
    public String toString() {
        return String.format("(%s, '%s', %s, %s, %s)", userId, timestamp, points, multipliedPoints, units);
    }
}