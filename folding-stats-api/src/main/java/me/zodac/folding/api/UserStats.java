package me.zodac.folding.api;

import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

// TODO: [zodac] This and FoldingStats are pretty similar, combine them?
public class UserStats {

    private final long points;
    private final int units;

    public UserStats(final long points, final int units) {
        this.points = points;
        this.units = units;
    }

    public long getPoints() {
        return points;
    }

    public int getUnits() {
        return units;
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, units);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "UserStats{" +
                "points=" + formatWithCommas(points) +
                ", units=" + formatWithCommas(units) +
                '}';
    }
}
