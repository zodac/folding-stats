package me.zodac.folding.rest.tc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class CompetitionResult {

    private int totalUnits = 0;
    private long totalPoints = 0L;
    private long totalPointsWithoutMultipliers = 0L;
    private List<TeamResult> teams = new ArrayList<>();

    public CompetitionResult() {

    }

    private CompetitionResult(final int totalUnits, final long totalPoints, final long totalPointsWithoutMultipliers, final List<TeamResult> teams) {
        this.totalUnits = totalUnits;
        this.totalPoints = totalPoints;
        this.totalPointsWithoutMultipliers = totalPointsWithoutMultipliers;
        this.teams = teams;
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

        return new CompetitionResult(totalUnits, totalPoints, totalPointsWithoutMultipliers, rankedTeams);
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

    public List<TeamResult> getTeams() {
        return teams;
    }

    public void setTeams(final List<TeamResult> teams) {
        this.teams = teams;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CompetitionResult that = (CompetitionResult) o;
        return totalUnits == that.totalUnits && totalPoints == that.totalPoints && totalPointsWithoutMultipliers == that.totalPointsWithoutMultipliers && Objects.equals(teams, that.teams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teams, totalUnits, totalPoints, totalPointsWithoutMultipliers);
    }

    @Override
    public String toString() {
        return "CompetitionResult::{" +
                "totalUnits: " + formatWithCommas(totalUnits) +
                ", totalPoints: " + formatWithCommas(totalPoints) +
                ", totalPointsWithoutMultipliers: " + formatWithCommas(totalPointsWithoutMultipliers) +
                ", teams: " + teams +
                '}';
    }
}
