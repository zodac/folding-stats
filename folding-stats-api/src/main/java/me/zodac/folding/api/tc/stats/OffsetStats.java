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
public class OffsetStats {

    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private long pointsOffset;
    private long multipliedPointsOffset;
    private int unitsOffset;

    public static OffsetStats create(final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset) {
        return new OffsetStats(pointsOffset, multipliedPointsOffset, unitsOffset);
    }

    public static OffsetStats empty() {
        return new OffsetStats(DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS);
    }

    public static OffsetStats updateWithHardwareMultiplier(final OffsetStats offsetStats, final double multiplier) {
        if (offsetStats.pointsOffset == DEFAULT_POINTS && offsetStats.multipliedPointsOffset != DEFAULT_MULTIPLIED_POINTS) {
            final long pointsOffset = Math.round(offsetStats.multipliedPointsOffset / multiplier);
            return new OffsetStats(pointsOffset, offsetStats.multipliedPointsOffset, offsetStats.unitsOffset);
        }

        if (offsetStats.multipliedPointsOffset == DEFAULT_MULTIPLIED_POINTS && offsetStats.pointsOffset != DEFAULT_POINTS) {
            final long multipliedPointsOffset = Math.round(offsetStats.pointsOffset * multiplier);
            return new OffsetStats(offsetStats.pointsOffset, multipliedPointsOffset, offsetStats.unitsOffset);
        }
        throw new IllegalStateException(String.format("Updating UserStatsOffset with hardware multiplier, but both points and multiplied points offsets are already set: %s", offsetStats));
    }

    public boolean isMissingPointsOrMultipliedPoints() {
        return (pointsOffset == DEFAULT_POINTS) != (multipliedPointsOffset == DEFAULT_MULTIPLIED_POINTS);
    }

    public boolean isEmpty() {
        return pointsOffset == DEFAULT_POINTS && multipliedPointsOffset == DEFAULT_MULTIPLIED_POINTS && unitsOffset == DEFAULT_UNITS;
    }
}
