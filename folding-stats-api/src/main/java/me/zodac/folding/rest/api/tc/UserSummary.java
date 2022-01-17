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

package me.zodac.folding.rest.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.User;

/**
 * Summary of the stats of an active {@link User} in the <code>Team Competition</code>.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserSummary {

    private static final int DEFAULT_RANK = 0;
    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private User user;
    private long points;
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
     */
    public static UserSummary create(final User user, final long points, final long multipliedPoints, final int units, final int rankInTeam) {
        return new UserSummary(user, points, multipliedPoints, units, rankInTeam);
    }

    /**
     * Creates a {@link UserSummary}, summarising the stats for an active {@link User}.
     *
     * <p>
     * The {@link UserSummary} is not ranked to begin with, since it is not aware of the other {@link UserSummary}s.
     * The rank can be updated later using {@link UserSummary#updateWithRankInTeam(UserSummary, int)}.
     *
     * @param user             the {@link User}
     * @param points           the points of the {@link User}
     * @param multipliedPoints the multiplied points of the {@link User}
     * @param units            the units of the {@link User}
     * @return the created {@link UserSummary}
     */
    public static UserSummary createWithDefaultRank(final User user, final long points, final long multipliedPoints, final int units) {
        return create(user, points, multipliedPoints, units, DEFAULT_RANK);
    }

    /**
     * Updates a {@link UserSummary} with a rank, after it has been calculated.
     *
     * @param userSummary the {@link UserSummary} to update
     * @param rankInTeam  the rank within the {@link TeamSummary}
     * @return the updated {@link UserSummary}
     */
    public static UserSummary updateWithRankInTeam(final UserSummary userSummary, final int rankInTeam) {
        return create(userSummary.user, userSummary.points, userSummary.multipliedPoints, userSummary.units, rankInTeam);
    }
}