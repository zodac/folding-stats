/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.bean.tc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.zodac.folding.api.tc.Category;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.bean.StatsRepository;
import net.zodac.folding.rest.api.tc.AllTeamsSummary;
import net.zodac.folding.rest.api.tc.TeamSummary;
import net.zodac.folding.rest.api.tc.UserSummary;
import net.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import net.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Generates the leaderboard stats for {@link Team}s and {@link User} {@link Category}s.
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
     * Generates the {@link Team} leaderboards.
     *
     * @return a {@link List} of {@link TeamLeaderboardEntry}s
     */
    public List<TeamLeaderboardEntry> generateTeamLeaderboards() {
        final AllTeamsSummary allTeamsSummary = statsRepository.getAllTeamsSummary();
        final List<TeamSummary> teamResults = allTeamsSummary.teams()
            .stream()
            .sorted(Comparator.comparingLong(TeamSummary::teamMultipliedPoints).reversed())
            .toList();

        if (teamResults.isEmpty()) {
            LOGGER.warn("No TC teams to show");
            return List.of();
        }

        final TeamLeaderboardEntry leader = TeamLeaderboardEntry.createLeader(teamResults.getFirst());

        final int numberOfTeamResults = teamResults.size();
        final List<TeamLeaderboardEntry> teamSummaries = new ArrayList<>(numberOfTeamResults);
        teamSummaries.add(leader);

        for (int i = 1; i < numberOfTeamResults; i++) {
            final TeamSummary teamSummary = teamResults.get(i);
            final TeamSummary teamAhead = teamResults.get(i - 1);

            final long diffToLeader = leader.teamMultipliedPoints() - teamSummary.teamMultipliedPoints();
            final long diffToNext = teamAhead.teamMultipliedPoints() - teamSummary.teamMultipliedPoints();

            final TeamLeaderboardEntry teamLeaderboardEntry = TeamLeaderboardEntry.create(teamSummary, diffToLeader, diffToNext);
            teamSummaries.add(teamLeaderboardEntry);
        }

        return teamSummaries;
    }

    /**
     * Generates the {@link User} {@link Category} leaderboards.
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
                .sorted(Comparator.comparingLong(UserSummary::multipliedPoints).reversed())
                .toList();

            final List<UserCategoryLeaderboardEntry> userSummariesInCategory = getUserLeaderboardForCategory(userSummaries);
            categoryLeaderboard.put(category, userSummariesInCategory);
        }

        return categoryLeaderboard;
    }

    private static List<UserCategoryLeaderboardEntry> getUserLeaderboardForCategory(final List<UserSummary> userResults) {
        // If we have no users for the category, no need to do anything
        if (userResults.isEmpty()) {
            return List.of();
        }

        final UserSummary firstResult = userResults.getFirst();
        final UserCategoryLeaderboardEntry categoryLeader = UserCategoryLeaderboardEntry.createLeader(firstResult);

        final int numberOfUserResults = userResults.size();
        final List<UserCategoryLeaderboardEntry> userSummariesInCategory = new ArrayList<>(numberOfUserResults);
        userSummariesInCategory.add(categoryLeader);

        for (int i = 1; i < numberOfUserResults; i++) {
            final UserSummary userSummary = userResults.get(i);
            final UserSummary userAhead = userResults.get(i - 1);

            final long diffToLeader = categoryLeader.multipliedPoints() - userSummary.multipliedPoints();
            final long diffToNext = userAhead.multipliedPoints() - userSummary.multipliedPoints();

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
            .teams()
            .stream()
            .map(TeamSummary::activeUsers)
            .flatMap(Collection::stream)
            .toList();

        // Iterate over each category so even if we don't have a user in that category, we will have an entry
        for (final Category category : Category.getAllValues()) {
            final List<UserSummary> usersInCategory = usersInAllTeams
                .stream()
                .filter(userSummary -> userSummary.user().category() == category)
                .toList();

            usersByCategory.put(category, usersInCategory);
        }
        return usersByCategory;
    }
}
