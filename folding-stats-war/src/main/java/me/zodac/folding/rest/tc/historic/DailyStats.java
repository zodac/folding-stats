package me.zodac.folding.rest.tc.historic;

import java.time.LocalDate;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class DailyStats {

    private LocalDate date;
    private long points;
    private long pointsWithoutMultiplier;
    private int units;

    public DailyStats() {

    }

    private DailyStats(final LocalDate date, final long points, final long pointsWithoutMultiplier, final int units) {
        this.date = date;
        this.points = points;
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
        this.units = units;
    }

    public static DailyStats create(final LocalDate date, final long points, final long pointsWithoutMultiplier, final int units) {
        return new DailyStats(date, points, pointsWithoutMultiplier, units);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
        this.date = date;
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
        final DailyStats that = (DailyStats) o;
        return points == that.points && pointsWithoutMultiplier == that.pointsWithoutMultiplier && units == that.units && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, points, pointsWithoutMultiplier, units);
    }

    @Override
    public String toString() {
        return "HistoricStats::{" +
                "date: " + date +
                ", points: " + formatWithCommas(points) +
                ", pointsWithoutMultiplier: " + formatWithCommas(pointsWithoutMultiplier) +
                ", units: " + formatWithCommas(units) +
                '}';
    }
}
