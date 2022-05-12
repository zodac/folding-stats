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

import static me.zodac.folding.test.integration.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.integration.util.TestGenerator.generateHardwareFromCategory;
import static me.zodac.folding.test.integration.util.TestGenerator.generateTeam;
import static me.zodac.folding.test.integration.util.TestGenerator.generateUserWithTeamIdAndCategory;
import static me.zodac.folding.test.integration.util.TestGenerator.nextUserName;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.getTeamFromCompetition;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.manuallyResetStats;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.manuallyUpdateStats;
import static me.zodac.folding.test.integration.util.rest.request.UserUtils.USER_REQUEST_SENDER;
import static me.zodac.folding.test.integration.util.rest.request.UserUtils.create;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.AllTeamsSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.rest.api.util.RestUtilConstants;
import me.zodac.folding.test.integration.util.rest.request.HardwareUtils;
import me.zodac.folding.test.integration.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for the monthly reset of the {@code Team Competition} {@link AllTeamsSummary}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResetTest {

    @BeforeAll
    static void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    @Order(1)
    void whenResetOccurs_andNoTeamsExist_thenNoErrorOccurs() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualReset(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Expected a 200_OK when no teams exist")
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    void whenResetOccurs_givenNoAuthentication_thenRequestFails_andResponseHasA401Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/stats/manual/reset"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<Void> response = RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenResetOccurs_andRetiredStatsExistForTeam_thenRetiredStatsAreRemovedOnReset() throws FoldingRestException {
        final Hardware captainHardware = HardwareUtils.create(generateHardwareFromCategory(Category.NVIDIA_GPU));
        final Hardware hardware = HardwareUtils.create(generateHardwareFromCategory(Category.AMD_GPU));
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest captainUser = UserRequest.builder()
            .foldingUserName(nextUserName())
            .displayName("displayName")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .hardwareId(captainHardware.id())
            .teamId(team.id())
            .userIsCaptain(true)
            .build();
        create(captainUser);

        final UserRequest userToRetire = UserRequest.builder()
            .foldingUserName(nextUserName())
            .displayName("displayName")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .hardwareId(hardware.id())
            .teamId(team.id())
            .build();

        final int userToRetireId = create(userToRetire).id();

        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());

        assertThat(teamSummary.getActiveUsers())
            .as("Expected exactly 2 active users at start: " + teamSummary)
            .hasSize(2);

        assertThat(teamSummary.getRetiredUsers())
            .as("Expected no retired users at start: " + teamSummary)
            .isEmpty();

        // User must have points or else will not show as 'retired' for the team
        StubbedFoldingEndpointUtils.addPoints(userToRetire, 1_000L);
        manuallyUpdateStats();

        USER_REQUEST_SENDER.delete(userToRetireId, ADMIN_USER.userName(), ADMIN_USER.password());
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterRetirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.teamName());

        assertThat(teamSummaryAfterRetirement.getActiveUsers())
            .as("Expected exactly 1 active users after retirement: " + teamSummaryAfterRetirement)
            .hasSize(1);

        assertThat(teamSummaryAfterRetirement.getRetiredUsers())
            .as("Expected exactly 1 retired users after retirement: " + teamSummaryAfterRetirement)
            .hasSize(1);

        manuallyResetStats();

        final AllTeamsSummary resultAfterReset = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterReset = getTeamFromCompetition(resultAfterReset, team.teamName());

        assertThat(teamSummaryAfterReset.getActiveUsers())
            .as("Expected exactly 1 active users after reset: " + teamSummaryAfterReset)
            .hasSize(1);

        assertThat(teamSummaryAfterReset.getRetiredUsers())
            .as("Expected no retired users after reset: " + teamSummaryAfterReset)
            .isEmpty();
    }

    @Test
    void whenResetOccurs_thenStatsAreResetForCompetitionAndTeamsAndUsers() throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());

        final UserRequest firstUser = generateUserWithTeamIdAndCategory(firstTeam.id(), Category.NVIDIA_GPU);
        firstUser.setUserIsCaptain(true);
        create(firstUser);

        final UserRequest secondUser = generateUserWithTeamIdAndCategory(firstTeam.id(), Category.AMD_GPU);
        create(secondUser);

        final Team secondTeam = TeamUtils.create(generateTeam());
        final UserRequest thirdUser = generateUserWithTeamIdAndCategory(secondTeam.id(), Category.AMD_GPU);
        thirdUser.setUserIsCaptain(true);
        create(thirdUser);

        final long firstUserPoints = 10_000L;
        final long secondUserPoints = 7_000L;
        final long thirdUserPoints = 15_750L;
        StubbedFoldingEndpointUtils.addPoints(firstUser, firstUserPoints);
        StubbedFoldingEndpointUtils.addPoints(secondUser, secondUserPoints);
        StubbedFoldingEndpointUtils.addPoints(thirdUser, thirdUserPoints);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        assertThat(result.getCompetitionSummary().getTotalPoints())
            .as("Expected points from all three users: " + result)
            .isEqualTo(firstUserPoints + secondUserPoints + thirdUserPoints);

        final TeamSummary firstTeamSummary = getTeamFromCompetition(result, firstTeam.teamName());
        final TeamSummary secondTeamSummary = getTeamFromCompetition(result, secondTeam.teamName());

        assertThat(firstTeamSummary.getTeamPoints())
            .as("Expected points for team for first and second user: " + firstTeamSummary)
            .isEqualTo(firstUserPoints + secondUserPoints);

        assertThat(secondTeamSummary.getTeamPoints())
            .as("Expected no points for team for third user only: " + secondTeamSummary)
            .isEqualTo(thirdUserPoints);

        final UserSummary firstUserSummary = getActiveUserFromTeam(firstTeamSummary, firstUser.getDisplayName());
        final UserSummary secondUserSummary = getActiveUserFromTeam(firstTeamSummary, secondUser.getDisplayName());
        final UserSummary thirdUserSummary = getActiveUserFromTeam(secondTeamSummary, thirdUser.getDisplayName());

        assertThat(firstUserSummary.getPoints())
            .as("Expected points for user: " + firstUserSummary)
            .isEqualTo(firstUserPoints);

        assertThat(secondUserSummary.getPoints())
            .as("Expected points for user: " + secondUserSummary)
            .isEqualTo(secondUserPoints);

        assertThat(thirdUserSummary.getPoints())
            .as("Expected points for user: " + thirdUserSummary)
            .isEqualTo(thirdUserPoints);

        manuallyResetStats();

        final AllTeamsSummary resultAfterReset = TeamCompetitionStatsUtils.getStats();
        assertThat(resultAfterReset.getCompetitionSummary().getTotalPoints())
            .as("Expected no points overall: " + result)
            .isZero();

        final TeamSummary firstTeamSummaryAfterReset = getTeamFromCompetition(resultAfterReset, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterReset = getTeamFromCompetition(resultAfterReset, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterReset.getTeamPoints())
            .as("Expected no points for team: " + firstTeamSummaryAfterReset)
            .isZero();

        assertThat(secondTeamSummaryAfterReset.getTeamPoints())
            .as("Expected no points for team: " + secondTeamSummaryAfterReset)
            .isZero();

        final UserSummary firstUserSummaryAfterReset = getActiveUserFromTeam(firstTeamSummaryAfterReset, firstUser.getDisplayName());
        final UserSummary secondUserSummaryAfterReset = getActiveUserFromTeam(firstTeamSummaryAfterReset, secondUser.getDisplayName());
        final UserSummary thirdUserSummaryAfterReset = getActiveUserFromTeam(secondTeamSummaryAfterReset, thirdUser.getDisplayName());

        assertThat(firstUserSummaryAfterReset.getPoints())
            .as("Expected no points for user: " + firstUserSummaryAfterReset)
            .isZero();

        assertThat(secondUserSummaryAfterReset.getPoints())
            .as("Expected no points for user: " + secondUserSummaryAfterReset)
            .isZero();

        assertThat(thirdUserSummaryAfterReset.getPoints())
            .as("Expected no points for user: " + thirdUserSummaryAfterReset)
            .isZero();
    }
}
