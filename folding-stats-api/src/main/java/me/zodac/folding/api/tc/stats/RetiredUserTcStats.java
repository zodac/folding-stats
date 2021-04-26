package me.zodac.folding.api.tc.stats;

import java.sql.Timestamp;
import java.util.Objects;

public class RetiredUserTcStats {

    private final int retiredUserId;
    private final int teamId;
    private final String displayUserName;
    private final UserTcStats retiredUserTcStats;

    private RetiredUserTcStats(final int retiredUserId, final int teamId, final String displayUserName, final UserTcStats retiredUserTcStats) {
        this.retiredUserId = retiredUserId;
        this.teamId = teamId;
        this.displayUserName = displayUserName;
        this.retiredUserTcStats = retiredUserTcStats;
    }

    public static RetiredUserTcStats create(final int retiredUserId, final int teamId, final String displayUserName, final UserTcStats retiredUserTcStats) {
        return new RetiredUserTcStats(retiredUserId, teamId, displayUserName, retiredUserTcStats);
    }

    public int getRetiredUserId() {
        return retiredUserId;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getUserId() {
        return retiredUserTcStats.getUserId();
    }

    public String getDisplayUserName() {
        return displayUserName;
    }

    public Timestamp getTimestamp() {
        return retiredUserTcStats.getTimestamp();
    }

    public long getPoints() {
        return retiredUserTcStats.getPoints();
    }

    public long getMultipliedPoints() {
        return retiredUserTcStats.getMultipliedPoints();
    }

    public int getUnits() {
        return retiredUserTcStats.getUnits();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RetiredUserTcStats that = (RetiredUserTcStats) o;
        return retiredUserId == that.retiredUserId && teamId == that.teamId && Objects.equals(displayUserName, that.displayUserName) && Objects.equals(retiredUserTcStats, that.retiredUserTcStats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(retiredUserId, teamId, displayUserName, retiredUserTcStats);
    }

    @Override
    public String toString() {
        return "RetiredUserTcStats::{" +
                "retiredUserId: " + retiredUserId +
                ", teamId: " + teamId +
                ", displayUserName: '" + displayUserName + "'" +
                ", retiredUserTcStats: " + retiredUserTcStats +
                '}';
    }
}