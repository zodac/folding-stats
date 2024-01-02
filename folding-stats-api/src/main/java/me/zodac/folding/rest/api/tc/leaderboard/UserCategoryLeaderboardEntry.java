/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.rest.api.tc.leaderboard;

import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.UserSummary;

/**
 * POJO for the {@link User} {@link me.zodac.folding.api.tc.Category} leaderboard, summarising
 * the stats for a {@link User} in a {@link me.zodac.folding.api.tc.Category}.
 */
public record UserCategoryLeaderboardEntry(User user,
                                           long points,
                                           long multipliedPoints,
                                           int units,
                                           int rank,
                                           long diffToLeader,
                                           long diffToNext
) {

    private static final long DEFAULT_DIFF = 0L;
    private static final int DEFAULT_RANK = 1;

    /**
     * Creates the {@link UserCategoryLeaderboardEntry} for a {@link User}.
     *
     * @param userSummary  the {@link UserSummary} for the {@link User}
     * @param rank         the rank of the {@link User} in their {@link me.zodac.folding.api.tc.Category}
     * @param diffToLeader the number of points between this {@link User} and the one in first place
     * @param diffToNext   the number of points between this {@link User} and the one a single place above
     * @return the created {@link UserCategoryLeaderboardEntry}
     */
    public static UserCategoryLeaderboardEntry create(final UserSummary userSummary, final int rank, final long diffToLeader, final long diffToNext) {
        return new UserCategoryLeaderboardEntry(
            userSummary.user(),
            userSummary.points(),
            userSummary.multipliedPoints(),
            userSummary.units(),
            rank,
            diffToLeader,
            diffToNext
        );
    }

    /**
     * Creates the {@link UserCategoryLeaderboardEntry} for the {@link User} in first place. The rank and diff
     * values are constant in this case.
     *
     * @param userSummary the {@link UserSummary} for the {@link User} in first
     * @return the created {@link UserCategoryLeaderboardEntry}
     */
    public static UserCategoryLeaderboardEntry createLeader(final UserSummary userSummary) {
        return create(userSummary, DEFAULT_RANK, DEFAULT_DIFF, DEFAULT_DIFF);
    }
}
