package me.zodac.folding.rest.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class RetiredUserResult {

    private static final int DEFAULT_USER_RANK = 0;
    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private int id;
    private String displayName;

    private long points;
    private long multipliedPoints;
    private int units;
    private int rankInTeam;

    // Not ranked to begin with, will be updated by the calling class
    public static RetiredUserResult create(final int id, final String displayName, final long points, final long multipliedPoints, final int units) {
        return new RetiredUserResult(id, displayName, points, multipliedPoints, units, DEFAULT_USER_RANK);
    }

    public static RetiredUserResult createFromRetiredStats(final RetiredUserTcStats retiredUserTcStats) {
        return new RetiredUserResult(retiredUserTcStats.getUserId(), retiredUserTcStats.getDisplayUserName(), retiredUserTcStats.getPoints(), retiredUserTcStats.getMultipliedPoints(), retiredUserTcStats.getUnits(), DEFAULT_USER_RANK);
    }

    public static RetiredUserResult empty(final String userName) {
        return new RetiredUserResult(0, userName, DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS, DEFAULT_USER_RANK);
    }

    public static RetiredUserResult updateWithRankInTeam(final RetiredUserResult userResult, final int teamRank) {
        return new RetiredUserResult(userResult.id, userResult.displayName, userResult.points, userResult.multipliedPoints, userResult.units, teamRank);
    }
}