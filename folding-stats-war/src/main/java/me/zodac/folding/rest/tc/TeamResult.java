package me.zodac.folding.rest.tc;

import java.util.List;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class TeamResult {

    private String teamName;
    private String captainName;

    // TODO: [zodac] Rank the users
    private List<UserResult> users;
    private long teamUnits;
    private long teamPoints;
    private long teamPointsWithoutMultipliers;

    public TeamResult() {

    }

    public TeamResult(final String teamName, final String captainName, final List<UserResult> users) {
        this.teamName = teamName;
        this.captainName = captainName;
        this.users = users;

        this.teamUnits = 0L;
        this.teamPoints = 0L;
        this.teamPointsWithoutMultipliers = 0L;

        for (final UserResult user : users) {
            teamUnits += user.getUnits();
            teamPoints += user.getPoints();
            teamPointsWithoutMultipliers += user.getPointsWithoutMultiplier();
        }
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

    public long getTeamUnits() {
        return teamUnits;
    }

    public void setTeamUnits(final long teamUnits) {
        this.teamUnits = teamUnits;
    }

    public long getTeamPoints() {
        return teamPoints;
    }

    public void setTeamPoints(final long teamPoints) {
        this.teamPoints = teamPoints;
    }

    public long getTeamPointsWithoutMultipliers() {
        return teamPointsWithoutMultipliers;
    }

    public void setTeamPointsWithoutMultipliers(final long teamPointsWithoutMultipliers) {
        this.teamPointsWithoutMultipliers = teamPointsWithoutMultipliers;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TeamResult teamResult = (TeamResult) o;
        return teamName.equals(teamResult.teamName) && captainName.equals(teamResult.captainName) && teamUnits == teamResult.teamUnits && teamPoints == teamResult.teamPoints && teamPointsWithoutMultipliers == teamResult.teamPointsWithoutMultipliers && Objects.equals(users, teamResult.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamName, captainName, teamUnits, teamPoints, teamPointsWithoutMultipliers, users);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "TcTeam{" +
                "teamName=" + teamName +
                ", captainName=" + captainName +
                ", users=" + users +
                ", teamUnits=" + formatWithCommas(teamUnits) +
                ", teamPoints=" + formatWithCommas(teamPoints) +
                ", teamPointsWithoutMultipliers=" + formatWithCommas(teamPointsWithoutMultipliers) +
                '}';
    }
}