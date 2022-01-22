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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.Team;

/**
 * Summary of the stats of a {@link Team}s and its {@link me.zodac.folding.api.tc.User}s in
 * the <code>Team Competition</code>.
 *
 * <p>
 * Available at the <code>folding/stats</code> REST endpoint.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class TeamSummary {

    private static final int DEFAULT_TEAM_RANK = 0;

    private Team team;
    private String captainName;

    private long teamPoints;
    private long teamMultipliedPoints;
    private int teamUnits;
    private int rank; // Rank in 'division', but we only have one division so no need to be more explicit with the name

    private Collection<UserSummary> activeUsers;
    private Collection<RetiredUserSummary> retiredUsers;

    /**
     * Creates a {@link TeamSummary}, using a {@link Collection} of {@link UserSummary}s and {@link RetiredUserSummary}.
     *
     * <p>
     * The {@link TeamSummary} is not ranked to begin with, since it is not aware of the other {@link TeamSummary}s. The
     * rank can be updated later using {@link TeamSummary#updateWithRank(TeamSummary, int)}.
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
     */
    public static TeamSummary createWithDefaultRank(final Team team,
                                                    final String captainName,
                                                    final Collection<UserSummary> activeUsers,
                                                    final Collection<RetiredUserSummary> retiredUsers) {
        int teamUnits = 0;
        long teamPoints = 0L;
        long teamMultipliedPoints = 0L;

        for (final UserSummary activeUser : activeUsers) {
            teamUnits += activeUser.getUnits();
            teamPoints += activeUser.getPoints();
            teamMultipliedPoints += activeUser.getMultipliedPoints();
        }

        for (final RetiredUserSummary retired : retiredUsers) {
            teamUnits += retired.getUnits();
            teamPoints += retired.getPoints();
            teamMultipliedPoints += retired.getMultipliedPoints();
        }

        final List<UserSummary> rankedActiveUsers = activeUsers
            .stream()
            .sorted(Comparator.comparingLong(UserSummary::getMultipliedPoints).reversed())
            .collect(new IntegerRankingCollector<>(
                Comparator.comparingLong(UserSummary::getMultipliedPoints),
                UserSummary::getRankInTeam,
                UserSummary::updateWithRankInTeam)
            );

        final List<RetiredUserSummary> rankedRetiredUsers = retiredUsers
            .stream()
            .sorted(Comparator.comparingLong(RetiredUserSummary::getMultipliedPoints).reversed())
            .collect(new IntegerRankingCollector<>(
                Comparator.comparingLong(RetiredUserSummary::getMultipliedPoints),
                RetiredUserSummary::getRankInTeam,
                RetiredUserSummary::updateWithRankInTeam)
            )
            // We need to offset the ranks of the retired users, so they are ranked below active users
            // Annoyingly, we cannot simply add an offset to the #updateWithRankInTeam call above, since the collector applies it multiple times
            // Instead, we now iterate over the retired users again and manually offset them
            .stream()
            .map(rankedRetiredUser -> RetiredUserSummary
                .updateWithRankInTeam(rankedRetiredUser, rankedRetiredUser.getRankInTeam() + activeUsers.size()))
            .toList();

        // Not ranked to begin with, will be updated by the calling class
        return new TeamSummary(
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

    /**
     * Updates a {@link TeamSummary} with a rank, after it has been calculated.
     *
     * @param teamSummary the {@link TeamSummary} to update
     * @param rank        the rank
     * @return the updated {@link TeamSummary}
     */
    public static TeamSummary updateWithRank(final TeamSummary teamSummary, final int rank) {
        return new TeamSummary(
            teamSummary.team,
            teamSummary.captainName,
            teamSummary.teamPoints,
            teamSummary.teamMultipliedPoints,
            teamSummary.teamUnits,
            rank,
            teamSummary.activeUsers,
            teamSummary.retiredUsers
        );
    }
}