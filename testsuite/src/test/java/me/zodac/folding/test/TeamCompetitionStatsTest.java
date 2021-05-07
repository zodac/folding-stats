package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.TeamCompetitionResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;
import me.zodac.folding.test.utils.HardwareUtils;
import me.zodac.folding.test.utils.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.utils.TeamCompetitionStatsUtils;
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
import java.util.List;

import static me.zodac.folding.test.utils.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.utils.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.utils.TeamCompetitionStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.utils.TeamCompetitionStatsUtils.getRetiredUserFromTeam;
import static me.zodac.folding.test.utils.TeamCompetitionStatsUtils.getTeamFromCompetition;
import static me.zodac.folding.test.utils.TestGenerator.generateHardware;
import static me.zodac.folding.test.utils.TestGenerator.generateHardwareWithMultiplier;
import static me.zodac.folding.test.utils.TestGenerator.generateTeamWithUserIds;
import static me.zodac.folding.test.utils.TestGenerator.generateUser;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithCategory;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithHardwareId;
import static me.zodac.folding.test.utils.UserUtils.USER_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the <code>Team Competition</code> stats calculation.
 * <p>
 * Since the TC stats are done on the full system (meaning all {@link Team}s), we wipe the system before each test with a {@link BeforeEach} method.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeamCompetitionStatsTest {

    @BeforeEach
    public void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    @Order(1)
    public void whenNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoStats_andNoTeams() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.get();

        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionResult result = TeamCompetitionResponseParser.get(response);

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
    public void whenOneTeamExistsWithOneUser_andUserEarnsStats_thenUserAndTeamAndOverallStartWithNoStats_thenAllIncrementAsUserPointsIncrease() throws FoldingRestException {
        final User user = generateUser();
        final int userId = UserUtils.createOrConflict(user).getId();
        final Team team = generateTeamWithUserIds(userId);
        TeamUtils.createOrConflict(team);

        final CompetitionResult resultBeforeStats = TeamCompetitionStatsUtils.get();

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

        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionResult resultAfterStats = TeamCompetitionStatsUtils.get();

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
    public void whenOneTeamExistsWithTwoUser_andUserEarnsStats_thenBothUsersStartAtRank1_thenRanksUpdateCorrectlyAsUsersEarnStats() throws FoldingRestException {
        final User firstUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User secondUser = generateUserWithCategory(Category.AMD_GPU);
        final int firstUserId = UserUtils.createOrConflict(firstUser).getId();
        final int secondUserId = UserUtils.createOrConflict(secondUser).getId();

        final Team team = generateTeamWithUserIds(firstUserId, secondUserId);
        TeamUtils.createOrConflict(team);

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
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
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterFirstUpdate = TeamCompetitionStatsUtils.get();
        final TeamResult teamResultAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, team.getTeamName());
        final UserResult firstUserResultAfterFirstUpdate = getActiveUserFromTeam(teamResultAfterFirstUpdate, firstUser.getDisplayName());
        final UserResult secondUserResultAfterFirstUpdate = getActiveUserFromTeam(teamResultAfterFirstUpdate, secondUser.getDisplayName());

        assertThat(firstUserResultAfterFirstUpdate.getRankInTeam())
                .as("Expected first user to be rank 1: " + teamResultAfterFirstUpdate)
                .isEqualTo(1);

        assertThat(secondUserResultAfterFirstUpdate.getRankInTeam())
                .as("Expected second user to be rank 2: " + teamResultAfterFirstUpdate)
                .isEqualTo(2);

        StubbedFoldingEndpointUtils.setPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.setUnits(secondUser, 20);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterSecondUpdate = TeamCompetitionStatsUtils.get();
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
    public void whenTwoTeamsExistsWithOneUserEach_andUserEarnsStats_thenBothTeamsStartAtRank1_thenTeamRanksUpdateCorrectlyAsUsersEarnStats() throws FoldingRestException {
        final User firstUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final int firstUserId = UserUtils.createOrConflict(firstUser).getId();
        final Team firstTeam = generateTeamWithUserIds(firstUserId);
        TeamUtils.createOrConflict(firstTeam);

        final User secondUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final int secondUserId = UserUtils.createOrConflict(secondUser).getId();
        final Team secondTeam = generateTeamWithUserIds(secondUserId);
        TeamUtils.createOrConflict(secondTeam);

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
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
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterFirstUpdate = TeamCompetitionStatsUtils.get();
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
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterSecondUpdate = TeamCompetitionStatsUtils.get();
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
    public void whenTeamExistsWithOneUser_andUserHasAHardwareMultiplier_thenUserPointsAreMultipliedCorrectly_andUserUnitsAreNotImpacted() throws FoldingRestException {
        final Hardware hardware = generateHardwareWithMultiplier(2.0D);
        final int hardwareId = HardwareUtils.createOrConflict(hardware).getId();
        final User user = generateUserWithHardwareId(hardwareId);
        final int userId = UserUtils.createOrConflict(user).getId();
        final Team team = generateTeamWithUserIds(userId);
        TeamUtils.createOrConflict(team);

        final long newPoints = 20_000L;
        final int newUnits = 20;
        StubbedFoldingEndpointUtils.setPoints(user, newPoints);
        StubbedFoldingEndpointUtils.setUnits(user, newUnits);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
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
    public void whenTeamExistsWithOneUser_andUserIsUpdatedWithANewHardwareMultiplier_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly() throws FoldingRestException {
        final Hardware hardware = generateHardware();
        final int hardwareId = HardwareUtils.createOrConflict(hardware).getId();

        final User user = generateUserWithHardwareId(hardwareId);
        final int userId = UserUtils.createOrConflict(user).getId();
        final Team team = generateTeamWithUserIds(userId);
        TeamUtils.createOrConflict(team);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(user, firstPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
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
        hardware.setId(hardwareId);
        HARDWARE_REQUEST_SENDER.update(hardware);

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.setPoints(user, secondPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterUpdate = TeamCompetitionStatsUtils.get();
        final TeamResult teamResultAfterUpdate = getTeamFromCompetition(resultAfterUpdate, team.getTeamName());
        final UserResult userResultAfterUpdate = getActiveUserFromTeam(teamResultAfterUpdate, user.getDisplayName());

        assertThat(userResultAfterUpdate.getPoints())
                .as("Expected user points to not be multiplied: " + userResultAfterUpdate)
                .isEqualTo(firstPoints + secondPoints);

        assertThat(userResultAfterUpdate.getMultipliedPoints())
                .as("Expected user multiplied points to be multiplied only after the second update: " + userResultAfterUpdate)
                .isEqualTo(firstPoints + (Math.round(secondPoints * hardware.getMultiplier())));
    }

    @Test
    public void whenTeamExistsWithOneUser_andUserIsUpdatedWithANewHardware_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly() throws FoldingRestException {
        final Hardware hardware = generateHardware();
        final int hardwareId = HardwareUtils.createOrConflict(hardware).getId();

        final User user = generateUserWithHardwareId(hardwareId);
        final int userId = UserUtils.createOrConflict(user).getId();
        final Team team = generateTeamWithUserIds(userId);
        TeamUtils.createOrConflict(team);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(user, firstPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final UserResult userResult = getActiveUserFromTeam(teamResult, user.getDisplayName());

        assertThat(userResult.getMultipliedPoints())
                .as("Expected user multiplied points to not be multiplied: " + userResult)
                .isEqualTo(firstPoints);

        assertThat(userResult.getPoints())
                .as("Expected user points to not be multiplied: " + userResult)
                .isEqualTo(firstPoints);

        // Update the user with a new hardware with a multiplier
        final Hardware hardwareWithMultiplier = generateHardwareWithMultiplier(2.0D);
        final int hardwareWithMultiplierId = HardwareUtils.createOrConflict(hardwareWithMultiplier).getId();
        user.setHardwareId(hardwareWithMultiplierId);
        user.setId(userId);
        USER_REQUEST_SENDER.update(user);

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.setPoints(user, secondPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterUpdate = TeamCompetitionStatsUtils.get();
        final TeamResult teamResultAfterUpdate = getTeamFromCompetition(resultAfterUpdate, team.getTeamName());
        final UserResult userResultAfterUpdate = getActiveUserFromTeam(teamResultAfterUpdate, user.getDisplayName());

        assertThat(userResultAfterUpdate.getMultipliedPoints())
                .as("Expected user multiplied points to be multiplied only after the second update: " + userResultAfterUpdate)
                .isEqualTo(firstPoints + (Math.round(secondPoints * hardwareWithMultiplier.getMultiplier())));

        assertThat(userResultAfterUpdate.getPoints())
                .as("Expected user points to not be multiplied: " + userResultAfterUpdate)
                .isEqualTo(firstPoints + secondPoints);
    }

    @Test
    public void whenTeamExistsWithTwoUsers_andOneUserRetires_andUserUnretired_thenOriginalStatsAreNotLostFromTeam_andNewStatsWhileRetiredAreNotAddedToTeam_andStatsAfterUnretirementAreCounted() throws FoldingRestException {
        final User firstUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User secondUser = generateUserWithCategory(Category.AMD_GPU);
        final int firstUserId = UserUtils.createOrConflict(firstUser).getId();
        final int secondUserId = UserUtils.createOrConflict(secondUser).getId();

        final Team team = generateTeamWithUserIds(firstUserId, secondUserId);
        final int teamId = TeamUtils.createOrConflict(team).getId();

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, firstPoints);
        StubbedFoldingEndpointUtils.setPoints(secondUser, firstPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());

        assertThat(teamResult.getActiveUsers())
                .as("Expected team to have 2 active users for first update: " + teamResult)
                .hasSize(2);

        assertThat(teamResult.getRetiredUsers())
                .as("Expected team to have no retired user for first update: " + teamResult)
                .isEmpty();

        final int retiredUserId = TeamUtils.retireUser(teamId, secondUserId);
        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, secondPoints);
        StubbedFoldingEndpointUtils.setPoints(secondUser, secondPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterRetirement = TeamCompetitionStatsUtils.get();
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

        TeamUtils.unretireUser(teamId, retiredUserId);
        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, thirdPoints);
        StubbedFoldingEndpointUtils.setPoints(secondUser, thirdPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterUnretirement = TeamCompetitionStatsUtils.get();
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
    public void whenTeamExistsWithTwoUsers_andOneUserRetires_andUserUnretiredToANewTeam_thenOriginalStatsAreNotLostFromOriginalTeam_andNewTeamGetsStatsAfterUnretirement_andStatsDuringRetirementAreNotCounted() throws FoldingRestException {
        final User originalTeamCaptain = generateUserWithCategory(Category.NVIDIA_GPU);
        final User userToRetire = generateUserWithCategory(Category.AMD_GPU);
        final int originalTeamCaptainId = UserUtils.createOrConflict(originalTeamCaptain).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetire).getId();

        final Team originalTeam = generateTeamWithUserIds(originalTeamCaptainId, userToRetireId);
        final int originalTeamId = TeamUtils.createOrConflict(originalTeam).getId();

        final User newTeamCaptain = generateUserWithCategory(Category.NVIDIA_GPU);
        final int newTeamCaptainId = UserUtils.createOrConflict(newTeamCaptain).getId();
        final Team newTeam = generateTeamWithUserIds(newTeamCaptainId);
        final int newTeamId = TeamUtils.createOrConflict(newTeam).getId();

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(userToRetire, firstPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
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


        final int retiredUserId = TeamUtils.retireUser(originalTeamId, userToRetireId);

        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.setPoints(userToRetire, secondPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        TeamUtils.unretireUser(newTeamId, retiredUserId);
        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.setPoints(userToRetire, thirdPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult resultAfterUnretirement = TeamCompetitionStatsUtils.get();
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
    public void whenOneTeamHasOneUser_andUserHasOffsetApplied_thenUserOffsetIsAppendedToStats() throws FoldingRestException {
        final User user = generateUser();
        final int userId = UserUtils.createOrConflict(user).getId();
        final Team team = generateTeamWithUserIds(userId);
        TeamUtils.createOrConflict(team);

        final long firstPoints = 2_500L;
        StubbedFoldingEndpointUtils.setPoints(user, firstPoints);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final long pointsOffset = 1_000L;
        USER_REQUEST_SENDER.offset(userId, pointsOffset, pointsOffset, 0);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final UserResult userResult = getActiveUserFromTeam(teamResult, user.getDisplayName());

        assertThat(userResult.getPoints())
                .as("Expected user points to be stats + offset: " + userResult)
                .isEqualTo(firstPoints + pointsOffset);

        assertThat(userResult.getMultipliedPoints())
                .as("Expected user multiplied points to be stats + offset: " + userResult)
                .isEqualTo(pointsOffset + firstPoints);
    }

    @Test
    public void whenOneTeamHasOneUser_andUserHasOffsetApplied_andOffsetIsNegative_andOffsetIsGreaterThanCurrentUserStats_thenUserHasZeroStats() throws FoldingRestException {
        final User user = generateUser();
        final int userId = UserUtils.createOrConflict(user).getId();
        final Team team = generateTeamWithUserIds(userId);
        TeamUtils.createOrConflict(team);

        final long firstPoints = 2_500L;
        final int firstUnits = 25;
        StubbedFoldingEndpointUtils.setPoints(user, firstPoints);
        StubbedFoldingEndpointUtils.setUnits(user, firstUnits);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final long pointsOffset = -20_000L;
        final int unitsOffset = -400;
        USER_REQUEST_SENDER.offset(userId, pointsOffset, pointsOffset, unitsOffset);
        TEAM_COMPETITION_REQUEST_SENDER.manualUpdate();

        final CompetitionResult result = TeamCompetitionStatsUtils.get();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final UserResult userResult = getActiveUserFromTeam(teamResult, user.getDisplayName());

        assertThat(userResult.getPoints())
                .as("Expected user points to be 0: " + userResult)
                .isEqualTo(0L);

        assertThat(userResult.getMultipliedPoints())
                .as("Expected user multiplied points to be 0: " + userResult)
                .isEqualTo(0L);

        assertThat(userResult.getUnits())
                .as("Expected user units to be 0: " + userResult)
                .isEqualTo(0);
    }

    @AfterAll
    public static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }
}
