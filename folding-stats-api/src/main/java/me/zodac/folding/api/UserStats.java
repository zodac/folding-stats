package me.zodac.folding.api;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserStats userStats = (UserStats) o;
        return points == userStats.points && units == userStats.units;
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, units);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "UserStats{" +
                "points=" + NumberFormat.getInstance(Locale.UK).format(points) +
                ", units=" + NumberFormat.getInstance(Locale.UK).format(units) +
                '}';
    }
}
