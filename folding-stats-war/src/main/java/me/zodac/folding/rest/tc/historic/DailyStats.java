package me.zodac.folding.rest.tc.historic;

import me.zodac.folding.api.tc.stats.UserTcStats;

import java.time.LocalDate;
import java.util.Objects;

public class DailyStats {

    private LocalDate date;
    private long points;
    private long multipliedPoints;
    private int units;

    public DailyStats() {

    }

    private DailyStats(final LocalDate date, final long points, final long multipliedPoints, final int units) {
        this.date = date;
        this.points = points;
        this.multipliedPoints = multipliedPoints;
        this.units = units;
    }

    public static DailyStats createFromTcStats(final LocalDate date, final UserTcStats userTcStats) {
        return new DailyStats(date, userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits());
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

    public long getMultipliedPoints() {
        return multipliedPoints;
    }

    public void setMultipliedPoints(final long multipliedPoints) {
        this.multipliedPoints = multipliedPoints;
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
        return points == that.points && multipliedPoints == that.multipliedPoints && units == that.units && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, points, multipliedPoints, units);
    }


    @Override
    public String toString() {
        return "DailyStats::{" +
                "date: " + date +
                ", points: " + points +
                ", multipliedPoints: " + multipliedPoints +
                ", units: " + units +
                '}';
    }
}
