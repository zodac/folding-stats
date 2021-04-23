package me.zodac.folding.api.tc.stats;

import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class Stats {

    private final long points;
    private final int units;

    private Stats(final long points, final int units) {
        this.points = points;
        this.units = units;
    }

    public static Stats create(final long points, final int units) {
        return new Stats(points, units);
    }

    public long getPoints() {
        return points;
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
        final Stats stats = (Stats) o;
        return points == stats.points && units == stats.units;
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, units);
    }

    @Override
    public String toString() {
        return "Stats::{" +
                "points: " + formatWithCommas(points) +
                ", units: " + formatWithCommas(units) +
                '}';
    }
}
