package me.zodac.folding;

import me.zodac.folding.api.Category;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class TcUser {

    private String userName; // displayName
    private String hardware; // displayName
    private Category category;

    // TODO: [zodac] Unsure if the below makes sense, now that we have made the users into a list, double check
    // Using 'Long' rather than 'long' because we want to still list the user+hardware in the JSON output for a team even if
    // an error occurs when retrieving points/WUs. This way, we can set the value to null, which will be excluded in the
    // JSON output (when using Gson, other 3PPs may differ).
    private Long points;
    private Long pointsWithoutMultiplier;
    private Long wus;

    public TcUser() {

    }

    public TcUser(final String userName, final String hardware, final Category category, final long points, final long pointsWithoutMultiplier, final long wus) {
        this.userName = userName;
        this.hardware = hardware;
        this.category = category;
        this.points = points;
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
        this.wus = wus;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    public long getPoints() {
        return points == null ? 0L : points;
    }

    public void setPoints(final long points) {
        this.points = points;
    }

    public long getPointsWithoutMultiplier() {
        return pointsWithoutMultiplier == null ? 0L : pointsWithoutMultiplier;
    }

    public void setPointsWithoutMultiplier(final long pointsWithoutMultiplier) {
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
    }

    public long getWus() {
        return wus == null ? 0L : wus;
    }

    public void setWus(final long wus) {
        this.wus = wus;
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
        return Objects.equals(points, tcUser.points) && Objects.equals(pointsWithoutMultiplier, tcUser.pointsWithoutMultiplier) && category == tcUser.category && Objects.equals(wus, tcUser.wus) && userName.equals(tcUser.userName) && hardware.equals(tcUser.hardware);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, hardware, category, points, pointsWithoutMultiplier, wus);
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
                ", wus=" + NumberFormat.getInstance(Locale.UK).format(wus) +
                '}';
    }
}