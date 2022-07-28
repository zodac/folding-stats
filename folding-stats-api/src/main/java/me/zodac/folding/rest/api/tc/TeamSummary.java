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

import java.util.ArrayList;
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
 * Summary of the stats of a {@link Team}s and its {@link me.zodac.folding.api.tc.User}s in the {@code Team Competition}.
 *
 * <p>
 * Available at the {@code folding/stats} REST endpoint.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class TeamSummary {

    private static final int DEFAULT_TEAM_RANK = 1;

    private Team team;
    private String captainName;

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
     * @throws IllegalArgumentException thrown if {@code team} is null
     */
    public static TeamSummary createWithDefaultRank(final Team team,
                                                    final String captainName,
                                                    final Collection<? extends UserSummary> activeUsers,
                                                    final Collection<? extends RetiredUserSummary> retiredUsers) {
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

        // Sort the active users by their points
        final List<? extends UserSummary> activeUsersSortedByPoints = activeUsers
            .stream()
            .sorted(Comparator.comparingLong(UserSummary::getMultipliedPoints).reversed())
            .toList();

        // Create new UserSummary instances for each active user, with the new rank
        // Their rank is their index in the sorted list
        final int numberOfActiveUsers = activeUsers.size();
        final Collection<UserSummary> rankedActiveUsers = getRankedActiveUsers(activeUsersSortedByPoints, numberOfActiveUsers);

        // Sort the retired users by their points
        final List<? extends RetiredUserSummary> retiredUsersSortedByPoints = retiredUsers
            .stream()
            .sorted(Comparator.comparingLong(RetiredUserSummary::getMultipliedPoints).reversed())
            .toList();

        // Create new RetiredUserSummary instances for each retired user, with the new rank
        // Their rank is their index in the sorted list, plus the number of active users
        // This way retired users are always ranked below active users
        final int numberOfRetiredUsers = retiredUsers.size();
        final Collection<RetiredUserSummary> rankedRetiredUsers =
            getRankedRetiredUsers(numberOfActiveUsers, retiredUsersSortedByPoints, numberOfRetiredUsers);

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

    // TODO: Move this ranking logic out of here and AllTeamSummary, use in single place
    private static Collection<UserSummary> getRankedActiveUsers(final List<? extends UserSummary> activeUsersSortedByPoints,
                                                                final int numberOfActiveUsers) {
        final Collection<UserSummary> rankedActiveUsers = new ArrayList<>(numberOfActiveUsers);
        long previousValue = 0;
        int previousRank = DEFAULT_TEAM_RANK;

        for (int i = 0; i < numberOfActiveUsers; i++) {
            // If the current user has the same points as the previous user, don't increment the rank
            final UserSummary userSummary = activeUsersSortedByPoints.get(i);
            final int newRank;
            if (previousValue == userSummary.getMultipliedPoints()) {
                newRank = previousRank;
            } else {
                // We explicitly want to move to the next rank after a tie (1st, 1st, 3rd) and skipping the tied value
                // So we increment based on index rather than previousRank (which would give us 1st, 1st, 2nd)
                newRank = i + 1;
            }

            rankedActiveUsers.add(UserSummary.updateWithRankInTeam(userSummary, newRank));

            previousValue = userSummary.getMultipliedPoints();
            previousRank = newRank;
        }
        return rankedActiveUsers;
    }

    private static Collection<RetiredUserSummary> getRankedRetiredUsers(final int numberOfActiveUsers,
                                                                        final List<? extends RetiredUserSummary> retiredUsersSortedByPoints,
                                                                        final int numberOfRetiredUsers) {
        final Collection<RetiredUserSummary> rankedRetiredUsers = new ArrayList<>(numberOfRetiredUsers);
        long previousValue = 0;
        int previousRank = DEFAULT_TEAM_RANK;

        for (int i = 0; i < numberOfRetiredUsers; i++) {
            final RetiredUserSummary retiredUserSummary = retiredUsersSortedByPoints.get(i);

            final int newRank;
            if (previousValue == retiredUserSummary.getMultipliedPoints()) {
                newRank = previousRank;
            } else {
                // We explicitly want to move to the next rank after a tie (1st, 1st, 3rd) and skipping the tied value
                // So we increment based on index rather than previousRank (which would give us 1st, 1st, 2nd)
                newRank = i + 1 + numberOfActiveUsers;
            }

            rankedRetiredUsers.add(RetiredUserSummary.updateWithRankInTeam(retiredUserSummary, newRank));

            previousValue = retiredUserSummary.getMultipliedPoints();
            previousRank = newRank;
        }
        return rankedRetiredUsers;
    }

    /**
     * Updates a {@link TeamSummary} with a rank, after it has been calculated.
     *
     * @param teamSummary the {@link TeamSummary} to update
     * @param rank        the rank
     * @return the updated {@link TeamSummary}
     * @throws IllegalArgumentException thrown if {@code team} is null
     */
    public static TeamSummary updateWithRank(final TeamSummary teamSummary, final int rank) {
        return createWithPoints(
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