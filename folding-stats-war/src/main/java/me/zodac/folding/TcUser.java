package me.zodac.folding;

import java.util.Objects;

public class TcUser {
    private String userName; // displayName
    private String hardware; // displayName
    private long points;
    private long pointsWithoutMultiplier;

    public TcUser() {

    }

    public TcUser(final String userName, final String hardware, final long points, final long pointsWithoutMultiplier) {
        this.userName = userName;
        this.hardware = hardware;
        this.points = points;
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TcUser tcUser = (TcUser) o;
        return points == tcUser.points && pointsWithoutMultiplier == tcUser.pointsWithoutMultiplier && userName.equals(tcUser.userName) && hardware.equals(tcUser.hardware);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, hardware, points, pointsWithoutMultiplier);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "TcUser{" +
                "userName='" + userName + '\'' +
                ", hardware='" + hardware + '\'' +
                ", points=" + points +
                ", pointsWithoutMultiplier=" + pointsWithoutMultiplier +
                '}';
    }
}