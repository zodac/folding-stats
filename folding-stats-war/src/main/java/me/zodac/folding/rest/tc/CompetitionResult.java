package me.zodac.folding.rest.tc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class CompetitionResult {

    private List<TeamResult> teams = new ArrayList<>();
    private long totalUnits = 0L;
    private long totalPoints = 0L;
    private long totalPointsWithoutMultipliers = 0L;

    public CompetitionResult() {

    }

    public CompetitionResult(final List<TeamResult> teams) {
        this.teams = teams;
        this.totalUnits = 0L;
        this.totalPoints = 0L;
        this.totalPointsWithoutMultipliers = 0L;

        // TODO: [zodac] Rank the teams
        for (final TeamResult team : teams) {
            this.totalUnits += team.getTeamUnits();
            this.totalPoints += team.getTeamPoints();
            this.totalPointsWithoutMultipliers += team.getTeamPointsWithoutMultipliers();
        }
    }

    public List<TeamResult> getTeams() {
        return teams;
    }

    public void setTeams(final List<TeamResult> teams) {
        this.teams = teams;
    }

    public long getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(final long totalUnits) {
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
