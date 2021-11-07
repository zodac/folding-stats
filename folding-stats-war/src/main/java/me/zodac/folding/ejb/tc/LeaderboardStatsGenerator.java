package me.zodac.folding.ejb.tc;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.ejb.api.BusinessLogic;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Generates the leaderboard stats for {@link me.zodac.folding.api.tc.Team}s and {@link me.zodac.folding.api.tc.User} {@link Category}s.
 */
@Singleton
public class LeaderboardStatsGenerator {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private BusinessLogic businessLogic;

    /**
     * Generates the {@link me.zodac.folding.api.tc.Team} leaderboards.
     *
     * @return a {@link List} of {@link TeamLeaderboardEntry}s
     */
    public List<TeamLeaderboardEntry> generateTeamLeaderboards() {
        final CompetitionSummary competitionSummary = businessLogic.getCompetitionSummary();
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
    public Map<Category, List<UserCategoryLeaderboardEntry>> generateUserCategoryLeaderboards() {
        final CompetitionSummary competitionSummary = businessLogic.getCompetitionSummary();
        if (competitionSummary.getTeams().isEmpty()) {
            LOGGER.warn("No TC teams to show");
            return Collections.emptyMap();
        }

        final Map<Category, List<UserSummary>> userResultsByCategory = new EnumMap<>(Category.class);
        final Map<String, String> teamNameForFoldingUserName = new HashMap<>(); // Convenient way to determine the team name of a user

        for (final TeamSummary teamSummary : competitionSummary.getTeams()) {
            final String teamName = teamSummary.getTeam().getTeamName();

            for (final UserSummary userSummary : teamSummary.getActiveUsers()) {
                final Category category = Category.get(userSummary.getCategory());

                final List<UserSummary> existingUsersInCategory = userResultsByCategory.getOrDefault(category, new ArrayList<>(0));
                existingUsersInCategory.add(userSummary);

                userResultsByCategory.put(category, existingUsersInCategory);
                teamNameForFoldingUserName.put(userSummary.getFoldingName(), teamName);
            }
        }

        final Map<Category, List<UserCategoryLeaderboardEntry>> categoryLeaderboard = new TreeMap<>();

        for (final var entry : userResultsByCategory.entrySet()) {
            final Category category = entry.getKey();
            final List<UserSummary> userSummaries = entry.getValue()
                    .stream()
                    .sorted(Comparator.comparingLong(UserSummary::getMultipliedPoints).reversed())
                    .collect(toList());

            final UserSummary firstResult = userSummaries.get(0);
            final UserCategoryLeaderboardEntry categoryLeader =
                    UserCategoryLeaderboardEntry.createLeader(firstResult, teamNameForFoldingUserName.get(firstResult.getFoldingName()));

            final List<UserCategoryLeaderboardEntry> userSummariesInCategory = new ArrayList<>(userSummaries.size());
            userSummariesInCategory.add(categoryLeader);

            for (int i = 1; i < userSummaries.size(); i++) {
                final UserSummary userSummary = userSummaries.get(i);
                final UserSummary userAhead = userSummaries.get(i - 1);

                final long diffToLeader = categoryLeader.getMultipliedPoints() - userSummary.getMultipliedPoints();
                final long diffToNext = userAhead.getMultipliedPoints() - userSummary.getMultipliedPoints();

                final String teamName = teamNameForFoldingUserName.get(userSummary.getFoldingName());
                final int rank = i + 1;
                final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry =
                        UserCategoryLeaderboardEntry.create(userSummary, teamName, rank, diffToLeader, diffToNext);
                userSummariesInCategory.add(userCategoryLeaderboardEntry);
            }

            categoryLeaderboard.put(category, userSummariesInCategory);
        }

        return categoryLeaderboard;
    }
}
