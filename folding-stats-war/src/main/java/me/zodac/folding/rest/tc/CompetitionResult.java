package me.zodac.folding.rest.tc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class CompetitionResult {

    private int totalUnits = 0;
    private long totalPoints = 0L;
    private long totalMultipliedPoints = 0L;
    private List<TeamResult> teams = new ArrayList<>();

    public CompetitionResult() {

    }

    private CompetitionResult(final int totalUnits, final long totalPoints, final long totalMultipliedPoints, final List<TeamResult> teams) {
        this.totalUnits = totalUnits;
        this.totalPoints = totalPoints;
        this.totalMultipliedPoints = totalMultipliedPoints;
        this.teams = teams;
    }

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

        return new CompetitionResult(totalUnits, totalPoints, totalMultipliedPoints, rankedTeams);
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

    public long getTotalMultipliedPoints() {
        return totalMultipliedPoints;
    }

    public void setTotalMultipliedPoints(final long totalMultipliedPoints) {
        this.totalMultipliedPoints = totalMultipliedPoints;
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
        return totalUnits == that.totalUnits && totalPoints == that.totalPoints && totalMultipliedPoints == that.totalMultipliedPoints && Objects.equals(teams, that.teams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teams, totalUnits, totalPoints, totalMultipliedPoints);
    }

    @Override
    public String toString() {
        return "CompetitionResult::{" +
                "totalUnits: " + formatWithCommas(totalUnits) +
                ", totalPoints: " + formatWithCommas(totalPoints) +
                ", totalMultipliedPoints: " + formatWithCommas(totalMultipliedPoints) +
                ", teams: " + teams +
                '}';
    }
}
