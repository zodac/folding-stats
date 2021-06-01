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

    private int id;
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

    // Not ranked to begin with, will be updated by the calling class
    public static UserResult create(final int id, final String displayName, final String foldingName, final Hardware hardware, final Category category, final long points, final long multipliedPoints, final int units, final String profileLink, final String liveStatsLink) {
        return new UserResult(id, displayName, foldingName, hardware, category.displayName(), points, multipliedPoints, units, DEFAULT_USER_RANK, profileLink, liveStatsLink);
    }

    public static UserResult empty(final String userName, final String foldingName, final Category category, final Hardware hardware) {
        return new UserResult(0, userName, foldingName, hardware, category.displayName(), DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS, DEFAULT_USER_RANK, "", "");
    }

    public static UserResult updateWithRankInTeam(final UserResult userResult, final int teamRank) {
        return new UserResult(userResult.id, userResult.displayName, userResult.foldingName, userResult.hardware, userResult.category, userResult.points, userResult.multipliedPoints, userResult.units, teamRank, userResult.profileLink, userResult.liveStatsLink);
    }
}