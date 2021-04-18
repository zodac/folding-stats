package me.zodac.folding.rest.tc;

import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class UserResult {

    private String userName;
    private String hardware;
    private String category;

    private long points;
    private long pointsWithoutMultiplier;
    private long units;
    private int rankInTeam;

    public UserResult() {

    }

    private UserResult(final String userName, final String hardware, final String category, final long points, final long pointsWithoutMultiplier, final long units, final int rankInTeam) {
        this.userName = userName;
        this.hardware = hardware;
        this.category = category;
        this.points = points;
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
        this.units = units;
        this.rankInTeam = rankInTeam;
    }

    // Not ranked to begin with, will be updated by the calling class
    public static UserResult create(final String userName, final String hardware, final String category, final long points, final long pointsWithoutMultiplier, final long units) {
        return new UserResult(userName, hardware, category, points, pointsWithoutMultiplier, units, 0);
    }

    public static UserResult updateWithRankInTeam(final UserResult userResult, final int teamRank) {
        return new UserResult(userResult.userName, userResult.hardware, userResult.category, userResult.points, userResult.pointsWithoutMultiplier,
                userResult.units, teamRank);
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

    public long getPointsWithoutMultiplier() {
        return pointsWithoutMultiplier;
    }

    public void setPointsWithoutMultiplier(final long pointsWithoutMultiplier) {
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserResult userResult = (UserResult) o;
        return rankInTeam == userResult.rankInTeam && Objects.equals(points, userResult.points) && Objects.equals(pointsWithoutMultiplier, userResult.pointsWithoutMultiplier) && category == userResult.category && Objects.equals(units, userResult.units) && userName.equals(userResult.userName) && hardware.equals(userResult.hardware);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, hardware, category, points, pointsWithoutMultiplier, units, rankInTeam);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "TcUser{" +
                "userName='" + userName + '\'' +
                ", hardware='" + hardware + '\'' +
                ", category='" + category + '\'' +
                ", points=" + formatWithCommas(points) +
                ", pointsWithoutMultiplier=" + formatWithCommas(pointsWithoutMultiplier) +
                ", units=" + formatWithCommas(units) +
                ", rankInTeam=" + rankInTeam +
                '}';
    }
}