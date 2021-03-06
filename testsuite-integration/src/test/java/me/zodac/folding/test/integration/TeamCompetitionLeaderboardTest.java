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

package me.zodac.folding.test.integration;

import static java.util.stream.Collectors.toList;
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
import me.zodac.folding.test.integration.util.SystemCleaner;
import me.zodac.folding.test.integration.util.TestGenerator;
import me.zodac.folding.test.integration.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamUtils;
import me.zodac.folding.test.integration.util.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@code Team Competition} {@link Team} and {@link User} category leaderboards.
 *
 * <p>
 * Since the TC stats are done on the full system (meaning all {@link Team}s), we wipe the system before each test with a {@link BeforeEach} method.
 */
class TeamCompetitionLeaderboardTest {

    @BeforeEach
    void setUp() throws FoldingRestException {
        SystemCleaner.cleanSystemForComplexTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        SystemCleaner.cleanSystemForComplexTests();
    }

    @Test
    void whenGettingTeamLeaderboard_andNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoStats_andNoTeams() throws FoldingRestException {
        final HttpResponse<String> response = TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER.getTeamLeaderboard();

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
        final Team firstTeam = TeamUtils.create(TestGenerator.generateTeam());
        final User firstUser = UserUtils.create(TestGenerator.generateUserWithTeamId(firstTeam.id()));
        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);

        final Team secondTeam = TeamUtils.create(TestGenerator.generateTeam());
        final User secondUser = UserUtils.create(TestGenerator.generateUserWithTeamId(secondTeam.id()));
        StubbedFoldingEndpointUtils.addPoints(secondUser, 15_000L);

        final Team thirdTeam = TeamUtils.create(TestGenerator.generateTeam());
        final User thirdUser = UserUtils.create(TestGenerator.generateUserWithTeamId(thirdTeam.id()));
        StubbedFoldingEndpointUtils.addPoints(thirdUser, 1_000L);

        TeamCompetitionStatsUtils.manuallyUpdateStats();
        final HttpResponse<String> response = TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER.getTeamLeaderboard();

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
        final HttpResponse<String> response = TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER.getCategoryLeaderboard();

        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Map<String, List<UserCategoryLeaderboardEntry>> result = TeamCompetitionStatsResponseParser.getCategoryLeaderboard(response);

        assertThat(result.keySet())
            .as("Expected category keys even if there are no teams: " + result)
            .hasSize(Category.getAllValues().size());

        for (final Category category : Category.getAllValues()) {
            assertThat(result.get(category.toString()))
                .as("Expected no values for category: " + category)
                .isEmpty();
        }
    }

    @Test
    void whenGettingCategoryLeaderboard_andUsersExistWithStats_thenUsersAreGroupedByCategory_andPointsDiffIsCalculatedCorrectly()
        throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(TestGenerator.generateTeam());
        final User firstUser = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(firstTeam.id(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);

        final Team secondTeam = TeamUtils.create(TestGenerator.generateTeam());
        final User secondUser = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(secondTeam.id(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.addPoints(secondUser, 15_000L);

        final Team thirdTeam = TeamUtils.create(TestGenerator.generateTeam());
        final User thirdUser = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(thirdTeam.id(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.addPoints(thirdUser, 1_000L);
        final User fourthUser = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(thirdTeam.id(), Category.NVIDIA_GPU));
        StubbedFoldingEndpointUtils.addPoints(fourthUser, 1_000L);

        TeamCompetitionStatsUtils.manuallyUpdateStats();
        final HttpResponse<String> response = TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER.getCategoryLeaderboard();

        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Map<String, List<UserCategoryLeaderboardEntry>> results = TeamCompetitionStatsResponseParser.getCategoryLeaderboard(response);

        assertThat(results)
            .as("Expected one entry per category, found: " + results.keySet() + ", : " + response.body())
            .hasSize(Category.getAllValues().size());

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

        final List<UserCategoryLeaderboardEntry> thirdCategoryUsers = results.get(Category.WILDCARD.toString());
        assertThat(thirdCategoryUsers)
            .as("Incorrect number of " + Category.WILDCARD + " user summaries returned: " + response.body())
            .isEmpty();

        final UserCategoryLeaderboardEntry firstResult = firstCategoryUsers.get(0);
        assertThat(firstResult.getUser().displayName())
            .as("Did not receive the expected user for rank 1, " + Category.AMD_GPU + ": " + response.body())
            .isEqualTo(secondUser.displayName());
        assertThat(firstResult)
            .as("Did not receive the expected result for rank 1, " + Category.AMD_GPU + ": " + response.body())
            .extracting("rank", "multipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(1, 15_000L, 0L, 0L);

        final UserCategoryLeaderboardEntry secondResult = firstCategoryUsers.get(1);
        assertThat(secondResult.getUser().displayName())
            .as("Did not receive the expected user for rank 2, " + Category.AMD_GPU + ": " + response.body())
            .isEqualTo(firstUser.displayName());
        assertThat(secondResult)
            .as("Did not receive the expected result for rank 2, " + Category.AMD_GPU + ": " + response.body())
            .extracting("rank", "multipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(2, 10_000L, 5_000L, 5_000L);

        final UserCategoryLeaderboardEntry thirdResult = firstCategoryUsers.get(2);
        assertThat(thirdResult.getUser().displayName())
            .as("Did not receive the expected user for rank 3, " + Category.AMD_GPU + ": " + response.body())
            .isEqualTo(thirdUser.displayName());
        assertThat(thirdResult)
            .as("Did not receive the expected result for rank 3, " + Category.AMD_GPU + ": " + response.body())
            .extracting("rank", "multipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(3, 1_000L, 14_000L, 9_000L);

        final UserCategoryLeaderboardEntry fourthResult = secondCategoryUsers.get(0);
        assertThat(fourthResult.getUser().displayName())
            .as("Did not receive the expected user for rank 1, " + Category.NVIDIA_GPU + ": " + response.body())
            .isEqualTo(fourthUser.displayName());
        assertThat(fourthResult)
            .as("Did not receive the expected result for rank 1, category " + Category.NVIDIA_GPU + ":" + response.body())
            .extracting("rank", "multipliedPoints", "diffToLeader", "diffToNext")
            .containsExactly(1, 1_000L, 0L, 0L);

        assertThat(fourthResult.getUser().passkey())
            .as("Expected user passkey to be masked: " + response.body())
            .contains("*");
    }
}
