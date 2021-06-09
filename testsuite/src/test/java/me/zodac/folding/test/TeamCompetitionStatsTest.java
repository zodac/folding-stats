package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.TeamCompetitionResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.RetiredUserSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
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

        final CompetitionSummary result = TeamCompetitionResponseParser.getStats(response);

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

        final CompetitionSummary resultBeforeStats = TeamCompetitionStatsUtils.getStats();

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

        final TeamSummary teamSummaryBeforeStats = getTeamFromCompetition(resultBeforeStats, team.getTeamName());

        assertThat(teamSummaryBeforeStats.getTeamPoints())
                .as("Expected no points for team: " + teamSummaryBeforeStats)
                .isEqualTo(0L);

        assertThat(teamSummaryBeforeStats.getTeamMultipliedPoints())
                .as("Expected no multiplied points for team: " + teamSummaryBeforeStats)
                .isEqualTo(0L);

        assertThat(teamSummaryBeforeStats.getTeamUnits())
                .as("Expected no units for team: " + teamSummaryBeforeStats)
                .isEqualTo(0);

        assertThat(teamSummaryBeforeStats.getActiveUsers())
                .as("Expected exactly 1 active user: " + teamSummaryBeforeStats)
                .hasSize(1);

        assertThat(teamSummaryBeforeStats.getRetiredUsers())
                .as("Expected no retired users: " + teamSummaryBeforeStats)
                .isEmpty();

        final UserSummary userSummaryBeforeStats = getActiveUserFromTeam(teamSummaryBeforeStats, user.getDisplayName());

        assertThat(userSummaryBeforeStats.getPoints())
                .as("Expected no points for user: " + userSummaryBeforeStats)
                .isEqualTo(0L);

        assertThat(userSummaryBeforeStats.getMultipliedPoints())
                .as("Expected no multiplied points for user: " + userSummaryBeforeStats)
                .isEqualTo(0L);

        assertThat(userSummaryBeforeStats.getUnits())
                .as("Expected no units for user: " + userSummaryBeforeStats)
                .isEqualTo(0);

        final long newPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(user, newPoints);

        final int newUnits = 5;
        StubbedFoldingEndpointUtils.setUnits(user, newUnits);

        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualUpdate(ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionSummary resultAfterStats = TeamCompetitionStatsUtils.getStats();

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

        final TeamSummary teamSummaryAfterStats = getTeamFromCompetition(resultAfterStats, team.getTeamName());

        assertThat(teamSummaryAfterStats.getTeamPoints())
                .as("Expected updated points for team: " + teamSummaryAfterStats)
                .isEqualTo(newPoints);

        assertThat(teamSummaryAfterStats.getTeamMultipliedPoints())
                .as("Expected updated multiplied points for team: " + teamSummaryAfterStats)
                .isEqualTo(newPoints);

        assertThat(teamSummaryAfterStats.getTeamUnits())
                .as("Expected updated units for team: " + teamSummaryAfterStats)
                .isEqualTo(newUnits);

        assertThat(teamSummaryAfterStats.getActiveUsers())
                .as("Expected exactly 1 active user: " + teamSummaryAfterStats)
                .hasSize(1);

        assertThat(teamSummaryAfterStats.getRetiredUsers())
                .as("Expected no retired users: " + teamSummaryAfterStats)
                .isEmpty();

        final UserSummary userSummaryAfterStats = getActiveUserFromTeam(teamSummaryAfterStats, user.getDisplayName());

        assertThat(userSummaryAfterStats.getPoints())
                .as("Expected updated points for user: " + userSummaryAfterStats)
                .isEqualTo(newPoints);

        assertThat(userSummaryAfterStats.getMultipliedPoints())
                .as("Expected updated multiplied points for user: " + userSummaryAfterStats)
                .isEqualTo(newPoints);

        assertThat(userSummaryAfterStats.getUnits())
                .as("Expected updated units for user: " + userSummaryAfterStats)
                .isEqualTo(newUnits);
    }

    @Test
    void whenOneTeamExistsWithTwoUser_andUserEarnsStats_thenBothUsersStartAtRank1_thenRanksUpdateCorrectlyAsUsersEarnStats() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.AMD_GPU));
        final User secondUser = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.NVIDIA_GPU));

        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.getTeamName());
        final Collection<UserSummary> activeUserSummaries = teamSummary.getActiveUsers();

        assertThat(activeUserSummaries)
                .as("Expected exactly 2 active users: " + teamSummary)
                .hasSize(2);

        final UserSummary firstUserSummary = getActiveUserFromTeam(teamSummary, firstUser.getDisplayName());
        final UserSummary secondUserSummary = getActiveUserFromTeam(teamSummary, secondUser.getDisplayName());

        assertThat(firstUserSummary.getRankInTeam())
                .as("Expected first user to be rank 1: " + firstUserSummary)
                .isEqualTo(1);

        assertThat(secondUserSummary.getRankInTeam())
                .as("Expected second user to be rank 1: " + secondUserSummary)
                .isEqualTo(1);

        StubbedFoldingEndpointUtils.setPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.setUnits(firstUser, 10);
        manuallyUpdateStats();


        final CompetitionSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, team.getTeamName());
        final UserSummary firstUserSummaryAfterFirstUpdate = getActiveUserFromTeam(teamSummaryAfterFirstUpdate, firstUser.getDisplayName());
        final UserSummary secondUserSummaryAfterFirstUpdate = getActiveUserFromTeam(teamSummaryAfterFirstUpdate, secondUser.getDisplayName());

        assertThat(firstUserSummaryAfterFirstUpdate.getRankInTeam())
                .as("Expected first user to be rank 1: " + teamSummaryAfterFirstUpdate)
                .isEqualTo(1);

        assertThat(secondUserSummaryAfterFirstUpdate.getRankInTeam())
                .as("Expected second user to be rank 2: " + teamSummaryAfterFirstUpdate)
                .isEqualTo(2);

        StubbedFoldingEndpointUtils.setPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.setUnits(secondUser, 20);
        manuallyUpdateStats();


        final CompetitionSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, team.getTeamName());
        final UserSummary firstUserSummaryAfterSecondUpdate = getActiveUserFromTeam(teamSummaryAfterSecondUpdate, firstUser.getDisplayName());
        final UserSummary secondUserSummaryAfterSecondUpdate = getActiveUserFromTeam(teamSummaryAfterSecondUpdate, secondUser.getDisplayName());

        assertThat(firstUserSummaryAfterSecondUpdate.getRankInTeam())
                .as("Expected first user to be rank 2: " + firstUserSummaryAfterSecondUpdate)
                .isEqualTo(2);

        assertThat(secondUserSummaryAfterSecondUpdate.getRankInTeam())
                .as("Expected second user to be rank 1: " + secondUserSummaryAfterSecondUpdate)
                .isEqualTo(1);
    }

    @Test
    void whenTwoTeamsExistsWithOneUserEach_andUserEarnsStats_thenBothTeamsStartAtRank1_thenTeamRanksUpdateCorrectlyAsUsersEarnStats() throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamId(firstTeam.getId()));

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User secondUser = UserUtils.create(generateUserWithTeamId(secondTeam.getId()));

        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        assertThat(result.getTeams())
                .as("Expected exactly 2 teams: " + result)
                .hasSize(2);

        final TeamSummary firstTeamSummary = getTeamFromCompetition(result, firstTeam.getTeamName());
        final TeamSummary secondTeamSummary = getTeamFromCompetition(result, secondTeam.getTeamName());

        assertThat(firstTeamSummary.getRank())
                .as("Expected first team to be rank 1: " + firstTeamSummary)
                .isEqualTo(1);

        assertThat(secondTeamSummary.getRank())
                .as("Expected second team to be rank 1: " + secondTeamSummary)
                .isEqualTo(1);

        StubbedFoldingEndpointUtils.setPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.setUnits(firstUser, 10);
        manuallyUpdateStats();


        final CompetitionSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, firstTeam.getTeamName());
        final TeamSummary secondTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, secondTeam.getTeamName());

        assertThat(firstTeamSummaryAfterFirstUpdate.getRank())
                .as("Expected first team to be rank 1: " + firstTeamSummaryAfterFirstUpdate)
                .isEqualTo(1);

        assertThat(secondTeamSummaryAfterFirstUpdate.getRank())
                .as("Expected second team to be rank 2: " + secondTeamSummaryAfterFirstUpdate)
                .isEqualTo(2);

        StubbedFoldingEndpointUtils.setPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.setUnits(secondUser, 20);
        manuallyUpdateStats();


        final CompetitionSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, firstTeam.getTeamName());
        final TeamSummary secondTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, secondTeam.getTeamName());

        assertThat(firstTeamSummaryAfterSecondUpdate.getRank())
                .as("Expected first team to be rank 2: " + firstTeamSummaryAfterSecondUpdate)
                .isEqualTo(2);

        assertThat(secondTeamSummaryAfterSecondUpdate.getRank())
                .as("Expected second team to be rank 1: " + secondTeamSummaryAfterSecondUpdate)
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

        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.getTeamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.getDisplayName());

        assertThat(userSummary.getMultipliedPoints())
                .as("Expected user multiplied points to be new points * hardware multiplier: " + userSummary)
                .isEqualTo(Math.round(newPoints * hardwareMultiplier));

        assertThat(userSummary.getPoints())
                .as("Expected user points to not be multiplied: " + userSummary)
                .isEqualTo(newPoints);

        assertThat(userSummary.getUnits())
                .as("Expected user units to not be multiplied: " + userSummary)
                .isEqualTo(newUnits);
    }

    @Test
    void whenTeamExistsWithOneUser_andUserIsUpdatedWithANewHardwareMultiplier_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly() throws FoldingRestException {
        final Hardware createdHardware = HardwareUtils.create(generateHardware());
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareId(createdHardware.getId());
        user.setTeamId(team.getId());
        final User createdUser = UserUtils.create(user);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.setPoints(createdUser, firstPoints);
        manuallyUpdateStats();

        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.getTeamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.getDisplayName());

        assertThat(userSummary.getMultipliedPoints())
                .as("Expected user multiplied points to not be multiplied: " + userSummary)
                .isEqualTo(firstPoints);

        assertThat(userSummary.getPoints())
                .as("Expected user points to not be multiplied: " + userSummary)
                .isEqualTo(firstPoints);

        // Change the multiplier on the hardware, no need to update the user
        final HardwareRequest updatedHardware = HardwareRequest.builder()
                .hardwareName(createdHardware.getHardwareName())
                .displayName(createdHardware.getDisplayName())
                .operatingSystem(createdHardware.getOperatingSystem().toString())
                .multiplier(2.0D)
                .build();

        HARDWARE_REQUEST_SENDER.update(createdHardware.getId(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.setPoints(createdUser, secondPoints);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUpdate = getTeamFromCompetition(resultAfterUpdate, team.getTeamName());
        final UserSummary userSummaryAfterUpdate = getActiveUserFromTeam(teamSummaryAfterUpdate, user.getDisplayName());

        assertThat(userSummaryAfterUpdate.getPoints())
                .as("Expected user points to not be multiplied: " + userSummaryAfterUpdate)
                .isEqualTo(firstPoints + secondPoints);

        assertThat(userSummaryAfterUpdate.getMultipliedPoints())
                .as("Expected user multiplied points to be multiplied only after the second update: " + userSummaryAfterUpdate)
                .isEqualTo(firstPoints + (Math.round(secondPoints * updatedHardware.getMultiplier())));
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


        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.getTeamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.getDisplayName());

        assertThat(userSummary.getMultipliedPoints())
                .as("Expected user multiplied points to not be multiplied: " + userSummary)
                .isEqualTo(firstPoints);

        assertThat(userSummary.getPoints())
                .as("Expected user points to not be multiplied: " + userSummary)
                .isEqualTo(firstPoints);

        // Update the user with a new hardware with a multiplier
        final HardwareRequest hardwareWithMultiplier = generateHardwareWithMultiplier(2.0D);
        final int hardwareWithMultiplierId = HardwareUtils.create(hardwareWithMultiplier).getId();
        user.setHardwareId(hardwareWithMultiplierId);

        USER_REQUEST_SENDER.update(createdUser.getId(), user, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.setPoints(createdUser, secondPoints);
        manuallyUpdateStats();


        final CompetitionSummary resultAfterUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUpdate = getTeamFromCompetition(resultAfterUpdate, team.getTeamName());
        final UserSummary userSummaryAfterUpdate = getActiveUserFromTeam(teamSummaryAfterUpdate, user.getDisplayName());

        assertThat(userSummaryAfterUpdate.getMultipliedPoints())
                .as("Expected user multiplied points to be multiplied only after the second update: " + userSummaryAfterUpdate)
                .isEqualTo(firstPoints + (Math.round(secondPoints * hardwareWithMultiplier.getMultiplier())));

        assertThat(userSummaryAfterUpdate.getPoints())
                .as("Expected user points to not be multiplied: " + userSummaryAfterUpdate)
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


        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.getTeamName());

        assertThat(teamSummary.getActiveUsers())
                .as("Expected team to have 2 active users for first update: " + teamSummary)
                .hasSize(2);

        assertThat(teamSummary.getRetiredUsers())
                .as("Expected team to have no retired user for first update: " + teamSummary)
                .isEmpty();

        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(createdUserToRetire.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Expected user to be deleted: " + response)
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, secondPoints);
        StubbedFoldingEndpointUtils.setPoints(createdUserToRetire, secondPoints);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterRetirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.getTeamName());

        assertThat(teamSummaryAfterRetirement.getTeamMultipliedPoints())
                .as("Expected team to have points from first update for both users, then second update only for the first user: " + teamSummaryAfterRetirement)
                .isEqualTo(firstPoints + firstPoints + secondPoints);

        assertThat(teamSummaryAfterRetirement.getActiveUsers())
                .as("Expected team to have only 1 active user after other user was retired: " + teamSummaryAfterRetirement)
                .hasSize(1);

        assertThat(teamSummaryAfterRetirement.getRetiredUsers())
                .as("Expected team to have 1 retired user after user was retired: " + teamSummaryAfterRetirement)
                .hasSize(1);

        final UserSummary firstUserSummary = getActiveUserFromTeam(teamSummaryAfterRetirement, firstUser.getDisplayName());
        final RetiredUserSummary secondUserResult = getRetiredUserFromTeam(teamSummaryAfterRetirement, userToRetire.getDisplayName());

        assertThat(firstUserSummary.getMultipliedPoints())
                .as("Expected user to have points from both updates: " + firstUserSummary)
                .isEqualTo(firstPoints + secondPoints);

        assertThat(secondUserResult.getMultipliedPoints())
                .as("Expected retired user to have points from first update only: " + secondUserResult)
                .isEqualTo(firstPoints);

        UserUtils.create(userToRetire);
        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.setPoints(firstUser, thirdPoints);
        StubbedFoldingEndpointUtils.setPoints(createdUserToRetire, thirdPoints);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterUnretirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, team.getTeamName());

        assertThat(teamSummaryAfterUnretirement.getTeamMultipliedPoints())
                .as("Expected team to have points from first update for both users, second update only for the first user, then third update for both users: " + teamSummaryAfterUnretirement)
                .isEqualTo(firstPoints + firstPoints + secondPoints + thirdPoints + thirdPoints);

        assertThat(teamSummaryAfterUnretirement.getActiveUsers())
                .as("Expected team to have 2 active users after unretirement: " + teamSummaryAfterUnretirement)
                .hasSize(2);

        assertThat(teamSummaryAfterUnretirement.getRetiredUsers())
                .as("Expected team to have 1 retired user after user was unretired: " + teamSummaryAfterUnretirement)
                .hasSize(1);

        final UserSummary firstUserSummaryAfterUnretirement = getActiveUserFromTeam(teamSummaryAfterUnretirement, firstUser.getDisplayName());
        final UserSummary secondUserSummaryAfterUnretirement = getActiveUserFromTeam(teamSummaryAfterUnretirement, userToRetire.getDisplayName());

        assertThat(firstUserSummaryAfterUnretirement.getMultipliedPoints())
                .as("Expected user to have points from all three updates: " + firstUserSummaryAfterUnretirement)
                .isEqualTo(firstPoints + secondPoints + thirdPoints);

        assertThat(secondUserSummaryAfterUnretirement.getMultipliedPoints())
                .as("Expected user to have points from third updates only: " + secondUserSummaryAfterUnretirement)
                .isEqualTo(thirdPoints);

        final RetiredUserSummary retiredUserAfterUnretirement = getRetiredUserFromTeam(teamSummaryAfterRetirement, userToRetire.getDisplayName());

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

        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary originalTeamSummary = getTeamFromCompetition(result, originalTeam.getTeamName());
        final TeamSummary newTeamSummary = getTeamFromCompetition(result, newTeam.getTeamName());

        assertThat(originalTeamSummary.getActiveUsers())
                .as("Expected original team to have 2 active users at the start: " + originalTeamSummary)
                .hasSize(2);

        assertThat(originalTeamSummary.getRetiredUsers())
                .as("Expected original team to have no retired users at the start: " + originalTeamSummary)
                .isEmpty();

        assertThat(newTeamSummary.getActiveUsers())
                .as("Expected new team to have 1 active user at the start: " + newTeamSummary)
                .hasSize(1);

        assertThat(newTeamSummary.getRetiredUsers())
                .as("Expected new team to have no retired users at the start: " + newTeamSummary)
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

        final CompetitionSummary resultAfterUnretirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary originalTeamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, originalTeam.getTeamName());
        final TeamSummary newTeamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, newTeam.getTeamName());

        assertThat(originalTeamSummaryAfterUnretirement.getActiveUsers())
                .as("Expected original team to have 1 active user after unretirement: " + originalTeamSummaryAfterUnretirement)
                .hasSize(1);

        assertThat(originalTeamSummaryAfterUnretirement.getRetiredUsers())
                .as("Expected original team to have 1 retired user after unretirement: " + originalTeamSummaryAfterUnretirement)
                .hasSize(1);

        assertThat(originalTeamSummaryAfterUnretirement.getTeamMultipliedPoints())
                .as("Expected original team to points from before retirement only: " + originalTeamSummaryAfterUnretirement)
                .isEqualTo(firstPoints);

        assertThat(newTeamSummaryAfterUnretirement.getActiveUsers())
                .as("Expected new team to have 1 active user after unretirement: " + newTeamSummaryAfterUnretirement)
                .hasSize(2);

        assertThat(newTeamSummaryAfterUnretirement.getRetiredUsers())
                .as("Expected new team to have no retired users after unretirement: " + newTeamSummaryAfterUnretirement)
                .isEmpty();

        assertThat(newTeamSummaryAfterUnretirement.getTeamMultipliedPoints())
                .as("Expected new team to points from after unretirement only: " + originalTeamSummaryAfterUnretirement)
                .isEqualTo(thirdPoints);

        final RetiredUserSummary retiredUserSummaryAfterUnretirement = getRetiredUserFromTeam(originalTeamSummaryAfterUnretirement, userToRetire.getDisplayName());
        final UserSummary activeUserSummaryAfterUnretirement = getActiveUserFromTeam(newTeamSummaryAfterUnretirement, userToRetire.getDisplayName());

        assertThat(retiredUserSummaryAfterUnretirement.getMultipliedPoints())
                .as("Expected retired user to have points from before retirement only: " + retiredUserSummaryAfterUnretirement)
                .isEqualTo(firstPoints);

        assertThat(activeUserSummaryAfterUnretirement.getMultipliedPoints())
                .as("Expected unretired user to have points from after unretirement only: " + activeUserSummaryAfterUnretirement)
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

        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.getTeamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.getDisplayName());

        assertThat(userSummary.getPoints())
                .as("Expected user points to be stats + offset: " + userSummary)
                .isEqualTo(firstPoints + pointsOffset);

        assertThat(userSummary.getMultipliedPoints())
                .as("Expected user multiplied points to be stats + offset: " + userSummary)
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

        final CompetitionSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.getTeamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.getDisplayName());

        assertThat(userSummary.getPoints())
                .as("Expected user points to be 0: " + userSummary)
                .isEqualTo(0L);

        assertThat(userSummary.getMultipliedPoints())
                .as("Expected user multiplied points to be 0: " + userSummary)
                .isEqualTo(0L);

        assertThat(userSummary.getUnits())
                .as("Expected user units to be 0: " + userSummary)
                .isEqualTo(0);
    }

    @Test
    void whenOneTeamExistsWithOneUser_andUserEarnsStats_thenUserStartsWithNoStats_thenIncrementsAsUserPointsIncrease() throws FoldingRestException {
        final User user = UserUtils.create(generateUser());
        final int userId = user.getId();

        manuallyUpdateStats();
        final UserSummary resultBeforeStats = TeamCompetitionStatsUtils.getStatsForUser(userId);

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

        final UserSummary resultAfterStats = TeamCompetitionStatsUtils.getStatsForUser(userId);

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
        final UserSummary resultBeforeStats = TeamCompetitionStatsUtils.getStatsForUser(secondInTeamThirdOverallId);
        assertThat(resultBeforeStats.getRankInTeam())
                .as("Expected all users to start at rank 1: " + resultBeforeStats)
                .isEqualTo(1);

        StubbedFoldingEndpointUtils.setPoints(firstInTeamFirstOverall, 10_000L);
        StubbedFoldingEndpointUtils.setPoints(secondInTeamThirdOverall, 1_000L);
        StubbedFoldingEndpointUtils.setPoints(firstInTeamSecondOverall, 5_000L);
        manuallyUpdateStats();

        final UserSummary resultAfterStats = TeamCompetitionStatsUtils.getStatsForUser(secondInTeamThirdOverallId);
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
        final UserRequest user = generateUserWithHardwareId(hardware.getId());

        final int userId = UserUtils.create(user).getId();
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.offset(userId, 100L, Math.round(100L * hardware.getMultiplier()), 10);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenPatchingAUserWithPointsOffsets_givenEmptyPayload_thenRequestFails_andResponseHasA400StatusCode() throws IOException, InterruptedException, FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final UserRequest user = generateUserWithHardwareId(hardware.getId());
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
