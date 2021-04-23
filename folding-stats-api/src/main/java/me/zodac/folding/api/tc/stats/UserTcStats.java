package me.zodac.folding.api.tc.stats;

import java.sql.Timestamp;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class UserTcStats {

    private final int userId;
    private final Timestamp timestamp;
    private final long points;
    private final long multipliedPoints;
    private final int units;

    private UserTcStats(final int userId, final Timestamp timestamp, final long points, final long multipliedPoints, final int units) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.points = points;
        this.multipliedPoints = multipliedPoints;
        this.units = units;
    }

    public static UserTcStats create(final int userId, final Timestamp timestamp, final long points, final long multipliedPoints, final int units) {
        return new UserTcStats(userId, timestamp, points, multipliedPoints, units);
    }

    public static UserTcStats createWithMultiplier(final int userId, final Timestamp timestamp, final Stats stats, final double multiplier) {
        return new UserTcStats(userId, timestamp, stats.getPoints(), Math.round(stats.getPoints() * multiplier), stats.getUnits());
    }

    public int getUserId() {
        return userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public long getPoints() {
        return points;
    }

    public long getMultipliedPoints() {
        return multipliedPoints;
    }

    public int getUnits() {
        return units;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserTcStats that = (UserTcStats) o;
        return userId == that.userId && points == that.points && multipliedPoints == that.multipliedPoints && units == that.units && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, timestamp, points, multipliedPoints, units);
    }

    @Override
    public String toString() {
        return "UserStats::{" +
                "userId: " + userId +
                ", timestamp: " + timestamp +
                ", points: " + formatWithCommas(points) +
                ", multipliedPoints: " + formatWithCommas(multipliedPoints) +
                ", units: " + formatWithCommas(units) +
                '}';
    }
}