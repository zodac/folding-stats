package me.zodac.folding.rest.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserResult {

    private static final int DEFAULT_USER_RANK = 0;
    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private String displayName;
    private String foldingName;
    private Hardware hardware;
    private String category;

    private long points;
    private long multipliedPoints;
    private int units;
    private int rankInTeam;
    private String profileLink;
    private String liveStatsLink;
    private boolean isRetired;

    // Not ranked to begin with, will be updated by the calling class
    public static UserResult createWithNoRank(final String displayName, final String foldingName, final Hardware hardware, final String category, final long points, final long pointsWithoutMultiplier, final int units, final String profileLink, final String liveStatsLink, final boolean isRetired) {
        return new UserResult(displayName, foldingName, hardware, category, points, pointsWithoutMultiplier, units, DEFAULT_USER_RANK, profileLink, liveStatsLink, isRetired);
    }

    public static UserResult empty(final String userName) {
        return new UserResult(userName, "", null, "", DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS, DEFAULT_USER_RANK, "", "", false);
    }

    public static UserResult updateWithRankInTeam(final UserResult userResult, final int teamRank) {
        return new UserResult(userResult.displayName, userResult.foldingName, userResult.hardware, userResult.category, userResult.points, userResult.multipliedPoints, userResult.units, teamRank, userResult.profileLink, userResult.liveStatsLink, userResult.isRetired);
    }

    public static UserResult createForRetiredUser(final User retiredUser, final Hardware retiredUserHardware, final RetiredUserTcStats retiredUserTcStats) {
        return new UserResult(retiredUserTcStats.getDisplayUserName(), retiredUserTcStats.getDisplayUserName(), retiredUserHardware, Category.get(retiredUser.getCategory()).displayName(), retiredUserTcStats.getPoints(), retiredUserTcStats.getMultipliedPoints(), retiredUserTcStats.getUnits(), DEFAULT_USER_RANK, retiredUser.getProfileLink(), retiredUser.getLiveStatsLink(), true);
    }
}