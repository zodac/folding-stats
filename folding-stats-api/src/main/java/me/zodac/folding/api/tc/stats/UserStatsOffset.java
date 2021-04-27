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
    private static final int DEFAULT_UNITS = 0;

    private long pointsOffset;
    private int unitsOffset;

    public static UserStatsOffset create(final long pointsOffset, final int unitsOffset) {
        return new UserStatsOffset(pointsOffset, unitsOffset);
    }

    public static UserStatsOffset empty() {
        return new UserStatsOffset(DEFAULT_POINTS, DEFAULT_UNITS);
    }
}
