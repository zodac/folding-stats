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

package me.zodac.folding.bean.tc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.rest.api.tc.AllTeamsSummary;
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
public class LeaderboardStatsGenerator {

    private static final Logger LOGGER = LogManager.getLogger();

    private final StatsRepository statsRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param statsRepository the {@link StatsRepository}
     */
    @Autowired
    public LeaderboardStatsGenerator(final StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    /**
     * Generates the {@link me.zodac.folding.api.tc.Team} leaderboards.
     *
     * @return a {@link List} of {@link TeamLeaderboardEntry}s
     */
    public List<TeamLeaderboardEntry> generateTeamLeaderboards() {
        final AllTeamsSummary allTeamsSummary = statsRepository.getAllTeamsSummary();
        final List<TeamSummary> teamResults = allTeamsSummary.getTeams()
            .stream()
            .sorted(Comparator.comparingLong(TeamSummary::getTeamMultipliedPoints).reversed())
            .toList();

        if (teamResults.isEmpty()) {
            LOGGER.warn("No TC teams to show");
            return Collections.emptyList();
        }

        final TeamLeaderboardEntry leader = TeamLeaderboardEntry.createLeader(teamResults.get(0));

        final int numberOfTeamResults = teamResults.size();
        final List<TeamLeaderboardEntry> teamSummaries = new ArrayList<>(numberOfTeamResults);
        teamSummaries.add(leader);

        for (int i = 1; i < numberOfTeamResults; i++) {
            final TeamSummary teamSummary = teamResults.get(i);
            final TeamSummary teamAhead = teamResults.get(i - 1);

            final long diffToLeader = leader.getTeamMultipliedPoints() - teamSummary.getTeamMultipliedPoints();
            final long diffToNext = teamAhead.getTeamMultipliedPoints() - teamSummary.getTeamMultipliedPoints();

            final int rank = i + 1; // TODO: Can we use the rank from the teamSummary?
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
    public Map<Category, List<UserCategoryLeaderboardEntry>> generateUserCategoryLeaderboards() {
        final AllTeamsSummary allTeamsSummary = statsRepository.getAllTeamsSummary();
        final Map<Category, List<UserSummary>> usersByCategory = getUsersSortedByCategory(allTeamsSummary);

        final Map<Category, List<UserCategoryLeaderboardEntry>> categoryLeaderboard = new EnumMap<>(Category.class);
        for (final var entry : usersByCategory.entrySet()) {
            final Category category = entry.getKey();
            final List<UserSummary> userSummaries = entry.getValue()
                .stream()
                .sorted(Comparator.comparingLong(UserSummary::getMultipliedPoints).reversed())
                .toList();

            final List<UserCategoryLeaderboardEntry> userSummariesInCategory = getUserLeaderboardForCategory(userSummaries);
            categoryLeaderboard.put(category, userSummariesInCategory);
        }

        return categoryLeaderboard;
    }

    private static List<UserCategoryLeaderboardEntry> getUserLeaderboardForCategory(final List<? extends UserSummary> userResults) {
        // If we have no users for the category, no need to do anything
        if (userResults.isEmpty()) {
            return Collections.emptyList();
        }

        final UserSummary firstResult = userResults.get(0);
        final UserCategoryLeaderboardEntry categoryLeader = UserCategoryLeaderboardEntry.createLeader(firstResult);

        final int numberOfUserResults = userResults.size();
        final List<UserCategoryLeaderboardEntry> userSummariesInCategory = new ArrayList<>(numberOfUserResults);
        userSummariesInCategory.add(categoryLeader);

        for (int i = 1; i < numberOfUserResults; i++) {
            final UserSummary userSummary = userResults.get(i);
            final UserSummary userAhead = userResults.get(i - 1);

            final long diffToLeader = categoryLeader.getMultipliedPoints() - userSummary.getMultipliedPoints();
            final long diffToNext = userAhead.getMultipliedPoints() - userSummary.getMultipliedPoints();

            final int rank = i + 1;
            final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry = UserCategoryLeaderboardEntry.create(userSummary, rank, diffToLeader,
                diffToNext);
            userSummariesInCategory.add(userCategoryLeaderboardEntry);
        }
        return userSummariesInCategory;
    }

    private static Map<Category, List<UserSummary>> getUsersSortedByCategory(final AllTeamsSummary allTeamsSummary) {
        final Map<Category, List<UserSummary>> usersByCategory = new EnumMap<>(Category.class);
        final Collection<UserSummary> usersInAllTeams = allTeamsSummary
            .getTeams()
            .stream()
            .map(TeamSummary::getActiveUsers)
            .flatMap(Collection::stream)
            .toList();

        // Iterate over each category so even if we don't have a user in that category, we will have an entry
        for (final Category category : Category.getAllValues()) {
            final List<UserSummary> usersInCategory = usersInAllTeams
                .stream()
                .filter(userSummary -> userSummary.getUser().category() == category)
                .toList();

            usersByCategory.put(category, usersInCategory);
        }
        return usersByCategory;
    }
}