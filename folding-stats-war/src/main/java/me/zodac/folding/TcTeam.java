package me.zodac.folding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TcTeam {

    private String teamName;
    private String captainName;

    // TODO: [zodac] Rank the users
    private List<TcUser> users;
    private long teamWus;
    private long teamPoints;
    private long teamPointsWithoutMultipliers;

    public TcTeam() {

    }

    public TcTeam(final String teamName, final String captainName, final List<TcUser> users) {
        this.teamName = teamName;
        this.captainName = captainName;
        this.users = users;

        this.teamWus = 0L;
        this.teamPoints = 0L;
        this.teamPointsWithoutMultipliers = 0L;

        for (final TcUser user : users) {
            teamWus += user.getWus();
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

    public List<TcUser> getUsers() {
        return users;
    }

    public void setUsers(final List<TcUser> users) {
        this.users = users;
    }

    public long getTeamWus() {
        return teamWus;
    }

    public void setTeamWus(final long teamWus) {
        this.teamWus = teamWus;
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
        final TcTeam tcTeam = (TcTeam) o;
        return teamName.equals(tcTeam.teamName) && captainName.equals(tcTeam.captainName) && teamWus == tcTeam.teamWus && teamPoints == tcTeam.teamPoints && teamPointsWithoutMultipliers == tcTeam.teamPointsWithoutMultipliers && Objects.equals(users, tcTeam.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamName, captainName, teamWus, teamPoints, teamPointsWithoutMultipliers, users);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "TcTeam{" +
                "teamName=" + teamName +
                ", captainName=" + captainName +
                ", users=" + users +
                ", teamWus=" + NumberFormat.getInstance(Locale.UK).format(teamWus) +
                ", teamPoints=" + NumberFormat.getInstance(Locale.UK).format(teamPoints) +
                ", teamPointsWithoutMultipliers=" + NumberFormat.getInstance(Locale.UK).format(teamPointsWithoutMultipliers) +
                '}';
    }
}