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