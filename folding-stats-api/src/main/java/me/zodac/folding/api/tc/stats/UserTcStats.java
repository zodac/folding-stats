package me.zodac.folding.api.tc.stats;

import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.utils.DateTimeUtils;

/**
 * POJO that extends {@link UserStats} adding multiplied points for a <code>Team Competition</code>
 * {@link me.zodac.folding.api.tc.User}, based on a hardware multiplier.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(doNotUseGetters = true, callSuper = true)
public class UserTcStats extends UserStats {

    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;

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
     * Creates an instance of {@link UserTcStats}. Assumes the {@link UserTcStats} were retrieved at {@link DateTimeUtils#currentUtcTimestamp()}.
     *
     * @param userId           the ID of the {@link me.zodac.folding.api.tc.User}
     * @param points           the points
     * @param multipliedPoints the multiplied points
     * @param units            the units
     * @return the created {@link UserTcStats}
     */
    public static UserTcStats createNow(final int userId, final long points, final long multipliedPoints, final int units) {
        return new UserTcStats(userId, DateTimeUtils.currentUtcTimestamp(), points, multipliedPoints, units);
    }

    /**
     * Creates an empty instance of {@link UserTcStats}, with no values. Can be used where no stats are necessary, but
     * an {@link java.util.Optional} is not clean enough.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User}
     * @return the empty {@link UserTcStats}
     */
    public static UserTcStats empty(final int userId) {
        return new UserTcStats(userId, DateTimeUtils.currentUtcTimestamp(), DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS);
    }

    /**
     * Creates a new instance of {@link UserTcStats} with {@link OffsetStats}. Can be used when retrieving a current
     * {@link me.zodac.folding.api.tc.User}'s {@link UserTcStats} and wanted to make an offset.
     *
     * <p>
     * In case the {@link OffsetStats} values are greater than the {@link UserTcStats}, the values will not be negative
     * and will be set to <b>0L</b>
     *
     * @param offsetStats the {@link OffsetStats} to apply
     * @return the new {@link UserTcStats} instances with {@link OffsetStats} applied
     */
    public UserTcStats updateWithOffsets(final OffsetStats offsetStats) {
        final long offsetPoints = Math.max(getPoints() + offsetStats.getPointsOffset(), DEFAULT_POINTS);
        final long offsetMultipliedPoints = Math.max(multipliedPoints + offsetStats.getMultipliedPointsOffset(), DEFAULT_MULTIPLIED_POINTS);
        final int offsetUnits = Math.max(getUnits() + offsetStats.getUnitsOffset(), DEFAULT_UNITS);

        return new UserTcStats(getUserId(), getTimestamp(), offsetPoints, offsetMultipliedPoints, offsetUnits);
    }

    @Override
    public boolean isEmpty() {
        return multipliedPoints == DEFAULT_MULTIPLIED_POINTS && super.isEmpty();
    }
}