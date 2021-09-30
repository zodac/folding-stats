package me.zodac.folding.test;

import static me.zodac.folding.rest.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.util.TestGenerator.generateTeam;
import static me.zodac.folding.test.util.TestGenerator.generateUserWithTeamIdAndCategory;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.manuallyUpdateStats;
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
import me.zodac.folding.test.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.util.rest.request.TeamUtils;
import me.zodac.folding.test.util.rest.request.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for the saving of each month's <code>Team Competition</code> results.
 */
class MonthlyResultTest {

    private static final MonthlyResultRequestSender MONTHLY_RESULT_REQUEST_SENDER = MonthlyResultRequestSender.createWithUrl(FOLDING_URL);

    @BeforeEach
    void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasNoEntry_thenEmptyResultIsReturned_andResponseHas200Status() throws FoldingRestException {
        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getMonthlyResult(Year.of(1999), Month.APRIL);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);
        assertThat(monthlyResult.getTeamLeaderboard())
            .as("Expected no results in team leaderboard")
            .isEmpty();

        assertThat(monthlyResult.getUserCategoryLeaderboard().keySet())
            .as("Expected each category to have an entry in user category leaderboard")
            .hasSize(Category.getAllValues().size());

        for (final Map.Entry<Category, List<UserCategoryLeaderboardEntry>> categoryEntry : monthlyResult.getUserCategoryLeaderboard().entrySet()) {
            assertThat(categoryEntry.getValue())
                .as("Expected category '" + categoryEntry.getKey() + "' to have no results")
                .isEmpty();
        }
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasOneUser_thenResultIsReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.NVIDIA_GPU));

        StubbedFoldingEndpointUtils.addPoints(user, 10_000L);
        manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);

        assertThat(monthlyResult.getTeamLeaderboard())
            .as("Expected one result in team leaderboard")
            .hasSize(1);

        final TeamLeaderboardEntry teamLeaderboardEntry = monthlyResult.getTeamLeaderboard().get(0);
        assertThat(teamLeaderboardEntry.getTeamName())
            .as("Expected result team to have same name as input team")
            .isEqualTo(team.getTeamName());
        assertThat(teamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected result team to have same points as user")
            .isEqualTo(10_000L);

        assertThat(monthlyResult.getUserCategoryLeaderboard().get(Category.AMD_GPU))
            .as("Expected no results in " + Category.AMD_GPU + " category leaderboard")
            .isEmpty();

        assertThat(monthlyResult.getUserCategoryLeaderboard().get(Category.WILDCARD))
            .as("Expected no results in " + Category.WILDCARD + " category leaderboard")
            .isEmpty();

        assertThat(monthlyResult.getUserCategoryLeaderboard().get(Category.NVIDIA_GPU))
            .as("Expected one result in " + Category.NVIDIA_GPU + " category leaderboard")
            .hasSize(1);

        final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry = monthlyResult.getUserCategoryLeaderboard().get(Category.NVIDIA_GPU).get(0);
        assertThat(userCategoryLeaderboardEntry.getTeamName())
            .as("Expected result user to have same team name as input team")
            .isEqualTo(team.getTeamName());
        assertThat(userCategoryLeaderboardEntry.getFoldingName())
            .as("Expected result user to have same name as input user")
            .isEqualTo(user.getFoldingUserName());
        assertThat(userCategoryLeaderboardEntry.getMultipliedPoints())
            .as("Expected result user to have same points as input user")
            .isEqualTo(10_000L);
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasMultipleUsers_thenResultIsReturnedWithCorrectRanks_andResponseHas200Stats()
        throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(firstTeam.getId(), Category.NVIDIA_GPU));
        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User secondUser = UserUtils.create(generateUserWithTeamIdAndCategory(secondTeam.getId(), Category.NVIDIA_GPU));
        final User thirdUser = UserUtils.create(generateUserWithTeamIdAndCategory(secondTeam.getId(), Category.AMD_GPU));
        StubbedFoldingEndpointUtils.addPoints(secondUser, 9_000L);
        StubbedFoldingEndpointUtils.addPoints(thirdUser, 11_000L);

        final Team thirdTeam = TeamUtils.create(generateTeam());
        final User fourthUser = UserUtils.create(generateUserWithTeamIdAndCategory(thirdTeam.getId(), Category.WILDCARD));
        StubbedFoldingEndpointUtils.addPoints(fourthUser, 30_000L);

        manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);

        assertThat(monthlyResult.getTeamLeaderboard())
            .as("Expected three results in team leaderboard")
            .hasSize(3);

        final TeamLeaderboardEntry firstTeamLeaderboardEntry = monthlyResult.getTeamLeaderboard().get(0);
        final TeamLeaderboardEntry secondTeamLeaderboardEntry = monthlyResult.getTeamLeaderboard().get(1);
        final TeamLeaderboardEntry thirdTeamLeaderboardEntry = monthlyResult.getTeamLeaderboard().get(2);

        assertThat(firstTeamLeaderboardEntry.getTeamName())
            .as("Expected team in first to be the third created team")
            .isEqualTo(thirdTeam.getTeamName());
        assertThat(firstTeamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected team in first to have the same points as fourth created user")
            .isEqualTo(30_000L);

        assertThat(secondTeamLeaderboardEntry.getTeamName())
            .as("Expected team in second to be the second created team")
            .isEqualTo(secondTeam.getTeamName());
        assertThat(secondTeamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected team in second to have the same points as second and third created users")
            .isEqualTo(20_000L);

        assertThat(thirdTeamLeaderboardEntry.getTeamName())
            .as("Expected team in third to be the first created team")
            .isEqualTo(firstTeam.getTeamName());
        assertThat(thirdTeamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected team in third to have the same points as first created user")
            .isEqualTo(10_000L);

        final List<UserCategoryLeaderboardEntry> amdLeaderboard = monthlyResult.getUserCategoryLeaderboard().get(Category.AMD_GPU);
        final List<UserCategoryLeaderboardEntry> wildcardLeaderboard = monthlyResult.getUserCategoryLeaderboard().get(Category.WILDCARD);
        final List<UserCategoryLeaderboardEntry> nvidiaLeaderboard = monthlyResult.getUserCategoryLeaderboard().get(Category.NVIDIA_GPU);

        assertThat(amdLeaderboard)
            .hasSize(1);
        assertThat(amdLeaderboard.get(0).getFoldingName())
            .as("Expected user in first to be the third created user")
            .isEqualTo(thirdUser.getFoldingUserName());
        assertThat(amdLeaderboard.get(0).getMultipliedPoints())
            .as("Expected user in first have the same points as third created user")
            .isEqualTo(11_000L);

        assertThat(wildcardLeaderboard)
            .hasSize(1);
        assertThat(wildcardLeaderboard.get(0).getFoldingName())
            .as("Expected user in first to be the fourth created user")
            .isEqualTo(fourthUser.getFoldingUserName());
        assertThat(wildcardLeaderboard.get(0).getMultipliedPoints())
            .as("Expected user in first have the same points as fourth created user")
            .isEqualTo(30_000L);

        assertThat(nvidiaLeaderboard)
            .hasSize(2);
        assertThat(nvidiaLeaderboard.get(0).getFoldingName())
            .as("Expected user in first to be the first created user")
            .isEqualTo(firstUser.getFoldingUserName());
        assertThat(nvidiaLeaderboard.get(0).getMultipliedPoints())
            .as("Expected user in first have the same points as first created user")
            .isEqualTo(10_000L);
        assertThat(nvidiaLeaderboard.get(1).getFoldingName())
            .as("Expected user in second to be the second created user")
            .isEqualTo(secondUser.getFoldingUserName());
        assertThat(nvidiaLeaderboard.get(1).getMultipliedPoints())
            .as("Expected user in second have the same points as second created user")
            .isEqualTo(9_000L);
    }

    @Test
    void whenGettingMonthlyResult_andGivenMonthHasMultipleEntriesForMonth_thenLatestResultIsReturnedWithCorrectRanks_andResponseHas200Stats()
        throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.NVIDIA_GPU));

        StubbedFoldingEndpointUtils.addPoints(user, 10_000L);
        manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        StubbedFoldingEndpointUtils.addPoints(user, 12_000L);
        manuallyUpdateStats();
        MONTHLY_RESULT_REQUEST_SENDER.manualSave(ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response = MONTHLY_RESULT_REQUEST_SENDER.getCurrentMonthlyResult();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final MonthlyResult monthlyResult = MonthlyResultResponseParser.getMonthlyResult(response);

        assertThat(monthlyResult.getTeamLeaderboard())
            .as("Expected one result in team leaderboard")
            .hasSize(1);

        final TeamLeaderboardEntry teamLeaderboardEntry = monthlyResult.getTeamLeaderboard().get(0);
        assertThat(teamLeaderboardEntry.getTeamName())
            .as("Expected result team to have same name as input team")
            .isEqualTo(team.getTeamName());
        assertThat(teamLeaderboardEntry.getTeamMultipliedPoints())
            .as("Expected result team to have same points as user's last update")
            .isEqualTo(22_000L);

        assertThat(monthlyResult.getUserCategoryLeaderboard().get(Category.AMD_GPU))
            .as("Expected no results in " + Category.AMD_GPU + " category leaderboard")
            .isEmpty();

        assertThat(monthlyResult.getUserCategoryLeaderboard().get(Category.WILDCARD))
            .as("Expected no results in " + Category.WILDCARD + " category leaderboard")
            .isEmpty();

        assertThat(monthlyResult.getUserCategoryLeaderboard().get(Category.NVIDIA_GPU))
            .as("Expected one result in " + Category.NVIDIA_GPU + " category leaderboard")
            .hasSize(1);

        final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry = monthlyResult.getUserCategoryLeaderboard().get(Category.NVIDIA_GPU).get(0);
        assertThat(userCategoryLeaderboardEntry.getTeamName())
            .as("Expected result user to have same team name as input team")
            .isEqualTo(team.getTeamName());
        assertThat(userCategoryLeaderboardEntry.getFoldingName())
            .as("Expected result user to have same name as input user")
            .isEqualTo(user.getFoldingUserName());
        assertThat(userCategoryLeaderboardEntry.getMultipliedPoints())
            .as("Expected result user to have same points as input user's last update")
            .isEqualTo(22_000L);
    }

    @Test
    void whenGettingMonthlyResult_andInvalidMonthIsGiven_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/results/result/2000/invalidMonth"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
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
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }
}
