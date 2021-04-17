package me.zodac.folding.rest.tc;

import java.time.LocalDate;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class HistoricStats {

    private LocalDate localDate;
    private long points;
    private long pointsWithoutMultiplier;
    private int units;

    public HistoricStats() {

    }

    public HistoricStats(final LocalDate localDate, final long points, final long pointsWithoutMultiplier, final int units) {
        this.localDate = localDate;
        this.points = points;
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
        this.units = units;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(final LocalDate localDate) {
        this.localDate = localDate;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(final long points) {
        this.points = points;
    }

    public long getPointsWithoutMultiplier() {
        return pointsWithoutMultiplier;
    }

    public void setPointsWithoutMultiplier(final long pointsWithoutMultiplier) {
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(final int units) {
        this.units = units;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HistoricStats that = (HistoricStats) o;
        return points == that.points && pointsWithoutMultiplier == that.pointsWithoutMultiplier && units == that.units && Objects.equals(localDate, that.localDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localDate, points, pointsWithoutMultiplier, units);
    }

    @Override
    public String toString() {
        return "HistoricStats{" +
                "localDate=" + localDate +
                ", points=" + formatWithCommas(points) +
                ", pointsWithoutMultiplier=" + formatWithCommas(pointsWithoutMultiplier) +
                ", units=" + formatWithCommas(units) +
                '}';
    }
}
