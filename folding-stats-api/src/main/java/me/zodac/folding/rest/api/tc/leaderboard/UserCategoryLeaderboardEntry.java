package me.zodac.folding.rest.api.tc.leaderboard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.UserSummary;

/**
 * POJO for the {@link User} {@link me.zodac.folding.api.tc.Category} leaderboard, summarising
 * the stats for a {@link User} in a {@link me.zodac.folding.api.tc.Category}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserCategoryLeaderboardEntry {

    private static final long DEFAULT_DIFF = 0L;
    private static final int DEFAULT_RANK = 1;

    private User user;
    private long points;
    private long multipliedPoints;
    private int units;
    private int rank;

    private long diffToLeader;
    private long diffToNext;

    /**
     * Creates the {@link UserCategoryLeaderboardEntry} for a {@link User}.
     *
     * @param userSummary  the {@link UserSummary} for the {@link User}
     * @param rank         the rank of the {@link User} in their {@link me.zodac.folding.api.tc.Category}
     * @param diffToLeader the number of points between this {@link User} and the one in first place
     * @param diffToNext   the number of points between this {@link User} and the one a single place above
     * @return the created {@link UserCategoryLeaderboardEntry}
     */
    public static UserCategoryLeaderboardEntry create(final UserSummary userSummary, final int rank, final long diffToLeader, final long diffToNext) {
        return new UserCategoryLeaderboardEntry(
            userSummary.getUser(),
            userSummary.getPoints(),
            userSummary.getMultipliedPoints(),
            userSummary.getUnits(),
            rank,
            diffToLeader,
            diffToNext
        );
    }

    /**
     * Creates the {@link UserCategoryLeaderboardEntry} for the {@link User} in first place. The rank and diff
     * values are constant in this case.
     *
     * @param userSummary the {@link UserSummary} for the {@link User} in first
     * @return the created {@link UserCategoryLeaderboardEntry}
     */
    public static UserCategoryLeaderboardEntry createLeader(final UserSummary userSummary) {
        return create(userSummary, DEFAULT_RANK, DEFAULT_DIFF, DEFAULT_DIFF);
    }
}
