package me.zodac.folding;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class TcUser {

    private String userName;
    private String hardware;
    private String category;

    private long points;
    private long pointsWithoutMultiplier;
    private long units;

    public TcUser() {

    }

    public TcUser(final String userName, final String hardware, final String category, final long points, final long pointsWithoutMultiplier, final long units) {
        this.userName = userName;
        this.hardware = hardware;
        this.category = category;
        this.points = points;
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
        this.units = units;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TcUser tcUser = (TcUser) o;
        return Objects.equals(points, tcUser.points) && Objects.equals(pointsWithoutMultiplier, tcUser.pointsWithoutMultiplier) && category == tcUser.category && Objects.equals(units, tcUser.units) && userName.equals(tcUser.userName) && hardware.equals(tcUser.hardware);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, hardware, category, points, pointsWithoutMultiplier, units);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "TcUser{" +
                "userName='" + userName + '\'' +
                ", hardware='" + hardware + '\'' +
                ", category='" + category + '\'' +
                ", points=" + NumberFormat.getInstance(Locale.UK).format(points) +
                ", pointsWithoutMultiplier=" + NumberFormat.getInstance(Locale.UK).format(pointsWithoutMultiplier) +
                ", units=" + NumberFormat.getInstance(Locale.UK).format(units) +
                '}';
    }
}