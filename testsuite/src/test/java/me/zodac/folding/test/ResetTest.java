package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;
import me.zodac.folding.test.utils.HardwareUtils;
import me.zodac.folding.test.utils.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.utils.TcStatsUtils;
import me.zodac.folding.test.utils.TeamUtils;
import me.zodac.folding.test.utils.UserUtils;
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
import static me.zodac.folding.test.utils.TcStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.utils.TcStatsUtils.getTeamFromCompetition;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the monthly reset of the <code>Team Competition</code> stats.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResetTest {

    @BeforeAll
    public static void setUp() {
        cleanSystemForComplexTests();
    }

    @Test
    @Order(1)
    public void whenResetOccurs_andNoTeamsExist_thenNoErrorOccurs() {
        final HttpResponse<Void> response = TcStatsUtils.RequestSender.manualReset();
        assertThat(response.statusCode())
                .as("Expected a 200_OK when no teams exist")
                .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    public void whenResetOccurs_andTeamHasRetiredUsers_thenRetiredUsersAreRemovedOnReset() {
        HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE);
        final User captainUser = User.createWithoutId("User1", "User1", "Passkey1", Category.NVIDIA_GPU, 1, "", false);
        final User userToRetire = User.createWithoutId("User2", "User2", "Passkey2", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(captainUser);
        StubbedFoldingEndpointUtils.enableUser(userToRetire);
        final int captainUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(captainUser)).getId();
        final int userToRetireId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(userToRetire)).getId();

        final Team team = Team.createWithoutId("Team1", "", captainUserId, Set.of(captainUserId, userToRetireId), Collections.emptySet());
        final int teamId = TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(team)).getId();

        TcStatsUtils.RequestSender.manualUpdate();
        final CompetitionResult result = TcStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());

        assertThat(teamResult.getActiveUsers())
                .as("Expected exactly 2 active users at start: " + teamResult)
                .hasSize(2);

        assertThat(teamResult.getRetiredUsers())
                .as("Expected no retired users at start: " + teamResult)
                .isEmpty();

        TeamUtils.RequestSender.retireUser(teamId, userToRetireId);
        TcStatsUtils.RequestSender.manualUpdate();
        final CompetitionResult resultAfterRetirement = TcStatsUtils.get();
        final TeamResult teamResultAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.getTeamName());

        assertThat(teamResultAfterRetirement.getActiveUsers())
                .as("Expected exactly 1 active users after retirement: " + teamResultAfterRetirement)
                .hasSize(1);

        assertThat(teamResultAfterRetirement.getRetiredUsers())
                .as("Expected exactly 1 retired users after retirement: " + teamResultAfterRetirement)
                .hasSize(1);

        TcStatsUtils.RequestSender.manualReset();
        TcStatsUtils.RequestSender.manualUpdate();
        final CompetitionResult resultAfterReset = TcStatsUtils.get();
        final TeamResult teamResultAfterReset = getTeamFromCompetition(resultAfterReset, team.getTeamName());

        assertThat(teamResultAfterReset.getActiveUsers())
                .as("Expected exactly 1 active users after reset: " + teamResultAfterReset)
                .hasSize(1);

        assertThat(teamResultAfterReset.getRetiredUsers())
                .as("Expected no retired users after reset: " + teamResultAfterReset)
                .isEmpty();
    }

    @Test
    public void whenResetOccurs_thenStatsAreResetForCompetitionAndTeamsAndUsers() {
        HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE);
        final User firstUser = User.createWithoutId("User3", "User3", "Passkey3", Category.NVIDIA_GPU, 1, "", false);
        final User secondUser = User.createWithoutId("User4", "User4", "Passkey4", Category.AMD_GPU, 1, "", false);
        final User thirdUser = User.createWithoutId("User5", "User5", "Passkey5", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(firstUser);
        StubbedFoldingEndpointUtils.enableUser(secondUser);
        StubbedFoldingEndpointUtils.enableUser(thirdUser);
        final int firstUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(firstUser)).getId();
        final int secondUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(secondUser)).getId();
        final int thirdUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(thirdUser)).getId();

        final Team firstTeam = TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(Team.createWithoutId("Team2", "", firstUserId, Set.of(firstUserId, secondUserId), Collections.emptySet())));
        final Team secondTeam = TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(Team.createWithoutId("Team3", "", thirdUserId, Set.of(thirdUserId), Collections.emptySet())));

        final long firstUserPoints = 10_000L;
        final long secondUserPoints = 7_000L;
        final long thirdUserPoints = 15_750L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, firstUserPoints);
        StubbedFoldingEndpointUtils.setPoints(secondUser, secondUserPoints);
        StubbedFoldingEndpointUtils.setPoints(thirdUser, thirdUserPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult result = TcStatsUtils.get();
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

        TcStatsUtils.RequestSender.manualReset();
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult resultAfterReset = TcStatsUtils.get();
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
    public static void tearDown() {
        cleanSystemForComplexTests();
    }
}
