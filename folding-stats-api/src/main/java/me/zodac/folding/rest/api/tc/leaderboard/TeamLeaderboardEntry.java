package me.zodac.folding.rest.api.tc.leaderboard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.rest.api.tc.TeamSummary;

/**
 * POJO for the {@link me.zodac.folding.api.tc.Team} leaderboard, summarising the stats for a {@link me.zodac.folding.api.tc.Team}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class TeamLeaderboardEntry {

    private String teamName;
    private long teamPoints;
    private long teamMultipliedPoints;
    private int teamUnits;
    private int rank;

    private long diffToLeader;
    private long diffToNext;

    /**
     * Creates the {@link TeamLeaderboardEntry} for a {@link me.zodac.folding.api.tc.Team}.
     *
     * @param teamSummary  the {@link TeamSummary} for the {@link me.zodac.folding.api.tc.Team}
     * @param rank         the rank of the {@link me.zodac.folding.api.tc.Team}
     * @param diffToLeader the number of points between this {@link me.zodac.folding.api.tc.Team} and the one in first place
     * @param diffToNext   the number of points between this {@link me.zodac.folding.api.tc.Team} and the one a single place above
     * @return the created {@link TeamLeaderboardEntry}
     */
    public static TeamLeaderboardEntry create(final TeamSummary teamSummary, final int rank, final long diffToLeader, final long diffToNext) {
        return new TeamLeaderboardEntry(teamSummary.getTeamName(), teamSummary.getTeamPoints(), teamSummary.getTeamMultipliedPoints(),
            teamSummary.getTeamUnits(), rank, diffToLeader, diffToNext);
    }

    /**
     * Creates the {@link TeamLeaderboardEntry} for the {@link me.zodac.folding.api.tc.Team} in first place. The rank and diff
     * values are constant in this case.
     *
     * @param teamSummary the {@link TeamSummary} for the {@link me.zodac.folding.api.tc.Team} in first
     * @return the created {@link TeamLeaderboardEntry}
     */
    public static TeamLeaderboardEntry createLeader(final TeamSummary teamSummary) {
        return create(teamSummary, 1, 0L, 0L);
    }
}
