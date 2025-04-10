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

package net.zodac.folding.test.integration;

import static net.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static net.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static net.zodac.folding.test.integration.util.TestConstants.FOLDING_URL;
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
import net.zodac.folding.api.tc.Category;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.api.tc.result.MonthlyResult;
import net.zodac.folding.client.java.request.MonthlyResultRequestSender;
import net.zodac.folding.client.java.response.MonthlyResultResponseParser;
import net.zodac.folding.rest.api.exception.FoldingRestException;
import net.zodac.folding.rest.api.header.ContentType;
import net.zodac.folding.rest.api.header.RestHeader;
import net.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import net.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import net.zodac.folding.test.integration.util.DummyDataGenerator;
import net.zodac.folding.test.integration.util.SystemCleaner;
import net.zodac.folding.test.integration.util.rest.request.StubbedFoldingEndpointUtils;
import net.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils;
import net.zodac.folding.test.integration.util.rest.request.TeamUtils;
import net.zodac.folding.test.integration.util.rest.request.UserUtils;
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
        final Team team = TeamUtils.create(DummyDataGenerator.generateTeam());
        UserUtils.create(DummyDataGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU));

        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasNoEntry_thenEmptyResultIsReturned_andResponseHas200Status() throws FoldingRestException {
        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getMonthlyResult(Year.of(1999), Month.APRIL);
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasOneUser_thenResultIsReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(DummyDataGenerator.generateTeam());
        final User user = UserUtils.create(DummyDataGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU));

        StubbedFoldingEndpointUtils.addPoints(user, 10_000L);
        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);

        assertThat(monthlyResult.teamLeaderboard())
            .as("Expected one result in team leaderboard")
            .hasSize(1);

        final TeamLeaderboardEntry teamLeaderboardEntry = monthlyResult.teamLeaderboard().getFirst();
        assertThat(teamLeaderboardEntry.team().teamName())
            .as("Expected result team to have same name as input team")
            .isEqualTo(team.teamName());
        assertThat(teamLeaderboardEntry.teamMultipliedPoints())
            .as("Expected result team to have same points as user")
            .isEqualTo(10_000L);

        assertThat(monthlyResult.userCategoryLeaderboard().getOrDefault(Category.AMD_GPU, List.of()))
            .as("Expected no results in %s category leaderboard", Category.AMD_GPU)
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().getOrDefault(Category.WILDCARD, List.of()))
            .as("Expected no results in %s category leaderboard", Category.WILDCARD)
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().getOrDefault(Category.NVIDIA_GPU, List.of()))
            .as("Expected no results in %s category leaderboard", Category.NVIDIA_GPU)
            .hasSize(1);

        final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry = monthlyResult.userCategoryLeaderboard()
            .getOrDefault(Category.NVIDIA_GPU, List.of())
            .getFirst();
        assertThat(userCategoryLeaderboardEntry.user().team().teamName())
            .as("Expected result user to have same team name as input team")
            .isEqualTo(team.teamName());
        assertThat(userCategoryLeaderboardEntry.user().foldingUserName())
            .as("Expected result user to have same name as input user")
            .isEqualTo(user.foldingUserName());
        assertThat(userCategoryLeaderboardEntry.multipliedPoints())
            .as("Expected result user to have same points as input user")
            .isEqualTo(10_000L);
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasMultipleUsers_thenResultIsReturnedWithCorrectRanks_andResponseHas200Stats()
        throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(DummyDataGenerator.generateTeam());
        final User firstUser = UserUtils.create(DummyDataGenerator.generateUserWithTeamIdAndCategory(firstTeam.id(), Category.NVIDIA_GPU));
        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);

        final Team secondTeam = TeamUtils.create(DummyDataGenerator.generateTeam());
        final User secondUser = UserUtils.create(DummyDataGenerator.generateUserWithTeamIdAndCategory(secondTeam.id(), Category.NVIDIA_GPU));
        final User thirdUser = UserUtils.create(DummyDataGenerator.generateUserWithTeamIdAndCategory(secondTeam.id(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.addPoints(secondUser, 9_000L);
        StubbedFoldingEndpointUtils.addPoints(thirdUser, 11_000L);

        final Team thirdTeam = TeamUtils.create(DummyDataGenerator.generateTeam());
        final User fourthUser = UserUtils.create(DummyDataGenerator.generateUserWithTeamIdAndCategory(thirdTeam.id(), Category.WILDCARD));
        StubbedFoldingEndpointUtils.addPoints(fourthUser, 30_000L);

        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);

        assertThat(monthlyResult.teamLeaderboard())
            .as("Expected three results in team leaderboard")
            .hasSize(3);

        final TeamLeaderboardEntry firstTeamLeaderboardEntry = monthlyResult.teamLeaderboard().getFirst();
        final TeamLeaderboardEntry secondTeamLeaderboardEntry = monthlyResult.teamLeaderboard().get(1);
        final TeamLeaderboardEntry thirdTeamLeaderboardEntry = monthlyResult.teamLeaderboard().get(2);

        assertThat(firstTeamLeaderboardEntry.team().teamName())
            .as("Expected team in first to be the third created team")
            .isEqualTo(thirdTeam.teamName());
        assertThat(firstTeamLeaderboardEntry.teamMultipliedPoints())
            .as("Expected team in first to have the same points as fourth created user")
            .isEqualTo(30_000L);

        assertThat(secondTeamLeaderboardEntry.team().teamName())
            .as("Expected team in second to be the second created team")
            .isEqualTo(secondTeam.teamName());
        assertThat(secondTeamLeaderboardEntry.teamMultipliedPoints())
            .as("Expected team in second to have the same points as second and third created users")
            .isEqualTo(20_000L);

        assertThat(thirdTeamLeaderboardEntry.team().teamName())
            .as("Expected team in third to be the first created team")
            .isEqualTo(firstTeam.teamName());
        assertThat(thirdTeamLeaderboardEntry.teamMultipliedPoints())
            .as("Expected team in third to have the same points as first created user")
            .isEqualTo(10_000L);

        final Map<Category, List<UserCategoryLeaderboardEntry>> userCategoryLeaderboard = monthlyResult.userCategoryLeaderboard();
        final List<UserCategoryLeaderboardEntry> amdLeaderboard = userCategoryLeaderboard.getOrDefault(Category.AMD_GPU, List.of());
        final List<UserCategoryLeaderboardEntry> wildcardLeaderboard = userCategoryLeaderboard.getOrDefault(Category.WILDCARD, List.of());
        final List<UserCategoryLeaderboardEntry> nvidiaLeaderboard = userCategoryLeaderboard.getOrDefault(Category.NVIDIA_GPU, List.of());

        assertThat(amdLeaderboard)
            .hasSize(1);
        assertThat(amdLeaderboard.getFirst().user().foldingUserName())
            .as("Expected user in first to be the third created user")
            .isEqualTo(thirdUser.foldingUserName());
        assertThat(amdLeaderboard.getFirst().multipliedPoints())
            .as("Expected user in first have the same points as third created user")
            .isEqualTo(11_000L);

        assertThat(wildcardLeaderboard)
            .hasSize(1);
        assertThat(wildcardLeaderboard.getFirst().user().foldingUserName())
            .as("Expected user in first to be the fourth created user")
            .isEqualTo(fourthUser.foldingUserName());
        assertThat(wildcardLeaderboard.getFirst().multipliedPoints())
            .as("Expected user in first have the same points as fourth created user")
            .isEqualTo(30_000L);

        assertThat(nvidiaLeaderboard)
            .hasSize(2);
        assertThat(nvidiaLeaderboard.getFirst().user().foldingUserName())
            .as("Expected user in first to be the first created user")
            .isEqualTo(firstUser.foldingUserName());
        assertThat(nvidiaLeaderboard.getFirst().multipliedPoints())
            .as("Expected user in first have the same points as first created user")
            .isEqualTo(10_000L);
        assertThat(nvidiaLeaderboard.get(1).user().foldingUserName())
            .as("Expected user in second to be the second created user")
            .isEqualTo(secondUser.foldingUserName());
        assertThat(nvidiaLeaderboard.get(1).multipliedPoints())
            .as("Expected user in second have the same points as second created user")
            .isEqualTo(9_000L);
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasMultipleEntriesForMonth_thenLatestResultIsReturnedWithCorrectRanks_andResponseHas200Stats()
        throws FoldingRestException {
        final Team team = TeamUtils.create(DummyDataGenerator.generateTeam());
        final User user = UserUtils.create(DummyDataGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU));

        StubbedFoldingEndpointUtils.addPoints(user, 10_000L);
        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        StubbedFoldingEndpointUtils.addPoints(user, 12_000L);
        TeamCompetitionStatsUtils.manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);

        assertThat(monthlyResult.teamLeaderboard())
            .as("Expected one result in team leaderboard")
            .hasSize(1);

        final TeamLeaderboardEntry teamLeaderboardEntry = monthlyResult.teamLeaderboard().getFirst();
        assertThat(teamLeaderboardEntry.team().teamName())
            .as("Expected result team to have same name as input team")
            .isEqualTo(team.teamName());
        assertThat(teamLeaderboardEntry.teamMultipliedPoints())
            .as("Expected result team to have same points as user's last update")
            .isEqualTo(22_000L);

        assertThat(monthlyResult.userCategoryLeaderboard().get(Category.AMD_GPU))
            .as("Expected no results in %s category leaderboard", Category.AMD_GPU)
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().get(Category.WILDCARD))
            .as("Expected no results in %s category leaderboard", Category.WILDCARD)
            .isEmpty();

        assertThat(monthlyResult.userCategoryLeaderboard().get(Category.NVIDIA_GPU))
            .as("Expected no results in %s category leaderboard", Category.NVIDIA_GPU)
            .hasSize(1);

        final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry = monthlyResult.userCategoryLeaderboard()
            .getOrDefault(Category.NVIDIA_GPU, List.of())
            .getFirst();
        assertThat(userCategoryLeaderboardEntry.user().team().teamName())
            .as("Expected result user to have same team name as input team")
            .isEqualTo(team.teamName());
        assertThat(userCategoryLeaderboardEntry.user().foldingUserName())
            .as("Expected result user to have same name as input user")
            .isEqualTo(user.foldingUserName());
        assertThat(userCategoryLeaderboardEntry.multipliedPoints())
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
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
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
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenManualSaveOccurs_givenNoStatsExist_thenRequestSucceeds_andResponseHasA400Status() throws FoldingRestException {
        final HttpResponse<Void> response = MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
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
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }
}
