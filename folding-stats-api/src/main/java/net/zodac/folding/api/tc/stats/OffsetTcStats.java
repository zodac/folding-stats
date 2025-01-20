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

package net.zodac.folding.api.tc.stats;

import net.zodac.folding.api.tc.User;
import net.zodac.folding.rest.api.tc.request.OffsetTcStatsRequest;

/**
 * POJO defining a stats offset for a {@link User} within the {@code Team Competition}. For a manual change of points/units being required for a
 * {@link User}, this object will define that offset.
 *
 * @param pointsOffset           the points offset
 * @param multipliedPointsOffset the multiplied points offset
 * @param unitsOffset            the units offset
 */
public record OffsetTcStats(long pointsOffset, long multipliedPointsOffset, int unitsOffset) {

    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    /**
     * Creates an instance of {@link OffsetTcStats}.
     *
     * @param pointsOffset           the points offset
     * @param multipliedPointsOffset the multiplied points offset
     * @param unitsOffset            the units offset
     * @return the created {@link OffsetTcStats}
     */
    public static OffsetTcStats create(final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset) {
        return new OffsetTcStats(pointsOffset, multipliedPointsOffset, unitsOffset);
    }

    /**
     * Creates an instance of {@link OffsetTcStats} from a {@link OffsetTcStatsRequest}.
     *
     * @param offsetTcStatsRequest the {@link OffsetTcStatsRequest} REST payload
     * @return the created {@link OffsetTcStats}
     */
    public static OffsetTcStats create(final OffsetTcStatsRequest offsetTcStatsRequest) {
        return create(
            offsetTcStatsRequest.pointsOffset(),
            offsetTcStatsRequest.multipliedPointsOffset(),
            offsetTcStatsRequest.unitsOffset()
        );
    }

    /**
     * Creates an empty instance of {@link OffsetTcStats}, with no offsets. Can be used where no offset is necessary, but
     * an {@link java.util.Optional} is not clean enough.
     *
     * @return the empty {@link OffsetTcStats}
     */
    public static OffsetTcStats empty() {
        return create(DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS);
    }

    /**
     * Updates an instance of {@link OffsetTcStats} based on a multiplier. It is possible for an {@link OffsetTcStats} instance
     * to be set with either the {@code pointsOffset} or the {@code multipliedPointsOffset} as <b>0</b>. In some cases,
     * we will still want to calculate the other based on a multiplier. We will do that calculation here, depending on
     * which field is not set.
     *
     * <p>
     * If both {@code pointsOffset} and {@code multipliedPointsOffset} are set (meaning neither are <b>0</b>), nor is the
     * provided {@link OffsetTcStats} instance equal to {@link OffsetTcStats#empty()}, then no changes are made.
     *
     * @param offsetTcStats the {@link OffsetTcStats} to update
     * @param multiplier    the value to multiply the {@code pointsOffset}, or to divide the {@code multipliedPointsOffset}
     * @return the updated {@link OffsetTcStats}
     */
    public static OffsetTcStats updateWithHardwareMultiplier(final OffsetTcStats offsetTcStats, final double multiplier) {
        if (offsetTcStats.isEmpty() || !offsetTcStats.isMissingPointsOrMultipliedPoints()) {
            return offsetTcStats;
        }

        if (offsetTcStats.pointsOffset == DEFAULT_POINTS) {
            // Offset only included multiplied points
            final long pointsOffset = Math.round(offsetTcStats.multipliedPointsOffset / multiplier);
            return create(pointsOffset, offsetTcStats.multipliedPointsOffset, offsetTcStats.unitsOffset);
        }

        // Offset only included non-multiplied points
        final long multipliedPointsOffset = Math.round(offsetTcStats.pointsOffset * multiplier);
        return create(offsetTcStats.pointsOffset, multipliedPointsOffset, offsetTcStats.unitsOffset);
    }

    /**
     * Checks if the {@link OffsetTcStats} instance has no offset values for {@code pointsOffset}, {@code multipliedPointsOffset} and
     * {@code unitsOffset}.
     *
     * @return {@code true} if the {@link OffsetTcStats} instance is {@link OffsetTcStats#empty()}
     */
    public boolean isEmpty() {
        return pointsOffset == DEFAULT_POINTS && multipliedPointsOffset == DEFAULT_MULTIPLIED_POINTS && unitsOffset == DEFAULT_UNITS;
    }

    private boolean isMissingPointsOrMultipliedPoints() {
        return (pointsOffset == DEFAULT_POINTS) != (multipliedPointsOffset == DEFAULT_MULTIPLIED_POINTS);
    }
}
