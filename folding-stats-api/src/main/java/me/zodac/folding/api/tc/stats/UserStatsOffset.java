package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserStatsOffset {

    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private long pointsOffset;
    private long multipliedPointsOffset;
    private int unitsOffset;

    public static UserStatsOffset create(final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset) {
        return new UserStatsOffset(pointsOffset, multipliedPointsOffset, unitsOffset);
    }

    public static UserStatsOffset empty() {
        return new UserStatsOffset(DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS);
    }

    public static UserStatsOffset updateWithHardwareMultiplier(final UserStatsOffset userStatsOffset, final double multiplier) {
        if (userStatsOffset.pointsOffset == DEFAULT_POINTS && userStatsOffset.multipliedPointsOffset != DEFAULT_MULTIPLIED_POINTS) {
            final long pointsOffset = Math.round(userStatsOffset.multipliedPointsOffset / multiplier);
            return new UserStatsOffset(pointsOffset, userStatsOffset.multipliedPointsOffset, userStatsOffset.unitsOffset);
        }

        if (userStatsOffset.multipliedPointsOffset == DEFAULT_MULTIPLIED_POINTS && userStatsOffset.pointsOffset != DEFAULT_POINTS) {
            final long multipliedPointsOffset = Math.round(userStatsOffset.pointsOffset * multiplier);
            return new UserStatsOffset(userStatsOffset.pointsOffset, multipliedPointsOffset, userStatsOffset.unitsOffset);
        }
        throw new IllegalStateException(String.format("Updating UserStatsOffset with hardware multiplier, but both points and multiplied points offsets are already set: %s", userStatsOffset));
    }

    public boolean isMissingPointsOrMultipliedPoints() {
        return (pointsOffset == DEFAULT_POINTS) != (multipliedPointsOffset == DEFAULT_MULTIPLIED_POINTS);
    }
}
