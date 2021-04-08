package me.zodac.folding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TcStats {

    private List<TcTeam> teams = new ArrayList<>();
    private long totalWus = 0L;
    private long totalPoints = 0L;
    private long totalPointsWithoutMultipliers = 0L;

    public TcStats() {

    }

    public TcStats(final List<TcTeam> teams) {
        this.teams = teams;
        this.totalWus = 0L;
        this.totalPoints = 0L;
        this.totalPointsWithoutMultipliers = 0L;

        // TODO: [zodac] Rank the teams
        for (final TcTeam team : teams) {
            this.totalWus += team.getTeamWus();
            this.totalPoints += team.getTeamPoints();
            this.totalPointsWithoutMultipliers += team.getTeamPointsWithoutMultipliers();
        }
    }

    public List<TcTeam> getTeams() {
        return teams;
    }

    public void setTeams(final List<TcTeam> teams) {
        this.teams = teams;
    }

    public long getTotalWus() {
        return totalWus;
    }

    public void setTotalWus(final long totalWus) {
        this.totalWus = totalWus;
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
        final TcStats tcStats = (TcStats) o;
        return totalPoints == tcStats.totalPoints && totalPointsWithoutMultipliers == tcStats.totalPointsWithoutMultipliers && Objects.equals(teams, tcStats.teams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teams, totalPoints, totalPointsWithoutMultipliers);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "TcStats{" +
                "teams=" + teams +
                ", totalWus=" + NumberFormat.getInstance(Locale.UK).format(totalWus) +
                ", totalPoints=" + NumberFormat.getInstance(Locale.UK).format(totalPoints) +
                ", totalPointsWithoutMultipliers=" + NumberFormat.getInstance(Locale.UK).format(totalPointsWithoutMultipliers) +
                '}';
    }
}
