package me.zodac.folding.api;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

// TODO: [zodac] This and FoldingStats are pretty similar, combine them?
public class UserStats {

    private final long points;
    private final int wus;

    public UserStats(final long points, final int wus) {
        this.points = points;
        this.wus = wus;
    }

    public long getPoints() {
        return points;
    }

    public int getWus() {
        return wus;
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
        return points == userStats.points && wus == userStats.wus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(points, wus);
    }

    // TODO: [zodac] #toString()
    @Override
    public String toString() {
        return "UserStats{" +
                "points=" + NumberFormat.getInstance(Locale.UK).format(points) +
                ", wus=" + NumberFormat.getInstance(Locale.UK).format(wus) +
                '}';
    }
}
