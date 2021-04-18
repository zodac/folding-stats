package me.zodac.folding.rest.tc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class CompetitionResult {

    private List<TeamResult> teams = new ArrayList<>();
    private int totalUnits = 0;
    private long totalPoints = 0L;
    private long totalPointsWithoutMultipliers = 0L;

    public CompetitionResult() {

    }

    private CompetitionResult(final List<TeamResult> teams, final int totalUnits, final long totalPoints, final long totalPointsWithoutMultipliers) {
        this.teams = teams;
        this.totalUnits = totalUnits;
        this.totalPoints = totalPoints;
        this.totalPointsWithoutMultipliers = totalPointsWithoutMultipliers;
    }

    public static CompetitionResult create(final List<TeamResult> teams) {
        int totalUnits = 0;
        long totalPoints = 0L;
        long totalPointsWithoutMultipliers = 0L;

        for (final TeamResult team : teams) {
            totalUnits += team.getTeamUnits();
            totalPoints += team.getTeamPoints();
            totalPointsWithoutMultipliers += team.getTeamPointsWithoutMultipliers();
        }

        final List<TeamResult> rankedTeams = teams
                .stream()
                .sorted(Comparator.comparingLong(TeamResult::getTeamPoints).reversed())
                .collect(new IntegerRankingCollector<>(
                        Comparator.comparingLong(TeamResult::getTeamPoints),
                        TeamResult::getRank,
                        TeamResult::updateWithRank)
                );

        return new CompetitionResult(rankedTeams, totalUnits, totalPoints, totalPointsWithoutMultipliers);
    }


    public List<TeamResult> getTeams() {
        return teams;
    }

    public void setTeams(final List<TeamResult> teams) {
        this.teams = teams;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(final int totalUnits) {
        this.totalUnits = totalUnits;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(final long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public long getTotalPointsWithoutMultipliers() {
        return totalPointsWithoutMultipliers;
    }

    public void setTotalPointsWithoutMultipliers(final long totalPointsWithoutMultipliers) {
        this.totalPointsWithoutMultipliers = totalPointsWithoutMultipliers;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CompetitionResult competitionResult = (CompetitionResult) o;
        return totalPoints == competitionResult.totalPoints && totalPointsWithoutMultipliers == competitionResult.totalPointsWithoutMultipliers && Objects.equals(teams, competitionResult.teams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teams, totalUnits, totalPoints, totalPointsWithoutMultipliers);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "TcStats{" +
                "teams=" + teams +
                ", totalUnits=" + formatWithCommas(totalUnits) +
                ", totalPoints=" + formatWithCommas(totalPoints) +
                ", totalPointsWithoutMultipliers=" + formatWithCommas(totalPointsWithoutMultipliers) +
                '}';
    }
}
