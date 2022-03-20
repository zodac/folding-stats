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

/**
 * Summary of the stats of all {@link me.zodac.folding.api.tc.Team}s and their {@link me.zodac.folding.api.tc.User}s in
 * the {@code Team Competition}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class AllTeamsSummary {

    private CompetitionSummary competitionSummary;
    private Collection<TeamSummary> teams = new ArrayList<>();

    /**
     * Creates a {@link AllTeamsSummary} from a {@link Collection} of {@link TeamSummary}s.
     *
     * <p>
     * The {@link TeamSummary}s are not ranked, so we will rank them using {@link IntegerRankingCollector}, by comparing
     * the multiplied points of each {@link TeamSummary}.
     *
     * <p>
     * The points, multiplied points and units from each {@link TeamSummary} are added up to give the total competition
     * points, multiplied points and units.
     *
     * @param teams the {@link TeamSummary}s taking part in the {@code Team Competition}
     * @return the created {@link AllTeamsSummary}
     */
    public static AllTeamsSummary create(final Collection<? extends TeamSummary> teams) {
        int totalUnits = 0;
        long totalPoints = 0L;
        long totalMultipliedPoints = 0L;

        for (final TeamSummary team : teams) {
            totalUnits += team.getTeamUnits();
            totalPoints += team.getTeamPoints();
            totalMultipliedPoints += team.getTeamMultipliedPoints();
        }

        final List<TeamSummary> rankedTeams = teams
            .stream()
            .sorted(Comparator.comparingLong(TeamSummary::getTeamMultipliedPoints).reversed())
            .collect(IntegerRankingCollector.create(
                Comparator.comparingLong(TeamSummary::getTeamMultipliedPoints),
                TeamSummary::getRank,
                TeamSummary::updateWithRank)
            );

        final CompetitionSummary competitionSummary = CompetitionSummary.create(totalPoints, totalMultipliedPoints, totalUnits);
        return new AllTeamsSummary(competitionSummary, rankedTeams);
    }
}
