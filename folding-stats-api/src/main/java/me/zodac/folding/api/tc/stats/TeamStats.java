package me.zodac.folding.api.tc.stats;

import java.sql.Timestamp;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class TeamStats {

    private final int teamId;
    private final Stats totalStats;
    private final Timestamp timestamp;

    // TODO: [zodac] Static constructor
    public TeamStats(final int teamId, final Stats totalStats, final Timestamp timestamp) {
        this.teamId = teamId;
        this.totalStats = totalStats;
        this.timestamp = timestamp;
    }

    public int getTeamId() {
        return teamId;
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
        final TeamStats that = (TeamStats) o;
        return teamId == that.teamId && Objects.equals(totalStats, that.totalStats) && timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, totalStats, timestamp);
    }

    // TODO: [zodac] toString()
    @Override
    public String toString() {
        return "FoldingStats{" +
                "teamId=" + teamId +
                ", points=" + formatWithCommas(totalStats.getPoints()) +
                ", unmultipliedPoints=" + formatWithCommas(totalStats.getUnmultipliedPoints()) +
                ", units=" + formatWithCommas(totalStats.getUnits()) +
                ", timestamp=" + timestamp +
                '}';
    }
}