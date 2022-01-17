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

package me.zodac.folding.test;

import static me.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.rest.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.util.TestGenerator.generateHardware;
import static me.zodac.folding.test.util.TestGenerator.generateHardwareWithMultiplier;
import static me.zodac.folding.test.util.TestGenerator.generateTeam;
import static me.zodac.folding.test.util.TestGenerator.generateUser;
import static me.zodac.folding.test.util.TestGenerator.generateUserWithHardwareId;
import static me.zodac.folding.test.util.TestGenerator.generateUserWithHardwareIdAndTeamId;
import static me.zodac.folding.test.util.TestGenerator.generateUserWithTeamId;
import static me.zodac.folding.test.util.TestGenerator.generateUserWithTeamIdAndCategory;
import static me.zodac.folding.test.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.getRetiredUserFromTeam;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.getTeamFromCompetition;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.manuallyUpdateStats;
import static me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils.offsetUserPoints;
import static me.zodac.folding.test.util.rest.request.UserUtils.USER_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.TeamCompetitionStatsResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.RetiredUserSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.util.TestConstants;
import me.zodac.folding.test.util.rest.request.HardwareUtils;
import me.zodac.folding.test.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils;
import me.zodac.folding.test.util.rest.request.TeamUtils;
import me.zodac.folding.test.util.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the <code>Team Competition</code> stats calculation.
 *
 * <p>
 * Since the TC stats are done on the full system (meaning all {@link Team}s), we wipe the system before each test with a {@link BeforeEach} method.
 */
class TeamCompetitionStatsTest {

    @BeforeEach
    void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    void whenNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoStats_andNoTeams() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getStats();

        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionSummary result = TeamCompetitionStatsResponseParser.getStats(response);

        assertThat(result.getTeams())
            .as("Expected no teams: " + result)
            .isEmpty();

        assertThat(result.getTotalPoints())
            .as("Expected no points: " + result)
            .isZero();

        assertThat(result.getTotalMultipliedPoints())
            .as("Expected no multiplied points: " + result)
            .isZero();

