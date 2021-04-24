package me.zodac.folding.api.tc;

import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class UserStatsOffset {


    private long pointsOffset;
    private int unitsOffset;

    public UserStatsOffset() {

    }

    private UserStatsOffset(final long pointsOffset, final int unitsOffset) {
        this.pointsOffset = pointsOffset;
        this.unitsOffset = unitsOffset;
    }

    public static UserStatsOffset create(final long pointsOffset, final int unitsOffset) {
        return new UserStatsOffset(pointsOffset, unitsOffset);
    }

    public static UserStatsOffset empty() {
        return new UserStatsOffset(0L, 0);
    }

    public long getPointsOffset() {
        return pointsOffset;
    }

    public void setPointsOffset(final long pointsOffset) {
        this.pointsOffset = pointsOffset;
    }

    public int getUnitsOffset() {
        return unitsOffset;
    }

    public void setUnitsOffset(final int unitsOffset) {
        this.unitsOffset = unitsOffset;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserStatsOffset that = (UserStatsOffset) o;
        return pointsOffset == that.pointsOffset && unitsOffset == that.unitsOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointsOffset, unitsOffset);
    }

    @Override
    public String toString() {
        return "UserStatsOffset::{" +
                "pointsOffset: " + formatWithCommas(pointsOffset) +
                ", unitsOffset: " + formatWithCommas(unitsOffset) +
                '}';
    }
}
