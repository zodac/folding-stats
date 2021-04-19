package me.zodac.folding.api.tc.stats;

import java.sql.Timestamp;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class UserStats {

    private final int userId;
    private final Stats totalStats;
    private final Timestamp timestamp;

    private UserStats(final int userId, final Stats totalStats, final Timestamp timestamp) {
        this.userId = userId;
        this.totalStats = totalStats;
        this.timestamp = timestamp;
    }

    public static UserStats create(final int userId, final Stats totalStats, final Timestamp timestamp) {
        return new UserStats(userId, totalStats, timestamp);
    }

    public int getUserId() {
        return userId;
    }

    public long getPoints() {
        return totalStats.getPoints();
    }

    public long getUnmultipliedPoints() {
        return totalStats.getUnmultipliedPoints();
    }

    public int getUnits() {
        return totalStats.getUnits();
    }

    public Stats getUserStats() {
        return totalStats;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserStats userStats = (UserStats) o;
        return userId == userStats.userId && Objects.equals(totalStats, userStats.totalStats) && Objects.equals(timestamp, userStats.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, totalStats, timestamp);
    }

    @Override
    public String toString() {
        return "UserStats::{" +
                "userId: " + userId +
                ", points: " + formatWithCommas(totalStats.getPoints()) +
                ", unmultipliedPoints: " + formatWithCommas(totalStats.getUnmultipliedPoints()) +
                ", units: " + formatWithCommas(totalStats.getUnits()) +
                ", timestamp: " + timestamp +
                '}';
    }
}