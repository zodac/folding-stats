package me.zodac.folding.test.utils;

/**
 * Simple POJO defining the stats for a user to be persisted in a test DB for stats-based tests.
 * Used since the normal stubbed endpoints insert stats with the timestamp based on the time of execution, whereas for
 * historic stats, we want to specify the timestamps.
 */
public class Stats {

    private final int userId;
    private final String timestamp;
    private final long points;
    private final long multipliedPoints;
    private final int units;

    private Stats(final int userId, final String timestamp, final long points, final long multipliedPoints, final int units) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.points = points;
        this.multipliedPoints = multipliedPoints;
        this.units = units;
    }

    public static Stats create(final int userId, final String timestamp, final long points, final long multipliedPoints, final int units) {
        return new Stats(userId, timestamp, points, multipliedPoints, units);
    }

    @Override
    public String toString() {
        return String.format("(%s, '%s', %s, %s, %s)", userId, timestamp, points, multipliedPoints, units);
    }
}