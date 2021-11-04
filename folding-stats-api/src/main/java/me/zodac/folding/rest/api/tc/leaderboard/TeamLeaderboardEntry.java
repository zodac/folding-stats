package me.zodac.folding.rest.api.tc.leaderboard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.rest.api.tc.TeamSummary;

/**
 * POJO for the {@link Team} leaderboard, summarising the stats for a {@link Team}.
 *
 * <p>
 * Available at the <code>folding/stats/leaderboard</code> REST endpoint.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class TeamLeaderboardEntry {

    private static final long DEFAULT_DIFF = 0L;
    private static final int DEFAULT_RANK = 1;

    private Team team;
    private long teamPoints;
    private long teamMultipliedPoints;
    private int teamUnits;
    private int rank;

    private long diffToLeader;
    private long diffToNext;

    /**
     * Creates the {@link TeamLeaderboardEntry} for a {@link Team}.
     *
     * @param teamSummary  the {@link TeamSummary} for the {@link Team}
     * @param rank         the rank of the {@link Team}
     * @param diffToLeader the number of points between this {@link Team} and the one in first place
     * @param diffToNext   the number of points between this {@link Team} and the one a single place above
     * @return the created {@link TeamLeaderboardEntry}
     */
    public static TeamLeaderboardEntry create(final TeamSummary teamSummary, final int rank, final long diffToLeader, final long diffToNext) {
        return new TeamLeaderboardEntry(
            teamSummary.getTeam(),
            teamSummary.getTeamPoints(),
            teamSummary.getTeamMultipliedPoints(),
            teamSummary.getTeamUnits(),
            rank,
            diffToLeader,
            diffToNext
        );
    }

    /**
     * Creates the {@link TeamLeaderboardEntry} for the {@link Team} in first place. The rank and diff
     * values are constant in this case.
     *
     * @param teamSummary the {@link TeamSummary} for the {@link Team} in first
     * @return the created {@link TeamLeaderboardEntry}
     */
    public static TeamLeaderboardEntry createLeader(final TeamSummary teamSummary) {
        return create(teamSummary, DEFAULT_RANK, DEFAULT_DIFF, DEFAULT_DIFF);
    }
}
