package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO defining a stats offset for a {@link me.zodac.folding.api.tc.User}. In the case of a manual change being required
 * for a {@link me.zodac.folding.api.tc.User}, this object will define that offset.
 */
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

    /**
     * Creates an instance of {@link OffsetStats}.
     *
     * @param pointsOffset           the points offset
     * @param multipliedPointsOffset the multiplied points offset
     * @param unitsOffset            the units offset
     * @return the created {@link OffsetStats}
     */
    public static OffsetStats create(final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset) {
        return new OffsetStats(pointsOffset, multipliedPointsOffset, unitsOffset);
    }

    /**
     * Creates an empty instance of {@link OffsetStats}, with no offsets. Can be used where no offset is necessary, but
     * an {@link java.util.Optional} is not clean enough.
     *
     * @return the empty {@link OffsetStats}
     */
    public static OffsetStats empty() {
        return new OffsetStats(DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS);
    }

    /**
     * Updates an instance of {@link OffsetStats} based on a multiplier. It is possible for an {@link OffsetStats} instance
     * to be set with either the {@code pointsOffset} or the {@code multipliedPointsOffset} as <b>0L</b>. In some cases,
     * we will still want to calculate the other based on a multiplier. We will do that calculation here, depending on
     * which field is not set.
     * <p>
     * If both {@code pointsOffset} and {@code multipliedPointsOffset} are set (meaning neither are <b>0L</b>, or the
     * provided {@link OffsetStats} instance is equal to {@link OffsetStats#empty()}, then no changes are made.
     *
     * @param offsetStats the {@link OffsetStats} to update
     * @param multiplier  the value to multiply the {@code pointsOffset}, or to divide the {@code multipliedPointsOffset}
     * @return the updated {@link OffsetStats}
     */
    public static OffsetStats updateWithHardwareMultiplier(final OffsetStats offsetStats, final double multiplier) {
        if (offsetStats.isEmpty() || !offsetStats.isMissingPointsOrMultipliedPoints()) {
            return offsetStats;
        }

        if (offsetStats.pointsOffset == DEFAULT_POINTS) {
            final long pointsOffset = Math.round(offsetStats.multipliedPointsOffset / multiplier);
            return new OffsetStats(pointsOffset, offsetStats.multipliedPointsOffset, offsetStats.unitsOffset);
        } else {
            final long multipliedPointsOffset = Math.round(offsetStats.pointsOffset * multiplier);
            return new OffsetStats(offsetStats.pointsOffset, multipliedPointsOffset, offsetStats.unitsOffset);
        }
    }

    /**
     * Checks if the {@link OffsetStats} instance has no offset values for {@code pointsOffset}, {@code multipliedPointsOffset} and {@code unitsOffset}.
     *
     * @return <code>true</code> if the {@link OffsetStats} instance is {@link OffsetStats#empty()}
     */
    public boolean isEmpty() {
        return pointsOffset == DEFAULT_POINTS && multipliedPointsOffset == DEFAULT_MULTIPLIED_POINTS && unitsOffset == DEFAULT_UNITS;
    }

    private boolean isMissingPointsOrMultipliedPoints() {
        return (pointsOffset == DEFAULT_POINTS) != (multipliedPointsOffset == DEFAULT_MULTIPLIED_POINTS);
    }
}
