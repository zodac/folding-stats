/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
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
@Accessors(fluent = true)
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
            teamSummary.team(),
            teamSummary.teamPoints(),
            teamSummary.teamMultipliedPoints(),
            teamSummary.teamUnits(),
            teamSummary.rank(),
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
