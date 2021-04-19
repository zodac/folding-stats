package me.zodac.folding.api.tc.stats;

import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class Stats {

    private final long points;
    private final long unmultipliedPoints;
    private final int units;

    private Stats(final long points, final long unmultipliedPoints, final int units) {
        this.points = points;
        this.unmultipliedPoints = unmultipliedPoints;
        this.units = units;
    }

    public static Stats create(final long points, final long unmultipliedPoints, final int units) {
        return new Stats(points, unmultipliedPoints, units);
    }

    public static Stats createWithMultiplier(final long points, final int units, final double multiplier) {
        return new Stats(points, Math.round(points * multiplier), units);
    }

    public long getPoints() {
        return points;
    }

    public long getUnmultipliedPoints() {
        return unmultipliedPoints;
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
        return points == stats.points && unmultipliedPoints == stats.unmultipliedPoints && units == stats.units;
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, unmultipliedPoints, units);
    }

    @Override
    public String toString() {
        return "Stats::{" +
                "points: " + formatWithCommas(points) +
                ", unmultipliedPoints: " + formatWithCommas(unmultipliedPoints) +
                ", units: " + formatWithCommas(units) +
                '}';
    }
}
