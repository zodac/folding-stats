package me.zodac.folding.api.tc.stats;

import java.sql.Timestamp;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class UserStats {

    private final int userId;
    private final Timestamp timestamp;
    private final Stats totalStats;

    private UserStats(final int userId, final Timestamp timestamp, final Stats totalStats) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.totalStats = totalStats;
    }

    public static UserStats create(final int userId, final Timestamp timestamp, final Stats totalStats) {
        return new UserStats(userId, timestamp, totalStats);
    }

    public int getUserId() {
        return userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public long getPoints() {
        return totalStats.getPoints();
    }

    public int getUnits() {
        return totalStats.getUnits();
    }

    public Stats getStats() {
        return totalStats;
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
                ", timestamp: " + timestamp +
                ", points: " + formatWithCommas(totalStats.getPoints()) +
                ", units: " + formatWithCommas(totalStats.getUnits()) +
                '}';
    }
}