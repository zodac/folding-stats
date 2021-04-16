package me.zodac.folding.api;

import java.sql.Timestamp;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class FoldingStats {

    private final int userId;
    private final UserStats totalStats;
    private final Timestamp timestamp;

    // TODO: [zodac] Static constructor
    public FoldingStats(final int userId, final UserStats totalStats, final Timestamp timestamp) {
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

    public int getUnits() {
        return totalStats.getUnits();
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
        final FoldingStats that = (FoldingStats) o;
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
                ", units=" + formatWithCommas(totalStats.getUnits()) +
                ", timestamp=" + timestamp +
                '}';
    }
}