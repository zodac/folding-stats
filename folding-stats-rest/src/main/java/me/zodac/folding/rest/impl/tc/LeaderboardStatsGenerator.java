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
 *
 */

package me.zodac.folding.rest.impl.tc;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.rest.api.FoldingStatsService;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.LeaderboardStatsGeneratorService;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Generates the leaderboard stats for {@link me.zodac.folding.api.tc.Team}s and {@link me.zodac.folding.api.tc.User} {@link Category}s.
 */
@Component
public class LeaderboardStatsGenerator implements LeaderboardStatsGeneratorService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private FoldingStatsService foldingStatsService;

    /**
     * Generates the {@link me.zodac.folding.api.tc.Team} leaderboards.
     *
     * @return a {@link List} of {@link TeamLeaderboardEntry}s
     */
    @Override
    public List<TeamLeaderboardEntry> generateTeamLeaderboards() {
        final CompetitionSummary competitionSummary = foldingStatsService.getCompetitionSummary();
        final List<TeamSummary> teamResults = competitionSummary.getTeams()
            .stream()
            .sorted(Comparator.comparingLong(TeamSummary::getTeamMultipliedPoints).reversed())
            .collect(toList());

        if (teamResults.isEmpty()) {
            LOGGER.warn("No TC teams to show");
            return Collections.emptyList();
        }

        final TeamLeaderboardEntry leader = TeamLeaderboardEntry.createLeader(teamResults.get(0));

        final List<TeamLeaderboardEntry> teamSummaries = new ArrayList<>(teamResults.size());
        teamSummaries.add(leader);

        for (int i = 1; i < teamResults.size(); i++) {
            final TeamSummary teamSummary = teamResults.get(i);
            final TeamSummary teamAhead = teamResults.get(i - 1);

            final long diffToLeader = leader.getTeamMultipliedPoints() - teamSummary.getTeamMultipliedPoints();
            final long diffToNext = teamAhead.getTeamMultipliedPoints() - teamSummary.getTeamMultipliedPoints();

            final int rank = i + 1;
            final TeamLeaderboardEntry teamLeaderboardEntry = TeamLeaderboardEntry.create(teamSummary, rank, diffToLeader, diffToNext);
            teamSummaries.add(teamLeaderboardEntry);
        }

        return teamSummaries;
    }

    /**
     * Generates the {@link me.zodac.folding.api.tc.User} {@link Category} leaderboards.
     *
     * @return a {@link Map} of {@link UserCategoryLeaderboardEntry}s keyed by the {@link Category}
     */
    @Override
    public Map<Category, List<UserCategoryLeaderboardEntry>> generateUserCategoryLeaderboards() {
        final CompetitionSummary competitionSummary = foldingStatsService.getCompetitionSummary();
        final Map<Category, List<UserSummary>> usersByCategory = getUsersSortedByCategory(competitionSummary);

        final Map<Category, List<UserCategoryLeaderboardEntry>> categoryLeaderboard = new TreeMap<>();
        for (final var entry : usersByCategory.entrySet()) {
            final Category category = entry.getKey();
            final List<UserSummary> userSummaries = entry.getValue()
                .stream()
                .sorted(Comparator.comparingLong(UserSummary::getMultipliedPoints).reversed())
                .collect(toList());

            final List<UserCategoryLeaderboardEntry> userSummariesInCategory = getUserLeaderboardForCategory(userSummaries);
            categoryLeaderboard.put(category, userSummariesInCategory);
        }

        return categoryLeaderboard;
    }

    private List<UserCategoryLeaderboardEntry> getUserLeaderboardForCategory(final List<UserSummary> userSummaries) {
        // If we have no users for the category, no need to do anything
        if (userSummaries.isEmpty()) {
            return Collections.emptyList();
        }

        final UserSummary firstResult = userSummaries.get(0);
        final UserCategoryLeaderboardEntry categoryLeader = UserCategoryLeaderboardEntry.createLeader(firstResult);

        final List<UserCategoryLeaderboardEntry> userSummariesInCategory = new ArrayList<>(userSummaries.size());
        userSummariesInCategory.add(categoryLeader);

        for (int i = 1; i < userSummaries.size(); i++) {
            final UserSummary userSummary = userSummaries.get(i);
            final UserSummary userAhead = userSummaries.get(i - 1);

            final long diffToLeader = categoryLeader.getMultipliedPoints() - userSummary.getMultipliedPoints();
            final long diffToNext = userAhead.getMultipliedPoints() - userSummary.getMultipliedPoints();

            final int rank = i + 1;
            final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry = UserCategoryLeaderboardEntry.create(userSummary, rank, diffToLeader,
                diffToNext);
            userSummariesInCategory.add(userCategoryLeaderboardEntry);
        }
        return userSummariesInCategory;
    }

    private static Map<Category, List<UserSummary>> getUsersSortedByCategory(final CompetitionSummary competitionSummary) {
        final Map<Category, List<UserSummary>> usersByCategory = new EnumMap<>(Category.class);
        final Collection<UserSummary> usersInAllTeams = competitionSummary
            .getTeams()
            .stream()
            .map(TeamSummary::getActiveUsers)
            .flatMap(Collection::stream)
            .collect(toList());

        // Iterate over each category so even if we don't have a user in that category, we will have an entry
        for (final Category category : Category.getAllValues()) {
            final List<UserSummary> usersInCategory = usersInAllTeams
                .stream()
                .filter(userSummary -> userSummary.getUser().getCategory() == category)
                .collect(toList());

            usersByCategory.put(category, usersInCategory);
        }
        return usersByCategory;
    }
}