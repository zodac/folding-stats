package me.zodac.folding.test;

import static me.zodac.folding.test.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.util.TestGenerator.generateHardware;
import static me.zodac.folding.test.util.TestGenerator.generateTeam;
import static me.zodac.folding.test.util.TestGenerator.generateUserWithCategory;
import static me.zodac.folding.test.util.TestGenerator.nextUserName;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.getTeamFromCompetition;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.manuallyResetStats;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.manuallyUpdateStats;
import static me.zodac.folding.test.util.rest.request.UserUtils.USER_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.UserUtils.create;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.util.rest.request.HardwareUtils;
import me.zodac.folding.test.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils;
import me.zodac.folding.test.util.rest.request.TeamUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for the monthly reset of the <code>Team Competition</code> {@link CompetitionSummary}.
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
    void whenResetOccurs_givenNoAuthentication_thenRequestFails_andResponseHasA401Status() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualReset();
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenResetOccurs_andRetiredStatsExistForTeam_thenRetiredStatsAreRemovedOnReset() throws FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest captainUser = UserRequest.builder()
            .foldingUserName(nextUserName())
            .displayName("displayName")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .userIsCaptain(true)
            .build();
        create(captainUser);

        final UserRequest userToRetire = UserRequest.builder()
            .foldingUserName(nextUserName())
            .displayName("displayName")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final int userToRetireId = create(userToRetire).getId();

        manuallyUpdateStats();

        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.getTeamName());

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

        final CompetitionSummary resultAfterRetirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.getTeamName());

        assertThat(teamSummaryAfterRetirement.getActiveUsers())
            .as("Expected exactly 1 active users after retirement: " + teamSummaryAfterRetirement)
            .hasSize(1);

        assertThat(teamSummaryAfterRetirement.getRetiredUsers())
            .as("Expected exactly 1 retired users after retirement: " + teamSummaryAfterRetirement)
            .hasSize(1);

        manuallyResetStats();

        final CompetitionSummary resultAfterReset = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterReset = getTeamFromCompetition(resultAfterReset, team.getTeamName());

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

        final UserRequest firstUser = generateUserWithCategory(Category.NVIDIA_GPU);
        firstUser.setUserIsCaptain(true);
        firstUser.setTeamId(firstTeam.getId());
        create(firstUser);

        final UserRequest secondUser = generateUserWithCategory(Category.AMD_GPU);
        secondUser.setTeamId(firstTeam.getId());
        create(secondUser);

        final Team secondTeam = TeamUtils.create(generateTeam());
        final UserRequest thirdUser = generateUserWithCategory(Category.AMD_GPU);
        thirdUser.setUserIsCaptain(true);
        thirdUser.setTeamId(secondTeam.getId());
        create(thirdUser);

        final long firstUserPoints = 10_000L;
        final long secondUserPoints = 7_000L;
        final long thirdUserPoints = 15_750L;
        StubbedFoldingEndpointUtils.addPoints(firstUser, firstUserPoints);
        StubbedFoldingEndpointUtils.addPoints(secondUser, secondUserPoints);
        StubbedFoldingEndpointUtils.addPoints(thirdUser, thirdUserPoints);
        manuallyUpdateStats();

        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        assertThat(result.getTotalPoints())
            .as("Expected points from all three users: " + result)
            .isEqualTo(firstUserPoints + secondUserPoints + thirdUserPoints);

        final TeamSummary firstTeamSummary = getTeamFromCompetition(result, firstTeam.getTeamName());
        final TeamSummary secondTeamSummary = getTeamFromCompetition(result, secondTeam.getTeamName());

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

        final CompetitionSummary resultAfterReset = TeamCompetitionStatsUtils.getStats();
        assertThat(resultAfterReset.getTotalPoints())
            .as("Expected no points overall: " + result)
            .isZero();

        final TeamSummary firstTeamSummaryAfterReset = getTeamFromCompetition(resultAfterReset, firstTeam.getTeamName());
        final TeamSummary secondTeamSummaryAfterReset = getTeamFromCompetition(resultAfterReset, secondTeam.getTeamName());

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
