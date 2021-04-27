package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class Stats {

    private final long points;
    private final int units;

    public static Stats create(final long points, final int units) {
        return new Stats(points, units);
    }
}
