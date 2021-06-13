package me.zodac.folding.test.utils;

/**
 * Simple POJO defining the stats for a user to be persisted in a test DB for stats-based tests.
 * Used since the normal stubbed endpoints insert stats with the timestamp based on the time of execution, whereas for
 * historic stats, we want to specify the timestamps.
 */
public final class TestStats {

    private final transient int userId;
    private final transient String timestamp;
    private final transient long points;
    private final transient long multipliedPoints;
    private final transient int units;

    private TestStats(final int userId, final String timestamp, final long points, final long multipliedPoints, final int units) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.points = points;
        this.multipliedPoints = multipliedPoints;
        this.units = units;
    }

    /**
     * Creates an instance of {@link TestStats}.
     *
     * @param userId           the {@link me.zodac.folding.api.tc.User} ID
     * @param timestamp        the {@link java.sql.Timestamp} the stats were retrieved, as a {@link String}
     * @param points           the points
     * @param multipliedPoints the multiplied points
     * @param units            the units
     * @return the created {@link TestStats}
     */
    public static TestStats create(final int userId, final String timestamp, final long points, final long multipliedPoints, final int units) {
        return new TestStats(userId, timestamp, points, multipliedPoints, units);
    }

    @Override
    public String toString() {
        return String.format("(%s, '%s', %s, %s, %s)", userId, timestamp, points, multipliedPoints, units);
    }
}