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

import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.TestConstants.FOLDING_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Map;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.client.java.request.MonthlyResultRequestSender;
import me.zodac.folding.client.java.response.MonthlyResultResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
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
 * Test for the saving of each month's {@code Team Competition} results.
 */
class MonthlyResultTest {

    private static final MonthlyResultRequestSender MONTHLY_RESULT_REQUEST_SENDER = MonthlyResultRequestSender.createWithUrl(FOLDING_URL);

    @BeforeEach
    void setUp() throws FoldingRestException {
        SystemCleaner.cleanSystemForComplexTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        SystemCleaner.cleanSystemForComplexTests();
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasNoStats_thenEmptyResultIsReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU));

        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);
        assertThat(monthlyResult.teamLeaderboard())
            .as("Expected no results in team leaderboard")
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().keySet())
            .as("Expected each category to have an entry in user category leaderboard")
            .hasSize(Category.getAllValues().size());

        for (final Map.Entry<Category, List<UserCategoryLeaderboardEntry>> categoryEntry : monthlyResult.userCategoryLeaderboard().entrySet()) {
            assertThat(categoryEntry.getValue())
                .as("Expected category '" + categoryEntry.getKey() + "' to have no results")
                .isEmpty();
        }
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasNoEntry_thenEmptyResultIsReturned_andResponseHas200Status() throws FoldingRestException {
        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getMonthlyResult(Year.of(1999), Month.APRIL);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);
        assertThat(monthlyResult.teamLeaderboard())
            .as("Expected no results in team leaderboard")
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().keySet())
            .as("Expected each category to have an entry in user category leaderboard")
            .hasSize(Category.getAllValues().size());

        for (final Map.Entry<Category, List<UserCategoryLeaderboardEntry>> categoryEntry : monthlyResult.userCategoryLeaderboard().entrySet()) {
            assertThat(categoryEntry.getValue())
                .as("Expected category '" + categoryEntry.getKey() + "' to have no results")
                .isEmpty();
        }
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasOneUser_thenResultIsReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final User user = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU));

        StubbedFoldingEndpointUtils.addPoints(user, 10_000L);
        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);

        assertThat(monthlyResult.teamLeaderboard())
            .as("Expected one result in team leaderboard")
            .hasSize(1);

        final TeamLeaderboardEntry teamLeaderboardEntry = monthlyResult.teamLeaderboard().get(0);
        assertThat(teamLeaderboardEntry.getTeam().teamName())
            .as("Expected result team to have same name as input team")
            .isEqualTo(team.teamName());
        assertThat(teamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected result team to have same points as user")
            .isEqualTo(10_000L);

        assertThat(monthlyResult.userCategoryLeaderboard().get(Category.AMD_GPU))
            .as("Expected no results in " + Category.AMD_GPU + " category leaderboard")
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().get(Category.WILDCARD))
            .as("Expected no results in " + Category.WILDCARD + " category leaderboard")
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().get(Category.NVIDIA_GPU))
            .as("Expected one result in " + Category.NVIDIA_GPU + " category leaderboard")
            .hasSize(1);

        final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry = monthlyResult.userCategoryLeaderboard().get(Category.NVIDIA_GPU).get(0);
        assertThat(userCategoryLeaderboardEntry.getUser().team().teamName())
            .as("Expected result user to have same team name as input team")
            .isEqualTo(team.teamName());
        assertThat(userCategoryLeaderboardEntry.getUser().foldingUserName())
            .as("Expected result user to have same name as input user")
            .isEqualTo(user.foldingUserName());
        assertThat(userCategoryLeaderboardEntry.getMultipliedPoints())
            .as("Expected result user to have same points as input user")
            .isEqualTo(10_000L);
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasMultipleUsers_thenResultIsReturnedWithCorrectRanks_andResponseHas200Stats()
        throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(TestGenerator.generateTeam());
        final User firstUser = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(firstTeam.id(), Category.NVIDIA_GPU));
        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);

        final Team secondTeam = TeamUtils.create(TestGenerator.generateTeam());
        final User secondUser = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(secondTeam.id(), Category.NVIDIA_GPU));
        final User thirdUser = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(secondTeam.id(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.addPoints(secondUser, 9_000L);
        StubbedFoldingEndpointUtils.addPoints(thirdUser, 11_000L);

        final Team thirdTeam = TeamUtils.create(TestGenerator.generateTeam());
        final User fourthUser = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(thirdTeam.id(), Category.WILDCARD));
        StubbedFoldingEndpointUtils.addPoints(fourthUser, 30_000L);

        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);

        assertThat(monthlyResult.teamLeaderboard())
            .as("Expected three results in team leaderboard")
            .hasSize(3);

        final TeamLeaderboardEntry firstTeamLeaderboardEntry = monthlyResult.teamLeaderboard().get(0);
        final TeamLeaderboardEntry secondTeamLeaderboardEntry = monthlyResult.teamLeaderboard().get(1);
        final TeamLeaderboardEntry thirdTeamLeaderboardEntry = monthlyResult.teamLeaderboard().get(2);

        assertThat(firstTeamLeaderboardEntry.getTeam().teamName())
            .as("Expected team in first to be the third created team")
            .isEqualTo(thirdTeam.teamName());
        assertThat(firstTeamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected team in first to have the same points as fourth created user")
            .isEqualTo(30_000L);

        assertThat(secondTeamLeaderboardEntry.getTeam().teamName())
            .as("Expected team in second to be the second created team")
            .isEqualTo(secondTeam.teamName());
        assertThat(secondTeamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected team in second to have the same points as second and third created users")
            .isEqualTo(20_000L);

        assertThat(thirdTeamLeaderboardEntry.getTeam().teamName())
            .as("Expected team in third to be the first created team")
            .isEqualTo(firstTeam.teamName());
        assertThat(thirdTeamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected team in third to have the same points as first created user")
            .isEqualTo(10_000L);

        final List<UserCategoryLeaderboardEntry> amdLeaderboard = monthlyResult.userCategoryLeaderboard().get(Category.AMD_GPU);
        final List<UserCategoryLeaderboardEntry> wildcardLeaderboard = monthlyResult.userCategoryLeaderboard().get(Category.WILDCARD);
        final List<UserCategoryLeaderboardEntry> nvidiaLeaderboard = monthlyResult.userCategoryLeaderboard().get(Category.NVIDIA_GPU);

        assertThat(amdLeaderboard)
            .hasSize(1);
        assertThat(amdLeaderboard.get(0).getUser().foldingUserName())
            .as("Expected user in first to be the third created user")
            .isEqualTo(thirdUser.foldingUserName());
        assertThat(amdLeaderboard.get(0).getMultipliedPoints())
            .as("Expected user in first have the same points as third created user")
            .isEqualTo(11_000L);

        assertThat(wildcardLeaderboard)
            .hasSize(1);
        assertThat(wildcardLeaderboard.get(0).getUser().foldingUserName())
            .as("Expected user in first to be the fourth created user")
            .isEqualTo(fourthUser.foldingUserName());
        assertThat(wildcardLeaderboard.get(0).getMultipliedPoints())
            .as("Expected user in first have the same points as fourth created user")
            .isEqualTo(30_000L);

        assertThat(nvidiaLeaderboard)
            .hasSize(2);
        assertThat(nvidiaLeaderboard.get(0).getUser().foldingUserName())
            .as("Expected user in first to be the first created user")
            .isEqualTo(firstUser.foldingUserName());
        assertThat(nvidiaLeaderboard.get(0).getMultipliedPoints())
            .as("Expected user in first have the same points as first created user")
            .isEqualTo(10_000L);
        assertThat(nvidiaLeaderboard.get(1).getUser().foldingUserName())
            .as("Expected user in second to be the second created user")
            .isEqualTo(secondUser.foldingUserName());
        assertThat(nvidiaLeaderboard.get(1).getMultipliedPoints())
            .as("Expected user in second have the same points as second created user")
            .isEqualTo(9_000L);
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasMultipleEntriesForMonth_thenLatestResultIsReturnedWithCorrectRanks_andResponseHas200Stats()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final User user = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU));

        StubbedFoldingEndpointUtils.addPoints(user, 10_000L);
        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        StubbedFoldingEndpointUtils.addPoints(user, 12_000L);
        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);

        assertThat(monthlyResult.teamLeaderboard())
            .as("Expected one result in team leaderboard")
            .hasSize(1);

        final TeamLeaderboardEntry teamLeaderboardEntry = monthlyResult.teamLeaderboard().get(0);
        assertThat(teamLeaderboardEntry.getTeam().teamName())
            .as("Expected result team to have same name as input team")
            .isEqualTo(team.teamName());
        assertThat(teamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected result team to have same points as user's last update")
            .isEqualTo(22_000L);

        assertThat(monthlyResult.userCategoryLeaderboard().get(Category.AMD_GPU))
            .as("Expected no results in " + Category.AMD_GPU + " category leaderboard")
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().get(Category.WILDCARD))
            .as("Expected no results in " + Category.WILDCARD + " category leaderboard")
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().get(Category.NVIDIA_GPU))
            .as("Expected one result in " + Category.NVIDIA_GPU + " category leaderboard")
            .hasSize(1);

        final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry = monthlyResult.userCategoryLeaderboard().get(Category.NVIDIA_GPU).get(0);
        assertThat(userCategoryLeaderboardEntry.getUser().team().teamName())
            .as("Expected result user to have same team name as input team")
            .isEqualTo(team.teamName());
        assertThat(userCategoryLeaderboardEntry.getUser().foldingUserName())
            .as("Expected result user to have same name as input user")
            .isEqualTo(user.foldingUserName());
        assertThat(userCategoryLeaderboardEntry.getMultipliedPoints())
            .as("Expected result user to have same points as input user's last update")
            .isEqualTo(22_000L);
    }

    @Test
    void whenGettingMonthlyResult_andInvalidMonthIsGiven_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/results/result/2000/invalidMonth"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGettingMonthlyResult_andInvalidYearIsGiven_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/results/result/invalidYear/01"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenManualSaveOccurs_givenNoStatsExist_thenRequestSucceeds_andResponseHasA400Status() throws FoldingRestException {
        final HttpResponse<Void> response = MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    void whenManualSaveOccurs_givenNoAuthentication_thenRequestFails_andResponseHasA401Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/results/manual/save"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }
}
