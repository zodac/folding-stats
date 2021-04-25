package me.zodac.folding.rest.tc;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class TeamResult {

    private static final int DEFAULT_TEAM_RANK = 0;

    private String teamName;
    private String captainName;

    private int rank; // Rank in 'division', but we only have one division so no need to be more explicit, yet
    private int teamUnits;
    private long teamPoints;
    private long teamMultipliedPoints;
    private List<UserResult> activeUsers;
    private List<UserResult> retiredUsers;

    public TeamResult() {

    }

    private TeamResult(final String teamName, final String captainName, final List<UserResult> activeUsers, final List<UserResult> retiredUsers, final int teamUnits, final long teamPoints, final long teamMultipliedPoints, final int rank) {
        this.teamName = teamName;
        this.captainName = captainName;
        this.activeUsers = activeUsers;
        this.retiredUsers = retiredUsers;
        this.teamUnits = teamUnits;
        this.teamPoints = teamPoints;
        this.teamMultipliedPoints = teamMultipliedPoints;
        this.rank = rank;
    }

    public static TeamResult create(final String teamName, final String captainName, final List<UserResult> activeUsers, final List<UserResult> retiredUsers) {
        int teamUnits = 0;
        long teamPoints = 0L;
        long teamMultipliedPoints = 0L;

        for (final UserResult activeUser : activeUsers) {
            teamUnits += activeUser.getUnits();
            teamPoints += activeUser.getPoints();
            teamMultipliedPoints += activeUser.getMultipliedPoints();
        }

        for (final UserResult retired : retiredUsers) {
            teamUnits += retired.getUnits();
            teamPoints += retired.getPoints();
            teamMultipliedPoints += retired.getMultipliedPoints();
        }

        final List<UserResult> rankedActiveUsers = activeUsers
                .stream()
                .sorted(Comparator.comparingLong(UserResult::getMultipliedPoints).reversed())
                .collect(new IntegerRankingCollector<>(
                        Comparator.comparingLong(UserResult::getMultipliedPoints),
                        UserResult::getRankInTeam,
                        UserResult::updateWithRankInTeam)
                );

        final List<UserResult> rankedRetiredUsers = retiredUsers
                .stream()
                .sorted(Comparator.comparingLong(UserResult::getMultipliedPoints).reversed())
                .collect(new IntegerRankingCollector<>(
                        Comparator.comparingLong(UserResult::getMultipliedPoints),
                        UserResult::getRankInTeam,
                        // TODO: [zodac] This isn't working as planned, look into it
                        (userResult, teamRank) -> UserResult.updateWithRankInTeam(userResult, (teamRank + activeUsers.size())))  // Offset the retired user ranks so they are after the active users
                );

        // Not ranked to begin with, will be updated by the calling class
        return new TeamResult(teamName, captainName, rankedActiveUsers, rankedRetiredUsers, teamUnits, teamPoints, teamMultipliedPoints, DEFAULT_TEAM_RANK);
    }


    public static TeamResult updateWithRank(final TeamResult teamResult, final int rank) {
        return new TeamResult(teamResult.teamName, teamResult.captainName, teamResult.activeUsers, teamResult.retiredUsers, teamResult.teamUnits, teamResult.teamPoints, teamResult.teamMultipliedPoints, rank);
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

    public List<UserResult> getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(final List<UserResult> activeUsers) {
        this.activeUsers = activeUsers;
    }

    public List<UserResult> getRetiredUsers() {
        return retiredUsers;
    }

    public void setRetiredUsers(final List<UserResult> retiredUsers) {
        this.retiredUsers = retiredUsers;
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
        return teamUnits == that.teamUnits && teamPoints == that.teamPoints && teamMultipliedPoints == that.teamMultipliedPoints && rank == that.rank && Objects.equals(teamName, that.teamName) && Objects.equals(captainName, that.captainName) && Objects.equals(activeUsers, that.activeUsers) && Objects.equals(retiredUsers, that.retiredUsers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamName, captainName, activeUsers, retiredUsers, teamUnits, teamPoints, teamMultipliedPoints, rank);
    }

    @Override
    public String toString() {
        return "TeamResult::{" +
                "teamName: '" + teamName + "'" +
                ", captainName: '" + captainName + "'" +
                ", activeUsers: " + activeUsers +
                ", retiredUsers: " + retiredUsers +
                ", teamUnits: " + formatWithCommas(teamUnits) +
                ", teamPoints: " + formatWithCommas(teamPoints) +
                ", teamMultipliedPoints: " + formatWithCommas(teamMultipliedPoints) +
                ", rank: " + rank +
                '}';
    }
}