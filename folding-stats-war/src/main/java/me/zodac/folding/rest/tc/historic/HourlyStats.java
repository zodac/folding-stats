package me.zodac.folding.rest.tc.historic;

import java.time.LocalDateTime;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

public class HourlyStats {

    private LocalDateTime dateTime;
    private long points;
    private long pointsWithoutMultiplier;
    private int units;

    public HourlyStats() {

    }

    private HourlyStats(final LocalDateTime dateTime, final long points, final long pointsWithoutMultiplier, final int units) {
        this.dateTime = dateTime;
        this.points = points;
        this.pointsWithoutMultiplier = pointsWithoutMultiplier;
        this.units = units;
    }

    public static HourlyStats create(final LocalDateTime dateTime, final long points, final long pointsWithoutMultiplier, final int units) {
        return new HourlyStats(dateTime, points, pointsWithoutMultiplier, units);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(final LocalDateTime dateTime) {
        this.dateTime = dateTime;
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
        final HourlyStats that = (HourlyStats) o;
        return points == that.points && pointsWithoutMultiplier == that.pointsWithoutMultiplier && units == that.units && Objects.equals(dateTime, that.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, points, pointsWithoutMultiplier, units);
    }

    @Override
    public String toString() {
        return "HistoricStats::{" +
                "dateTime: " + dateTime +
                ", points: " + formatWithCommas(points) +
                ", pointsWithoutMultiplier: " + formatWithCommas(pointsWithoutMultiplier) +
                ", units: " + formatWithCommas(units) +
                '}';
    }
}
