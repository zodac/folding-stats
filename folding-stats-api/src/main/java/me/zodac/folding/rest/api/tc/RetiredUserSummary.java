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
import lombok.experimental.Accessors;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.util.StringUtils;

/**
 * Summary of the stats of a retired {@link me.zodac.folding.api.tc.User} in the {@code Team Competition}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public non-sealed class RetiredUserSummary implements RankableSummary {

    private static final int DEFAULT_USER_RANK = 1;

    private int id;
    private String displayName;

    private long points;
    private long multipliedPoints;
    private int units;
    private int rankInTeam;

    /**
     * Creates a {@link RetiredUserSummary}.
     *
     * @param id               the ID of the {@link RetiredUserSummary}
     * @param displayName      the display name for the {@link RetiredUserSummary}
     * @param points           the points for the {@link RetiredUserSummary}
     * @param multipliedPoints the multiplied points for the {@link RetiredUserSummary}
     * @param units            the units for the {@link RetiredUserSummary}
     * @param rankInTeam       the rank of the {@link RetiredUserTcStats} in the {@link me.zodac.folding.api.tc.Team}
     * @return the created {@link RetiredUserSummary}
     * @throws IllegalArgumentException thrown if {@code displayName} {@link StringUtils#isBlank(String)}
     */
    public static RetiredUserSummary create(final int id,
                                            final String displayName,
                                            final long points,
                                            final long multipliedPoints,
                                            final int units,
                                            final int rankInTeam) {
        if (StringUtils.isBlank(displayName)) {
            throw new IllegalArgumentException("'displayName' must not be null or blank");
        }

        return new RetiredUserSummary(id, displayName, points, multipliedPoints, units, rankInTeam);
    }

    /**
     * Creates a {@link RetiredUserSummary}, summarising the stats for a now retired {@link me.zodac.folding.api.tc.User}.
     *
     * <p>
     * The {@link RetiredUserSummary} is not ranked to begin with, since it is not aware of the other
     * {@link RetiredUserSummary}s or {@link UserSummary}s. The rank can be updated later using
     * {@link RetiredUserSummary#updateWithNewRank(int)}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} for the {@link me.zodac.folding.api.tc.User}
     * @param rankInTeam         the rank of the {@link RetiredUserTcStats} in the {@link me.zodac.folding.api.tc.Team}
     * @return the created {@link RetiredUserSummary}
     * @throws IllegalArgumentException thrown if {@code displayName} {@link StringUtils#isBlank(String)}
     */
    public static RetiredUserSummary createWithStats(final RetiredUserTcStats retiredUserTcStats, final int rankInTeam) {
        return create(
            retiredUserTcStats.retiredUserId(),
            retiredUserTcStats.displayName(),
            retiredUserTcStats.points(),
            retiredUserTcStats.multipliedPoints(),
            retiredUserTcStats.units(),
            rankInTeam
        );
    }

    /**
     * Creates a {@link RetiredUserSummary}, summarising the stats for a now retired {@link me.zodac.folding.api.tc.User}.
     *
     * <p>
     * The {@link RetiredUserSummary} is not ranked to begin with, since it is not aware of the other
     * {@link RetiredUserSummary}s or {@link UserSummary}s. The rank can be updated later using
     * {@link RetiredUserSummary#updateWithNewRank(int)}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} for the {@link me.zodac.folding.api.tc.User}
     * @return the created {@link RetiredUserSummary}
     * @throws IllegalArgumentException thrown if {@code displayName} {@link StringUtils#isBlank(String)}
     */
    public static RetiredUserSummary createWithDefaultRank(final RetiredUserTcStats retiredUserTcStats) {
        return createWithStats(retiredUserTcStats, DEFAULT_USER_RANK);
    }

    @Override
    public RetiredUserSummary updateWithNewRank(final int newRank) {
        return create(id, displayName, points, multipliedPoints, units, newRank);
    }

    @Override
    public long getRankableValue() {
        return multipliedPoints;
    }
}