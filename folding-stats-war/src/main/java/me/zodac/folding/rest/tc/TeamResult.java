package me.zodac.folding.rest.tc;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class TeamResult {

    private String teamName;
    private String captainName;

    private int rank; // Rank in 'division', but we only have one division so no need to be more explicit, yet
    private int teamUnits;
    private long teamPoints;
    private long teamMultipliedPoints;
    private List<UserResult> users;

    public TeamResult() {

    }

    private TeamResult(final String teamName, final String captainName, final List<UserResult> users, final int teamUnits, final long teamPoints, final long teamMultipliedPoints, final int rank) {
        this.teamName = teamName;
        this.captainName = captainName;
        this.users = users;
        this.teamUnits = teamUnits;
        this.teamPoints = teamPoints;
        this.teamMultipliedPoints = teamMultipliedPoints;
        this.rank = rank;
    }

    public static TeamResult create(final String teamName, final String captainName, final List<UserResult> users) {
        int teamUnits = 0;
        long teamPoints = 0L;
        long teamMultipliedPoints = 0L;

        for (final UserResult user : users) {
            teamUnits += user.getUnits();
            teamPoints += user.getPoints();
            teamMultipliedPoints += user.getMultipliedPoints();
        }

        final List<UserResult> rankedUsers = users
                .stream()
                .sorted(Comparator.comparingLong(UserResult::getPoints).reversed())
                .collect(new IntegerRankingCollector<>(
                        Comparator.comparingLong(UserResult::getPoints),
                        UserResult::getRankInTeam,
                        UserResult::updateWithRankInTeam)
                );

        // Not ranked to begin with, will be updated by the calling class
        return new TeamResult(teamName, captainName, rankedUsers, teamUnits, teamPoints, teamMultipliedPoints, 0);
    }

    public static TeamResult updateWithRank(final TeamResult teamResult, final int rank) {
        return new TeamResult(teamResult.teamName, teamResult.captainName, teamResult.users, teamResult.teamUnits, teamResult.teamPoints,
                teamResult.teamMultipliedPoints, rank);
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(final String teamName) {
        this.teamName = teamName;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(final String captainName) {
        this.captainName = captainName;
    }

    public List<UserResult> getUsers() {
        return users;
    }

    public void setUsers(final List<UserResult> users) {
        this.users = users;
    }

    public int getTeamUnits() {
        return teamUnits;
    }

    public void setTeamUnits(final int teamUnits) {
        this.teamUnits = teamUnits;
    }

    public long getTeamPoints() {
        return teamPoints;
    }

    public void setTeamPoints(final long teamPoints) {
        this.teamPoints = teamPoints;
    }

    public long getTeamMultipliedPoints() {
        return teamMultipliedPoints;
    }

    public void setTeamMultipliedPoints(final long teamMultipliedPoints) {
        this.teamMultipliedPoints = teamMultipliedPoints;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(final int rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TeamResult that = (TeamResult) o;
        return teamUnits == that.teamUnits && teamPoints == that.teamPoints && teamMultipliedPoints == that.teamMultipliedPoints && rank == that.rank && Objects.equals(teamName, that.teamName) && Objects.equals(captainName, that.captainName) && Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamName, captainName, users, teamUnits, teamPoints, teamMultipliedPoints, rank);
    }

    @Override
    public String toString() {
        return "TeamResult::{" +
                "teamName: '" + teamName + "'" +
                ", captainName: '" + captainName + "'" +
                ", users: " + users +
                ", teamUnits: " + formatWithCommas(teamUnits) +
                ", teamPoints: " + formatWithCommas(teamPoints) +
                ", teamMultipliedPoints: " + formatWithCommas(teamMultipliedPoints) +
                ", rank: " + rank +
                '}';
    }
}