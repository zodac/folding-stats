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

package me.zodac.folding.rest.api.tc.leaderboard;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.rest.api.tc.TeamSummary;

/**
 * POJO for the {@link Team} leaderboard, summarising the stats for a {@link Team}.
 *
 * <p>
 * Available at the {@code folding/stats/leaderboard} REST endpoint.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class TeamLeaderboardEntry {

    private static final long DEFAULT_DIFF = 0L;

    private Team team;
    private long teamPoints;
    private long teamMultipliedPoints;
    private int teamUnits;
    private int rank;

    private long diffToLeader;
    private long diffToNext;

    /**
     * Creates the {@link TeamLeaderboardEntry} for a {@link Team}.
     *
     * @param teamSummary  the {@link TeamSummary} for the {@link Team}
     * @param diffToLeader the number of points between this {@link Team} and the one in first place
     * @param diffToNext   the number of points between this {@link Team} and the one a single place above
     * @return the created {@link TeamLeaderboardEntry}
     */
    public static TeamLeaderboardEntry create(final TeamSummary teamSummary, final long diffToLeader, final long diffToNext) {
        return new TeamLeaderboardEntry(
            teamSummary.getTeam(),
            teamSummary.getTeamPoints(),
            teamSummary.getTeamMultipliedPoints(),
            teamSummary.getTeamUnits(),
            teamSummary.getRank(),
            diffToLeader,
            diffToNext
        );
    }

    /**
     * Creates the {@link TeamLeaderboardEntry} for the {@link Team} in first place. The rank and diff
     * values are constant in this case.
     *
     * @param teamSummary the {@link TeamSummary} for the {@link Team} in first
     * @return the created {@link TeamLeaderboardEntry}
     */
    public static TeamLeaderboardEntry createLeader(final TeamSummary teamSummary) {
        return create(teamSummary, DEFAULT_DIFF, DEFAULT_DIFF);
    }
}
