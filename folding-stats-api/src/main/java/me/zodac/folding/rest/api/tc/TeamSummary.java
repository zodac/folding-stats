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

import java.util.Collection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.tc.Team;

/**
 * Summary of the stats of a {@link Team}s and its {@link me.zodac.folding.api.tc.User}s in the {@code Team Competition}.
 *
 * <p>
 * Available at the {@code folding/stats} REST endpoint.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public non-sealed class TeamSummary implements RankableSummary {

    private static final int DEFAULT_TEAM_RANK = 1;

    private Team team;
    private String captainName; // Can be null for cases where no captain is set for a team

    private long teamPoints;
    private long teamMultipliedPoints;
    private int teamUnits;
    private int rank; // Rank in 'division', but we only have one division so no need to be more explicit with the name

    private Collection<UserSummary> activeUsers;
    private Collection<RetiredUserSummary> retiredUsers;

    /**
     * Creates a {@link TeamSummary}, manually defining the points/units for all {@link me.zodac.folding.api.tc.User}s in a {@link Team}.
     *
     * @param team                 the {@link Team}
     * @param captainName          the captain's display name for the {@link Team}, or <b>null</b> if no captain
     * @param teamPoints           the points for the {@link TeamSummary}
     * @param teamMultipliedPoints the multiplied points for the {@link TeamSummary}
     * @param teamUnits            the units for the {@link TeamSummary}
     * @param rank                 the {@link TeamSummary} rank
     * @param activeUsers          the active {@link me.zodac.folding.api.tc.User} {@link UserSummary}s
     * @param retiredUsers         the retired {@link me.zodac.folding.api.tc.User} {@link RetiredUserSummary}
     * @return the created {@link UserSummary}
     * @throws IllegalArgumentException thrown if {@code team} is null
     */
    public static TeamSummary createWithPoints(final Team team,
                                               final String captainName,
                                               final long teamPoints,
                                               final long teamMultipliedPoints,
                                               final int teamUnits,
                                               final int rank,
                                               final Collection<UserSummary> activeUsers,
                                               final Collection<RetiredUserSummary> retiredUsers) {
        if (team == null) {
            throw new IllegalArgumentException("'team' must not be null");
        }

        return new TeamSummary(team, captainName, teamPoints, teamMultipliedPoints, teamUnits, rank, activeUsers, retiredUsers);
    }

    /**
     * Creates a {@link TeamSummary}, using a {@link Collection} of {@link UserSummary}s and {@link RetiredUserSummary}.
     *
     * <p>
     * The {@link TeamSummary} is not ranked to begin with, since it is not aware of the other {@link TeamSummary}s. The
     * rank can be updated later using {@link TeamSummary#updateWithNewRank(int)}.
     *
     * <p>
     * The points, multiplied points and units from each {@link UserSummary} and {@link RetiredUserSummary} are added
     * up to give the total team points, multiplied points and units.
     *
     * <p>
     * <b>NOTE:</b> The {@link RetiredUserSummary}s will always be ranked after the {@link UserSummary}s, to highlight
     * the active {@link me.zodac.folding.api.tc.User}s.
     *
     * @param team         the {@link Team}
     * @param captainName  the captain's display name for the {@link Team}, or <b>null</b> if no captain
     * @param activeUsers  the active {@link me.zodac.folding.api.tc.User} {@link UserSummary}s
     * @param retiredUsers the retired {@link me.zodac.folding.api.tc.User} {@link RetiredUserSummary}
     * @return the created {@link TeamSummary}
     * @throws IllegalArgumentException thrown if {@code team} is null
     */
    public static TeamSummary createWithDefaultRank(final Team team,
                                                    final String captainName,
                                                    final Collection<? extends UserSummary> activeUsers,
                                                    final Collection<? extends RetiredUserSummary> retiredUsers) {
        long teamPoints = 0L;
        long teamMultipliedPoints = 0L;
        int teamUnits = 0;

        for (final UserSummary activeUser : activeUsers) {
            teamPoints += activeUser.points();
            teamMultipliedPoints += activeUser.multipliedPoints();
            teamUnits += activeUser.units();
        }

        for (final RetiredUserSummary retired : retiredUsers) {
            teamPoints += retired.points();
            teamMultipliedPoints += retired.multipliedPoints();
            teamUnits += retired.units();
        }

        // Create new UserSummary instances for each active user, with the new rank
        // Their rank is their index in the sorted list
        final Collection<UserSummary> rankedActiveUsers = RankableSummary.rank(activeUsers)
            .stream()
            .map(rankableSummary -> (UserSummary) rankableSummary)
            .toList();

        // Create new RetiredUserSummary instances for each retired user, with the new rank
        // Their rank is their index in the sorted list, plus the number of active users
        // This way retired users are always ranked below active users
        final int numberOfActiveUsers = activeUsers.size();
        final Collection<RetiredUserSummary> rankedRetiredUsers = RankableSummary.rank(retiredUsers, numberOfActiveUsers)
            .stream()
            .map(rankableSummary -> (RetiredUserSummary) rankableSummary)
            .toList();

        // Not ranked to begin with, will be updated by the calling class
        return createWithPoints(
            team,
            captainName,
            teamPoints,
            teamMultipliedPoints,
            teamUnits,
            DEFAULT_TEAM_RANK,
            rankedActiveUsers,
            rankedRetiredUsers
        );
    }

    @Override
    public TeamSummary updateWithNewRank(final int newRank) {
        return createWithPoints(team, captainName, teamPoints, teamMultipliedPoints, teamUnits, newRank, activeUsers, retiredUsers);
    }

    @Override
    public long getRankableValue() {
        return teamMultipliedPoints;
    }
}
