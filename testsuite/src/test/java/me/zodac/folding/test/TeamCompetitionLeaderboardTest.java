package me.zodac.folding.test;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.test.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.util.TestGenerator.generateTeam;
import static me.zodac.folding.test.util.TestGenerator.generateUserWithTeamId;
import static me.zodac.folding.test.util.TestGenerator.generateUserWithTeamIdAndCategory;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.manuallyUpdateStats;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.TeamCompetitionStatsResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import me.zodac.folding.test.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.util.rest.request.TeamUtils;
import me.zodac.folding.test.util.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the <code>Team Competition</code> {@link Team} and {@link User} category leaderboards.
 *
 * <p>
 * Since the TC stats are done on the full system (meaning all {@link Team}s), we wipe the system before each test with a {@link BeforeEach} method.
 */
class TeamCompetitionLeaderboardTest {

    @BeforeEach
    void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    void whenGettingTeamLeaderboard_andNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoStats_andNoTeams() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getTeamLeaderboard();

        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<TeamLeaderboardEntry> result = TeamCompetitionStatsResponseParser.getTeamLeaderboard(response);

        assertThat(result)
            .as("Expected no teams: " + result)
            .isEmpty();
    }

    @Test
    void whenGettingTeamLeaderboard_andTeamsExistWithStats_thenTeamsAreOrderedCorrectly_andPointsDiffIsCorrectlyCalculated()
        throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamId(firstTeam.getId()));
        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User secondUser = UserUtils.create(generateUserWithTeamId(secondTeam.getId()));
        StubbedFoldingEndpointUtils.addPoints(secondUser, 15_000L);

        final Team thirdTeam = TeamUtils.create(generateTeam());
        final User thirdUser = UserUtils.create(generateUserWithTeamId(thirdTeam.getId()));
        StubbedFoldingEndpointUtils.addPoints(thirdUser, 1_000L);

        manuallyUpdateStats();
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getTeamLeaderboard();

        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<TeamLeaderboardEntry> results = new ArrayList<>(TeamCompetitionStatsResponseParser.getTeamLeaderboard(response));

        assertThat(results)
            .as("Incorrect number of team summaries returned: " + response.body())
            .hasSize(3);

        final TeamLeaderboardEntry firstResult = results.get(0);
        assertThat(firstResult)
            .as("Did not receive the expected result for rank 1: " + response.body())
            .extracting("rank", "team", "teamMultipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(1, secondTeam, 15_000L, 0L, 0L);

        final TeamLeaderboardEntry secondResult = results.get(1);
        assertThat(secondResult)
            .as("Did not receive the expected result for rank 2: " + response.body())
            .extracting("rank", "team", "teamMultipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(2, firstTeam, 10_000L, 5_000L, 5_000L);

        final TeamLeaderboardEntry thirdResult = results.get(2);
        assertThat(thirdResult)
            .as("Did not receive the expected result for rank 3: " + response.body())
            .extracting("rank", "team", "teamMultipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(3, thirdTeam, 1_000L, 14_000L, 9_000L);
    }

    @Test
    void whenGettingCategoryLeaderboard_andNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoStats_andNoTeams() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getCategoryLeaderboard();

        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Map<String, List<UserCategoryLeaderboardEntry>> result = TeamCompetitionStatsResponseParser.getCategoryLeaderboard(response);

        assertThat(result)
            .as("Expected no users: " + result)
            .isEmpty();
    }

    @Test
    void whenGettingCategoryLeaderboard_andUsersExistWithStats_thenUsersAreGroupedByCategory_andPointsDiffIsCalculatedCorrectly()
        throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(firstTeam.getId(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User secondUser = UserUtils.create(generateUserWithTeamIdAndCategory(secondTeam.getId(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.addPoints(secondUser, 15_000L);

        final Team thirdTeam = TeamUtils.create(generateTeam());
        final User thirdUser = UserUtils.create(generateUserWithTeamIdAndCategory(thirdTeam.getId(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.addPoints(thirdUser, 1_000L);
        final User fourthUser = UserUtils.create(generateUserWithTeamIdAndCategory(thirdTeam.getId(), Category.NVIDIA_GPU));
        StubbedFoldingEndpointUtils.addPoints(fourthUser, 1_000L);

        manuallyUpdateStats();
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getCategoryLeaderboard();

        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Map<String, List<UserCategoryLeaderboardEntry>> results = TeamCompetitionStatsResponseParser.getCategoryLeaderboard(response);

        assertThat(results)
            .as("Incorrect number of categories returned: " + response.body())
            .hasSize(2);

        assertThat(results.values().stream().flatMap(Collection::stream).collect(toList()))
            .as("Incorrect number of user summaries returned: " + response.body())
            .hasSize(4);

        final List<UserCategoryLeaderboardEntry> firstCategoryUsers = results.get(Category.AMD_GPU.toString());
        assertThat(firstCategoryUsers)
            .as("Incorrect number of " + Category.AMD_GPU + " user summaries returned: " + response.body())
            .hasSize(3);

        final List<UserCategoryLeaderboardEntry> secondCategoryUsers = results.get(Category.NVIDIA_GPU.toString());
        assertThat(secondCategoryUsers)
            .as("Incorrect number of " + Category.NVIDIA_GPU + " user summaries returned: " + response.body())
            .hasSize(1);

        assertThat(results)
            .as("Incorrect number of " + Category.WILDCARD + " user summaries returned: " + response.body())
            .doesNotContainKey(Category.WILDCARD.toString());

        final UserCategoryLeaderboardEntry firstResult = firstCategoryUsers.get(0);
        assertThat(firstResult.getUser().getDisplayName())
            .as("Did not receive the expected user for rank 1, " + Category.AMD_GPU + ": " + response.body())
            .isEqualTo(secondUser.getDisplayName());
        assertThat(firstResult)
            .as("Did not receive the expected result for rank 1, " + Category.AMD_GPU + ": " + response.body())
            .extracting("rank", "multipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(1, 15_000L, 0L, 0L);

        final UserCategoryLeaderboardEntry secondResult = firstCategoryUsers.get(1);
        assertThat(secondResult.getUser().getDisplayName())
            .as("Did not receive the expected user for rank 2, " + Category.AMD_GPU + ": " + response.body())
            .isEqualTo(firstUser.getDisplayName());
        assertThat(secondResult)
            .as("Did not receive the expected result for rank 2, " + Category.AMD_GPU + ": " + response.body())
            .extracting("rank", "multipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(2, 10_000L, 5_000L, 5_000L);

        final UserCategoryLeaderboardEntry thirdResult = firstCategoryUsers.get(2);
        assertThat(thirdResult.getUser().getDisplayName())
            .as("Did not receive the expected user for rank 3, " + Category.AMD_GPU + ": " + response.body())
            .isEqualTo(thirdUser.getDisplayName());
        assertThat(thirdResult)
            .as("Did not receive the expected result for rank 3, " + Category.AMD_GPU + ": " + response.body())
            .extracting("rank", "multipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(3, 1_000L, 14_000L, 9_000L);

        final UserCategoryLeaderboardEntry fourthResult = secondCategoryUsers.get(0);
        assertThat(fourthResult.getUser().getDisplayName())
            .as("Did not receive the expected user for rank 1, " + Category.NVIDIA_GPU + ": " + response.body())
            .isEqualTo(fourthUser.getDisplayName());
        assertThat(fourthResult)
            .as("Did not receive the expected result for rank 1, category " + Category.NVIDIA_GPU + ":" + response.body())
            .extracting("rank", "multipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(1, 1_000L, 0L, 0L);
    }
}
