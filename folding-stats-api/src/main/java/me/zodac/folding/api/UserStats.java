package me.zodac.folding.api;

import java.sql.Timestamp;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class UserStats {

    private final int userId;
    private final Stats totalStats;
    private final Timestamp timestamp;

    // TODO: [zodac] Static constructor
    public UserStats(final int userId, final Stats totalStats, final Timestamp timestamp) {
        this.userId = userId;
        this.totalStats = totalStats;
        this.timestamp = timestamp;
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
        final UserStats that = (UserStats) o;
        return userId == that.userId && Objects.equals(totalStats, that.totalStats) && timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, totalStats, timestamp);
    }

    // TODO: [zodac] toString()
    @Override
    public String toString() {
        return "FoldingStats{" +
                "userId=" + userId +
                ", points=" + formatWithCommas(totalStats.getPoints()) +
                ", unmultipliedPoints=" + formatWithCommas(totalStats.getUnmultipliedPoints()) +
                ", units=" + formatWithCommas(totalStats.getUnits()) +
                ", timestamp=" + timestamp +
                '}';
    }
}