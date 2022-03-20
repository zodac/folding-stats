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

import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.util.DateTimeUtils;

/**
 * POJO that extends {@link UserStats} adding multiplied points for a {@code Team Competition}
 * {@link me.zodac.folding.api.tc.User}, based on a hardware multiplier.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(doNotUseGetters = true, callSuper = true)
public class UserTcStats extends UserStats {

    /**
     * The default number of multiplied points, <b>0</b>.
     */
    public static final long DEFAULT_MULTIPLIED_POINTS = 0L;

    private static final DateTimeUtils DATE_TIME_UTILS = DateTimeUtils.create();

    private final long multipliedPoints;

    /**
     * Constructor for {@link UserTcStats}.
     *
     * @param userId           the {@link me.zodac.folding.api.tc.User} ID
     * @param timestamp        the {@link Timestamp} the stats were retrieved
     * @param points           the points
     * @param multipliedPoints the multiplied points
     * @param units            the units
     */
    protected UserTcStats(final int userId, final Timestamp timestamp, final long points, final long multipliedPoints, final int units) {
        super(userId, timestamp, points, units);
        this.multipliedPoints = multipliedPoints;
    }

    /**
     * Creates an instance of {@link UserTcStats}.
     *
     * @param userId           the ID of the {@link me.zodac.folding.api.tc.User}
     * @param timestamp        the {@link Timestamp} the {@link UserTcStats} were retrieved
     * @param points           the points
     * @param multipliedPoints the multiplied points
     * @param units            the units
     * @return the created {@link UserTcStats}
     */
    public static UserTcStats create(final int userId, final Timestamp timestamp, final long points, final long multipliedPoints, final int units) {
        return new UserTcStats(userId, timestamp, points, multipliedPoints, units);
    }

    /**
     * Creates an instance of {@link UserTcStats}.
     *
     * @param userId           the ID of the {@link me.zodac.folding.api.tc.User}
     * @param points           the points
     * @param multipliedPoints the multiplied points
     * @param units            the units
     * @return the created {@link UserTcStats}
     */
    public static UserTcStats createNow(final int userId, final long points, final long multipliedPoints, final int units) {
        return create(userId, DATE_TIME_UTILS.currentUtcTimestamp(), points, multipliedPoints, units);
    }

    /**
     * Creates an empty instance of {@link UserTcStats}, with no values. Can be used where no stats are necessary, but
     * an {@link java.util.Optional} is not clean enough.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User}
     * @return the empty {@link UserTcStats}
     */
    public static UserTcStats empty(final int userId) {
        return create(userId, DATE_TIME_UTILS.currentUtcTimestamp(), Stats.DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, Stats.DEFAULT_UNITS);
    }

    /**
     * Creates a new instance of {@link UserTcStats} with {@link OffsetTcStats}. Can be used when retrieving a current
     * {@link me.zodac.folding.api.tc.User}'s {@link UserTcStats} and wanted to make an offset.
     *
     * <p>
     * In case the {@link OffsetTcStats} values are greater than the {@link UserTcStats}, the values will not be negative
     * and will be set to <b>0L</b>
     *
     * @param offsetTcStats the {@link OffsetTcStats} to apply
     * @return the new {@link UserTcStats} instances with {@link OffsetTcStats} applied
     */
    public UserTcStats updateWithOffsets(final OffsetTcStats offsetTcStats) {
        final long offsetPoints = Math.max(getPoints() + offsetTcStats.getPointsOffset(), Stats.DEFAULT_POINTS);
        final long offsetMultipliedPoints = Math.max(multipliedPoints + offsetTcStats.getMultipliedPointsOffset(), DEFAULT_MULTIPLIED_POINTS);
        final int offsetUnits = Math.max(getUnits() + offsetTcStats.getUnitsOffset(), Stats.DEFAULT_UNITS);

        return create(getUserId(), getTimestamp(), offsetPoints, offsetMultipliedPoints, offsetUnits);
    }

    @Override
    public boolean isEmpty() {
        return multipliedPoints == DEFAULT_MULTIPLIED_POINTS && super.isEmpty();
    }
}