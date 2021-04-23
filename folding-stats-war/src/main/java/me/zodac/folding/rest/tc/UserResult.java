package me.zodac.folding.rest.tc;

import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class UserResult {

    private String userName;
    private String hardware;
    private String category;

    private long points;
    private long multipliedPoints;
    private long units;
    private int rankInTeam;
    private String liveStatsLink;

    public UserResult() {

    }

    private UserResult(final String userName, final String hardware, final String category, final long points, final long multipliedPoints, final long units, final int rankInTeam, final String liveStatsLink) {
        this.userName = userName;
        this.hardware = hardware;
        this.category = category;
        this.points = points;
        this.multipliedPoints = multipliedPoints;
        this.units = units;
        this.rankInTeam = rankInTeam;
        this.liveStatsLink = liveStatsLink;
    }

    // Not ranked to begin with, will be updated by the calling class
    public static UserResult create(final String userName, final String hardware, final String category, final long points, final long pointsWithoutMultiplier, final long units, final String liveStatsLink) {
        return new UserResult(userName, hardware, category, points, pointsWithoutMultiplier, units, 0, liveStatsLink);
    }

    public static UserResult createWithNoPoints(final String userName, final String hardware, final String category, final String liveStatsLink) {
        return new UserResult(userName, hardware, category, 0L, 0L, 0, 0, liveStatsLink);
    }

    public static UserResult updateWithRankInTeam(final UserResult userResult, final int teamRank) {
        return new UserResult(userResult.userName, userResult.hardware, userResult.category, userResult.points, userResult.multipliedPoints,
                userResult.units, teamRank, userResult.liveStatsLink);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(final String hardware) {
        this.hardware = hardware;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(final long points) {
        this.points = points;
    }

    public long getMultipliedPoints() {
        return multipliedPoints;
    }

    public void setMultipliedPoints(final long multipliedPoints) {
        this.multipliedPoints = multipliedPoints;
    }

    public long getUnits() {
        return units;
    }

    public void setUnits(final long units) {
        this.units = units;
    }

    public int getRankInTeam() {
        return rankInTeam;
    }

    public void setRankInTeam(final int rankInTeam) {
        this.rankInTeam = rankInTeam;
    }

    public String getLiveStatsLink() {
        return liveStatsLink;
    }

    public void setLiveStatsLink(final String liveStatsLink) {
        this.liveStatsLink = liveStatsLink;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserResult that = (UserResult) o;
        return points == that.points && multipliedPoints == that.multipliedPoints && units == that.units && rankInTeam == that.rankInTeam && Objects.equals(userName, that.userName) && Objects.equals(hardware, that.hardware) && Objects.equals(category, that.category) && Objects.equals(liveStatsLink, that.liveStatsLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, hardware, category, points, multipliedPoints, units, rankInTeam, liveStatsLink);
    }

    @Override
    public String toString() {
        return "UserResult::{" +
                "userName: '" + userName + "'" +
                ", hardware: '" + hardware + "'" +
                ", category: '" + category + "'" +
                ", points: " + formatWithCommas(points) +
                ", multipliedPoints: " + formatWithCommas(multipliedPoints) +
                ", units: " + formatWithCommas(units) +
                ", rankInTeam: " + rankInTeam +
                ", liveStatsLink: '" + liveStatsLink + "'" +
                '}';
    }
}