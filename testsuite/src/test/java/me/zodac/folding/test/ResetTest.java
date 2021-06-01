package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.utils.rest.request.HardwareUtils;
import me.zodac.folding.test.utils.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils;
import me.zodac.folding.test.utils.rest.request.TeamUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestGenerator.generateHardware;
import static me.zodac.folding.test.utils.TestGenerator.generateTeam;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithCategory;
import static me.zodac.folding.test.utils.TestGenerator.nextUserName;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.getTeamFromCompetition;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.manuallyResetStats;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.manuallyUpdateStats;
import static me.zodac.folding.test.utils.rest.request.UserUtils.USER_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.request.UserUtils.create;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the monthly reset of the <code>Team Competition</code> {@link CompetitionResult}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResetTest {

    @BeforeAll
    public static void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    @Order(1)
    public void whenResetOccurs_andNoTeamsExist_thenNoErrorOccurs() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualReset(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Expected a 200_OK when no teams exist")
                .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    public void whenResetOccurs_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualReset();
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenResetOccurs_andRetiredStatsExistForTeam_thenRetiredStatsAreRemovedOnReset() throws FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest captainUser = UserRequest.builder()
                .foldingUserName(nextUserName())
                .displayName("displayName")
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.NVIDIA_GPU.displayName())
                .hardwareId(hardware.getId())
                .teamId(team.getId())
                .userIsCaptain(true)
                .build();
        create(captainUser);

        final UserRequest userToRetire = UserRequest.builder()
                .foldingUserName(nextUserName())
                .displayName("displayName")
                .passkey("DummyPasskey12345678901234567890")
                .category(Category.AMD_GPU.displayName())
                .hardwareId(hardware.getId())
                .teamId(team.getId())
                .build();

        final int userToRetireId = create(userToRetire).getId();

        manuallyUpdateStats();

        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());

        assertThat(teamResult.getActiveUsers())
                .as("Expected exactly 2 active users at start: " + teamResult)
                .hasSize(2);

        assertThat(teamResult.getRetiredUsers())
                .as("Expected no retired users at start: " + teamResult)
                .isEmpty();

        // User must have points or else will not show as 'retired' for the team
        StubbedFoldingEndpointUtils.setPoints(userToRetire, 1_000L);
        manuallyUpdateStats();

        USER_REQUEST_SENDER.delete(userToRetireId, ADMIN_USER.userName(), ADMIN_USER.password());
        manuallyUpdateStats();

        final CompetitionResult resultAfterRetirement = TeamCompetitionStatsUtils.getStats();
        final TeamResult teamResultAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.getTeamName());

        assertThat(teamResultAfterRetirement.getActiveUsers())
                .as("Expected exactly 1 active users after retirement: " + teamResultAfterRetirement)
                .hasSize(1);

        assertThat(teamResultAfterRetirement.getRetiredUsers())
                .as("Expected exactly 1 retired users after retirement: " + teamResultAfterRetirement)
                .hasSize(1);

        manuallyResetStats();
        manuallyUpdateStats();

        final CompetitionResult resultAfterReset = TeamCompetitionStatsUtils.getStats();
        final TeamResult teamResultAfterReset = getTeamFromCompetition(resultAfterReset, team.getTeamName());

        assertThat(teamResultAfterReset.getActiveUsers())
                .as("Expected exactly 1 active users after reset: " + teamResultAfterReset)
                .hasSize(1);

        assertThat(teamResultAfterReset.getRetiredUsers())
                .as("Expected no retired users after reset: " + teamResultAfterReset)
                .isEmpty();
    }

    @Test
    public void whenResetOccurs_thenStatsAreResetForCompetitionAndTeamsAndUsers() throws FoldingRestException {
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
        StubbedFoldingEndpointUtils.setPoints(firstUser, firstUserPoints);
        StubbedFoldingEndpointUtils.setPoints(secondUser, secondUserPoints);
        StubbedFoldingEndpointUtils.setPoints(thirdUser, thirdUserPoints);
        manuallyUpdateStats();

        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
        assertThat(result.getTotalPoints())
                .as("Expected points from all three users: " + result)
                .isEqualTo(firstUserPoints + secondUserPoints + thirdUserPoints);

        final TeamResult firstTeamResult = getTeamFromCompetition(result, firstTeam.getTeamName());
        final TeamResult secondTeamResult = getTeamFromCompetition(result, secondTeam.getTeamName());

        assertThat(firstTeamResult.getTeamPoints())
                .as("Expected points for team for first and second user: " + firstTeamResult)
                .isEqualTo(firstUserPoints + secondUserPoints);

        assertThat(secondTeamResult.getTeamPoints())
                .as("Expected no points for team for third user only: " + secondTeamResult)
                .isEqualTo(thirdUserPoints);

        final UserResult firstUserResult = getActiveUserFromTeam(firstTeamResult, firstUser.getDisplayName());
        final UserResult secondUserResult = getActiveUserFromTeam(firstTeamResult, secondUser.getDisplayName());
        final UserResult thirdUserResult = getActiveUserFromTeam(secondTeamResult, thirdUser.getDisplayName());

        assertThat(firstUserResult.getPoints())
                .as("Expected points for user: " + firstUserResult)
                .isEqualTo(firstUserPoints);

        assertThat(secondUserResult.getPoints())
                .as("Expected points for user: " + secondUserResult)
                .isEqualTo(secondUserPoints);

        assertThat(thirdUserResult.getPoints())
                .as("Expected points for user: " + thirdUserResult)
                .isEqualTo(thirdUserPoints);

        manuallyResetStats();
        manuallyUpdateStats();

        final CompetitionResult resultAfterReset = TeamCompetitionStatsUtils.getStats();
        assertThat(resultAfterReset.getTotalPoints())
                .as("Expected no points overall: " + result)
                .isEqualTo(0L);

        final TeamResult firstTeamResultAfterReset = getTeamFromCompetition(resultAfterReset, firstTeam.getTeamName());
        final TeamResult secondTeamResultAfterReset = getTeamFromCompetition(resultAfterReset, secondTeam.getTeamName());

        assertThat(firstTeamResultAfterReset.getTeamPoints())
                .as("Expected no points for team: " + firstTeamResultAfterReset)
                .isEqualTo(0L);

        assertThat(secondTeamResultAfterReset.getTeamPoints())
                .as("Expected no points for team: " + secondTeamResultAfterReset)
                .isEqualTo(0L);

        final UserResult firstUserResultAfterReset = getActiveUserFromTeam(firstTeamResultAfterReset, firstUser.getDisplayName());
        final UserResult secondUserResultAfterReset = getActiveUserFromTeam(firstTeamResultAfterReset, secondUser.getDisplayName());
        final UserResult thirdUserResultAfterReset = getActiveUserFromTeam(secondTeamResultAfterReset, thirdUser.getDisplayName());

        assertThat(firstUserResultAfterReset.getPoints())
                .as("Expected no points for user: " + firstUserResultAfterReset)
                .isEqualTo(0L);

        assertThat(secondUserResultAfterReset.getPoints())
                .as("Expected no points for user: " + secondUserResultAfterReset)
                .isEqualTo(0L);

        assertThat(thirdUserResultAfterReset.getPoints())
                .as("Expected no points for user: " + thirdUserResultAfterReset)
                .isEqualTo(0L);
    }

    @AfterAll
    public static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }
}
