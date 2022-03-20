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

package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO defining a stats offset for a {@link me.zodac.folding.api.tc.User} within the {@code Team Competition}. In the case of a manual change
 * of points/units being required for a {@link me.zodac.folding.api.tc.User}, this object will define that offset.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class OffsetTcStats {

    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private long pointsOffset;
    private long multipliedPointsOffset;
    private int unitsOffset;

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
     * to be set with either the {@code pointsOffset} or the {@code multipliedPointsOffset} as <b>0L</b>. In some cases,
     * we will still want to calculate the other based on a multiplier. We will do that calculation here, depending on
     * which field is not set.
     *
     * <p>
     * If both {@code pointsOffset} and {@code multipliedPointsOffset} are set (meaning neither are <b>0L</b>, or the
     * provided {@link OffsetTcStats} instance is equal to {@link OffsetTcStats#empty()}, then no changes are made.
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
            final long pointsOffset = Math.round(offsetTcStats.multipliedPointsOffset / multiplier);
            return create(pointsOffset, offsetTcStats.multipliedPointsOffset, offsetTcStats.unitsOffset);
        } else {
            final long multipliedPointsOffset = Math.round(offsetTcStats.pointsOffset * multiplier);
            return create(offsetTcStats.pointsOffset, multipliedPointsOffset, offsetTcStats.unitsOffset);
        }
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
