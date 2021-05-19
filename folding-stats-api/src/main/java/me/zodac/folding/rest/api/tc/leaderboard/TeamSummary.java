package me.zodac.folding.rest.api.tc.leaderboard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.rest.api.tc.TeamResult;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class TeamSummary {

    private String teamName;
    private long teamPoints;
    private long teamMultipliedPoints;
    private int teamUnits;

    private long diffToLeader;
    private long diffToNext;

    public static TeamSummary create(final TeamResult teamResult, final long diffToLeader, final long diffToNext) {
        return new TeamSummary(teamResult.getTeamName(), teamResult.getTeamPoints(), teamResult.getTeamMultipliedPoints(), teamResult.getTeamUnits(), diffToLeader, diffToNext);
    }

    public static TeamSummary createLeader(final TeamResult teamResult) {
        return create(teamResult, 0L, 0L);
    }
}
