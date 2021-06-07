package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Simple POJO containing stats for a Folding@Home user: points and units.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class Stats {

    private final long points;
    private final int units;

    /**
     * Creates an instance of {@link Stats}.
     *
     * @param points the points
     * @param units  the units
     * @return the created {@link Stats}
     */
    public static Stats create(final long points, final int units) {
        return new Stats(points, units);
    }

    /**
     * Creates an empty instance of {@link Stats}, with no values. Can be used where no {@link Stats} are necessary, but
     * an {@link java.util.Optional} is not clean enough.
     *
     * @return the empty {@link Stats}
     */
    public static Stats empty() {
        return new Stats(0L, 0);
    }

    /**
     * Checks if the {@link Stats} instance has both {@code points} set to <b>0L</b> and {@code unitsOffset} set to
     * <b>0</b>.
     *
     * @return <code>true</code> if the {@link Stats} instance is {@link Stats#empty()}
     */
    public boolean isEmpty() {
        return points == 0L && units == 0;
    }
}
