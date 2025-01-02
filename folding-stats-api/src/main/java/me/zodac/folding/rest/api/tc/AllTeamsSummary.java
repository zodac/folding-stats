/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

/**
 * Summary of the stats of all {@link me.zodac.folding.api.tc.Team}s and their {@link me.zodac.folding.api.tc.User}s in
 * the {@code Team Competition}.
 */
public record AllTeamsSummary(CompetitionSummary competitionSummary, Collection<TeamSummary> teams) {

    /**
     * Creates a {@link AllTeamsSummary} from a {@link Collection} of {@link TeamSummary}s.
     *
     * <p>
     * The {@link TeamSummary}s are not ranked, so we will rank them by comparing the multiplied points of each {@link TeamSummary}.
     *
     * <p>
     * The points, multiplied points and units from each {@link TeamSummary} are added up to give the total competition
     * points, multiplied points and units.
     *
     * @param teams the {@link TeamSummary}s taking part in the {@code Team Competition}
     * @return the created {@link AllTeamsSummary}
     */
    public static AllTeamsSummary create(final Collection<TeamSummary> teams) {
        long totalPoints = 0L;
        long totalMultipliedPoints = 0L;
        int totalUnits = 0;

        for (final TeamSummary team : teams) {
            totalPoints += team.teamPoints();
            totalMultipliedPoints += team.teamMultipliedPoints();
            totalUnits += team.teamUnits();
        }

        final Collection<TeamSummary> rankedTeams = RankableSummary.rank(teams)
            .stream()
            .map(TeamSummary.class::cast)
            .toList();

        final CompetitionSummary competitionSummary = new CompetitionSummary(totalPoints, totalMultipliedPoints, totalUnits);
        return new AllTeamsSummary(competitionSummary, rankedTeams);
    }
}
