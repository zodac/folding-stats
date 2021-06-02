package me.zodac.folding.rest.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class CompetitionResult {

    private long totalPoints;
    private long totalMultipliedPoints;
    private int totalUnits;
    private Collection<TeamResult> teams = new ArrayList<>();

    public static CompetitionResult create(final List<TeamResult> teams) {
        int totalUnits = 0;
        long totalPoints = 0L;
        long totalMultipliedPoints = 0L;

        for (final TeamResult team : teams) {
            totalUnits += team.getTeamUnits();
            totalPoints += team.getTeamPoints();
            totalMultipliedPoints += team.getTeamMultipliedPoints();
        }

        final List<TeamResult> rankedTeams = teams
                .stream()
                .sorted(Comparator.comparingLong(TeamResult::getTeamMultipliedPoints).reversed())
                .collect(new IntegerRankingCollector<>(
                        Comparator.comparingLong(TeamResult::getTeamMultipliedPoints),
                        TeamResult::getRank,
                        TeamResult::updateWithRank)
                );

        return new CompetitionResult(totalPoints, totalMultipliedPoints, totalUnits, rankedTeams);
    }
}
