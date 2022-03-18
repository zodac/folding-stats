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
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.client.java.response.TeamCompetitionStatsResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.AllTeamsSummary;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.RetiredUserSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.rest.util.RestUtilConstants;
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

        final AllTeamsSummary result = TeamCompetitionStatsResponseParser.getStats(response);

        assertThat(result.getTeams())
            .as("Expected no teams: " + result)
            .isEmpty();

        assertThat(result.getCompetitionSummary().getTotalPoints())
            .as("Expected no points: " + result)
            .isZero();

        assertThat(result.getCompetitionSummary().getTotalMultipliedPoints())
            .as("Expected no multiplied points: " + result)
            .isZero();

        assertThat(result.getCompetitionSummary().getTotalUnits())
            .as("Expected no units: " + result)
            .isZero();
    }

    @Test
    void whenNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoOverallStats() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getOverallStats();

        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionSummary result = TeamCompetitionStatsResponseParser.getOverallStats(response);

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
        final User user = UserUtils.create(generateUserWithTeamId(team.id()));

        final AllTeamsSummary resultBeforeStats = TeamCompetitionStatsUtils.getStats();

        assertThat(resultBeforeStats.getCompetitionSummary().getTotalPoints())
            .as("Expected no points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.getCompetitionSummary().getTotalMultipliedPoints())
            .as("Expected no multiplied points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.getCompetitionSummary().getTotalUnits())
            .as("Expected no units: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.getTeams())
            .as("Expected exactly 1 team: " + resultBeforeStats)
            .hasSize(1);

        final TeamSummary teamSummaryBeforeStats = getTeamFromCompetition(resultBeforeStats, team.teamName());

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

        final UserSummary userSummaryBeforeStats = getActiveUserFromTeam(teamSummaryBeforeStats, user.displayName());

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

        final AllTeamsSummary resultAfterStats = TeamCompetitionStatsUtils.getStats();

        assertThat(resultAfterStats.getCompetitionSummary().getTotalPoints())
            .as("Expected updated points: " + resultAfterStats)
            .isEqualTo(newPoints);

        assertThat(resultAfterStats.getCompetitionSummary().getTotalMultipliedPoints())
            .as("Expected updated multiplied points: " + resultAfterStats)
            .isEqualTo(newPoints);

        assertThat(resultAfterStats.getCompetitionSummary().getTotalUnits())
            .as("Expected updated units: " + resultAfterStats)
            .isEqualTo(newUnits);

        assertThat(resultAfterStats.getTeams())
            .as("Expected exactly 1 team: " + resultAfterStats)
            .hasSize(1);

        final TeamSummary teamSummaryAfterStats = getTeamFromCompetition(resultAfterStats, team.teamName());

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

        final UserSummary userSummaryAfterStats = getActiveUserFromTeam(teamSummaryAfterStats, user.displayName());

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
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(team.id(), Category.AMD_GPU));
        final User secondUser = UserUtils.create(generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU));

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final Collection<UserSummary> activeUserSummaries = teamSummary.getActiveUsers();

        assertThat(activeUserSummaries)
            .as("Expected exactly 2 active users: " + teamSummary)
            .hasSize(2);

        final UserSummary firstUserSummary = getActiveUserFromTeam(teamSummary, firstUser.displayName());
        final UserSummary secondUserSummary = getActiveUserFromTeam(teamSummary, secondUser.displayName());

        assertThat(firstUserSummary.getRankInTeam())
            .as("Expected first user to be rank 1: " + firstUserSummary)
            .isEqualTo(1);

        assertThat(secondUserSummary.getRankInTeam())
            .as("Expected second user to be rank 1: " + secondUserSummary)
            .isEqualTo(1);

        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.addUnits(firstUser, 10);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, team.teamName());
        final UserSummary firstUserSummaryAfterFirstUpdate = getActiveUserFromTeam(teamSummaryAfterFirstUpdate, firstUser.displayName());
        final UserSummary secondUserSummaryAfterFirstUpdate = getActiveUserFromTeam(teamSummaryAfterFirstUpdate, secondUser.displayName());

        assertThat(firstUserSummaryAfterFirstUpdate.getRankInTeam())
            .as("Expected first user to be rank 1: " + teamSummaryAfterFirstUpdate)
            .isEqualTo(1);

        assertThat(secondUserSummaryAfterFirstUpdate.getRankInTeam())
            .as("Expected second user to be rank 2: " + teamSummaryAfterFirstUpdate)
            .isEqualTo(2);

        StubbedFoldingEndpointUtils.addPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.addUnits(secondUser, 20);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, team.teamName());
        final UserSummary firstUserSummaryAfterSecondUpdate = getActiveUserFromTeam(teamSummaryAfterSecondUpdate, firstUser.displayName());
        final UserSummary secondUserSummaryAfterSecondUpdate = getActiveUserFromTeam(teamSummaryAfterSecondUpdate, secondUser.displayName());

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
        final User firstUser = UserUtils.create(generateUserWithTeamId(firstTeam.id()));

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User secondUser = UserUtils.create(generateUserWithTeamId(secondTeam.id()));

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        assertThat(result.getTeams())
            .as("Expected exactly 2 teams: " + result)
            .hasSize(2);

        final TeamSummary firstTeamSummary = getTeamFromCompetition(result, firstTeam.teamName());
        final TeamSummary secondTeamSummary = getTeamFromCompetition(result, secondTeam.teamName());

        assertThat(firstTeamSummary.getRank())
            .as("Expected first team to be rank 1: " + firstTeamSummary)
            .isEqualTo(1);

        assertThat(secondTeamSummary.getRank())
            .as("Expected second team to be rank 1: " + secondTeamSummary)
            .isEqualTo(1);

        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.addUnits(firstUser, 10);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterFirstUpdate.getRank())
            .as("Expected first team to be rank 1: " + resultAfterFirstUpdate)
            .isEqualTo(1);

        assertThat(secondTeamSummaryAfterFirstUpdate.getRank())
            .as("Expected second team to be rank 2: " + resultAfterFirstUpdate)
            .isEqualTo(2);

        StubbedFoldingEndpointUtils.addPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.addUnits(secondUser, 20);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterSecondUpdate.getRank())
            .as("Expected first team to be rank 2: " + firstTeamSummaryAfterSecondUpdate)
            .isEqualTo(2);

        assertThat(secondTeamSummaryAfterSecondUpdate.getRank())
            .as("Expected second team to be rank 1: " + secondTeamSummaryAfterSecondUpdate)
            .isEqualTo(1);

        final HttpResponse<String> overallResultAfterSecondUpdate = TEAM_COMPETITION_REQUEST_SENDER.getOverallStats();
        final CompetitionSummary overallResult = TeamCompetitionStatsResponseParser.getOverallStats(overallResultAfterSecondUpdate);

        assertThat(overallResult)
            .as("Expected overall stats to be same as competition stats")
            .isEqualTo(resultAfterSecondUpdate.getCompetitionSummary());

        assertThat(overallResult.getTotalMultipliedPoints())
            .isEqualTo(30_000L);

        assertThat(overallResult.getTotalUnits())
            .isEqualTo(30);
    }

    @Test
    void whenTeamExistsWithOneUser_andUserHasHardwareMultiplier_thenUserPointsAreMultipliedCorrectly_andUserUnitsAreNotImpacted()
        throws FoldingRestException {
        final double hardwareMultiplier = 2.00D;
        final int hardwareId = HardwareUtils.create(generateHardwareWithMultiplier(hardwareMultiplier)).id();
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareId(hardwareId);
        user.setTeamId(team.id());
        final User createdUser = UserUtils.create(user);

        final long newPoints = 20_000L;
        final int newUnits = 20;
        StubbedFoldingEndpointUtils.addPoints(createdUser, newPoints);
        StubbedFoldingEndpointUtils.addUnits(createdUser, newUnits);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
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

        final UserRequest user = generateUserWithHardwareId(createdHardware.id());
        user.setTeamId(team.id());
        final User createdUser = UserUtils.create(user);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, firstPoints);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.getDisplayName());

        assertThat(userSummary.getMultipliedPoints())
            .as("Expected user multiplied points to not be multiplied: " + userSummary)
            .isEqualTo(firstPoints);

        assertThat(userSummary.getPoints())
            .as("Expected user points to not be multiplied: " + userSummary)
            .isEqualTo(firstPoints);

        // Change the multiplier on the hardware, no need to update the user
        final HardwareRequest updatedHardware = HardwareRequest.builder()
            .hardwareName(createdHardware.hardwareName())
            .displayName(createdHardware.displayName())
            .hardwareMake(createdHardware.hardwareMake().toString())
            .hardwareType(createdHardware.hardwareType().toString())
            .multiplier(2.00D)
            .averagePpd(createdHardware.averagePpd())
            .build();

        HARDWARE_REQUEST_SENDER.update(createdHardware.id(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, secondPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUpdate = getTeamFromCompetition(resultAfterUpdate, team.teamName());
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
        final int hardwareId = HardwareUtils.create(generateHardware()).id();
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareId(hardwareId);
        user.setTeamId(team.id());
        final User createdUser = UserUtils.create(user);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, firstPoints);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.getDisplayName());

        assertThat(userSummary.getMultipliedPoints())
            .as("Expected user multiplied points to not be multiplied: " + userSummary)
            .isEqualTo(firstPoints);

        assertThat(userSummary.getPoints())
            .as("Expected user points to not be multiplied: " + userSummary)
            .isEqualTo(firstPoints);

        // Update the user with a new hardware with a multiplier
        final HardwareRequest hardwareWithMultiplier = generateHardwareWithMultiplier(2.00D);
        final int hardwareWithMultiplierId = HardwareUtils.create(hardwareWithMultiplier).id();
        user.setHardwareId(hardwareWithMultiplierId);

        USER_REQUEST_SENDER.update(createdUser.id(), user, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, secondPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUpdate = getTeamFromCompetition(resultAfterUpdate, team.teamName());
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
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(team.id(), Category.AMD_GPU));

        final UserRequest userToRetire = generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU);
        final User createdUserToRetire = UserUtils.create(userToRetire);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(firstUser, firstPoints);
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, firstPoints);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());

        assertThat(teamSummary.getActiveUsers())
            .as("Expected team to have 2 active users for first update: " + teamSummary)
            .hasSize(2);

        assertThat(teamSummary.getRetiredUsers())
            .as("Expected team to have no retired user for first update: " + teamSummary)
            .isEmpty();

        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(createdUserToRetire.id(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Expected user to be deleted: " + response)
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.addPoints(firstUser, secondPoints);
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, secondPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterRetirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterRetirement = getTeamFromCompetition(resultAfterRetirement, team.teamName());

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

        final UserSummary firstUserSummary = getActiveUserFromTeam(teamSummaryAfterRetirement, firstUser.displayName());
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

        final AllTeamsSummary resultAfterUnretirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, team.teamName());

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

        final UserSummary firstUserSummaryAfterUnretirement = getActiveUserFromTeam(teamSummaryAfterUnretirement, firstUser.displayName());
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

        UserUtils.create(generateUserWithTeamIdAndCategory(originalTeam.id(), Category.NVIDIA_GPU));
        final UserRequest userToRetire = generateUserWithTeamIdAndCategory(originalTeam.id(), Category.AMD_GPU);
        final User createdUserToRetire = UserUtils.create(userToRetire);

        final Team newTeam = TeamUtils.create(generateTeam());
        UserUtils.create(generateUserWithTeamIdAndCategory(newTeam.id(), Category.NVIDIA_GPU));

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, firstPoints);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary originalTeamSummary = getTeamFromCompetition(result, originalTeam.teamName());
        final TeamSummary newTeamSummary = getTeamFromCompetition(result, newTeam.teamName());

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

        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(createdUserToRetire.id(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Expected user to be deleted: " + response)
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, secondPoints);
        manuallyUpdateStats();

        userToRetire.setTeamId(newTeam.id());
        UserUtils.create(userToRetire);

        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, thirdPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterUnretirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary originalTeamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, originalTeam.teamName());
        final TeamSummary newTeamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, newTeam.teamName());

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
        final User user = UserUtils.create(generateUserWithTeamId(team.id()));
        final int userId = user.id();

        final long firstPoints = 2_500L;
        StubbedFoldingEndpointUtils.addPoints(user, firstPoints);
        manuallyUpdateStats();

        final long pointsOffset = 1_000L;
        TEAM_COMPETITION_REQUEST_SENDER.offset(userId, pointsOffset, pointsOffset, 0, ADMIN_USER.userName(), ADMIN_USER.password());
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.displayName());

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
        final User user = UserUtils.create(generateUserWithTeamId(team.id()));

        final long initialPoints = 2_500L;
        StubbedFoldingEndpointUtils.addPoints(user, initialPoints);
        manuallyUpdateStats();

        final long firstPointsOffset = 1_000L;
        offsetUserPoints(user, firstPointsOffset);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterFirstOffset = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterFirstOffset = getTeamFromCompetition(resultAfterFirstOffset, team.teamName());
        final UserSummary userSummaryAfterFirstOffset = getActiveUserFromTeam(teamSummaryAfterFirstOffset, user.displayName());

        assertThat(userSummaryAfterFirstOffset.getPoints())
            .as("Expected user points to be stats + first offset: " + userSummaryAfterFirstOffset)
            .isEqualTo(initialPoints + firstPointsOffset);

        assertThat(userSummaryAfterFirstOffset.getMultipliedPoints())
            .as("Expected user multiplied points to be stats + first offset: " + userSummaryAfterFirstOffset)
            .isEqualTo(initialPoints + firstPointsOffset);

        final long secondPointsOffset = 250L;
        offsetUserPoints(user, secondPointsOffset);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterSecondOffset = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterSecondOffset = getTeamFromCompetition(resultAfterSecondOffset, team.teamName());
        final UserSummary userSummaryAfterSecondOffset = getActiveUserFromTeam(teamSummaryAfterSecondOffset, user.displayName());

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
        final User user = UserUtils.create(generateUserWithTeamId(team.id()));
        final int userId = user.id();

        final long firstPoints = 2_500L;
        final int firstUnits = 25;
        StubbedFoldingEndpointUtils.addPoints(user, firstPoints);
        StubbedFoldingEndpointUtils.addUnits(user, firstUnits);
        manuallyUpdateStats();

        final long pointsOffset = -20_000L;
        final int unitsOffset = -400;
        TEAM_COMPETITION_REQUEST_SENDER.offset(userId, pointsOffset, pointsOffset, unitsOffset, ADMIN_USER.userName(), ADMIN_USER.password());
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.displayName());

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
        final int userId = user.id();

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
        final User firstInTeamFirstOverall = UserUtils.create(generateUserWithTeamIdAndCategory(mainTeam.id(), Category.AMD_GPU));

        final User secondInTeamThirdOverall = UserUtils.create(generateUserWithTeamIdAndCategory(mainTeam.id(), Category.NVIDIA_GPU));
        final int secondInTeamThirdOverallId = secondInTeamThirdOverall.id();

        final Team otherTeam = TeamUtils.create(generateTeam());
        final User firstInTeamSecondOverall = UserUtils.create(generateUserWithTeamId(otherTeam.id()));

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
    void whenGettingStatsForUser_givenAnInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/stats/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
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
        final UserRequest user = generateUserWithHardwareId(hardware.id());

        final int userId = UserUtils.create(user).id();
        final HttpResponse<Void> patchResponse = TEAM_COMPETITION_REQUEST_SENDER
            .offset(userId, 100L, Math.round(100L * hardware.multiplier()), 10, ADMIN_USER.userName(), ADMIN_USER.password());
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
    void whenPatchingUserWithPointsOffsets_givenAnInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .method("PATCH", HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/stats/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenPatchingUserWithPointsOffsets_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final UserRequest user = generateUserWithHardwareId(hardware.id());

        final int userId = UserUtils.create(user).id();

        final OffsetTcStats offsetTcStats = OffsetTcStats.create(100L, Math.round(100L * hardware.multiplier()), 10);
        final HttpRequest request = HttpRequest.newBuilder()
            .method("PATCH", HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(offsetTcStats)))
            .uri(URI.create(FOLDING_URL + "/stats/users/" + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<Void> response = RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenPatchingUserWithPointsOffsets_givenEmptyPayload_thenRequestFails_andResponseHas400Status()
        throws IOException, InterruptedException, FoldingRestException {
        final Hardware hardware = HardwareUtils.create(generateHardware());
        final UserRequest user = generateUserWithHardwareId(hardware.id());
        final int userId = UserUtils.create(user).id();

        final HttpRequest request = HttpRequest.newBuilder()
            .method("PATCH", HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/stats/users/" + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
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
        final User user = UserUtils.create(generateUserWithHardwareIdAndTeamId(hardware.id(), team.id()));

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(user, firstPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, team.teamName());
        final UserSummary userAfterFirstUpdate = getActiveUserFromTeam(teamAfterFirstUpdate, user.displayName());

        assertThat(userAfterFirstUpdate.getPoints())
            .as("Expected initial points for user: " + userAfterFirstUpdate)
            .isEqualTo(firstPoints);

        final long firstOffsetPoints = 2_000L;
        offsetUserPoints(user, firstOffsetPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, team.teamName());
        final UserSummary userAfterSecondUpdate = getActiveUserFromTeam(teamAfterSecondUpdate, user.displayName());

        assertThat(userAfterSecondUpdate.getPoints())
            .as("Expected initial points + first offset points for user: " + userAfterSecondUpdate)
            .isEqualTo(firstPoints + firstOffsetPoints);

        // Update hardware, should clear all offsets from the user
        final double newMultiplier = 2.00D;
        final HardwareRequest updatedHardware = HardwareRequest.builder()
            .hardwareName(hardware.hardwareName())
            .displayName(hardware.displayName())
            .hardwareMake(hardware.hardwareMake().toString())
            .hardwareType(hardware.hardwareType().toString())
            .multiplier(newMultiplier)
            .averagePpd(hardware.averagePpd())
            .build();
        final HttpResponse<String> hardwareUpdateResponse =
            HARDWARE_REQUEST_SENDER.update(hardware.id(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(hardwareUpdateResponse.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + hardwareUpdateResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final long secondPoints = 333L;
        StubbedFoldingEndpointUtils.addPoints(user, secondPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterThirdUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterThirdUpdate = getTeamFromCompetition(resultAfterThirdUpdate, team.teamName());
        final UserSummary userAfterThirdUpdate = getActiveUserFromTeam(teamAfterThirdUpdate, user.displayName());

        assertThat(userAfterThirdUpdate.getPoints())
            .as("Expected initial points + first offset points + second points for user: " + userAfterThirdUpdate)
            .isEqualTo(firstPoints + firstOffsetPoints + secondPoints);

        final long secondOffsetPoints = 95L;
        offsetUserPoints(user, secondOffsetPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterFourthUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterFourthUpdate = getTeamFromCompetition(resultAfterFourthUpdate, team.teamName());
        final UserSummary userAfterFourthUpdate = getActiveUserFromTeam(teamAfterFourthUpdate, user.displayName());

        assertThat(userAfterFourthUpdate.getPoints())
            .as("Expected initial points + first offset points + second points for user: " + userAfterFourthUpdate)
            .isEqualTo(firstPoints + firstOffsetPoints + secondPoints + secondOffsetPoints);
    }

    @Test
    void whenUserIsUpdated_givenUserChangesTeam_andUserHasStatsOnOldTeam_thenRetiredUserWithStatsAddedToOldTeam_andUserHasNoStatsForNewTeam()
        throws FoldingRestException {
        final Team firstTeam = TeamUtils.create(generateTeam());
        final User firstUser = UserUtils.create(generateUserWithTeamIdAndCategory(firstTeam.id(), Category.AMD_GPU));
        final User secondUser = UserUtils.create(generateUserWithTeamIdAndCategory(firstTeam.id(), Category.NVIDIA_GPU));

        final Team secondTeam = TeamUtils.create(generateTeam());
        final User thirdUser = UserUtils.create(generateUserWithTeamIdAndCategory(secondTeam.id(), Category.NVIDIA_GPU));

        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.addPoints(secondUser, 5_000L);
        StubbedFoldingEndpointUtils.addPoints(thirdUser, 1_000L);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterFirstUpdate.getTeamMultipliedPoints())
            .as("Expected first team to have points of first and second user: " + resultAfterFirstUpdate)
            .isEqualTo(15_000L);
        assertThat(firstTeamSummaryAfterFirstUpdate.getRetiredUsers())
            .as("Expected first team to have no retired users: " + resultAfterFirstUpdate)
            .isEmpty();

        assertThat(secondTeamSummaryAfterFirstUpdate.getTeamMultipliedPoints())
            .as("Expected second team to have points of third user: " + resultAfterFirstUpdate)
            .isEqualTo(1_000L);
        assertThat(secondTeamSummaryAfterFirstUpdate.getActiveUsers())
            .as("Expected second team to have one active user: " + resultAfterFirstUpdate)
            .hasSize(1);

        final UserRequest updatedFirstUserRequest = UserRequest.builder()
            .foldingUserName(firstUser.foldingUserName())
            .displayName(firstUser.displayName())
            .passkey(firstUser.passkey())
            .category(firstUser.category().toString())
            .hardwareId(firstUser.hardware().id())
            .teamId(secondTeam.id())
            .build();

        final User updatedFirstUser = UserUtils.update(firstUser.id(), updatedFirstUserRequest);

        final AllTeamsSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterSecondUpdate.getTeamMultipliedPoints())
            .as("Expected first team to have no change to points: " + resultAfterSecondUpdate)
            .isEqualTo(15_000L);
        assertThat(firstTeamSummaryAfterSecondUpdate.getRetiredUsers())
            .as("Expected first team to have one retired user: " + resultAfterSecondUpdate)
            .hasSize(1);

        assertThat(secondTeamSummaryAfterSecondUpdate.getTeamMultipliedPoints())
            .as("Expected second team to have no change to points: " + resultAfterSecondUpdate)
            .isEqualTo(1_000L);
        assertThat(secondTeamSummaryAfterSecondUpdate.getActiveUsers())
            .as("Expected second team to have two active users: " + resultAfterSecondUpdate)
            .hasSize(2);

        StubbedFoldingEndpointUtils.addPoints(updatedFirstUser, 10_000L);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterThirdUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterThirdUpdate = getTeamFromCompetition(resultAfterThirdUpdate, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterThirdUpdate = getTeamFromCompetition(resultAfterThirdUpdate, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterThirdUpdate.getTeamMultipliedPoints())
            .as("Expected first team to have no change to points: " + resultAfterThirdUpdate)
            .isEqualTo(15_000L);
        assertThat(firstTeamSummaryAfterThirdUpdate.getRetiredUsers())
            .as("Expected first team to have one retired user: " + resultAfterThirdUpdate)
            .hasSize(1);

        assertThat(secondTeamSummaryAfterThirdUpdate.getTeamMultipliedPoints())
            .as("Expected second team to have new points from the moved user: " + resultAfterThirdUpdate)
            .isEqualTo(11_000L);
    }
}
