package me.zodac.folding.parsing;

import java.util.Objects;

public class FoldingStats {

    private final int userId;
    private final long totalPoints;
    private final long timestamp;

    public FoldingStats(final int userId, final long totalPoints, final long timestamp) {
        this.userId = userId;
        this.totalPoints = totalPoints;
        this.timestamp = timestamp;
    }

    public int getUserId() {
        return userId;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public long getTimestamp() {
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
        return userId == that.userId && totalPoints == that.totalPoints && timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, totalPoints, timestamp);
    }

    // TODO: [zodac] toString()
}