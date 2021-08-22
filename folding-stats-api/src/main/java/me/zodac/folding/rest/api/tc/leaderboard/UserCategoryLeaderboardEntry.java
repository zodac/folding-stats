package me.zodac.folding.rest.api.tc.leaderboard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.rest.api.tc.UserSummary;

/**
 * POJO for the {@link me.zodac.folding.api.tc.User} {@link me.zodac.folding.api.tc.Category} leaderboard, summarising
 * the stats for a {@link me.zodac.folding.api.tc.User} in a {@link me.zodac.folding.api.tc.Category}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserCategoryLeaderboardEntry {

    private String displayName;
    private String foldingName;
    private String hardware;
    private String teamName;
    private long points;
    private long multipliedPoints;
    private int units;
    private int rank;

    private long diffToLeader;
    private long diffToNext;

    /**
     * Creates the {@link UserCategoryLeaderboardEntry} for a {@link me.zodac.folding.api.tc.User}.
     *
     * @param userSummary  the {@link UserSummary} for the {@link me.zodac.folding.api.tc.User}
     * @param teamName     the name of {@link me.zodac.folding.api.tc.Team} the {@link me.zodac.folding.api.tc.User} is part of
     * @param rank         the rank of the {@link me.zodac.folding.api.tc.User} in their {@link me.zodac.folding.api.tc.Category}
     * @param diffToLeader the number of points between this {@link me.zodac.folding.api.tc.User} and the one in first place
     * @param diffToNext   the number of points between this {@link me.zodac.folding.api.tc.User} and the one a single place above
     * @return the created {@link UserCategoryLeaderboardEntry}
     */
    public static UserCategoryLeaderboardEntry create(final UserSummary userSummary, final String teamName, final int rank, final long diffToLeader,
                                                      final long diffToNext) {
        return new UserCategoryLeaderboardEntry(userSummary.getDisplayName(), userSummary.getFoldingName(),
            userSummary.getHardware().getDisplayName(), teamName, userSummary.getPoints(), userSummary.getMultipliedPoints(),
            userSummary.getUnits(), rank, diffToLeader, diffToNext);
    }

    /**
     * Creates the {@link UserCategoryLeaderboardEntry} for the {@link me.zodac.folding.api.tc.User} in first place. The rank and diff
     * values are constant in this case.
     *
     * @param userSummary the {@link UserSummary} for the {@link me.zodac.folding.api.tc.User} in first
     * @param teamName    the name of {@link me.zodac.folding.api.tc.Team} the {@link me.zodac.folding.api.tc.User} is part of
     * @return the created {@link UserCategoryLeaderboardEntry}
     */
    public static UserCategoryLeaderboardEntry createLeader(final UserSummary userSummary, final String teamName) {
        return create(userSummary, teamName, 1, 0L, 0L);
    }
}
