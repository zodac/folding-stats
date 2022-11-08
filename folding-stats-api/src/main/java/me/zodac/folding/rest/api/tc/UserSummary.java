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

package me.zodac.folding.rest.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.tc.User;

/**
 * Summary of the stats of an active {@link User} in the {@code Team Competition}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public non-sealed class UserSummary implements RankableSummary {

    private static final int DEFAULT_RANK = 1;
    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private User user;
    private long points; // TODO: Replace with stats object?
    private long multipliedPoints;
    private int units;
    private int rankInTeam;

    /**
     * Creates a {@link UserSummary}, summarising the stats for an active {@link User}.
     *
     * @param user             the {@link User}
     * @param points           the points of the {@link User}
     * @param multipliedPoints the multiplied points of the {@link User}
     * @param units            the units of the {@link User}
     * @param rankInTeam       the rank of the {@link User} in this {@link me.zodac.folding.api.tc.Team}
     * @return the created {@link UserSummary}
     * @throws IllegalArgumentException thrown if {@code user} is null
     */
    public static UserSummary create(final User user, final long points, final long multipliedPoints, final int units, final int rankInTeam) {
        if (user == null) {
            throw new IllegalArgumentException("'user' must not be null");
        }

        return new UserSummary(user, points, multipliedPoints, units, rankInTeam);
    }

    /**
     * Creates a {@link UserSummary}, summarising the stats for an active {@link User}.
     *
     * <p>
     * The {@link UserSummary} is not ranked to begin with, since it is not aware of the other {@link UserSummary}s.
     * The rank can be updated later using {@link UserSummary#updateWithNewRank(int)}.
     *
     * @param user             the {@link User}
     * @param points           the points of the {@link User}
     * @param multipliedPoints the multiplied points of the {@link User}
     * @param units            the units of the {@link User}
     * @return the created {@link UserSummary}
     * @throws IllegalArgumentException thrown if {@code user} is null
     */
    public static UserSummary createWithDefaultRank(final User user, final long points, final long multipliedPoints, final int units) {
        return create(user, points, multipliedPoints, units, DEFAULT_RANK);
    }

    @Override
    public UserSummary updateWithNewRank(final int newRank) {
        return create(user, points, multipliedPoints, units, newRank);
    }

    @Override
    public long getRankableValue() {
        return multipliedPoints;
    }
}