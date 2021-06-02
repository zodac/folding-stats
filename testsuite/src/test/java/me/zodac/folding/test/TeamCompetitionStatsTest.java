package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.TeamCompetitionResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.RetiredUserResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.utils.TestConstants;
import me.zodac.folding.test.utils.rest.request.HardwareUtils;
import me.zodac.folding.test.utils.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils;
import me.zodac.folding.test.utils.rest.request.TeamUtils;
import me.zodac.folding.test.utils.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;

import static me.zodac.folding.api.utils.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.utils.TestConstants.HTTP_CLIENT;
import static me.zodac.folding.test.utils.TestGenerator.generateHardware;
import static me.zodac.folding.test.utils.TestGenerator.generateHardwareWithMultiplier;
import static me.zodac.folding.test.utils.TestGenerator.generateTeam;
import static me.zodac.folding.test.utils.TestGenerator.generateUser;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithHardwareId;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithId;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithTeamId;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithTeamIdAndCategory;
import static me.zodac.folding.test.utils.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.getRetiredUserFromTeam;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.getTeamFromCompetition;
import static me.zodac.folding.test.utils.rest.request.TeamCompetitionStatsUtils.manuallyUpdateStats;
import static me.zodac.folding.test.utils.rest.request.UserUtils.USER_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the <code>Team Competition</code> stats calculation.
 * <p>
 * Since the TC stats are done on the full system (meaning all {@link Team}s), we wipe the system before each test with a {@link BeforeEach} method.
 */
class TeamCompetitionStatsTest {

