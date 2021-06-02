package me.zodac.folding.test.utils;

/**
 * Simple POJO defining the stats for a user to be persisted in a test DB for stats-based tests.
 * Used since the normal stubbed endpoints insert stats with the timestamp based on the time of execution, whereas for
 * historic stats, we want to specify the timestamps.
 */
public final class TestStats {

    private transient final int userId;
    private transient final String timestamp;
    private transient final long points;
    private transient final long multipliedPoints;
    private transient final int units;

    private TestStats(final int userId, final String timestamp, final long points, final long multipliedPoints, final int units) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.points = points;
        this.multipliedPoints = multipliedPoints;
        this.units = units;
    }

    public static TestStats create(final int userId, final String timestamp, final long points, final long multipliedPoints, final int units) {
        return new TestStats(userId, timestamp, points, multipliedPoints, units);
    }

    @Override
    public String toString() {
        return String.format("(%s, '%s', %s, %s, %s)", userId, timestamp, points, multipliedPoints, units);
    }
}