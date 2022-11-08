/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.api.tc.stats;

import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.DateTimeUtils;

/**
 * POJO that extends {@link Stats} adding a {@link User} ID and a {@link Timestamp}.
 */
@Accessors(fluent = true)
@Getter
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
     * @return {@code true} if there are <b>0</b> stats
     * @see Stats#isEmpty()
     */
    public boolean isEmptyStats() {
        return super.isEmpty();
    }
}