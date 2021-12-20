/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.rest.impl;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.rest.api.FoldingService;
import me.zodac.folding.rest.api.StorageService;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

///**
// * {@link Singleton} EJB implementation of {@link FoldingStatsService}.
// *
// * <p>
// * For the most part, this will serve as a wrapper to {@link Storage}, which knows how to perform CRUD operations on the backend storage and caches.
// * But since some logic is needed for special cases (like retrieving the latest Folding@Home stats for a {@link User} when it is created), we
// * implement that logic here, and delegate any CRUD needs to {@link Storage}.
// */
//@Singleton
@Component
public class FoldingServiceImpl implements FoldingService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    @Autowired
    private StorageService storageService;

//    @Autowired
//    private StatsSchedulerService statsScheduler;
//
//    @Autowired
//    private UserCaptainHandlerService userCaptainHandler;
//
//    @Autowired
//    private UserStateChangeHandlerService userStateChangeHandler;
//
//    @Autowired
//    private UserStatsParserService userStatsParser;
//
//    @Autowired
//    private UserTeamChangeHandlerService userTeamChangeHandler;

    @Override
    public Hardware createHardware(final Hardware hardware) {
        return storageService.createHardware(hardware);
    }


    @Override
    public Optional<Hardware> getHardware(final int hardwareId) {
        return storageService.getHardware(hardwareId);
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return storageService.getAllHardware();
    }

    @Override
    public Hardware updateHardware(final Hardware hardwareToUpdate, final Hardware existingHardware) {
        final Hardware updatedHardware = storageService.updateHardware(hardwareToUpdate);

//        if (userStateChangeHandler.isHardwareStateChange(updatedHardware, existingHardware)) {
//            final Collection<User> usersUsingThisHardware = getUsersWithHardware(updatedHardware);
//
//            for (final User userUsingHardware : usersUsingThisHardware) {
//                LOGGER.debug("User '{}' (ID: {}) had state change to hardware", userUsingHardware.getDisplayName(), userUsingHardware.getId());
//                userStateChangeHandler.handleStateChange(userUsingHardware);
//            }
//        }

        return updatedHardware;
    }

    private Collection<User> getUsersWithHardware(final Hardware hardware) {
        if (hardware.getId() == Hardware.EMPTY_HARDWARE_ID) {
            return Collections.emptyList();
        }

        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getHardware().getId() == hardware.getId())
            .collect(toList());
    }

    @Override
    public void deleteHardware(final Hardware hardware) {
        storageService.deleteHardware(hardware.getId());
    }

    @Override
    public Team createTeam(final Team team) {
        final Team createdTeam = storageService.createTeam(team);
//        statsScheduler.manualTeamCompetitionStatsParsing(ProcessingType.SYNCHRONOUS);
        return createdTeam;
    }

    @Override
    public Optional<Team> getTeam(final int teamId) {
        return storageService.getTeam(teamId);
    }

    @Override
    public Collection<Team> getAllTeams() {
        return storageService.getAllTeams();
    }

    @Override
    public Team updateTeam(final Team teamToUpdate) {
        return storageService.updateTeam(teamToUpdate);
    }

    @Override
    public void deleteTeam(final Team team) {
        storageService.deleteTeam(team.getId());
    }

    @Override
    public User createUser(final User user) {
//        if (userCaptainHandler.isUserCaptainAndCaptainExistsOnTeam(user)) {
//            userCaptainHandler.removeCaptaincyFromExistingTeamCaptain(user.getTeam());
//        }

        final User createdUser = storageService.createUser(user);

//        try {
//            // When adding a new user, we configure the initial stats DB/cache
//            final UserStats currentUserStats = FOLDING_STATS_RETRIEVER.getTotalStats(createdUser);
//            final UserStats initialStats = createInitialStats(currentUserStats);
//            LOGGER.info("User '{}' (ID: {}) created with initial stats: {}", createdUser.getDisplayName(), createdUser.getId(), initialStats);
//            userStatsParser.parseTcStatsForUser(createdUser);
//        } catch (final ExternalConnectionException e) {
//            LOGGER.error("Error retrieving initial stats for user '{}' (ID: {})", createdUser.getDisplayName(), createdUser.getId(), e);
//        }

        return createdUser;
    }

    @Override
    public Optional<User> getUserWithPasskey(final int userId) {
        return storageService.getUser(userId);
    }

    @Override
    public Optional<User> getUserWithoutPasskey(final int userId) {
        final Optional<User> user = getUserWithPasskey(userId);

        if (user.isEmpty()) {
            return user;
        }

        return Optional.of(User.hidePasskey(user.get()));
    }

    @Override
    public Collection<User> getAllUsersWithPasskeys() {
        return storageService.getAllUsers();
    }

    @Override
    public Collection<User> getAllUsersWithoutPasskeys() {
        return getAllUsersWithPasskeys()
            .stream()
            .map(User::hidePasskey)
            .collect(toList());
    }

    @Override
    public User updateUser(final User userToUpdate, final User existingUser) {
//        if (userCaptainHandler.isUserCaptainAndCaptainExistsOnTeam(userToUpdate)) {
//            final boolean isCaptainChange = userToUpdate.isUserIsCaptain() != existingUser.isUserIsCaptain();
//            final boolean isTeamChange = userToUpdate.getTeam().getId() != existingUser.getTeam().getId();
//
//            if (isCaptainChange || isTeamChange) {
//                userCaptainHandler.removeCaptaincyFromExistingTeamCaptain(userToUpdate.getTeam());
//            }
//        }

        // Perform any stats handling before updating the user
//        if (userTeamChangeHandler.isUserTeamChange(userToUpdate, existingUser)) {
//            userTeamChangeHandler.handleTeamChange(userToUpdate, existingUser.getTeam());
//        }

        final User updatedUser = storageService.updateUser(userToUpdate);

//        if (userStateChangeHandler.isUserStateChange(updatedUser, existingUser)) {
//            userStateChangeHandler.handleStateChange(updatedUser);
//            LOGGER.trace("User updated with required state change");
//        }

        return updatedUser;
    }

    @Override
    public void deleteUser(final User user) {
        // Retrieve the user's stats before deleting the user, so we can use the values for the retried user stats
        final Optional<UserTcStats> userStats = storageService.getHourlyTcStats(user.getId());
        storageService.deleteUser(user.getId());

        if (userStats.isEmpty()) {
            LOGGER.warn("User '{}' (ID: {}) has no stats, not saving any retired stats", user.getDisplayName(), user.getId());
            return;
        }

        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.createWithoutId(user, userStats.get());
        final RetiredUserTcStats createdRetiredUserTcStats = storageService.createRetiredUserStats(retiredUserTcStats);
        LOGGER.info("User '{}' (ID: {}) retired with retired stats ID: {}", user.getDisplayName(), user.getId(),
            createdRetiredUserTcStats.getRetiredUserId());
    }

    @Override
    public Collection<User> getUsersOnTeam(final Team team) {
        if (team.getId() == Team.EMPTY_TEAM_ID) {
            return Collections.emptyList();
        }

        return getUsersOnTeamWithPasskeys(team)
            .stream()
            .map(User::hidePasskey)
            .collect(toList());
    }

    @Override
    public Collection<User> getUsersOnTeamWithPasskeys(final Team team) {
        if (team.getId() == Team.EMPTY_TEAM_ID) {
            return Collections.emptyList();
        }

        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getTeam().getId() == team.getId())
            .collect(toList());
    }

    @Override
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        final UserAuthenticationResult userAuthenticationResult = storageService.authenticateSystemUser(userName, password);

        if (userAuthenticationResult.isUserExists() && userAuthenticationResult.isPasswordMatch()) {
            LOGGER.debug("System user '{}' successfully logged in", userName);
        } else {
            LOGGER.debug("Error authenticating system user '{}': {}", userName, userAuthenticationResult);
        }

        return userAuthenticationResult;
    }

    @Override
    public void printCacheContents() {
        storageService.printCacheContents();
    }
}
