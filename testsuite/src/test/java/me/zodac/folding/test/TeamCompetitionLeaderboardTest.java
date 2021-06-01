package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.TeamCompetitionResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.leaderboard.TeamSummary;
import me.zodac.folding.rest.api.tc.leaderboard.UserSummary;
import me.zodac.folding.test.utils.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.utils.rest.request.TeamUtils;
import me.zodac.folding.test.utils.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.utils.TestGenerator.generateTeam;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithTeamId;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithTeamIdAndCategory;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.manuallyUpdateStats;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the <code>Team Competition</code> {@link Team} and {@link User} category leaderboards.
 * <p>
 * Since the TC stats are done on the full system (meaning all {@link Team}s), we wipe the system before each test with a {@link BeforeEach} method.
 */
public class TeamCompetitionLeaderboardTest {

    @BeforeEach
    public void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    public void whenGettingTeamLeaderboard_andNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoStats_andNoTeams() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getTeamLeaderboard();

        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<TeamSummary> result = TeamCompetitionResponseParser.getTeamLeaderboard(response);

        assertThat(result)
                .as("Expected no teams: " + result)
                .isEmpty();
    }

    @Test
    public void whenGettingTeamLeaderboard_andTeamsExistWithStats_thenTeamsAreOrderedCorrectly_andPointsDiffIsCorrectlyCalculated() throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamId(firstTeam.getId()));
        StubbedFoldingEndpointUtils.setPoints(firstUser, 10_000L);

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User secondUser = UserUtils.create(generateUserWithTeamId(secondTeam.getId()));
        StubbedFoldingEndpointUtils.setPoints(secondUser, 15_000L);

        final Team thirdTeam = TeamUtils.create(generateTeam());
        final User thirdUser = UserUtils.create(generateUserWithTeamId(thirdTeam.getId()));
        StubbedFoldingEndpointUtils.setPoints(thirdUser, 1_000L);

        manuallyUpdateStats();
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getTeamLeaderboard();

        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<TeamSummary> results = new ArrayList<>(TeamCompetitionResponseParser.getTeamLeaderboard(response));

        assertThat(results)
                .as("Incorrect number of team summaries returned: " + response.body())
                .hasSize(3);

        final TeamSummary firstResult = results.get(0);
        assertThat(firstResult)
                .as("Did not receive the expected result for rank 1: " + response.body())
                .extracting("rank", "teamName", "teamMultipliedPoints", "diffToLeader", "diffToNext")
                .containsExactly(1, secondTeam.getTeamName(), 15_000L, 0L, 0L);

        final TeamSummary secondResult = results.get(1);
        assertThat(secondResult)
                .as("Did not receive the expected result for rank 2: " + response.body())
                .extracting("rank", "teamName", "teamMultipliedPoints", "diffToLeader", "diffToNext")
                .containsExactly(2, firstTeam.getTeamName(), 10_000L, 5_000L, 5_000L);

        final TeamSummary thirdResult = results.get(2);
        assertThat(thirdResult)
                .as("Did not receive the expected result for rank 3: " + response.body())
                .extracting("rank", "teamName", "teamMultipliedPoints", "diffToLeader", "diffToNext")
                .containsExactly(3, thirdTeam.getTeamName(), 1_000L, 14_000L, 9_000L);
    }

    @Test
    public void whenGettingCategoryLeaderboard_andNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoStats_andNoTeams() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getCategoryLeaderboard();

        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Map<String, List<UserSummary>> result = TeamCompetitionResponseParser.getCategoryLeaderboard(response);

        assertThat(result)
                .as("Expected no users: " + result)
                .isEmpty();
    }

    @Test
    public void whenGettingCategoryLeaderboard_andUsersExistWithStats_thenUsersAreGroupedByCategory_andPointsDiffIsCalculatedCorrectly() throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(firstTeam.getId(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.setPoints(firstUser, 10_000L);

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User secondUser = UserUtils.create(generateUserWithTeamIdAndCategory(secondTeam.getId(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.setPoints(secondUser, 15_000L);

        final Team thirdTeam = TeamUtils.create(generateTeam());
        final User thirdUser = UserUtils.create(generateUserWithTeamIdAndCategory(thirdTeam.getId(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.setPoints(thirdUser, 1_000L);
        final User fourthUser = UserUtils.create(generateUserWithTeamIdAndCategory(thirdTeam.getId(), Category.NVIDIA_GPU));
        StubbedFoldingEndpointUtils.setPoints(fourthUser, 1_000L);


        manuallyUpdateStats();
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getCategoryLeaderboard();

        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Map<String, List<UserSummary>> results = TeamCompetitionResponseParser.getCategoryLeaderboard(response);

        assertThat(results)
                .as("Incorrect number of categories returned: " + response.body())
                .hasSize(2);

        assertThat(results.values().stream().flatMap(Collection::stream).collect(toList()))
                .as("Incorrect number of user summaries returned: " + response.body())
                .hasSize(4);


        final List<UserSummary> firstCategoryUsers = results.get(Category.AMD_GPU.displayName());
        assertThat(firstCategoryUsers)
                .as("Incorrect number of " + Category.AMD_GPU.displayName() + " user summaries returned: " + response.body())
                .hasSize(3);


        final List<UserSummary> secondCategoryUsers = results.get(Category.NVIDIA_GPU.displayName());
        assertThat(secondCategoryUsers)
                .as("Incorrect number of " + Category.NVIDIA_GPU.displayName() + " user summaries returned: " + response.body())
                .hasSize(1);

        assertThat(results)
                .as("Incorrect number of " + Category.WILDCARD.displayName() + " user summaries returned: " + response.body())
                .doesNotContainKey(Category.WILDCARD.displayName());

        final UserSummary firstResult = firstCategoryUsers.get(0);
        assertThat(firstResult)
                .as("Did not receive the expected result for rank 1, " + Category.AMD_GPU.displayName() + ": " + response.body())
                .extracting("rank", "displayName", "multipliedPoints", "diffToLeader", "diffToNext")
                .containsExactly(1, secondUser.getDisplayName(), 15_000L, 0L, 0L);

        final UserSummary secondResult = firstCategoryUsers.get(1);
        assertThat(secondResult)
                .as("Did not receive the expected result for rank 2, " + Category.AMD_GPU.displayName() + ": " + response.body())
                .extracting("rank", "displayName", "multipliedPoints", "diffToLeader", "diffToNext")
                .containsExactly(2, firstUser.getDisplayName(), 10_000L, 5_000L, 5_000L);

        final UserSummary thirdResult = firstCategoryUsers.get(2);
        assertThat(thirdResult)
                .as("Did not receive the expected result for rank 3, " + Category.AMD_GPU.displayName() + ": " + response.body())
                .extracting("rank", "displayName", "multipliedPoints", "diffToLeader", "diffToNext")
                .containsExactly(3, thirdUser.getDisplayName(), 1_000L, 14_000L, 9_000L);

        final UserSummary fourthResult = secondCategoryUsers.get(0);
        assertThat(fourthResult)
                .as("Did not receive the expected result for rank 1, category " + Category.NVIDIA_GPU.displayName() + ":" + response.body())
                .extracting("rank", "displayName", "multipliedPoints", "diffToLeader", "diffToNext")
                .containsExactly(1, fourthUser.getDisplayName(), 1_000L, 0L, 0L);
    }

    @AfterAll
    public static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }
}