        assertThat(result.getTotalUnits())
            .as("Expected no units: " + result)
            .isZero();
    }

    @Test
    void whenOneTeamExistsWithOneUser_andUserEarnsStats_thenUserAndTeamAndOverallStartWithNoStats_thenAllIncrementAsUserPointsIncrease()
        throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamId(team.getId()));

        final CompetitionSummary resultBeforeStats = TeamCompetitionStatsUtils.getStats();

        assertThat(resultBeforeStats.getTotalPoints())
            .as("Expected no points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.getTotalMultipliedPoints())
            .as("Expected no multiplied points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.getTotalUnits())
            .as("Expected no units: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.getTeams())
            .as("Expected exactly 1 team: " + resultBeforeStats)
            .hasSize(1);

        final TeamSummary teamSummaryBeforeStats = getTeamFromCompetition(resultBeforeStats, team.getTeamName());

        assertThat(teamSummaryBeforeStats.getTeamPoints())
            .as("Expected no points for team: " + teamSummaryBeforeStats)
            .isZero();

        assertThat(teamSummaryBeforeStats.getTeamMultipliedPoints())
            .as("Expected no multiplied points for team: " + teamSummaryBeforeStats)
            .isZero();

        assertThat(teamSummaryBeforeStats.getTeamUnits())
            .as("Expected no units for team: " + teamSummaryBeforeStats)
            .isZero();

        assertThat(teamSummaryBeforeStats.getActiveUsers())
            .as("Expected exactly 1 active user: " + teamSummaryBeforeStats)
            .hasSize(1);

        assertThat(teamSummaryBeforeStats.getRetiredUsers())
            .as("Expected no retired users: " + teamSummaryBeforeStats)
            .isEmpty();

        final UserSummary userSummaryBeforeStats = getActiveUserFromTeam(teamSummaryBeforeStats, user.getDisplayName());

        assertThat(userSummaryBeforeStats.getPoints())
            .as("Expected no points for user: " + userSummaryBeforeStats)
            .isZero();

        assertThat(userSummaryBeforeStats.getMultipliedPoints())
            .as("Expected no multiplied points for user: " + userSummaryBeforeStats)
            .isZero();

        assertThat(userSummaryBeforeStats.getUnits())
            .as("Expected no units for user: " + userSummaryBeforeStats)
            .isZero();

        final long newPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(user, newPoints);

        final int newUnits = 5;
        StubbedFoldingEndpointUtils.addUnits(user, newUnits);

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
    void whenOneTeamExistsWithTwoUser_andUserEarnsStats_thenBothUsersStartAtRank1_thenRanksUpdateCorrectlyAsUsersEarnStats()
        throws FoldingRestException {
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

        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.addUnits(firstUser, 10);
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

        StubbedFoldingEndpointUtils.addPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.addUnits(secondUser, 20);
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
    void whenTwoTeamsExistsWithOneUserEach_andUserEarnsStats_thenBothTeamsStartAtRank1_thenTeamRanksUpdateCorrectlyAsUsersEarnStats()
        throws FoldingRestException {
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

        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.addUnits(firstUser, 10);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, firstTeam.getTeamName());
        final TeamSummary secondTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, secondTeam.getTeamName());

        assertThat(firstTeamSummaryAfterFirstUpdate.getRank())
            .as("Expected first team to be rank 1: " + resultAfterFirstUpdate)
            .isEqualTo(1);

        assertThat(secondTeamSummaryAfterFirstUpdate.getRank())
            .as("Expected second team to be rank 2: " + resultAfterFirstUpdate)
            .isEqualTo(2);

        StubbedFoldingEndpointUtils.addPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.addUnits(secondUser, 20);
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
    void whenTeamExistsWithOneUser_andUserHasHardwareMultiplier_thenUserPointsAreMultipliedCorrectly_andUserUnitsAreNotImpacted()
        throws FoldingRestException {
        final double hardwareMultiplier = 2.00D;
        final int hardwareId = HardwareUtils.create(generateHardwareWithMultiplier(hardwareMultiplier)).getId();
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareId(hardwareId);
        user.setTeamId(team.getId());
        final User createdUser = UserUtils.create(user);

        final long newPoints = 20_000L;
        final int newUnits = 20;
        StubbedFoldingEndpointUtils.addPoints(createdUser, newPoints);
        StubbedFoldingEndpointUtils.addUnits(createdUser, newUnits);
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
    void whenTeamExistsWithOneUser_andUserIsUpdatedWithNewHardwareMultiplier_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly()
        throws FoldingRestException {
        final Hardware createdHardware = HardwareUtils.create(generateHardware());
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareId(createdHardware.getId());
        user.setTeamId(team.getId());
        final User createdUser = UserUtils.create(user);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, firstPoints);
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
            .hardwareMake(createdHardware.getHardwareMake().toString())
            .hardwareType(createdHardware.getHardwareType().toString())
            .multiplier(2.00D)
            .averagePpd(createdHardware.getAveragePpd())
            .build();

        HARDWARE_REQUEST_SENDER.update(createdHardware.getId(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, secondPoints);
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
    void whenTeamExistsWithOneUser_andUserIsUpdatedWithNewHardware_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly()
        throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).getId();
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareId(hardwareId);
        user.setTeamId(team.getId());
        final User createdUser = UserUtils.create(user);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, firstPoints);
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
        final HardwareRequest hardwareWithMultiplier = generateHardwareWithMultiplier(2.00D);
        final int hardwareWithMultiplierId = HardwareUtils.create(hardwareWithMultiplier).getId();
        user.setHardwareId(hardwareWithMultiplierId);

        USER_REQUEST_SENDER.update(createdUser.getId(), user, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, secondPoints);
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
    void whenTeamExistsWithUsers_andUserIsDeletedAndReAdded_thenOriginalStatsRemainOnTeam_andNewStatsWhileDeletedAreNotAdded_andStatsAfterAreCounted()
        throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.AMD_GPU));

        final UserRequest userToRetire = generateUserWithTeamIdAndCategory(team.getId(), Category.NVIDIA_GPU);
        final User createdUserToRetire = UserUtils.create(userToRetire);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(firstUser, firstPoints);
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, firstPoints);
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
        StubbedFoldingEndpointUtils.addPoints(firstUser, secondPoints);
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, secondPoints);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterRetirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.getTeamName());

        assertThat(teamSummaryAfterRetirement.getTeamMultipliedPoints())
            .as("Expected team to have points from first update for both users, then second update only for the first user: "
                + teamSummaryAfterRetirement)
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
        StubbedFoldingEndpointUtils.addPoints(firstUser, thirdPoints);
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, thirdPoints);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterUnretirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, team.getTeamName());

        assertThat(teamSummaryAfterUnretirement.getTeamMultipliedPoints())
            .as("Expected team to have points from first update for both users, second update for the first user, third update for both users: "
                + teamSummaryAfterUnretirement)
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
    void whenTeamExistsWithUsers_andUserIsDeleted_andUserIsReAddedToNewTeam_thenOriginalStatsRemainOnFirstTeam_andNewTeamGetsStatsAfterUnretirement()
        throws FoldingRestException {
        final Team originalTeam = TeamUtils.create(generateTeam());

        UserUtils.create(generateUserWithTeamIdAndCategory(originalTeam.getId(), Category.NVIDIA_GPU));
        final UserRequest userToRetire = generateUserWithTeamIdAndCategory(originalTeam.getId(), Category.AMD_GPU);
        final User createdUserToRetire = UserUtils.create(userToRetire);

        final Team newTeam = TeamUtils.create(generateTeam());
        UserUtils.create(generateUserWithTeamIdAndCategory(newTeam.getId(), Category.NVIDIA_GPU));

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, firstPoints);
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

        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(createdUserToRetire.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Expected user to be deleted: " + response)
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, secondPoints);
        manuallyUpdateStats();

        userToRetire.setTeamId(newTeam.getId());
        UserUtils.create(userToRetire);

        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, thirdPoints);
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

        final RetiredUserSummary retiredUserSummaryAfterUnretirement =
            getRetiredUserFromTeam(originalTeamSummaryAfterUnretirement, userToRetire.getDisplayName());
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
        StubbedFoldingEndpointUtils.addPoints(user, firstPoints);
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
    void whenOneTeamHasOneUser_andUserHasMultipleOffsetsApplied_thenUserOffsetIsAppendedToStats() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamId(team.getId()));

        final long initialPoints = 2_500L;
        StubbedFoldingEndpointUtils.addPoints(user, initialPoints);
        manuallyUpdateStats();

        final long firstPointsOffset = 1_000L;
        offsetUserPoints(user, firstPointsOffset);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterFirstOffset = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterFirstOffset = getTeamFromCompetition(resultAfterFirstOffset, team.getTeamName());
        final UserSummary userSummaryAfterFirstOffset = getActiveUserFromTeam(teamSummaryAfterFirstOffset, user.getDisplayName());

        assertThat(userSummaryAfterFirstOffset.getPoints())
            .as("Expected user points to be stats + first offset: " + userSummaryAfterFirstOffset)
            .isEqualTo(initialPoints + firstPointsOffset);

        assertThat(userSummaryAfterFirstOffset.getMultipliedPoints())
            .as("Expected user multiplied points to be stats + first offset: " + userSummaryAfterFirstOffset)
            .isEqualTo(initialPoints + firstPointsOffset);

        final long secondPointsOffset = 250L;
        offsetUserPoints(user, secondPointsOffset);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterSecondOffset = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterSecondOffset = getTeamFromCompetition(resultAfterSecondOffset, team.getTeamName());
        final UserSummary userSummaryAfterSecondOffset = getActiveUserFromTeam(teamSummaryAfterSecondOffset, user.getDisplayName());

        assertThat(userSummaryAfterSecondOffset.getPoints())
            .as("Expected user points to be stats + both offsets: " + userSummaryAfterSecondOffset)
            .isEqualTo(initialPoints + firstPointsOffset + secondPointsOffset);

        assertThat(userSummaryAfterSecondOffset.getMultipliedPoints())
            .as("Expected user multiplied points to be stats + both offsets: " + userSummaryAfterSecondOffset)
            .isEqualTo(initialPoints + firstPointsOffset + secondPointsOffset);
    }

    @Test
    void whenOneTeamHasOneUser_andUserHasOffsetApplied_andOffsetIsNegative_andOffsetIsGreaterThanCurrentUserStats_thenUserHasZeroStats()
        throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamId(team.getId()));
        final int userId = user.getId();

        final long firstPoints = 2_500L;
        final int firstUnits = 25;
        StubbedFoldingEndpointUtils.addPoints(user, firstPoints);
        StubbedFoldingEndpointUtils.addUnits(user, firstUnits);
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
            .isZero();

        assertThat(userSummary.getMultipliedPoints())
            .as("Expected user multiplied points to be 0: " + userSummary)
            .isZero();

        assertThat(userSummary.getUnits())
            .as("Expected user units to be 0: " + userSummary)
            .isZero();
    }

    @Test
    void whenOneTeamExistsWithOneUser_andUserEarnsStats_thenUserStartsWithNoStats_thenIncrementsAsUserPointsIncrease() throws FoldingRestException {
        final User user = UserUtils.create(generateUser());
        final int userId = user.getId();

        manuallyUpdateStats();
        final UserSummary resultBeforeStats = TeamCompetitionStatsUtils.getStatsForUser(userId);

        assertThat(resultBeforeStats.getPoints())
            .as("Expected no points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.getMultipliedPoints())
            .as("Expected no multiplied points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.getUnits())
            .as("Expected no units: " + resultBeforeStats)
            .isZero();

        final long newPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(user, newPoints);

        final int newUnits = 5;
        StubbedFoldingEndpointUtils.addUnits(user, newUnits);

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

        StubbedFoldingEndpointUtils.addPoints(firstInTeamFirstOverall, 10_000L);
        StubbedFoldingEndpointUtils.addPoints(secondInTeamThirdOverall, 1_000L);
        StubbedFoldingEndpointUtils.addPoints(firstInTeamSecondOverall, 5_000L);
        manuallyUpdateStats();

        final UserSummary resultAfterStats = TeamCompetitionStatsUtils.getStatsForUser(secondInTeamThirdOverallId);
        assertThat(resultAfterStats.getRankInTeam())
            .as("Expected user to be third overall, but second in team: " + resultBeforeStats)
            .isEqualTo(2);
    }

    @Test
    void whenGettingStatsForUser_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getStatsForUser(TestConstants.NON_EXISTING_ID);
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingStatsForUser_givenOutOfRangeUserId_thenResponseHas400Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getStatsForUser(TestConstants.OUT_OF_RANGE_ID);

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("out of range");
    }

    @Test
    void whenGettingStatsForUser_givenAnInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/stats/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenPatchingUserWithPointsOffsets_givenThePayloadIsValid_thenResponseHas200Status() throws FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final UserRequest user = generateUserWithHardwareId(hardware.getId());

        final int userId = UserUtils.create(user).getId();
        final HttpResponse<Void> patchResponse = TEAM_COMPETITION_REQUEST_SENDER
            .offset(userId, 100L, Math.round(100L * hardware.getMultiplier()), 10, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(patchResponse.statusCode())
            .as("Was not able to patch user: " + patchResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    void whenPatchingUserWithPointsOffsets_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<Void> response =
            TEAM_COMPETITION_REQUEST_SENDER.offset(TestConstants.NON_EXISTING_ID, 100L, 1_000L, 10, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Was able to patch user, was expected user to not be found: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenPatchingUserWithPointsOffsets_givenOutOfRangeUserId_thenResponseHas400Status() throws FoldingRestException {
        final HttpResponse<Void> response =
            TEAM_COMPETITION_REQUEST_SENDER.offset(TestConstants.OUT_OF_RANGE_ID, 100L, 1_000L, 10, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenPatchingUserWithPointsOffsets_givenAnInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .method("PATCH", HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/stats/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenPatchingUserWithPointsOffsets_givenNoAuthentication_thenRequestFails_andResponseHas401Status() throws FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final UserRequest user = generateUserWithHardwareId(hardware.getId());

        final int userId = UserUtils.create(user).getId();
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.offset(userId, 100L, Math.round(100L * hardware.getMultiplier()), 10);
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenPatchingUserWithPointsOffsets_givenEmptyPayload_thenRequestFails_andResponseHas400Status()
        throws IOException, InterruptedException, FoldingRestException {
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

    @Test
    void whenChangingHardwareMultiplier_givenUserIsUsingHardware_andUserHasAnOffsetApplied_andAnotherOffsetIsAdded_thenInitialOffsetShouldBeIgnored()
        throws FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardwareWithMultiplier(1.00D));
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithHardwareIdAndTeamId(hardware.getId(), team.getId()));

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(user, firstPoints);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, team.getTeamName());
        final UserSummary userAfterFirstUpdate = getActiveUserFromTeam(teamAfterFirstUpdate, user.getDisplayName());

        assertThat(userAfterFirstUpdate.getPoints())
            .as("Expected initial points for user: " + userAfterFirstUpdate)
            .isEqualTo(firstPoints);

        final long firstOffsetPoints = 2_000L;
        offsetUserPoints(user, firstOffsetPoints);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, team.getTeamName());
        final UserSummary userAfterSecondUpdate = getActiveUserFromTeam(teamAfterSecondUpdate, user.getDisplayName());

        assertThat(userAfterSecondUpdate.getPoints())
            .as("Expected initial points + first offset points for user: " + userAfterSecondUpdate)
            .isEqualTo(firstPoints + firstOffsetPoints);

        // Update hardware, should clear all offsets from the user
        final double newMultiplier = 2.00D;
        final HardwareRequest updatedHardware = HardwareRequest.builder()
            .hardwareName(hardware.getHardwareName())
            .displayName(hardware.getDisplayName())
            .hardwareMake(hardware.getHardwareMake().toString())
            .hardwareType(hardware.getHardwareType().toString())
            .multiplier(newMultiplier)
            .averagePpd(hardware.getAveragePpd())
            .build();
        final HttpResponse<String> hardwareUpdateResponse =
            HARDWARE_REQUEST_SENDER.update(hardware.getId(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(hardwareUpdateResponse.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + hardwareUpdateResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final long secondPoints = 333L;
        StubbedFoldingEndpointUtils.addPoints(user, secondPoints);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterThirdUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterThirdUpdate = getTeamFromCompetition(resultAfterThirdUpdate, team.getTeamName());
        final UserSummary userAfterThirdUpdate = getActiveUserFromTeam(teamAfterThirdUpdate, user.getDisplayName());

        assertThat(userAfterThirdUpdate.getPoints())
            .as("Expected initial points + first offset points + second points for user: " + userAfterThirdUpdate)
            .isEqualTo(firstPoints + firstOffsetPoints + secondPoints);

        final long secondOffsetPoints = 95L;
        offsetUserPoints(user, secondOffsetPoints);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterFourthUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterFourthUpdate = getTeamFromCompetition(resultAfterFourthUpdate, team.getTeamName());
        final UserSummary userAfterFourthUpdate = getActiveUserFromTeam(teamAfterFourthUpdate, user.getDisplayName());

        assertThat(userAfterFourthUpdate.getPoints())
            .as("Expected initial points + first offset points + second points for user: " + userAfterFourthUpdate)
            .isEqualTo(firstPoints + firstOffsetPoints + secondPoints + secondOffsetPoints);
    }

    @Test
    void whenUserIsUpdated_givenUserChangesTeam_andUserHasStatsOnOldTeam_thenRetiredUserWithStatsAddedToOldTeam_andUserHasNoStatsForNewTeam()
        throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(firstTeam.getId(), Category.AMD_GPU));
        final User secondUser = UserUtils.create(generateUserWithTeamIdAndCategory(firstTeam.getId(), Category.NVIDIA_GPU));

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User thirdUser = UserUtils.create(generateUserWithTeamIdAndCategory(secondTeam.getId(), Category.NVIDIA_GPU));

        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.addPoints(secondUser, 5_000L);
        StubbedFoldingEndpointUtils.addPoints(thirdUser, 1_000L);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, firstTeam.getTeamName());
        final TeamSummary secondTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, secondTeam.getTeamName());

        assertThat(firstTeamSummaryAfterFirstUpdate.getTeamMultipliedPoints())
            .as("Expected first team to have points of first and second user")
            .isEqualTo(15_000L);
        assertThat(firstTeamSummaryAfterFirstUpdate.getRetiredUsers())
            .as("Expected first team to have no retired users")
            .isEmpty();

        assertThat(secondTeamSummaryAfterFirstUpdate.getTeamMultipliedPoints())
            .as("Expected second team to have points of third user")
            .isEqualTo(1_000L);
        assertThat(secondTeamSummaryAfterFirstUpdate.getActiveUsers())
            .as("Expected second team to have one active user")
            .hasSize(1);

        final UserRequest updatedFirstUserRequest = UserRequest.builder()
            .foldingUserName(firstUser.getFoldingUserName())
            .displayName(firstUser.getDisplayName())
            .passkey(firstUser.getPasskey())
            .category(firstUser.getCategory().toString())
            .hardwareId(firstUser.getHardware().getId())
            .teamId(secondTeam.getId())
            .build();

        final User updatedFirstUser = UserUtils.update(firstUser.getId(), updatedFirstUserRequest);

        final CompetitionSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, firstTeam.getTeamName());
        final TeamSummary secondTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, secondTeam.getTeamName());

        assertThat(firstTeamSummaryAfterSecondUpdate.getTeamMultipliedPoints())
            .as("Expected first team to have no change to points")
            .isEqualTo(15_000L);
        assertThat(firstTeamSummaryAfterSecondUpdate.getRetiredUsers())
            .as("Expected first team to have one retired user")
            .hasSize(1);

        assertThat(secondTeamSummaryAfterSecondUpdate.getTeamMultipliedPoints())
            .as("Expected second team to have no change to points")
            .isEqualTo(1_000L);
        assertThat(secondTeamSummaryAfterSecondUpdate.getActiveUsers())
            .as("Expected second team to have two active users")
            .hasSize(2);

        StubbedFoldingEndpointUtils.addPoints(updatedFirstUser, 10_000L);
        manuallyUpdateStats();

        final CompetitionSummary resultAfterThirdUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterThirdUpdate = getTeamFromCompetition(resultAfterThirdUpdate, firstTeam.getTeamName());
        final TeamSummary secondTeamSummaryAfterThirdUpdate = getTeamFromCompetition(resultAfterThirdUpdate, secondTeam.getTeamName());

        assertThat(firstTeamSummaryAfterThirdUpdate.getTeamMultipliedPoints())
            .as("Expected first team to have no change to points")
            .isEqualTo(15_000L);
        assertThat(firstTeamSummaryAfterThirdUpdate.getRetiredUsers())
            .as("Expected first team to have one retired user")
            .hasSize(1);

        assertThat(secondTeamSummaryAfterThirdUpdate.getTeamMultipliedPoints())
            .as("Expected second team to have new points from the moved user")
            .isEqualTo(11_000L);
    }
}
