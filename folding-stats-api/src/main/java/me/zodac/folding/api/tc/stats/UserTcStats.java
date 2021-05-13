package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.utils.DateTimeUtils;

import java.sql.Timestamp;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserTcStats {

    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private final int userId;
    private final Timestamp timestamp;
    private final long points;
    private final long multipliedPoints;
    private final int units;

    public static UserTcStats create(final int userId, final Timestamp timestamp, final long points, final long multipliedPoints, final int units) {
        return new UserTcStats(userId, timestamp, points, multipliedPoints, units);
    }

    public static UserTcStats createWithoutTimestamp(final int userId, final long points, final long multipliedPoints, final int units) {
        return new UserTcStats(userId, DateTimeUtils.currentUtcTimestamp(), points, multipliedPoints, units);
    }

    public static UserTcStats empty(final int userId) {
        return new UserTcStats(userId, DateTimeUtils.currentUtcTimestamp(), DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS);
    }

    public static UserTcStats updateWithOffsets(final UserTcStats tcStatsForUser, final OffsetStats offsetStats) {
        final long offsetPoints = Math.max(tcStatsForUser.getPoints() + offsetStats.getPointsOffset(), 0);
        final long offsetMultipliedPoints = Math.max(tcStatsForUser.getMultipliedPoints() + offsetStats.getMultipliedPointsOffset(), 0);
        final int offsetUnits = Math.max(tcStatsForUser.getUnits() + offsetStats.getUnitsOffset(), 0);

        return new UserTcStats(tcStatsForUser.getUserId(), tcStatsForUser.getTimestamp(), offsetPoints, offsetMultipliedPoints, offsetUnits);
    }
}