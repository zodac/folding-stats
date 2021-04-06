package me.zodac.folding;

import java.util.Objects;

public class TcTeam {

    private String teamName;
    private String captainName;
    private long teamPoints;
    private long teamPointsWithoutMultipliers;
    private TcUser nvidiaGpuUser;
    private TcUser amdGpuUser;
    private TcUser wildcardUser;

    public TcTeam() {

    }

    public TcTeam(final String teamName, final String captainName, final long teamPoints, final long teamPointsWithoutMultipliers, final TcUser nvidiaGpuUser, final TcUser amdGpuUser, final TcUser wildcardUser) {
        this.teamName = teamName;
        this.captainName = captainName;
        this.teamPoints = teamPoints;
        this.teamPointsWithoutMultipliers = teamPointsWithoutMultipliers;
        this.nvidiaGpuUser = nvidiaGpuUser;
        this.amdGpuUser = amdGpuUser;
        this.wildcardUser = wildcardUser;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TcTeam tcTeam = (TcTeam) o;
        return teamName.equals(tcTeam.teamName) && captainName.equals(tcTeam.captainName) && teamPoints == tcTeam.teamPoints && teamPointsWithoutMultipliers == tcTeam.teamPointsWithoutMultipliers && Objects.equals(nvidiaGpuUser, tcTeam.nvidiaGpuUser) && Objects.equals(amdGpuUser, tcTeam.amdGpuUser) && Objects.equals(wildcardUser, tcTeam.wildcardUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamName, captainName, teamPoints, teamPointsWithoutMultipliers, nvidiaGpuUser, amdGpuUser, wildcardUser);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "TcTeam{" +
                "teamName=" + teamName +
                "captainName=" + captainName +
                "teamPoints=" + teamPoints +
                ", teamPointsWithoutMultipliers=" + teamPointsWithoutMultipliers +
                ", nvidiaGpuUser=" + nvidiaGpuUser +
                ", amdGpuUser=" + amdGpuUser +
                ", wildcardUser=" + wildcardUser +
                '}';
    }
}