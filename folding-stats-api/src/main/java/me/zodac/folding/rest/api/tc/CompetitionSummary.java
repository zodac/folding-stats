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

/**
 * Summary of the stats of all {@link me.zodac.folding.api.tc.Team}s and their {@link me.zodac.folding.api.tc.User}s in
 * the <code>Team Competition</code>.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class CompetitionSummary {

    private long totalPoints;
    private long totalMultipliedPoints;
    private int totalUnits;
    private Collection<TeamSummary> teams = new ArrayList<>();

    /**
     * Creates a {@link CompetitionSummary} from a {@link Collection} of {@link TeamSummary}s.
     * <p>
     * The {@link TeamSummary}s are not ranked, so we will rank them using {@link IntegerRankingCollector}, by comparing
     * the multiplied points of each {@link TeamSummary}.
     * <p>
     * The points, multiplied points and units from each {@link TeamSummary} are added up to give the total competition
     * points, multiplied points and units.
     *
     * @param teams the {@link TeamSummary}s taking part in the <code>Team Competition</code>
     * @return the created {@link CompetitionSummary}
     */
    public static CompetitionSummary create(final Collection<TeamSummary> teams) {
        int totalUnits = 0;
        long totalPoints = 0L;
        long totalMultipliedPoints = 0L;

        for (final TeamSummary team : teams) {
            totalUnits += team.getTeamUnits();
            totalPoints += team.getTeamPoints();
            totalMultipliedPoints += team.getTeamMultipliedPoints();
        }

        final List<TeamSummary> rankedTeams = teams
                .stream()
                .sorted(Comparator.comparingLong(TeamSummary::getTeamMultipliedPoints).reversed())
                .collect(new IntegerRankingCollector<>(
                        Comparator.comparingLong(TeamSummary::getTeamMultipliedPoints),
                        TeamSummary::getRank,
                        TeamSummary::updateWithRank)
                );

        return new CompetitionSummary(totalPoints, totalMultipliedPoints, totalUnits, rankedTeams);
    }
}
