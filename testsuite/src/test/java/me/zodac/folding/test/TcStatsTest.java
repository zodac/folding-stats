package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.utils.TcStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.utils.TcStatsUtils.getRetiredUserFromTeam;
import static me.zodac.folding.test.utils.TcStatsUtils.getTeamFromCompetition;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the <code>Team Competition</code> stats calculation.
 * <p>
 * Since the TC stats are done on the full system (meaning all {@link Team}s), we wipe the system before each test with a {@link BeforeEach} method.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TcStatsTest {

    @BeforeEach
    public void setUp() {
        cleanSystemForComplexTests();
    }

    @Test
    @Order(1)
    public void whenNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoStats_andNoTeams() {
        final HttpResponse<String> response = TcStatsUtils.RequestSender.get();

        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionResult result = TcStatsUtils.ResponseParser.get(response);

        assertThat(result.getTeams())
                .as("Expected no teams: " + result)
                .isEmpty();

        assertThat(result.getTotalPoints())
                .as("Expected no points: " + result)
                .isEqualTo(0L);

        assertThat(result.getTotalMultipliedPoints())
                .as("Expected no multiplied points: " + result)
                .isEqualTo(0L);

        assertThat(result.getTotalUnits())
                .as("Expected no units: " + result)
                .isEqualTo(0);
    }

    @Test
    public void whenOneTeamExistsWithOneUser_andUserEarnsStats_thenUserAndTeamAndOverallStartWithNoStats_thenAllIncrementAsUserPointsIncrease() {
        HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE);
        final User user = User.createWithoutId("User1", "User1", "Passkey1", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(user);
        final int userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(user)).getId();

        final Team team = Team.createWithoutId("Team1", "", userId, Set.of(userId), Collections.emptySet());
        TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(team));

        final CompetitionResult resultBeforeStats = TcStatsUtils.get();

        assertThat(resultBeforeStats.getTotalPoints())
                .as("Expected no points: " + resultBeforeStats)
                .isEqualTo(0L);

        assertThat(resultBeforeStats.getTotalMultipliedPoints())
                .as("Expected no multiplied points: " + resultBeforeStats)
                .isEqualTo(0L);

        assertThat(resultBeforeStats.getTotalUnits())
                .as("Expected no units: " + resultBeforeStats)
                .isEqualTo(0);

        assertThat(resultBeforeStats.getTeams())
                .as("Expected exactly 1 team: " + resultBeforeStats)
                .hasSize(1);

        final TeamResult teamResultBeforeStats = getTeamFromCompetition(resultBeforeStats, team.getTeamName());

        assertThat(teamResultBeforeStats.getTeamPoints())
                .as("Expected no points for team: " + teamResultBeforeStats)
                .isEqualTo(0L);

        assertThat(teamResultBeforeStats.getTeamMultipliedPoints())
                .as("Expected no multiplied points for team: " + teamResultBeforeStats)
                .isEqualTo(0L);

        assertThat(teamResultBeforeStats.getTeamUnits())
                .as("Expected no units for team: " + teamResultBeforeStats)
                .isEqualTo(0);

        assertThat(teamResultBeforeStats.getActiveUsers())
                .as("Expected exactly 1 active user: " + teamResultBeforeStats)
                .hasSize(1);

        assertThat(teamResultBeforeStats.getRetiredUsers())
                .as("Expected no retired users: " + teamResultBeforeStats)
                .isEmpty();

        final UserResult userResultBeforeStats = getActiveUserFromTeam(teamResultBeforeStats, user.getDisplayName());

        assertThat(userResultBeforeStats.getPoints())
                .as("Expected no points for user: " + userResultBeforeStats)
                .isEqualTo(0L);

        assertThat(userResultBeforeStats.getMultipliedPoints())
                .as("Expected no multiplied points for user: " + userResultBeforeStats)
                .isEqualTo(0L);

        assertThat(userResultBeforeStats.getUnits())
                .as("Expected no units for user: " + userResultBeforeStats)
                .isEqualTo(0);

        final long newPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(user, newPoints);

        final int newUnits = 5;
        StubbedFoldingEndpointUtils.setUnits(user, newUnits);

        final HttpResponse<Void> response = TcStatsUtils.RequestSender.manualUpdate();
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionResult resultAfterStats = TcStatsUtils.get();

        assertThat(resultAfterStats.getTotalPoints())
                .as("Expected updated points: " + resultAfterStats)
                .isEqualTo(newPoints);

        assertThat(resultAfterStats.getTotalMultipliedPoints())
                .as("Expected updated multiplied points: " + resultAfterStats)
                .isEqualTo(newPoints);

        assertThat(resultAfterStats.getTotalUnits())
                .as("Expected updated units: " + resultAfterStats)
                .isEqualTo(newUnits);

        assertThat(resultAfterStats.getTeams())
                .as("Expected exactly 1 team: " + resultAfterStats)
                .hasSize(1);

        final TeamResult teamResultAfterStats = getTeamFromCompetition(resultAfterStats, team.getTeamName());

        assertThat(teamResultAfterStats.getTeamPoints())
                .as("Expected updated points for team: " + teamResultAfterStats)
                .isEqualTo(newPoints);

        assertThat(teamResultAfterStats.getTeamMultipliedPoints())
                .as("Expected updated multiplied points for team: " + teamResultAfterStats)
                .isEqualTo(newPoints);

        assertThat(teamResultAfterStats.getTeamUnits())
                .as("Expected updated units for team: " + teamResultAfterStats)
                .isEqualTo(newUnits);

        assertThat(teamResultAfterStats.getActiveUsers())
                .as("Expected exactly 1 active user: " + teamResultAfterStats)
                .hasSize(1);

        assertThat(teamResultAfterStats.getRetiredUsers())
                .as("Expected no retired users: " + teamResultAfterStats)
                .isEmpty();

        final UserResult userResultAfterStats = getActiveUserFromTeam(teamResultAfterStats, user.getDisplayName());

        assertThat(userResultAfterStats.getPoints())
                .as("Expected updated points for user: " + userResultAfterStats)
                .isEqualTo(newPoints);

        assertThat(userResultAfterStats.getMultipliedPoints())
                .as("Expected updated multiplied points for user: " + userResultAfterStats)
                .isEqualTo(newPoints);

        assertThat(userResultAfterStats.getUnits())
                .as("Expected updated units for user: " + userResultAfterStats)
                .isEqualTo(newUnits);
    }

    @Test
    public void whenOneTeamExistsWithTwoUser_andUserEarnsStats_thenBothUsersStartAtRank1_thenRanksUpdateCorrectlyAsUsersEarnStats() {
        HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE);
        final User firstUser = User.createWithoutId("User2", "User2", "Passkey2", Category.NVIDIA_GPU, 1, "", false);
        final User secondUser = User.createWithoutId("User3", "User3", "Passkey3", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(firstUser);
        StubbedFoldingEndpointUtils.enableUser(secondUser);
        final int firstUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(firstUser)).getId();
        final int secondUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(secondUser)).getId();

        final Team team = Team.createWithoutId("Team2", "", firstUserId, Set.of(firstUserId, secondUserId), Collections.emptySet());
        TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(team));

        final CompetitionResult result = TcStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final List<UserResult> activeUserResults = teamResult.getActiveUsers();

        assertThat(activeUserResults)
                .as("Expected exactly 2 active users: " + teamResult)
                .hasSize(2);

        final UserResult firstUserResult = getActiveUserFromTeam(teamResult, firstUser.getDisplayName());
        final UserResult secondUserResult = getActiveUserFromTeam(teamResult, secondUser.getDisplayName());

        assertThat(firstUserResult.getRankInTeam())
                .as("Expected first user to be rank 1: " + firstUserResult)
                .isEqualTo(1);

        assertThat(secondUserResult.getRankInTeam())
                .as("Expected second user to be rank 1: " + secondUserResult)
                .isEqualTo(1);

        StubbedFoldingEndpointUtils.setPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.setUnits(firstUser, 10);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult resultAfterFirstUpdate = TcStatsUtils.get();
        final TeamResult teamResultAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, team.getTeamName());
        final UserResult firstUserResultAfterFirstUpdate = getActiveUserFromTeam(teamResultAfterFirstUpdate, firstUser.getDisplayName());
        final UserResult secondUserResultAfterFirstUpdate = getActiveUserFromTeam(teamResultAfterFirstUpdate, secondUser.getDisplayName());

        assertThat(firstUserResultAfterFirstUpdate.getRankInTeam())
                .as("Expected first user to be rank 1: " + firstUserResultAfterFirstUpdate)
                .isEqualTo(1);

        assertThat(secondUserResultAfterFirstUpdate.getRankInTeam())
                .as("second first user to be rank 2: " + secondUserResultAfterFirstUpdate)
                .isEqualTo(2);

        StubbedFoldingEndpointUtils.setPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.setUnits(secondUser, 20);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult resultAfterSecondUpdate = TcStatsUtils.get();
        final TeamResult teamResultAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, team.getTeamName());
        final UserResult firstUserResultAfterSecondUpdate = getActiveUserFromTeam(teamResultAfterSecondUpdate, firstUser.getDisplayName());
        final UserResult secondUserResultAfterSecondUpdate = getActiveUserFromTeam(teamResultAfterSecondUpdate, secondUser.getDisplayName());

        assertThat(firstUserResultAfterSecondUpdate.getRankInTeam())
                .as("Expected first user to be rank 2: " + firstUserResultAfterSecondUpdate)
                .isEqualTo(2);

        assertThat(secondUserResultAfterSecondUpdate.getRankInTeam())
                .as("second first user to be rank 1: " + secondUserResultAfterSecondUpdate)
                .isEqualTo(1);
    }

    @Test
    public void whenTwoTeamsExistsWithOneUserEach_andUserEarnsStats_thenBothTeamsStartAtRank1_thenTeamRanksUpdateCorrectlyAsUsersEarnStats() {
        HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE);

        final User firstUser = User.createWithoutId("User4", "User4", "Passkey4", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(firstUser);
        final int firstUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(firstUser)).getId();
        final Team firstTeam = Team.createWithoutId("Team3", "", firstUserId, Set.of(firstUserId), Collections.emptySet());
        TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(firstTeam));

        final User secondUser = User.createWithoutId("User5", "User5", "Passkey5", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(secondUser);
        final int secondUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(secondUser)).getId();
        final Team secondTeam = Team.createWithoutId("Team4", "", secondUserId, Set.of(secondUserId), Collections.emptySet());
        TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(secondTeam));

        final CompetitionResult result = TcStatsUtils.get();
        assertThat(result.getTeams())
                .as("Expected exactly 2 teams: " + result)
                .hasSize(2);

        final TeamResult firstTeamResult = getTeamFromCompetition(result, firstTeam.getTeamName());
        final TeamResult secondTeamResult = getTeamFromCompetition(result, secondTeam.getTeamName());

        assertThat(firstTeamResult.getRank())
                .as("Expected first team to be rank 1: " + firstTeamResult)
                .isEqualTo(1);

        assertThat(secondTeamResult.getRank())
                .as("Expected second team to be rank 1: " + secondTeamResult)
                .isEqualTo(1);

        StubbedFoldingEndpointUtils.setPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.setUnits(firstUser, 10);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult resultAfterFirstUpdate = TcStatsUtils.get();
        final TeamResult firstTeamResultAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, firstTeam.getTeamName());
        final TeamResult secondTeamResultAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, secondTeam.getTeamName());

        assertThat(firstTeamResultAfterFirstUpdate.getRank())
                .as("Expected first team to be rank 1: " + firstTeamResultAfterFirstUpdate)
                .isEqualTo(1);

        assertThat(secondTeamResultAfterFirstUpdate.getRank())
                .as("Expected second team to be rank 2: " + secondTeamResultAfterFirstUpdate)
                .isEqualTo(2);

        StubbedFoldingEndpointUtils.setPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.setUnits(secondUser, 20);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult resultAfterSecondUpdate = TcStatsUtils.get();
        final TeamResult firstTeamResultAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, firstTeam.getTeamName());
        final TeamResult secondTeamResultAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, secondTeam.getTeamName());

        assertThat(firstTeamResultAfterSecondUpdate.getRank())
                .as("Expected first team to be rank 2: " + firstTeamResultAfterSecondUpdate)
                .isEqualTo(2);

        assertThat(secondTeamResultAfterSecondUpdate.getRank())
                .as("Expected second team to be rank 1: " + secondTeamResultAfterSecondUpdate)
                .isEqualTo(1);
    }

    @Test
    public void whenTeamExistsWithOneUser_andUserHasAHardwareMultiplier_thenUserPointsAreMultipliedCorrectly_andUserUnitsAreNotImpacted() {
        final Hardware hardware = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(Hardware.createWithoutId("HardwareWithMultiplier", "Hardware With Multiplier", OperatingSystem.WINDOWS, 2.0D)));
        final User user = User.createWithoutId("User6", "User6", "Passkey6", Category.NVIDIA_GPU, hardware.getId(), "", false);
        StubbedFoldingEndpointUtils.enableUser(user);
        final int userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(user)).getId();

        final Team team = Team.createWithoutId("Team5", "", userId, Set.of(userId), Collections.emptySet());
        TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(team));

        final long newPoints = 20_000L;
        final int newUnits = 20;
        StubbedFoldingEndpointUtils.setPoints(user, newPoints);
        StubbedFoldingEndpointUtils.setUnits(user, newUnits);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult result = TcStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final UserResult userResult = getActiveUserFromTeam(teamResult, user.getDisplayName());


        assertThat(userResult.getMultipliedPoints())
                .as("Expected user multiplied points to be new points * hardware multiplier: " + userResult)
                .isEqualTo(Math.round(newPoints * hardware.getMultiplier()));

        assertThat(userResult.getPoints())
                .as("Expected user points to not be multiplied: " + userResult)
                .isEqualTo(newPoints);

        assertThat(userResult.getUnits())
                .as("Expected user units to not be multiplied: " + userResult)
                .isEqualTo(newUnits);
    }

    @Test
    public void whenTeamExistsWithOneUser_andUserIsUpdatedWithANewHardwareMultiplier_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly() {
        final Hardware hardware = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(Hardware.createWithoutId("HardwareWithNoMultiplier", "Hardware With No Multiplier", OperatingSystem.WINDOWS, 1.0D)));
        final User user = User.createWithoutId("User7", "User7", "Passkey7", Category.NVIDIA_GPU, hardware.getId(), "", false);
        StubbedFoldingEndpointUtils.enableUser(user);
        final int userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(user)).getId();
        final Team team = Team.createWithoutId("Team6", "", userId, Set.of(userId), Collections.emptySet());
        TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(team));

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(user, firstPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult result = TcStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final UserResult userResult = getActiveUserFromTeam(teamResult, user.getDisplayName());

        assertThat(userResult.getMultipliedPoints())
                .as("Expected user multiplied points to not be multiplied: " + userResult)
                .isEqualTo(firstPoints);

        assertThat(userResult.getPoints())
                .as("Expected user points to not be multiplied: " + userResult)
                .isEqualTo(firstPoints);

        // Change the multiplier on the hardware, no need to update the user
        hardware.setMultiplier(2.0D);
        HardwareUtils.RequestSender.update(hardware);

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.setPoints(user, secondPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult resultAfterUpdate = TcStatsUtils.get();
        final TeamResult teamResultAfterUpdate = getTeamFromCompetition(resultAfterUpdate, team.getTeamName());
        final UserResult userResultAfterUpdate = getActiveUserFromTeam(teamResultAfterUpdate, user.getDisplayName());

        assertThat(userResultAfterUpdate.getMultipliedPoints())
                .as("Expected user multiplied points to be multiplied only after the second update: " + userResultAfterUpdate)
                .isEqualTo(firstPoints + (Math.round(secondPoints * hardware.getMultiplier())));

        assertThat(userResultAfterUpdate.getPoints())
                .as("Expected user points to not be multiplied: " + userResultAfterUpdate)
                .isEqualTo(firstPoints + secondPoints);
    }

    @Test
    public void whenTeamExistsWithTwoUsers_andOneUserRetires_andUserUnretired_thenOriginalStatsAreNotLostFromTeam_andNewStatsWhileRetiredAreNotAddedToTeam_andStatsAfterUnretirementAreCounted() {
        final Hardware hardware = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE));
        final User firstUser = User.createWithoutId("User8", "User8", "Passkey8", Category.NVIDIA_GPU, hardware.getId(), "", false);
        final User secondUser = User.createWithoutId("User9", "User9", "Passkey9", Category.AMD_GPU, hardware.getId(), "", false);
        StubbedFoldingEndpointUtils.enableUser(firstUser);
        StubbedFoldingEndpointUtils.enableUser(secondUser);
        final int firstUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(firstUser)).getId();
        final int secondUserId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(secondUser)).getId();

        final Team team = Team.createWithoutId("Team7", "", firstUserId, Set.of(firstUserId, secondUserId), Collections.emptySet());
        final int teamId = TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(team)).getId();

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, firstPoints);
        StubbedFoldingEndpointUtils.setPoints(secondUser, firstPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult result = TcStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());

        assertThat(teamResult.getActiveUsers())
                .as("Expected team to have 2 active users for first update: " + teamResult)
                .hasSize(2);

        assertThat(teamResult.getRetiredUsers())
                .as("Expected team to have no retired user for first update: " + teamResult)
                .isEmpty();

        final int retiredUserId = TeamUtils.ResponseParser.retireUser(TeamUtils.RequestSender.retireUser(teamId, secondUserId)).getRetiredUserIds().iterator().next();
        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, secondPoints);
        StubbedFoldingEndpointUtils.setPoints(secondUser, secondPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult resultAfterRetirement = TcStatsUtils.get();
        final TeamResult teamResultAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.getTeamName());

        assertThat(teamResultAfterRetirement.getTeamMultipliedPoints())
                .as("Expected team to have points from first update for both users, then second update only for the first user: " + teamResultAfterRetirement)
                .isEqualTo(firstPoints + firstPoints + secondPoints);

        assertThat(teamResultAfterRetirement.getActiveUsers())
                .as("Expected team to have only 1 active user after other user was retired: " + teamResultAfterRetirement)
                .hasSize(1);

        assertThat(teamResultAfterRetirement.getRetiredUsers())
                .as("Expected team to have 1 retired user after user was retired: " + teamResultAfterRetirement)
                .hasSize(1);

        final UserResult firstUserResult = getActiveUserFromTeam(teamResultAfterRetirement, firstUser.getDisplayName());
        final UserResult secondUserResult = getRetiredUserFromTeam(teamResultAfterRetirement, secondUser.getDisplayName());

        assertThat(firstUserResult.getMultipliedPoints())
                .as("Expected user to have points from both updates: " + firstUserResult)
                .isEqualTo(firstPoints + secondPoints);

        assertThat(secondUserResult.getMultipliedPoints())
                .as("Expected user to have points from first update only: " + secondUserResult)
                .isEqualTo(firstPoints);

        TeamUtils.RequestSender.unretireUser(teamId, retiredUserId);
        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, thirdPoints);
        StubbedFoldingEndpointUtils.setPoints(secondUser, thirdPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult resultAfterUnretirement = TcStatsUtils.get();
        final TeamResult teamResultAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, team.getTeamName());

        assertThat(teamResultAfterUnretirement.getTeamMultipliedPoints())
                .as("Expected team to have points from first update for both users, second update only for the first user, then third update for both users: " + teamResultAfterUnretirement)
                .isEqualTo(firstPoints + firstPoints + secondPoints + thirdPoints + thirdPoints);

        assertThat(teamResultAfterUnretirement.getActiveUsers())
                .as("Expected team to have 2 active users after unretirement: " + teamResultAfterUnretirement)
                .hasSize(2);

        assertThat(teamResultAfterUnretirement.getRetiredUsers())
                .as("Expected team to have no retired users after user was unretired: " + teamResultAfterUnretirement)
                .isEmpty();

        final UserResult firstUserResultAfterUnretirement = getActiveUserFromTeam(teamResultAfterUnretirement, firstUser.getDisplayName());
        final UserResult secondUserResultAfterUnretirement = getActiveUserFromTeam(teamResultAfterUnretirement, secondUser.getDisplayName());

        assertThat(firstUserResultAfterUnretirement.getMultipliedPoints())
                .as("Expected user to have points from all three updates: " + firstUserResultAfterUnretirement)
                .isEqualTo(firstPoints + secondPoints + thirdPoints);

        assertThat(secondUserResultAfterUnretirement.getMultipliedPoints())
                .as("Expected user to have points from first and third updates only: " + secondUserResultAfterUnretirement)
                .isEqualTo(firstPoints + thirdPoints);
    }

    @Test
    public void whenTeamExistsWithTwoUsers_andOneUserRetires_andUserUnretiredToANewTeam_thenOriginalStatsAreNotLostFromOriginalTeam_andNewTeamGetsStatsAfterUnretirement_andStatsDuringRetirementAreNotCounted() {
        final Hardware hardware = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE));
        final User originalTeamCaptain = User.createWithoutId("User10", "User10", "Passkey10", Category.NVIDIA_GPU, hardware.getId(), "", false);
        final User userToRetire = User.createWithoutId("User11", "User11", "Passkey11", Category.AMD_GPU, hardware.getId(), "", false);
        StubbedFoldingEndpointUtils.enableUser(originalTeamCaptain);
        StubbedFoldingEndpointUtils.enableUser(userToRetire);
        final int originalTeamCaptainId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(originalTeamCaptain)).getId();
        final int userToRetireId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(userToRetire)).getId();

        final Team originalTeam = Team.createWithoutId("Team8", "", originalTeamCaptainId, Set.of(originalTeamCaptainId, userToRetireId), Collections.emptySet());
        final int originalTeamId = TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(originalTeam)).getId();

        final User newTeamCaptain = User.createWithoutId("User12", "User12", "Passkey12", Category.NVIDIA_GPU, hardware.getId(), "", false);
        StubbedFoldingEndpointUtils.enableUser(newTeamCaptain);
        final int newTeamCaptainId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(newTeamCaptain)).getId();
        final Team newTeam = Team.createWithoutId("Team9", "", newTeamCaptainId, Set.of(newTeamCaptainId), Collections.emptySet());
        final int newTeamId = TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(newTeam)).getId();

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(userToRetire, firstPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult result = TcStatsUtils.get();
        final TeamResult originalTeamResult = getTeamFromCompetition(result, originalTeam.getTeamName());
        final TeamResult newTeamResult = getTeamFromCompetition(result, newTeam.getTeamName());

        assertThat(originalTeamResult.getActiveUsers())
                .as("Expected original team to have 2 active users at the start: " + originalTeamResult)
                .hasSize(2);

        assertThat(originalTeamResult.getRetiredUsers())
                .as("Expected original team to have no retired users at the start: " + originalTeamResult)
                .isEmpty();

        assertThat(newTeamResult.getActiveUsers())
                .as("Expected new team to have 1 active user at the start: " + newTeamResult)
                .hasSize(1);

        assertThat(newTeamResult.getRetiredUsers())
                .as("Expected new team to have no retired users at the start: " + newTeamResult)
                .isEmpty();


        final int retiredUserId = TeamUtils.ResponseParser.retireUser(TeamUtils.RequestSender.retireUser(originalTeamId, userToRetireId)).getRetiredUserIds().iterator().next();

        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.setPoints(userToRetire, secondPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        TeamUtils.RequestSender.unretireUser(newTeamId, retiredUserId);
        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.setPoints(userToRetire, thirdPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult resultAfterUnretirement = TcStatsUtils.get();
        final TeamResult originalTeamResultAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, originalTeam.getTeamName());
        final TeamResult newTeamResultAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, newTeam.getTeamName());

        assertThat(originalTeamResultAfterUnretirement.getActiveUsers())
                .as("Expected original team to have 1 active user after unretirement: " + originalTeamResultAfterUnretirement)
                .hasSize(1);

        assertThat(originalTeamResultAfterUnretirement.getRetiredUsers())
                .as("Expected original team to have 1 retired user after unretirement: " + originalTeamResultAfterUnretirement)
                .hasSize(1);

        assertThat(originalTeamResultAfterUnretirement.getTeamMultipliedPoints())
                .as("Expected original team to points from before retirement only: " + originalTeamResultAfterUnretirement)
                .isEqualTo(firstPoints);

        assertThat(newTeamResultAfterUnretirement.getActiveUsers())
                .as("Expected new team to have 1 active user after unretirement: " + newTeamResultAfterUnretirement)
                .hasSize(2);

        assertThat(newTeamResultAfterUnretirement.getRetiredUsers())
                .as("Expected new team to have no retired users after unretirement: " + newTeamResultAfterUnretirement)
                .isEmpty();

        assertThat(newTeamResultAfterUnretirement.getTeamMultipliedPoints())
                .as("Expected new team to points from after unretirement only: " + originalTeamResultAfterUnretirement)
                .isEqualTo(thirdPoints);

        final UserResult retiredUserResultAfterUnretirement = getRetiredUserFromTeam(originalTeamResultAfterUnretirement, userToRetire.getDisplayName());
        final UserResult activeUserResultAfterUnretirement = getActiveUserFromTeam(newTeamResultAfterUnretirement, userToRetire.getDisplayName());

        assertThat(retiredUserResultAfterUnretirement.getMultipliedPoints())
                .as("Expected retired user to have points from before retirement only: " + retiredUserResultAfterUnretirement)
                .isEqualTo(firstPoints);

        assertThat(activeUserResultAfterUnretirement.getMultipliedPoints())
                .as("Expected unretired user to have points from after unretirement only: " + activeUserResultAfterUnretirement)
                .isEqualTo(thirdPoints);
    }

    @Test
    public void whenOneTeamHasOneUser_andUserHasOffsetApplied_thenUserOffsetIsAppendedToStats() {
        HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE);

        final User user = User.createWithoutId("User13", "User13", "Passkey13", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(user);
        final int userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(user)).getId();
        final Team team = Team.createWithoutId("Team10", "", userId, Set.of(userId), Collections.emptySet());
        TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(team));

        final long firstPoints = 2_500L;
        StubbedFoldingEndpointUtils.setPoints(user, firstPoints);
        TcStatsUtils.RequestSender.manualUpdate();

        final long pointsOffset = 1_000L;
        final long multipliedPointsOffset = Math.round(pointsOffset * HardwareTest.DUMMY_HARDWARE.getMultiplier());
        UserUtils.RequestSender.offset(userId, pointsOffset, multipliedPointsOffset, 0);
        TcStatsUtils.RequestSender.manualUpdate();

        final CompetitionResult result = TcStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final UserResult userResult = getActiveUserFromTeam(teamResult, user.getDisplayName());

        assertThat(userResult.getPoints())
                .as("Expected user points to be stats + offset: " + userResult)
                .isEqualTo(firstPoints + pointsOffset);

        assertThat(userResult.getMultipliedPoints())
                .as("Expected user multiplied points to be stats + offset: " + userResult)
                .isEqualTo(multipliedPointsOffset + Math.round(firstPoints * HardwareTest.DUMMY_HARDWARE.getMultiplier()));
    }

    @AfterAll
    public static void tearDown() {
        cleanSystemForComplexTests();
    }
}
