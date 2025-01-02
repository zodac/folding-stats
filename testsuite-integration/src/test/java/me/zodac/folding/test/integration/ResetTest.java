/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

package me.zodac.folding.test.integration;

import static me.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateCaptainWithTeamIdAndCategory;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateHardwareFromCategory;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateTeam;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateUserWithTeamIdAndCategory;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.nextUserName;
import static me.zodac.folding.test.integration.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.integration.util.TestConstants.FOLDING_URL;
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
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenResetOccurs_andRetiredStatsExistForTeam_thenRetiredStatsAreRemovedOnReset() throws FoldingRestException {
        final Hardware captainHardware = HardwareUtils.create(generateHardwareFromCategory(Category.NVIDIA_GPU));
        final Hardware hardware = HardwareUtils.create(generateHardwareFromCategory(Category.AMD_GPU));
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest captainUser = new UserRequest(
            nextUserName(),
            "displayName",
            "DummyPasskey12345678901234567890",
            Category.NVIDIA_GPU.toString(),
            null,
            null,
            captainHardware.id(),
            team.id(),
            true
        );
        create(captainUser);

        final UserRequest userToRetire = new UserRequest(
            nextUserName(),
            "displayName",
            "DummyPasskey12345678901234567890",
            Category.AMD_GPU.toString(),
            null,
            null,
            hardware.id(),
            team.id(),
            false
        );

        final int userToRetireId = create(userToRetire).id();

        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());

        assertThat(teamSummary.activeUsers())
            .as("Expected exactly 2 active users at start: %s", teamSummary)
            .hasSize(2);

        assertThat(teamSummary.retiredUsers())
            .as("Expected no retired users at start: %s", teamSummary)
            .isEmpty();

        // User must have points or else will not show as 'retired' for the team
        StubbedFoldingEndpointUtils.addPoints(userToRetire, 1_000L);
        manuallyUpdateStats();

        USER_REQUEST_SENDER.delete(userToRetireId, ADMIN_USER.userName(), ADMIN_USER.password());
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterRetirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.teamName());

        assertThat(teamSummaryAfterRetirement.activeUsers())
            .as("Expected exactly 1 active users after retirement: %s", teamSummaryAfterRetirement)
            .hasSize(1);

        assertThat(teamSummaryAfterRetirement.retiredUsers())
            .as("Expected exactly 1 retired users after retirement: %s", teamSummaryAfterRetirement)
            .hasSize(1);

        manuallyResetStats();

        final AllTeamsSummary resultAfterReset = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterReset = getTeamFromCompetition(resultAfterReset, team.teamName());

        assertThat(teamSummaryAfterReset.activeUsers())
            .as("Expected exactly 1 active users after reset: %s", teamSummaryAfterReset)
            .hasSize(1);

        assertThat(teamSummaryAfterReset.retiredUsers())
            .as("Expected no retired users after reset: %s", teamSummaryAfterReset)
            .isEmpty();
    }

    @Test
    void whenResetOccurs_thenStatsAreResetForCompetitionAndTeamsAndUsers() throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());

        final UserRequest firstUser = generateCaptainWithTeamIdAndCategory(firstTeam.id(), Category.NVIDIA_GPU);
        create(firstUser);

        final UserRequest secondUser = generateUserWithTeamIdAndCategory(firstTeam.id(), Category.AMD_GPU);
        create(secondUser);

        final Team secondTeam = TeamUtils.create(generateTeam());
        final UserRequest thirdUser = generateCaptainWithTeamIdAndCategory(secondTeam.id(), Category.AMD_GPU);
        create(thirdUser);

        final long firstUserPoints = 10_000L;
        final long secondUserPoints = 7_000L;
        final long thirdUserPoints = 15_750L;
        StubbedFoldingEndpointUtils.addPoints(firstUser, firstUserPoints);
        StubbedFoldingEndpointUtils.addPoints(secondUser, secondUserPoints);
        StubbedFoldingEndpointUtils.addPoints(thirdUser, thirdUserPoints);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        assertThat(result.competitionSummary().totalPoints())
            .as("Expected points from all three users: %s", result)
            .isEqualTo(firstUserPoints + secondUserPoints + thirdUserPoints);

        final TeamSummary firstTeamSummary = getTeamFromCompetition(result, firstTeam.teamName());
        final TeamSummary secondTeamSummary = getTeamFromCompetition(result, secondTeam.teamName());

        assertThat(firstTeamSummary.teamPoints())
            .as("Expected points for team for first and second user: %s", firstTeamSummary)
            .isEqualTo(firstUserPoints + secondUserPoints);

        assertThat(secondTeamSummary.teamPoints())
            .as("Expected no points for team for third user only: %s", secondTeamSummary)
            .isEqualTo(thirdUserPoints);

        final UserSummary firstUserSummary = getActiveUserFromTeam(firstTeamSummary, firstUser.displayName());
        final UserSummary secondUserSummary = getActiveUserFromTeam(firstTeamSummary, secondUser.displayName());
        final UserSummary thirdUserSummary = getActiveUserFromTeam(secondTeamSummary, thirdUser.displayName());

        assertThat(firstUserSummary.points())
            .as("Expected points for user: %s", firstUserSummary)
            .isEqualTo(firstUserPoints);

        assertThat(secondUserSummary.points())
            .as("Expected points for user: %s", secondUserSummary)
            .isEqualTo(secondUserPoints);

        assertThat(thirdUserSummary.points())
            .as("Expected points for user: %s", thirdUserSummary)
            .isEqualTo(thirdUserPoints);

        manuallyResetStats();

        final AllTeamsSummary resultAfterReset = TeamCompetitionStatsUtils.getStats();
        assertThat(resultAfterReset.competitionSummary().totalPoints())
            .as("Expected no points in summary: %s", result)
            .isZero();

        final TeamSummary firstTeamSummaryAfterReset = getTeamFromCompetition(resultAfterReset, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterReset = getTeamFromCompetition(resultAfterReset, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterReset.teamPoints())
            .as("Expected no points for team: %s", firstTeamSummaryAfterReset)
            .isZero();

        assertThat(secondTeamSummaryAfterReset.teamPoints())
            .as("Expected no points for team: %s", secondTeamSummaryAfterReset)
            .isZero();

        final UserSummary firstUserSummaryAfterReset = getActiveUserFromTeam(firstTeamSummaryAfterReset, firstUser.displayName());
        final UserSummary secondUserSummaryAfterReset = getActiveUserFromTeam(firstTeamSummaryAfterReset, secondUser.displayName());
        final UserSummary thirdUserSummaryAfterReset = getActiveUserFromTeam(secondTeamSummaryAfterReset, thirdUser.displayName());

        assertThat(firstUserSummaryAfterReset.points())
            .as("Expected no points for user: %s", firstUserSummaryAfterReset)
            .isZero();

        assertThat(secondUserSummaryAfterReset.points())
            .as("Expected no points for user: %s", secondUserSummaryAfterReset)
            .isZero();

        assertThat(thirdUserSummaryAfterReset.points())
            .as("Expected no points for user: %s", thirdUserSummaryAfterReset)
            .isZero();
    }
}
