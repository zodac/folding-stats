/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;

/**
 * Summary of the stats of a retired {@link me.zodac.folding.api.tc.User} in the <code>Team Competition</code>.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class RetiredUserSummary {

    private static final int DEFAULT_USER_RANK = 0;

    private int id;
    private String displayName;

    private long points;
    private long multipliedPoints;
    private int units;
    private int rankInTeam;

    /**
     * Creates a {@link RetiredUserSummary}, summarising the stats for a now retired {@link me.zodac.folding.api.tc.User}.
     *
     * <p>
     * The {@link RetiredUserSummary} is not ranked to begin with, since it is not aware of the other
     * {@link RetiredUserSummary}s or {@link UserSummary}s. The rank can be updated later using
     * {@link RetiredUserSummary#updateWithRankInTeam(RetiredUserSummary, int)}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} for the {@link me.zodac.folding.api.tc.User}
     * @param rankInTeam         the rank of the {@link RetiredUserTcStats} in the {@link me.zodac.folding.api.tc.Team}
     * @return the created {@link RetiredUserSummary}
     */
    public static RetiredUserSummary create(final RetiredUserTcStats retiredUserTcStats, final int rankInTeam) {
        return new RetiredUserSummary(
            retiredUserTcStats.getUserId(),
            retiredUserTcStats.getDisplayName(),
            retiredUserTcStats.getPoints(),
            retiredUserTcStats.getMultipliedPoints(),
            retiredUserTcStats.getUnits(),
            rankInTeam
        );
    }

    /**
     * Creates a {@link RetiredUserSummary}, summarising the stats for a now retired {@link me.zodac.folding.api.tc.User}.
     *
     * <p>
     * The {@link RetiredUserSummary} is not ranked to begin with, since it is not aware of the other
     * {@link RetiredUserSummary}s or {@link UserSummary}s. The rank can be updated later using
     * {@link RetiredUserSummary#updateWithRankInTeam(RetiredUserSummary, int)}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} for the {@link me.zodac.folding.api.tc.User}
     * @return the created {@link RetiredUserSummary}
     */
    public static RetiredUserSummary createWithDefaultRank(final RetiredUserTcStats retiredUserTcStats) {
        return create(retiredUserTcStats, DEFAULT_USER_RANK);
    }

    /**
     * Updates a {@link RetiredUserSummary} with a rank, after it has been calculated.
     *
     * @param retiredUserSummary the {@link RetiredUserSummary} to update
     * @param rankInTeam         the rank within the {@link TeamSummary}
     * @return the updated {@link RetiredUserSummary}
     */
    public static RetiredUserSummary updateWithRankInTeam(final RetiredUserSummary retiredUserSummary, final int rankInTeam) {
        return new RetiredUserSummary(
            retiredUserSummary.id,
            retiredUserSummary.displayName,
            retiredUserSummary.points,
            retiredUserSummary.multipliedPoints,
            retiredUserSummary.units,
            rankInTeam
        );
    }
}