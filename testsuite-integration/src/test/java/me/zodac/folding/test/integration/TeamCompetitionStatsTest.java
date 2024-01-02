/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.test.integration;

import static me.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateHardware;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateHardwareWithMultiplier;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateTeam;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateUser;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateUserWithHardwareId;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateUserWithHardwareIdAndTeamId;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateUserWithTeamId;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateUserWithTeamIdAndCategory;
import static me.zodac.folding.test.integration.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.integration.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.integration.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.TEAM_COMPETITION_REQUEST_SENDER;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.getActiveUserFromTeam;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.getRetiredUserFromTeam;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.getTeamFromCompetition;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.manuallyUpdateStats;
import static me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils.offsetUserPoints;
import static me.zodac.folding.test.integration.util.rest.request.UserUtils.USER_REQUEST_SENDER;
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
import me.zodac.folding.rest.api.util.RestUtilConstants;
import me.zodac.folding.test.integration.util.TestConstants;
import me.zodac.folding.test.integration.util.rest.request.HardwareUtils;
import me.zodac.folding.test.integration.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamUtils;
import me.zodac.folding.test.integration.util.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@code Team Competition} stats calculation.
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

        assertThat(result.teams())
            .as("Expected no teams: " + result)
            .isEmpty();

        assertThat(result.competitionSummary().totalPoints())
            .as("Expected no points: " + result)
            .isZero();

        assertThat(result.competitionSummary().totalMultipliedPoints())
            .as("Expected no multiplied points: " + result)
            .isZero();

        assertThat(result.competitionSummary().totalUnits())
            .as("Expected no units: " + result)
            .isZero();
    }

    @Test
    void whenNoTeamsExistInTheSystem_thenResponseIsReturnedWithNoSummaryStats() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getSummaryStats();

        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final CompetitionSummary result = TeamCompetitionStatsResponseParser.getSummaryStats(response);

        assertThat(result.totalPoints())
            .as("Expected no points: " + result)
            .isZero();

        assertThat(result.totalMultipliedPoints())
            .as("Expected no multiplied points: " + result)
            .isZero();

        assertThat(result.totalUnits())
            .as("Expected no units: " + result)
            .isZero();
    }

    @Test
    void whenOneTeamExistsWithOneUser_andUserEarnsStats_thenUserAndTeamAndSummaryStartWithNoStats_thenAllIncrementAsUserPointsIncrease()
        throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamId(team.id()));

        final AllTeamsSummary resultBeforeStats = TeamCompetitionStatsUtils.getStats();

        assertThat(resultBeforeStats.competitionSummary().totalPoints())
            .as("Expected no points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.competitionSummary().totalMultipliedPoints())
            .as("Expected no multiplied points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.competitionSummary().totalUnits())
            .as("Expected no units: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.teams())
            .as("Expected exactly 1 team: " + resultBeforeStats)
            .hasSize(1);

        final TeamSummary teamSummaryBeforeStats = getTeamFromCompetition(resultBeforeStats, team.teamName());

        assertThat(teamSummaryBeforeStats.teamPoints())
            .as("Expected no points for team: " + teamSummaryBeforeStats)
            .isZero();

        assertThat(teamSummaryBeforeStats.teamMultipliedPoints())
            .as("Expected no multiplied points for team: " + teamSummaryBeforeStats)
            .isZero();

        assertThat(teamSummaryBeforeStats.teamUnits())
            .as("Expected no units for team: " + teamSummaryBeforeStats)
            .isZero();

        assertThat(teamSummaryBeforeStats.activeUsers())
            .as("Expected exactly 1 active user: " + teamSummaryBeforeStats)
            .hasSize(1);

        assertThat(teamSummaryBeforeStats.retiredUsers())
            .as("Expected no retired users: " + teamSummaryBeforeStats)
            .isEmpty();

        final UserSummary userSummaryBeforeStats = getActiveUserFromTeam(teamSummaryBeforeStats, user.displayName());

        assertThat(userSummaryBeforeStats.points())
            .as("Expected no points for user: " + userSummaryBeforeStats)
            .isZero();

        assertThat(userSummaryBeforeStats.multipliedPoints())
            .as("Expected no multiplied points for user: " + userSummaryBeforeStats)
            .isZero();

        assertThat(userSummaryBeforeStats.units())
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

        assertThat(resultAfterStats.competitionSummary().totalPoints())
            .as("Expected updated points: " + resultAfterStats)
            .isEqualTo(newPoints);

        assertThat(resultAfterStats.competitionSummary().totalMultipliedPoints())
            .as("Expected updated multiplied points: " + resultAfterStats)
            .isEqualTo(newPoints);

        assertThat(resultAfterStats.competitionSummary().totalUnits())
            .as("Expected updated units: " + resultAfterStats)
            .isEqualTo(newUnits);

        assertThat(resultAfterStats.teams())
            .as("Expected exactly 1 team: " + resultAfterStats)
            .hasSize(1);

        final TeamSummary teamSummaryAfterStats = getTeamFromCompetition(resultAfterStats, team.teamName());

        assertThat(teamSummaryAfterStats.teamPoints())
            .as("Expected updated points for team: " + teamSummaryAfterStats)
            .isEqualTo(newPoints);

        assertThat(teamSummaryAfterStats.teamMultipliedPoints())
            .as("Expected updated multiplied points for team: " + teamSummaryAfterStats)
            .isEqualTo(newPoints);

        assertThat(teamSummaryAfterStats.teamUnits())
            .as("Expected updated units for team: " + teamSummaryAfterStats)
            .isEqualTo(newUnits);

        assertThat(teamSummaryAfterStats.activeUsers())
            .as("Expected exactly 1 active user: " + teamSummaryAfterStats)
            .hasSize(1);

        assertThat(teamSummaryAfterStats.retiredUsers())
            .as("Expected no retired users: " + teamSummaryAfterStats)
            .isEmpty();

        final UserSummary userSummaryAfterStats = getActiveUserFromTeam(teamSummaryAfterStats, user.displayName());

        assertThat(userSummaryAfterStats.points())
            .as("Expected updated points for user: " + userSummaryAfterStats)
            .isEqualTo(newPoints);

        assertThat(userSummaryAfterStats.multipliedPoints())
            .as("Expected updated multiplied points for user: " + userSummaryAfterStats)
            .isEqualTo(newPoints);

        assertThat(userSummaryAfterStats.units())
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
        final Collection<UserSummary> activeUserSummaries = teamSummary.activeUsers();

        assertThat(activeUserSummaries)
            .as("Expected exactly 2 active users: " + teamSummary)
            .hasSize(2);

        final UserSummary firstUserSummary = getActiveUserFromTeam(teamSummary, firstUser.displayName());
        final UserSummary secondUserSummary = getActiveUserFromTeam(teamSummary, secondUser.displayName());

        assertThat(firstUserSummary.rankInTeam())
            .as("Expected first user to be rank 1: " + firstUserSummary)
            .isEqualTo(1);

        assertThat(secondUserSummary.rankInTeam())
            .as("Expected second user to be rank 1: " + secondUserSummary)
            .isEqualTo(1);

        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.addUnits(firstUser, 10);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, team.teamName());
        final UserSummary firstUserSummaryAfterFirstUpdate = getActiveUserFromTeam(teamSummaryAfterFirstUpdate, firstUser.displayName());
        final UserSummary secondUserSummaryAfterFirstUpdate = getActiveUserFromTeam(teamSummaryAfterFirstUpdate, secondUser.displayName());

        assertThat(firstUserSummaryAfterFirstUpdate.rankInTeam())
            .as("Expected first user to be rank 1: " + teamSummaryAfterFirstUpdate)
            .isEqualTo(1);

        assertThat(secondUserSummaryAfterFirstUpdate.rankInTeam())
            .as("Expected second user to be rank 2: " + teamSummaryAfterFirstUpdate)
            .isEqualTo(2);

        StubbedFoldingEndpointUtils.addPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.addUnits(secondUser, 20);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, team.teamName());
        final UserSummary firstUserSummaryAfterSecondUpdate = getActiveUserFromTeam(teamSummaryAfterSecondUpdate, firstUser.displayName());
        final UserSummary secondUserSummaryAfterSecondUpdate = getActiveUserFromTeam(teamSummaryAfterSecondUpdate, secondUser.displayName());

        assertThat(firstUserSummaryAfterSecondUpdate.rankInTeam())
            .as("Expected first user to be rank 2: " + firstUserSummaryAfterSecondUpdate)
            .isEqualTo(2);

        assertThat(secondUserSummaryAfterSecondUpdate.rankInTeam())
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
        assertThat(result.teams())
            .as("Expected exactly 2 teams: " + result)
            .hasSize(2);

        final TeamSummary firstTeamSummary = getTeamFromCompetition(result, firstTeam.teamName());
        final TeamSummary secondTeamSummary = getTeamFromCompetition(result, secondTeam.teamName());

        assertThat(firstTeamSummary.rank())
            .as("Expected first team to be rank 1: " + firstTeamSummary)
            .isEqualTo(1);

        assertThat(secondTeamSummary.rank())
            .as("Expected second team to be rank 1: " + secondTeamSummary)
            .isEqualTo(1);

        StubbedFoldingEndpointUtils.addPoints(firstUser, 10_000L);
        StubbedFoldingEndpointUtils.addUnits(firstUser, 10);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterFirstUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterFirstUpdate = getTeamFromCompetition(resultAfterFirstUpdate, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterFirstUpdate.rank())
            .as("Expected first team to be rank 1: " + resultAfterFirstUpdate)
            .isEqualTo(1);

        assertThat(secondTeamSummaryAfterFirstUpdate.rank())
            .as("Expected second team to be rank 2: " + resultAfterFirstUpdate)
            .isEqualTo(2);

        StubbedFoldingEndpointUtils.addPoints(secondUser, 20_000L);
        StubbedFoldingEndpointUtils.addUnits(secondUser, 20);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterSecondUpdate.rank())
            .as("Expected first team to be rank 2: " + firstTeamSummaryAfterSecondUpdate)
            .isEqualTo(2);

        assertThat(secondTeamSummaryAfterSecondUpdate.rank())
            .as("Expected second team to be rank 1: " + secondTeamSummaryAfterSecondUpdate)
            .isEqualTo(1);

        final HttpResponse<String> summaryResultAfterSecondUpdate = TEAM_COMPETITION_REQUEST_SENDER.getSummaryStats();
        final CompetitionSummary summaryResult = TeamCompetitionStatsResponseParser.getSummaryStats(summaryResultAfterSecondUpdate);

        assertThat(summaryResult)
            .as("Expected summary stats to be same as competition stats")
            .isEqualTo(resultAfterSecondUpdate.competitionSummary());

        assertThat(summaryResult.totalMultipliedPoints())
            .isEqualTo(30_000L);

        assertThat(summaryResult.totalUnits())
            .isEqualTo(30);
    }

    @Test
    void whenTeamExistsWithOneUser_andUserHasHardwareMultiplier_thenUserPointsAreMultipliedCorrectly_andUserUnitsAreNotImpacted()
        throws FoldingRestException {
        final double hardwareMultiplier = 2.00D;
        final int hardwareId = HardwareUtils.create(generateHardwareWithMultiplier(hardwareMultiplier)).id();
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareIdAndTeamId(hardwareId, team.id());
        final User createdUser = UserUtils.create(user);

        final long newPoints = 20_000L;
        final int newUnits = 20;
        StubbedFoldingEndpointUtils.addPoints(createdUser, newPoints);
        StubbedFoldingEndpointUtils.addUnits(createdUser, newUnits);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.displayName());

        assertThat(userSummary.multipliedPoints())
            .as("Expected user multiplied points to be new points * hardware multiplier: " + userSummary)
            .isEqualTo(Math.round(newPoints * hardwareMultiplier));

        assertThat(userSummary.points())
            .as("Expected user points to not be multiplied: " + userSummary)
            .isEqualTo(newPoints);

        assertThat(userSummary.units())
            .as("Expected user units to not be multiplied: " + userSummary)
            .isEqualTo(newUnits);
    }

    @Test
    void whenTeamExistsWithOneUser_andUserIsUpdatedWithNewHardwareMultiplier_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly()
        throws FoldingRestException {
        final Hardware createdHardware = HardwareUtils.create(generateHardware());
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareIdAndTeamId(createdHardware.id(), team.id());
        final User createdUser = UserUtils.create(user);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, firstPoints);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.displayName());

        assertThat(userSummary.multipliedPoints())
            .as("Expected user multiplied points to not be multiplied: " + userSummary)
            .isEqualTo(firstPoints);

        assertThat(userSummary.points())
            .as("Expected user points to not be multiplied: " + userSummary)
            .isEqualTo(firstPoints);

        // Change the multiplier on the hardware, no need to update the user
        final HardwareRequest updatedHardware = new HardwareRequest(
            createdHardware.hardwareName(),
            createdHardware.displayName(),
            createdHardware.hardwareMake().toString(),
            createdHardware.hardwareType().toString(),
            2.00D,
            createdHardware.averagePpd()
        );

        HARDWARE_REQUEST_SENDER.update(createdHardware.id(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, secondPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUpdate = getTeamFromCompetition(resultAfterUpdate, team.teamName());
        final UserSummary userSummaryAfterUpdate = getActiveUserFromTeam(teamSummaryAfterUpdate, user.displayName());

        assertThat(userSummaryAfterUpdate.points())
            .as("Expected user points to not be multiplied: " + userSummaryAfterUpdate)
            .isEqualTo(firstPoints + secondPoints);

        assertThat(userSummaryAfterUpdate.multipliedPoints())
            .as("Expected user multiplied points to be multiplied only after the second update: " + userSummaryAfterUpdate)
            .isEqualTo(firstPoints + (Math.round(secondPoints * updatedHardware.multiplier())));
    }

    @Test
    void whenTeamExistsWithOneUser_andUserIsUpdatedWithNewHardware_thenOriginalPointsAreNotChanged_andNewPointsAreMultipliedCorrectly()
        throws FoldingRestException {
        final int hardwareId = HardwareUtils.create(generateHardware()).id();
        final Team team = TeamUtils.create(generateTeam());

        final UserRequest user = generateUserWithHardwareIdAndTeamId(hardwareId, team.id());
        final User createdUser = UserUtils.create(user);

        final long firstPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, firstPoints);
        manuallyUpdateStats();

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.displayName());

        assertThat(userSummary.multipliedPoints())
            .as("Expected user multiplied points to not be multiplied: " + userSummary)
            .isEqualTo(firstPoints);

        assertThat(userSummary.points())
            .as("Expected user points to not be multiplied: " + userSummary)
            .isEqualTo(firstPoints);

        // Update the user with a new hardware with a multiplier
        final HardwareRequest hardwareWithMultiplier = generateHardwareWithMultiplier(2.00D);
        final int hardwareWithMultiplierId = HardwareUtils.create(hardwareWithMultiplier).id();
        final UserRequest userUpdatedWithMultiplier = new UserRequest(
            user.foldingUserName(),
            user.displayName(),
            user.passkey(),
            user.category(),
            null,
            null,
            hardwareWithMultiplierId,
            user.teamId(),
            false
        );

        USER_REQUEST_SENDER.update(createdUser.id(), userUpdatedWithMultiplier, ADMIN_USER.userName(), ADMIN_USER.password());

        final long secondPoints = 5_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUser, secondPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUpdate = getTeamFromCompetition(resultAfterUpdate, team.teamName());
        final UserSummary userSummaryAfterUpdate = getActiveUserFromTeam(teamSummaryAfterUpdate, userUpdatedWithMultiplier.displayName());

        assertThat(userSummaryAfterUpdate.multipliedPoints())
            .as("Expected user multiplied points to be multiplied only after the second update: " + userSummaryAfterUpdate)
            .isEqualTo(firstPoints + (Math.round(secondPoints * hardwareWithMultiplier.multiplier())));

        assertThat(userSummaryAfterUpdate.points())
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

        assertThat(teamSummary.activeUsers())
            .as("Expected team to have 2 active users for first update: " + teamSummary)
            .hasSize(2);

        assertThat(teamSummary.retiredUsers())
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

        assertThat(teamSummaryAfterRetirement.teamMultipliedPoints())
            .as("Expected team to have points from first update for both users, then second update only for the first user: "
                + teamSummaryAfterRetirement)
            .isEqualTo(firstPoints + firstPoints + secondPoints);

        assertThat(teamSummaryAfterRetirement.activeUsers())
            .as("Expected team to have only 1 active user after other user was retired: " + teamSummaryAfterRetirement)
            .hasSize(1);

        assertThat(teamSummaryAfterRetirement.retiredUsers())
            .as("Expected team to have 1 retired user after user was retired: " + teamSummaryAfterRetirement)
            .hasSize(1);

        final UserSummary firstUserSummary = getActiveUserFromTeam(teamSummaryAfterRetirement, firstUser.displayName());
        final RetiredUserSummary secondUserResult = getRetiredUserFromTeam(teamSummaryAfterRetirement, userToRetire.displayName());

        assertThat(firstUserSummary.multipliedPoints())
            .as("Expected user to have points from both updates: " + firstUserSummary)
            .isEqualTo(firstPoints + secondPoints);

        assertThat(secondUserResult.multipliedPoints())
            .as("Expected retired user to have points from first update only: " + secondUserResult)
            .isEqualTo(firstPoints);

        UserUtils.create(userToRetire);
        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.addPoints(firstUser, thirdPoints);
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, thirdPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterUnretirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, team.teamName());

        assertThat(teamSummaryAfterUnretirement.teamMultipliedPoints())
            .as("Expected team to have points from first update for both users, second update for the first user, third update for both users: "
                + teamSummaryAfterUnretirement)
            .isEqualTo(firstPoints + firstPoints + secondPoints + thirdPoints + thirdPoints);

        assertThat(teamSummaryAfterUnretirement.activeUsers())
            .as("Expected team to have 2 active users after unretirement: " + teamSummaryAfterUnretirement)
            .hasSize(2);

        assertThat(teamSummaryAfterUnretirement.retiredUsers())
            .as("Expected team to have 1 retired user after user was unretired: " + teamSummaryAfterUnretirement)
            .hasSize(1);

        final UserSummary firstUserSummaryAfterUnretirement = getActiveUserFromTeam(teamSummaryAfterUnretirement, firstUser.displayName());
        final UserSummary secondUserSummaryAfterUnretirement = getActiveUserFromTeam(teamSummaryAfterUnretirement, userToRetire.displayName());

        assertThat(firstUserSummaryAfterUnretirement.multipliedPoints())
            .as("Expected user to have points from all three updates: " + firstUserSummaryAfterUnretirement)
            .isEqualTo(firstPoints + secondPoints + thirdPoints);

        assertThat(secondUserSummaryAfterUnretirement.multipliedPoints())
            .as("Expected user to have points from third updates only: " + secondUserSummaryAfterUnretirement)
            .isEqualTo(thirdPoints);

        final RetiredUserSummary retiredUserAfterUnretirement = getRetiredUserFromTeam(teamSummaryAfterRetirement, userToRetire.displayName());

        assertThat(retiredUserAfterUnretirement.multipliedPoints())
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

        assertThat(originalTeamSummary.activeUsers())
            .as("Expected original team to have 2 active users at the start: " + originalTeamSummary)
            .hasSize(2);

        assertThat(originalTeamSummary.retiredUsers())
            .as("Expected original team to have no retired users at the start: " + originalTeamSummary)
            .isEmpty();

        assertThat(newTeamSummary.activeUsers())
            .as("Expected new team to have 1 active user at the start: " + newTeamSummary)
            .hasSize(1);

        assertThat(newTeamSummary.retiredUsers())
            .as("Expected new team to have no retired users at the start: " + newTeamSummary)
            .isEmpty();

        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(createdUserToRetire.id(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Expected user to be deleted: " + response)
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final long secondPoints = 8_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, secondPoints);
        manuallyUpdateStats();

        final UserRequest userToRetireWithNewTeamId = new UserRequest(
            userToRetire.foldingUserName(),
            userToRetire.displayName(),
            userToRetire.passkey(),
            userToRetire.category(),
            null,
            null,
            userToRetire.hardwareId(),
            newTeam.id(),
            false
        );
        UserUtils.create(userToRetireWithNewTeamId);

        final long thirdPoints = 14_000L;
        StubbedFoldingEndpointUtils.addPoints(createdUserToRetire, thirdPoints);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterUnretirement = TeamCompetitionStatsUtils.getStats();
        final TeamSummary originalTeamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, originalTeam.teamName());
        final TeamSummary newTeamSummaryAfterUnretirement = getTeamFromCompetition(resultAfterUnretirement, newTeam.teamName());

        assertThat(originalTeamSummaryAfterUnretirement.activeUsers())
            .as("Expected original team to have 1 active user after unretirement: " + originalTeamSummaryAfterUnretirement)
            .hasSize(1);

        assertThat(originalTeamSummaryAfterUnretirement.retiredUsers())
            .as("Expected original team to have 1 retired user after unretirement: " + originalTeamSummaryAfterUnretirement)
            .hasSize(1);

        assertThat(originalTeamSummaryAfterUnretirement.teamMultipliedPoints())
            .as("Expected original team to points from before retirement only: " + originalTeamSummaryAfterUnretirement)
            .isEqualTo(firstPoints);

        assertThat(newTeamSummaryAfterUnretirement.activeUsers())
            .as("Expected new team to have 1 active user after unretirement: " + newTeamSummaryAfterUnretirement)
            .hasSize(2);

        assertThat(newTeamSummaryAfterUnretirement.retiredUsers())
            .as("Expected new team to have no retired users after unretirement: " + newTeamSummaryAfterUnretirement)
            .isEmpty();

        assertThat(newTeamSummaryAfterUnretirement.teamMultipliedPoints())
            .as("Expected new team to points from after unretirement only: " + originalTeamSummaryAfterUnretirement)
            .isEqualTo(thirdPoints);

        final RetiredUserSummary retiredUserSummaryAfterUnretirement =
            getRetiredUserFromTeam(originalTeamSummaryAfterUnretirement, userToRetireWithNewTeamId.displayName());
        final UserSummary activeUserSummaryAfterUnretirement =
            getActiveUserFromTeam(newTeamSummaryAfterUnretirement, userToRetireWithNewTeamId.displayName());

        assertThat(retiredUserSummaryAfterUnretirement.multipliedPoints())
            .as("Expected retired user to have points from before retirement only: " + retiredUserSummaryAfterUnretirement)
            .isEqualTo(firstPoints);

        assertThat(activeUserSummaryAfterUnretirement.multipliedPoints())
            .as("Expected unretired user to have points from after unretirement only: " + activeUserSummaryAfterUnretirement)
            .isEqualTo(thirdPoints);
    }

    @Test
    void whenOneTeamHasOneUser_andUserHasOffsetApplied_thenUserOffsetIsAppendedToStats() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamId(team.id()));

        final long firstPoints = 2_500L;
        StubbedFoldingEndpointUtils.addPoints(user, firstPoints);
        manuallyUpdateStats();

        final long pointsOffset = 1_000L;
        offsetUserPoints(user, pointsOffset);

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.displayName());

        assertThat(userSummary.points())
            .as("Expected user points to be stats + offset: " + userSummary)
            .isEqualTo(firstPoints + pointsOffset);

        assertThat(userSummary.multipliedPoints())
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

        final AllTeamsSummary resultAfterFirstOffset = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterFirstOffset = getTeamFromCompetition(resultAfterFirstOffset, team.teamName());
        final UserSummary userSummaryAfterFirstOffset = getActiveUserFromTeam(teamSummaryAfterFirstOffset, user.displayName());

        assertThat(userSummaryAfterFirstOffset.points())
            .as("Expected user points to be stats + first offset: " + userSummaryAfterFirstOffset)
            .isEqualTo(initialPoints + firstPointsOffset);

        assertThat(userSummaryAfterFirstOffset.multipliedPoints())
            .as("Expected user multiplied points to be stats + first offset: " + userSummaryAfterFirstOffset)
            .isEqualTo(initialPoints + firstPointsOffset);

        final long secondPointsOffset = 250L;
        offsetUserPoints(user, secondPointsOffset);

        final AllTeamsSummary resultAfterSecondOffset = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummaryAfterSecondOffset = getTeamFromCompetition(resultAfterSecondOffset, team.teamName());
        final UserSummary userSummaryAfterSecondOffset = getActiveUserFromTeam(teamSummaryAfterSecondOffset, user.displayName());

        assertThat(userSummaryAfterSecondOffset.points())
            .as("Expected user points to be stats + both offsets: " + userSummaryAfterSecondOffset)
            .isEqualTo(initialPoints + firstPointsOffset + secondPointsOffset);

        assertThat(userSummaryAfterSecondOffset.multipliedPoints())
            .as("Expected user multiplied points to be stats + both offsets: " + userSummaryAfterSecondOffset)
            .isEqualTo(initialPoints + firstPointsOffset + secondPointsOffset);
    }

    @Test
    void whenOneTeamHasOneUser_andUserHasOffsetApplied_andOffsetIsNegative_andOffsetIsGreaterThanCurrentUserStats_thenUserHasZeroStats()
        throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final User user = UserUtils.create(generateUserWithTeamId(team.id()));

        final long firstPoints = 2_500L;
        StubbedFoldingEndpointUtils.addPoints(user, firstPoints);
        manuallyUpdateStats();

        final long pointsOffset = -20_000L;
        offsetUserPoints(user, pointsOffset);

        final AllTeamsSummary result = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamSummary = getTeamFromCompetition(result, team.teamName());
        final UserSummary userSummary = getActiveUserFromTeam(teamSummary, user.displayName());

        assertThat(userSummary.points())
            .as("Expected user points to be 0: " + userSummary)
            .isZero();

        assertThat(userSummary.multipliedPoints())
            .as("Expected user multiplied points to be 0: " + userSummary)
            .isZero();
    }

    @Test
    void whenOneTeamExistsWithOneUser_andUserEarnsStats_thenUserStartsWithNoStats_thenIncrementsAsUserPointsIncrease() throws FoldingRestException {
        final User user = UserUtils.create(generateUser());
        final int userId = user.id();

        manuallyUpdateStats();
        final UserSummary resultBeforeStats = TeamCompetitionStatsUtils.getStatsForUser(userId);

        assertThat(resultBeforeStats.points())
            .as("Expected no points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.multipliedPoints())
            .as("Expected no multiplied points: " + resultBeforeStats)
            .isZero();

        assertThat(resultBeforeStats.units())
            .as("Expected no units: " + resultBeforeStats)
            .isZero();

        final long newPoints = 10_000L;
        StubbedFoldingEndpointUtils.addPoints(user, newPoints);

        final int newUnits = 5;
        StubbedFoldingEndpointUtils.addUnits(user, newUnits);

        manuallyUpdateStats();

        final UserSummary resultAfterStats = TeamCompetitionStatsUtils.getStatsForUser(userId);

        assertThat(resultAfterStats.points())
            .as("Expected updated points: " + resultAfterStats)
            .isEqualTo(newPoints);

        assertThat(resultAfterStats.multipliedPoints())
            .as("Expected updated multiplied points: " + resultAfterStats)
            .isEqualTo(newPoints);

        assertThat(resultAfterStats.units())
            .as("Expected updated units: " + resultAfterStats)
            .isEqualTo(newUnits);
    }

    @Test
    void whenGettingStatsForUser_andUserRankIs2ndInTeamBut3rdInCompetition_thenResponseHasTeamRankListed() throws FoldingRestException {
        final Team mainTeam = TeamUtils.create(generateTeam());
        final User firstInTeamFirstRank = UserUtils.create(generateUserWithTeamIdAndCategory(mainTeam.id(), Category.AMD_GPU));

        final User secondInTeamThirdRank = UserUtils.create(generateUserWithTeamIdAndCategory(mainTeam.id(), Category.NVIDIA_GPU));
        final int secondInTeamThirdRankId = secondInTeamThirdRank.id();

        final Team otherTeam = TeamUtils.create(generateTeam());
        final User firstInTeamSecondRank = UserUtils.create(generateUserWithTeamId(otherTeam.id()));

        manuallyUpdateStats();
        final UserSummary resultBeforeStats = TeamCompetitionStatsUtils.getStatsForUser(secondInTeamThirdRankId);
        assertThat(resultBeforeStats.rankInTeam())
            .as("Expected all users to start at rank 1: " + resultBeforeStats)
            .isEqualTo(1);

        StubbedFoldingEndpointUtils.addPoints(firstInTeamFirstRank, 10_000L);
        StubbedFoldingEndpointUtils.addPoints(secondInTeamThirdRank, 1_000L);
        StubbedFoldingEndpointUtils.addPoints(firstInTeamSecondRank, 5_000L);
        manuallyUpdateStats();

        final UserSummary resultAfterStats = TeamCompetitionStatsUtils.getStatsForUser(secondInTeamThirdRankId);
        assertThat(resultAfterStats.rankInTeam())
            .as("Expected user to be third rank, but second in team: " + resultBeforeStats)
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
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.offset(TestConstants.NON_EXISTING_ID, 100L, 1_000L, 10,
            ADMIN_USER.userName(), ADMIN_USER.password());
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

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
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

        assertThat(userAfterFirstUpdate.points())
            .as("Expected initial points for user: " + userAfterFirstUpdate)
            .isEqualTo(firstPoints);

        final long firstOffsetPoints = 2_000L;
        offsetUserPoints(user, firstOffsetPoints);

        final AllTeamsSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, team.teamName());
        final UserSummary userAfterSecondUpdate = getActiveUserFromTeam(teamAfterSecondUpdate, user.displayName());

        assertThat(userAfterSecondUpdate.points())
            .as("Expected initial points + first offset points for user: " + userAfterSecondUpdate)
            .isEqualTo(firstPoints + firstOffsetPoints);

        // Update hardware, should clear all offsets from the user
        final double newMultiplier = 2.00D;
        final HardwareRequest updatedHardware = new HardwareRequest(
            hardware.hardwareName(),
            hardware.displayName(),
            hardware.hardwareMake().toString(),
            hardware.hardwareType().toString(),
            newMultiplier,
            hardware.averagePpd()
        );
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

        assertThat(userAfterThirdUpdate.points())
            .as("Expected initial points + first offset points + second points for user: " + userAfterThirdUpdate)
            .isEqualTo(firstPoints + firstOffsetPoints + secondPoints);

        final long secondOffsetPoints = 95L;
        offsetUserPoints(user, secondOffsetPoints);

        final AllTeamsSummary resultAfterFourthUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary teamAfterFourthUpdate = getTeamFromCompetition(resultAfterFourthUpdate, team.teamName());
        final UserSummary userAfterFourthUpdate = getActiveUserFromTeam(teamAfterFourthUpdate, user.displayName());

        assertThat(userAfterFourthUpdate.points())
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

        assertThat(firstTeamSummaryAfterFirstUpdate.teamMultipliedPoints())
            .as("Expected first team to have points of first and second user: " + resultAfterFirstUpdate)
            .isEqualTo(15_000L);
        assertThat(firstTeamSummaryAfterFirstUpdate.retiredUsers())
            .as("Expected first team to have no retired users: " + resultAfterFirstUpdate)
            .isEmpty();

        assertThat(secondTeamSummaryAfterFirstUpdate.teamMultipliedPoints())
            .as("Expected second team to have points of third user: " + resultAfterFirstUpdate)
            .isEqualTo(1_000L);
        assertThat(secondTeamSummaryAfterFirstUpdate.activeUsers())
            .as("Expected second team to have one active user: " + resultAfterFirstUpdate)
            .hasSize(1);

        final UserRequest updatedFirstUserRequest = new UserRequest(
            firstUser.foldingUserName(),
            firstUser.displayName(),
            firstUser.passkey(),
            firstUser.category().toString(),
            null,
            null,
            firstUser.hardware().id(),
            secondTeam.id(),
            false
        );

        final User updatedFirstUser = UserUtils.update(firstUser.id(), updatedFirstUserRequest);

        final AllTeamsSummary resultAfterSecondUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterSecondUpdate = getTeamFromCompetition(resultAfterSecondUpdate, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterSecondUpdate.teamMultipliedPoints())
            .as("Expected first team to have no change to points: " + resultAfterSecondUpdate)
            .isEqualTo(15_000L);
        assertThat(firstTeamSummaryAfterSecondUpdate.retiredUsers())
            .as("Expected first team to have one retired user: " + resultAfterSecondUpdate)
            .hasSize(1);

        assertThat(secondTeamSummaryAfterSecondUpdate.teamMultipliedPoints())
            .as("Expected second team to have no change to points: " + resultAfterSecondUpdate)
            .isEqualTo(1_000L);
        assertThat(secondTeamSummaryAfterSecondUpdate.activeUsers())
            .as("Expected second team to have two active users: " + resultAfterSecondUpdate)
            .hasSize(2);

        StubbedFoldingEndpointUtils.addPoints(updatedFirstUser, 10_000L);
        manuallyUpdateStats();

        final AllTeamsSummary resultAfterThirdUpdate = TeamCompetitionStatsUtils.getStats();
        final TeamSummary firstTeamSummaryAfterThirdUpdate = getTeamFromCompetition(resultAfterThirdUpdate, firstTeam.teamName());
        final TeamSummary secondTeamSummaryAfterThirdUpdate = getTeamFromCompetition(resultAfterThirdUpdate, secondTeam.teamName());

        assertThat(firstTeamSummaryAfterThirdUpdate.teamMultipliedPoints())
            .as("Expected first team to have no change to points: " + resultAfterThirdUpdate)
            .isEqualTo(15_000L);
        assertThat(firstTeamSummaryAfterThirdUpdate.retiredUsers())
            .as("Expected first team to have one retired user: " + resultAfterThirdUpdate)
            .hasSize(1);

        assertThat(secondTeamSummaryAfterThirdUpdate.teamMultipliedPoints())
            .as("Expected second team to have new points from the moved user: " + resultAfterThirdUpdate)
            .isEqualTo(11_000L);
    }
}
