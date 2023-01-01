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

import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.util.DateTimeUtils;

/**
 * POJO that extends {@link UserStats} adding multiplied points for a {@code Team Competition}
 * {@link me.zodac.folding.api.tc.User}, based on a hardware multiplier.
 */
@Accessors(fluent = true)
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
     * Creates a new instance of {@link UserTcStats} where the original {@link UserTcStats} has been added to by the values in the input
     * {@link OffsetTcStats}.
     *
     * <p>
     * In case the {@link OffsetTcStats} values are negative and greater than the {@link UserTcStats}, the final values will not be negative and will
     * be set to <b>0</b>.
     *
     * @param offsetTcStats the {@link OffsetTcStats} to apply
     * @return the new {@link UserTcStats} instances with {@link OffsetTcStats} applied
     */
    public UserTcStats add(final OffsetTcStats offsetTcStats) {
        final long offsetPoints = Math.max(points() + offsetTcStats.pointsOffset(), Stats.DEFAULT_POINTS);
        final long offsetMultipliedPoints = Math.max(multipliedPoints + offsetTcStats.multipliedPointsOffset(), DEFAULT_MULTIPLIED_POINTS);
        final int offsetUnits = Math.max(units() + offsetTcStats.unitsOffset(), Stats.DEFAULT_UNITS);

        return create(userId(), timestamp(), offsetPoints, offsetMultipliedPoints, offsetUnits);
    }

    /**
     * Creates a new instance of {@link UserTcStats} where the original {@link UserTcStats} has been subtracted by the values in the input
     * {@link UserTcStats}.
     *
     * <p>
     * In case the input {@link UserTcStats} values are greater than the {@link UserTcStats}, the final values will not be negative and will be set to
     * <b>0</b>.
     *
     * @param userTcStats the {@link UserTcStats} to subtract
     * @return the new {@link UserTcStats} instances with {@link OffsetTcStats} applied
     */
    public UserTcStats subtract(final UserTcStats userTcStats) {
        final long updatedPoints = Math.max(points() - userTcStats.points(), Stats.DEFAULT_POINTS);
        final long updatedMultipliedPoints = Math.max(multipliedPoints - userTcStats.multipliedPoints, DEFAULT_MULTIPLIED_POINTS);
        final int updatedUnits = Math.max(units() - userTcStats.units(), Stats.DEFAULT_UNITS);

        return create(userId(), timestamp(), updatedPoints, updatedMultipliedPoints, updatedUnits);
    }

    @Override
    public boolean isEmpty() {
        return multipliedPoints == DEFAULT_MULTIPLIED_POINTS && super.isEmpty();
    }
}