    @BeforeEach
    void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    void whenNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoStats_andNoTeams() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getStats();

        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionResult result = TeamCompetitionResponseParser.getStats(response);

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
    void whenOneTeamExistsWithOneUser_andUserEarnsStats_thenUserAndTeamAndOverallStartWithNoStats_thenAllIncrementAsUserPointsIncrease() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamId(team.getId()));

        final CompetitionResult resultBeforeStats = TeamCompetitionStatsUtils.getStats();

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

        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualUpdate(ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionResult resultAfterStats = TeamCompetitionStatsUtils.getStats();

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
    void whenOneTeamExistsWithTwoUser_andUserEarnsStats_thenBothUsersStartAtRank1_thenRanksUpdateCorrectlyAsUsersEarnStats() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.AMD_GPU));
        final User secondUser = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.NVIDIA_GPU));

        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final Collection<UserResult> activeUserResults = teamResult.getActiveUsers();

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
        manuallyUpdateStats();


        final CompetitionResult resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
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
        manuallyUpdateStats();


        final CompetitionResult resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamResult teamResultAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, team.getTeamName());
        final UserResult firstUserResultAfterSecondUpdate = getActiveUserFromTeam(teamResultAfterSecondUpdate, firstUser.getDisplayName());
        final UserResult secondUserResultAfterSecondUpdate = getActiveUserFromTeam(teamResultAfterSecondUpdate, secondUser.getDisplayName());

        assertThat(firstUserResultAfterSecondUpdate.getRankInTeam())
                .as("Expected first user to be rank 2: " + firstUserResultAfterSecondUpdate)
                .isEqualTo(2);

        assertThat(secondUserResultAfterSecondUpdate.getRankInTeam())
                .as("Expected second user to be rank 1: " + secondUserResultAfterSecondUpdate)
                .isEqualTo(1);
    }

    @Test
    void whenTwoTeamsExistsWithOneUserEach_andUserEarnsStats_thenBothTeamsStartAtRank1_thenTeamRanksUpdateCorrectlyAsUsersEarnStats() throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamId(firstTeam.getId()));

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User secondUser = UserUtils.create(generateUserWithTeamId(secondTeam.getId()));

        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
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
        manuallyUpdateStats();


        final CompetitionResult resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
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
        manuallyUpdateStats();


        final CompetitionResult resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
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
    void whenTeamExistsWithOneUser_andUserHasAHardwareMultiplier_thenUserPointsAreMultipliedCorrectly_andUserUnitsAreNotImpacted() throws FoldingRestException {
        final double hardwareMultiplier = 2.0D;
        final int hardwareId = HardwareUtils.create(generateHardwareWithMultiplier(2.0D)).getId();
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareId(hardwareId);
        user.setTeamId(team.getId());
        final User createdUser = UserUtils.create(user);


        final long newPoints = 20_000L;
        final int newUnits = 20;
        StubbedFoldingEndpointUtils.setPoints(createdUser, newPoints);
        StubbedFoldingEndpointUtils.setUnits(createdUser, newUnits);
        manuallyUpdateStats();

        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final UserResult userResult = getActiveUserFromTeam(teamResult, user.getDisplayName());

        assertThat(userResult.getMultipliedPoints())
                .as("Expected user multiplied points to be new points * hardware multiplier: " + userResult)
                .isEqualTo(Math.round(newPoints * hardwareMultiplier));

        assertThat(userResult.getPoints())
                .as("Expected user points to not be multiplied: " + userResult)
                .isEqualTo(newPoints);

        assertThat(userResult.getUnits())
                .as("Expected user units to not be multiplied: " + userResult)
                .isEqualTo(newUnits);
    }

    @Test
    void whenTeamExistsWithOneUser_andUserIsUpdatedWithANewHardwareMultiplier_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly() throws FoldingRestException {
        final HardwareRequest hardware = generateHardware();
        final int hardwareId = HardwareUtils.create(hardware).getId();
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareId(hardwareId);
        user.setTeamId(team.getId());
        final User createdUser = UserUtils.create(user);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(createdUser, firstPoints);
        manuallyUpdateStats();


        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
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
        HARDWARE_REQUEST_SENDER.update(hardware, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.setPoints(createdUser, secondPoints);
        manuallyUpdateStats();


        final CompetitionResult resultAfterUpdate = TeamCompetitionStatsUtils.getStats();
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
    void whenTeamExistsWithOneUser_andUserIsUpdatedWithANewHardware_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly() throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareId(hardwareId);
        user.setTeamId(team.getId());
        final User createdUser = UserUtils.create(user);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(createdUser, firstPoints);
        manuallyUpdateStats();


        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());
        final UserResult userResult = getActiveUserFromTeam(teamResult, user.getDisplayName());

        assertThat(userResult.getMultipliedPoints())
                .as("Expected user multiplied points to not be multiplied: " + userResult)
                .isEqualTo(firstPoints);

        assertThat(userResult.getPoints())
                .as("Expected user points to not be multiplied: " + userResult)
                .isEqualTo(firstPoints);

        // Update the user with a new hardware with a multiplier
        final HardwareRequest hardwareWithMultiplier = generateHardwareWithMultiplier(2.0D);
        final int hardwareWithMultiplierId = HardwareUtils.create(hardwareWithMultiplier).getId();
        user.setHardwareId(hardwareWithMultiplierId);
        user.setId(createdUser.getId());
        USER_REQUEST_SENDER.update(user, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.setPoints(createdUser, secondPoints);
        manuallyUpdateStats();


        final CompetitionResult resultAfterUpdate = TeamCompetitionStatsUtils.getStats();
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
    void whenTeamExistsWithTwoUsers_andOneUserIsDeleted_andUserIsAddedAgain_thenOriginalStatsAreNotLostFromTeam_andNewStatsWhileDeletedAreNotAddedToTeam_andStatsAfterReturnAreCounted() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.AMD_GPU));

        final UserRequest userToRetire = generateUserWithTeamIdAndCategory(team.getId(), Category.NVIDIA_GPU);
        final User createdUserToRetire = UserUtils.create(userToRetire);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, firstPoints);
        StubbedFoldingEndpointUtils.setPoints(createdUserToRetire, firstPoints);
        manuallyUpdateStats();


        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
        final TeamResult teamResult = getTeamFromCompetition(result, team.getTeamName());

        assertThat(teamResult.getActiveUsers())
                .as("Expected team to have 2 active users for first update: " + teamResult)
                .hasSize(2);

        assertThat(teamResult.getRetiredUsers())
                .as("Expected team to have no retired user for first update: " + teamResult)
                .isEmpty();

        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(createdUserToRetire.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Expected user to be deleted: " + response)
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, secondPoints);
        StubbedFoldingEndpointUtils.setPoints(createdUserToRetire, secondPoints);
        manuallyUpdateStats();

        final CompetitionResult resultAfterRetirement = TeamCompetitionStatsUtils.getStats();
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
        final RetiredUserResult secondUserResult = getRetiredUserFromTeam(teamResultAfterRetirement, userToRetire.getDisplayName());

        assertThat(firstUserResult.getMultipliedPoints())
                .as("Expected user to have points from both updates: " + firstUserResult)
                .isEqualTo(firstPoints + secondPoints);

        assertThat(secondUserResult.getMultipliedPoints())
                .as("Expected retired user to have points from first update only: " + secondUserResult)
                .isEqualTo(firstPoints);

        UserUtils.create(userToRetire);
        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, thirdPoints);
        StubbedFoldingEndpointUtils.setPoints(createdUserToRetire, thirdPoints);
        manuallyUpdateStats();

        final CompetitionResult resultAfterUnretirement = TeamCompetitionStatsUtils.getStats();
        final TeamResult teamResultAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, team.getTeamName());

        assertThat(teamResultAfterUnretirement.getTeamMultipliedPoints())
                .as("Expected team to have points from first update for both users, second update only for the first user, then third update for both users: " + teamResultAfterUnretirement)
                .isEqualTo(firstPoints + firstPoints + secondPoints + thirdPoints + thirdPoints);

        assertThat(teamResultAfterUnretirement.getActiveUsers())
                .as("Expected team to have 2 active users after unretirement: " + teamResultAfterUnretirement)
                .hasSize(2);

        assertThat(teamResultAfterUnretirement.getRetiredUsers())
                .as("Expected team to have 1 retired user after user was unretired: " + teamResultAfterUnretirement)
                .hasSize(1);

        final UserResult firstUserResultAfterUnretirement = getActiveUserFromTeam(teamResultAfterUnretirement, firstUser.getDisplayName());
        final UserResult secondUserResultAfterUnretirement = getActiveUserFromTeam(teamResultAfterUnretirement, userToRetire.getDisplayName());

        assertThat(firstUserResultAfterUnretirement.getMultipliedPoints())
                .as("Expected user to have points from all three updates: " + firstUserResultAfterUnretirement)
                .isEqualTo(firstPoints + secondPoints + thirdPoints);

        assertThat(secondUserResultAfterUnretirement.getMultipliedPoints())
                .as("Expected user to have points from third updates only: " + secondUserResultAfterUnretirement)
                .isEqualTo(thirdPoints);

        final RetiredUserResult retiredUserAfterUnretirement = getRetiredUserFromTeam(teamResultAfterRetirement, userToRetire.getDisplayName());

        assertThat(retiredUserAfterUnretirement.getMultipliedPoints())
                .as("Expected retired user to have points from first update only: " + retiredUserAfterUnretirement)
                .isEqualTo(firstPoints);
    }

    @Test
    void whenTeamExistsWithTwoUsers_andOneUserIsDeleted_andUserIsReAddedToANewTeam_thenOriginalStatsAreNotLostFromOriginalTeam_andNewTeamGetsStatsAfterUnretirement_andStatsDuringRetirementAreNotCounted() throws FoldingRestException {
        final Team originalTeam = TeamUtils.create(generateTeam());

        UserUtils.create(generateUserWithTeamIdAndCategory(originalTeam.getId(), Category.NVIDIA_GPU));
        final UserRequest userToRetire = generateUserWithTeamIdAndCategory(originalTeam.getId(), Category.AMD_GPU);
        final User createUserToRetire = UserUtils.create(userToRetire);

        final Team newTeam = TeamUtils.create(generateTeam());
        UserUtils.create(generateUserWithTeamIdAndCategory(newTeam.getId(), Category.NVIDIA_GPU));

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(createUserToRetire, firstPoints);
        manuallyUpdateStats();

        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
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


        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(createUserToRetire.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Expected user to be deleted: " + response)
                .isEqualTo(HttpURLConnection.HTTP_OK);


        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.setPoints(createUserToRetire, secondPoints);
        manuallyUpdateStats();

        userToRetire.setTeamId(newTeam.getId());
        UserUtils.create(userToRetire);

        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.setPoints(createUserToRetire, thirdPoints);
        manuallyUpdateStats();

        final CompetitionResult resultAfterUnretirement = TeamCompetitionStatsUtils.getStats();
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

        final RetiredUserResult retiredUserResultAfterUnretirement = getRetiredUserFromTeam(originalTeamResultAfterUnretirement, userToRetire.getDisplayName());
        final UserResult activeUserResultAfterUnretirement = getActiveUserFromTeam(newTeamResultAfterUnretirement, userToRetire.getDisplayName());

        assertThat(retiredUserResultAfterUnretirement.getMultipliedPoints())
                .as("Expected retired user to have points from before retirement only: " + retiredUserResultAfterUnretirement)
                .isEqualTo(firstPoints);

        assertThat(activeUserResultAfterUnretirement.getMultipliedPoints())
                .as("Expected unretired user to have points from after unretirement only: " + activeUserResultAfterUnretirement)
                .isEqualTo(thirdPoints);
    }

    @Test
    void whenOneTeamHasOneUser_andUserHasOffsetApplied_thenUserOffsetIsAppendedToStats() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamId(team.getId()));
        final int userId = user.getId();

        final long firstPoints = 2_500L;
        StubbedFoldingEndpointUtils.setPoints(user, firstPoints);
        manuallyUpdateStats();


        final long pointsOffset = 1_000L;
        TEAM_COMPETITION_REQUEST_SENDER.offset(userId, pointsOffset, pointsOffset, 0, ADMIN_USER.userName(), ADMIN_USER.password());
        manuallyUpdateStats();

        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
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
    void whenOneTeamHasOneUser_andUserHasOffsetApplied_andOffsetIsNegative_andOffsetIsGreaterThanCurrentUserStats_thenUserHasZeroStats() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamId(team.getId()));
        final int userId = user.getId();

        final long firstPoints = 2_500L;
        final int firstUnits = 25;
        StubbedFoldingEndpointUtils.setPoints(user, firstPoints);
        StubbedFoldingEndpointUtils.setUnits(user, firstUnits);
        manuallyUpdateStats();

        final long pointsOffset = -20_000L;
        final int unitsOffset = -400;
        TEAM_COMPETITION_REQUEST_SENDER.offset(userId, pointsOffset, pointsOffset, unitsOffset, ADMIN_USER.userName(), ADMIN_USER.password());
        manuallyUpdateStats();

        final CompetitionResult result = TeamCompetitionStatsUtils.getStats();
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

    @Test
    void whenOneTeamExistsWithOneUser_andUserEarnsStats_thenUserStartsWithNoStats_thenIncrementsAsUserPointsIncrease() throws FoldingRestException {
        final User user = UserUtils.create(generateUser());
        final int userId = user.getId();

        manuallyUpdateStats();
        final UserResult resultBeforeStats = TeamCompetitionStatsUtils.getStatsForUser(userId);

        assertThat(resultBeforeStats.getPoints())
                .as("Expected no points: " + resultBeforeStats)
                .isEqualTo(0L);

        assertThat(resultBeforeStats.getMultipliedPoints())
                .as("Expected no multiplied points: " + resultBeforeStats)
                .isEqualTo(0L);

        assertThat(resultBeforeStats.getUnits())
                .as("Expected no units: " + resultBeforeStats)
                .isEqualTo(0);

        final long newPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(user, newPoints);

        final int newUnits = 5;
        StubbedFoldingEndpointUtils.setUnits(user, newUnits);

        manuallyUpdateStats();

        final UserResult resultAfterStats = TeamCompetitionStatsUtils.getStatsForUser(userId);

        assertThat(resultAfterStats.getPoints())
                .as("Expected updated points: " + resultAfterStats)
                .isEqualTo(newPoints);

        assertThat(resultAfterStats.getMultipliedPoints())
                .as("Expected updated multiplied points: " + resultAfterStats)
                .isEqualTo(newPoints);

        assertThat(resultAfterStats.getUnits())
                .as("Expected updated units: " + resultAfterStats)
                .isEqualTo(newUnits);
    }

    @Test
    void whenGettingStatsForUser_andUserRankIs2ndInTeamBut3rdInCompetition_thenResponseHasTeamRankListed() throws FoldingRestException {
        final Team mainTeam = TeamUtils.create(generateTeam());
        final User firstInTeamFirstOverall = UserUtils.create(generateUserWithTeamIdAndCategory(mainTeam.getId(), Category.AMD_GPU));

        final User secondInTeamThirdOverall = UserUtils.create(generateUserWithTeamIdAndCategory(mainTeam.getId(), Category.NVIDIA_GPU));
        final int secondInTeamThirdOverallId = secondInTeamThirdOverall.getId();

        final Team otherTeam = TeamUtils.create(generateTeam());
        final User firstInTeamSecondOverall = UserUtils.create(generateUserWithTeamId(otherTeam.getId()));

        manuallyUpdateStats();
        final UserResult resultBeforeStats = TeamCompetitionStatsUtils.getStatsForUser(secondInTeamThirdOverallId);
        assertThat(resultBeforeStats.getRankInTeam())
                .as("Expected all users to start at rank 1: " + resultBeforeStats)
                .isEqualTo(1);

        StubbedFoldingEndpointUtils.setPoints(firstInTeamFirstOverall, 10_000L);
        StubbedFoldingEndpointUtils.setPoints(secondInTeamThirdOverall, 1_000L);
        StubbedFoldingEndpointUtils.setPoints(firstInTeamSecondOverall, 5_000L);
        manuallyUpdateStats();

        final UserResult resultAfterStats = TeamCompetitionStatsUtils.getStatsForUser(secondInTeamThirdOverallId);
        assertThat(resultAfterStats.getRankInTeam())
                .as("Expected user to be third overall, but second in team: " + resultBeforeStats)
                .isEqualTo(2);
    }

    @Test
    void whenGettingStatsForUser_andUserDoesNotExist_thenResponseHasA404Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getStatsForUser(TestConstants.INVALID_ID);
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenPatchingAUserWithPointsOffsets_givenThePayloadIsValid_thenResponseHasA200Status() throws FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final UserRequest user = generateUserWithHardwareId(hardware.getId());

        final int userId = UserUtils.create(user).getId();
        final HttpResponse<Void> patchResponse = TEAM_COMPETITION_REQUEST_SENDER.offset(userId, 100L, Math.round(100L * hardware.getMultiplier()), 10, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(patchResponse.statusCode())
                .as("Was not able to patch user: " + patchResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    void whenPatchingAUserWithPointsOffsets_AndUserDoesNotExist_thenResponseHasA404Status() throws FoldingRestException {
        final HttpResponse<Void> patchResponse = TEAM_COMPETITION_REQUEST_SENDER.offset(TestConstants.INVALID_ID, 100L, 1_000L, 10, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(patchResponse.statusCode())
                .as("Was able to patch user, was expected user to not be found: " + patchResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenPatchingAUserWithPointsOffsets_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final UserRequest user = generateUserWithId(hardware.getId());

        final int userId = UserUtils.create(user).getId();
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.offset(userId, 100L, Math.round(100L * hardware.getMultiplier()), 10);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenPatchingAUserWithPointsOffsets_givenEmptyPayload_thenRequestFails_andResponseHasA400StatusCode() throws IOException, InterruptedException, FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final UserRequest user = generateUserWithId(hardware.getId());
        final int userId = UserUtils.create(user).getId();

        final HttpRequest request = HttpRequest.newBuilder()
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(FOLDING_URL + "/stats/users/" + userId))
                .header("Content-Type", "application/json")
                .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
                .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }
}
