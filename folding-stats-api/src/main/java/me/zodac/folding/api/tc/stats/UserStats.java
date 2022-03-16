/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.api.tc.stats;

import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.DateTimeUtils;

/**
 * POJO that extends {@link Stats} adding a {@link User} ID and a {@link Timestamp}.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(doNotUseGetters = true, callSuper = true)
public class UserStats extends Stats {

    private static final DateTimeUtils DATE_TIME_UTILS = DateTimeUtils.create();

    private final int userId;
    private final Timestamp timestamp;

    /**
     * Constructor for {@link UserStats}.
     *
     * @param userId    the {@link me.zodac.folding.api.tc.User} ID
     * @param timestamp the {@link Timestamp} the stats were retrieved
     * @param points    the points
     * @param units     the units
     */
    protected UserStats(final int userId, final Timestamp timestamp, final long points, final int units) {
        super(points, units);
        this.userId = userId;
        this.timestamp = new Timestamp(timestamp.getTime());
    }

    /**
     * Creates an instance of {@link UserStats}.
     *
     * @param userId    the ID of the {@link User}
     * @param timestamp the {@link Timestamp} the {@link UserStats} were retrieved
     * @param points    the points
     * @param units     the units
     * @return the created {@link UserStats}
     */
    public static UserStats create(final int userId, final Timestamp timestamp, final long points, final int units) {
        return new UserStats(userId, timestamp, points, units);
    }

    /**
     * Creates an instance of {@link UserStats}.
     *
     * @param userId the ID of the {@link User}
     * @param points the points
     * @param units  the units
     * @return the created {@link UserStats}
     */
    public static UserStats createNow(final int userId, final long points, final int units) {
        return create(userId, DATE_TIME_UTILS.currentUtcTimestamp(), points, units);
    }

    /**
     * Creates an empty instance of {@link UserStats}, with no values. Can be used where no stats are necessary, but
     * an {@link java.util.Optional} is not clean enough.
     *
     * @return the empty {@link UserStats}
     */
    public static UserStats empty() {
        return create(User.EMPTY_USER_ID, DATE_TIME_UTILS.currentUtcTimestamp(), Stats.DEFAULT_POINTS, Stats.DEFAULT_UNITS);
    }

    /**
     * Retrieve the ID of the {@link User}.
     *
     * @return the {@link User} ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * The {@link Timestamp} for when the {@link UserStats} were created.
     *
     * @return the {@link Timestamp}
     */
    public Timestamp getTimestamp() {
        return new Timestamp(timestamp.getTime());
    }

    @Override
    public boolean isEmpty() {
        return userId == User.EMPTY_USER_ID && super.isEmpty();
    }

    /**
     * Checks whether the {@link UserStats} has no points or units.
     *
     * <p>
     * This is distinct from {@link #isEmpty()}, as it does not check the {@code userId}.
     *
     * @return <code>true</code> is there are <b>0</b> stats
     */
    public boolean isEmptyStats() {
        return super.isEmpty();
    }
}