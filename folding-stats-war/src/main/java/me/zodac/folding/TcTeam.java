package me.zodac.folding;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class TcTeam {

    private String teamName;
    private String captainName;

    // TODO: [zodac] Rank the users
    private TcUser nvidiaGpuUser;
    private TcUser amdGpuUser;
    private TcUser wildcardUser;
    private long teamWus;
    private long teamPoints;
    private long teamPointsWithoutMultipliers;

    public TcTeam() {

    }

    public TcTeam(final String teamName, final String captainName, final TcUser nvidiaGpuUser, final TcUser amdGpuUser, final TcUser wildcardUser) {
        this.teamName = teamName;
        this.captainName = captainName;
        this.nvidiaGpuUser = nvidiaGpuUser;
        this.amdGpuUser = amdGpuUser;
        this.wildcardUser = wildcardUser;

        this.teamWus = 0L;
        this.teamPoints = 0L;
        this.teamPointsWithoutMultipliers = 0L;

        if (nvidiaGpuUser != null) {
            teamWus += nvidiaGpuUser.getWus();
            teamPoints += nvidiaGpuUser.getPoints();
            teamPointsWithoutMultipliers += nvidiaGpuUser.getPointsWithoutMultiplier();
        }

        if (amdGpuUser != null) {
            teamWus += amdGpuUser.getWus();
            teamPoints += amdGpuUser.getPoints();
            teamPointsWithoutMultipliers += amdGpuUser.getPointsWithoutMultiplier();
        }

        if (wildcardUser != null) {
            teamWus += wildcardUser.getWus();
            teamPoints += wildcardUser.getPoints();
            teamPointsWithoutMultipliers += wildcardUser.getPointsWithoutMultiplier();
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

    public TcUser getNvidiaGpuUser() {
        return nvidiaGpuUser;
    }

    public void setNvidiaGpuUser(final TcUser nvidiaGpuUser) {
        this.nvidiaGpuUser = nvidiaGpuUser;
    }

    public TcUser getAmdGpuUser() {
        return amdGpuUser;
    }

    public void setAmdGpuUser(final TcUser amdGpuUser) {
        this.amdGpuUser = amdGpuUser;
    }

    public TcUser getWildcardUser() {
        return wildcardUser;
    }

    public void setWildcardUser(final TcUser wildcardUser) {
        this.wildcardUser = wildcardUser;
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
        return teamName.equals(tcTeam.teamName) && captainName.equals(tcTeam.captainName) && teamWus == tcTeam.teamWus && teamPoints == tcTeam.teamPoints && teamPointsWithoutMultipliers == tcTeam.teamPointsWithoutMultipliers && Objects.equals(nvidiaGpuUser, tcTeam.nvidiaGpuUser) && Objects.equals(amdGpuUser, tcTeam.amdGpuUser) && Objects.equals(wildcardUser, tcTeam.wildcardUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamName, captainName, teamWus, teamPoints, teamPointsWithoutMultipliers, nvidiaGpuUser, amdGpuUser, wildcardUser);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "TcTeam{" +
                "teamName=" + teamName +
                ", captainName=" + captainName +
                ", nvidiaGpuUser=" + nvidiaGpuUser +
                ", amdGpuUser=" + amdGpuUser +
                ", wildcardUser=" + wildcardUser +
                ", teamWus=" + NumberFormat.getInstance(Locale.UK).format(teamWus) +
                ", teamPoints=" + NumberFormat.getInstance(Locale.UK).format(teamPoints) +
                ", teamPointsWithoutMultipliers=" + NumberFormat.getInstance(Locale.UK).format(teamPointsWithoutMultipliers) +
                '}';
    }
}