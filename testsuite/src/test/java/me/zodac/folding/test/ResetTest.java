package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;
import me.zodac.folding.test.utils.HardwareUtils;
import me.zodac.folding.test.utils.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.utils.TeamCompetitionStatsUtils;
import me.zodac.folding.test.utils.TeamUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Set;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.utils.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.utils.TeamCompetitionStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.utils.TeamCompetitionStatsUtils.getTeamFromCompetition;
import static me.zodac.folding.test.utils.TeamUtils.TEAM_REQUEST_SENDER;
import static me.zodac.folding.test.utils.UserUtils.createOrConflict;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the monthly reset of the <code>Team Competition</code> stats.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResetTest {

    @BeforeAll
    public static void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
        HardwareUtils.createOrConflict(HardwareTest.DUMMY_HARDWARE);
    }

    @Test
    @Order(1)
    public void whenResetOccurs_andNoTeamsExist_thenNoErrorOccurs() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualReset();
        assertThat(response.statusCode())
                .as("Expected a 200_OK when no teams exist")
                .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    public void whenResetOccurs_andTeamHasRetiredUsers_thenRetiredUsersAreRemovedOnReset() throws FoldingRestException {
        final User captainUser = User.createWithoutId("User1", "User1", "Passkey1", Category.NVIDIA_GPU, 1, "", false);
        final User userToRetire = User.createWithoutId("User2", "User2", "Passkey2", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(captainUser);
        StubbedFoldingEndpointUtils.enableUser(userToRetire);
        final int captainUserId = createOrConflict(captainUser).getId();
        final int userToRetireId = createOrConflict(userToRetire).getId();

        final Team team = Team.createWithoutId("Team1", "", captainUserId, Set.of(captainUserId, userToRetireId), Collections.emptySet());
        final int teamId = TeamUtils.createOrConflict(team).getId();

        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();
        final CompetitionResult result = TeamCompetitionStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());

        assertThat(teamResult.getActiveUsers())
                .as("Expected exactly 2 active users at start: " + teamResult)
                .hasSize(2);

        assertThat(teamResult.getRetiredUsers())
                .as("Expected no retired users at start: " + teamResult)
                .isEmpty();

        TEAM_REQUEST_SENDER.retireUser(teamId, userToRetireId);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();
        final CompetitionResult resultAfterRetirement = TeamCompetitionStatsUtils.get();
        final TeamResult teamResultAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.getTeamName());

        assertThat(teamResultAfterRetirement.getActiveUsers())
                .as("Expected exactly 1 active users after retirement: " + teamResultAfterRetirement)
                .hasSize(1);

        assertThat(teamResultAfterRetirement.getRetiredUsers())
                .as("Expected exactly 1 retired users after retirement: " + teamResultAfterRetirement)
                .hasSize(1);

        TEAM_COMPETITION_REQUEST_SENDER.manualReset();
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();
        final CompetitionResult resultAfterReset = TeamCompetitionStatsUtils.get();
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
        final User firstUser = User.createWithoutId("User3", "User3", "Passkey3", Category.NVIDIA_GPU, 1, "", false);
        final User secondUser = User.createWithoutId("User4", "User4", "Passkey4", Category.AMD_GPU, 1, "", false);
        final User thirdUser = User.createWithoutId("User5", "User5", "Passkey5", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(firstUser);
        StubbedFoldingEndpointUtils.enableUser(secondUser);
        StubbedFoldingEndpointUtils.enableUser(thirdUser);
        final int firstUserId = createOrConflict(firstUser).getId();
        final int secondUserId = createOrConflict(secondUser).getId();
        final int thirdUserId = createOrConflict(thirdUser).getId();

        final Team firstTeam = TeamUtils.createOrConflict(Team.createWithoutId("Team2", "", firstUserId, Set.of(firstUserId, secondUserId), Collections.emptySet()));
        final Team secondTeam = TeamUtils.createOrConflict(Team.createWithoutId("Team3", "", thirdUserId, Set.of(thirdUserId), Collections.emptySet()));

        final long firstUserPoints = 10_000L;
        final long secondUserPoints = 7_000L;
        final long thirdUserPoints = 15_750L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, firstUserPoints);
        StubbedFoldingEndpointUtils.setPoints(secondUser, secondUserPoints);
        StubbedFoldingEndpointUtils.setPoints(thirdUser, thirdUserPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
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

        TEAM_COMPETITION_REQUEST_SENDER.manualReset();
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterReset = TeamCompetitionStatsUtils.get();
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
