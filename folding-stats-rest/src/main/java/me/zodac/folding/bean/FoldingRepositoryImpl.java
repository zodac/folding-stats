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

package me.zodac.folding.bean;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import me.zodac.folding.api.FoldingRepository;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.bean.tc.user.UserStatsParser;
import me.zodac.folding.rest.exception.NotFoundException;
import me.zodac.folding.state.ParsingStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link Component} used for CRUD operations for <code>folding-stats</code> classes:
 * <ul>
 *     <li>{@link Hardware}</li>
 *     <li>{@link Team}</li>
 *     <li>{@link User}</li>
 *     <li>{@link UserChange}</li>
 * </ul>
 *
 * <p>
 * For the most part, this will serve as a wrapper to {@link Storage}, which knows how to perform CRUD operations on the backend storage and caches.
 * But since some logic is needed for special cases (like retrieving the latest Folding@Home stats for a {@link User} when it is created), we
 * implement that logic here, and delegate any CRUD needs to {@link Storage}.
 */
@Component
public class FoldingRepositoryImpl implements FoldingRepository {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FoldingStatsRetriever foldingStatsRetriever;
    private final StatsRepository statsRepository;
    private final Storage storage;
    private final UserStatsParser userStatsParser;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingStatsRetriever the {@link FoldingStatsRetriever}
     * @param statsRepository       the {@link StatsRepository}
     * @param storage               the {@link Storage}
     * @param userStatsParser       the {@link UserStatsParser}
     */
    @Autowired
    public FoldingRepositoryImpl(final FoldingStatsRetriever foldingStatsRetriever,
                                 final StatsRepository statsRepository,
                                 final Storage storage,
                                 final UserStatsParser userStatsParser) {
        this.foldingStatsRetriever = foldingStatsRetriever;
        this.statsRepository = statsRepository;
        this.storage = storage;
        this.userStatsParser = userStatsParser;
    }

    @Override
    public Hardware createHardware(final Hardware hardware) {
        return storage.createHardware(hardware);
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return storage.getAllHardware();
    }

    @Override
    public Hardware getHardware(final int hardwareId) {
        return storage.getHardware(hardwareId)
            .orElseThrow(() -> new NotFoundException(Hardware.class, hardwareId));
    }

    @Override
    public Hardware updateHardware(final Hardware hardwareToUpdate, final Hardware existingHardware) {
        final Hardware updatedHardware = storage.updateHardware(hardwareToUpdate);

        if (isHardwareStateChange(updatedHardware, existingHardware)) {
            final Collection<User> usersUsingThisHardware = getUsersWithHardware(updatedHardware);

            for (final User userUsingHardware : usersUsingThisHardware) {
                LOGGER.debug("User '{}' (ID: {}) had state change to hardware", userUsingHardware.getDisplayName(), userUsingHardware.getId());
                handleStateChange(userUsingHardware);
            }
        }

        return updatedHardware;
    }

    private Collection<User> getUsersWithHardware(final Hardware hardware) {
        if (hardware.id() == Hardware.EMPTY_HARDWARE_ID) {
            return Collections.emptyList();
        }

        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getHardware().id() == hardware.id())
            .toList();
    }

    @Override
    public void deleteHardware(final Hardware hardware) {
        storage.deleteHardware(hardware.id());
    }

    @Override
    public Team createTeam(final Team team) {
        final Team createdTeam = storage.createTeam(team);

        // Manual update to add the new (empty) team to the stats
        final Collection<User> users = getAllUsersWithPasskeys();
        if (ParsingStateManager.current() != ParsingState.DISABLED) {
            userStatsParser.parseTcStatsForUsersAndWait(users);
        }
        return createdTeam;
    }

    @Override
    public Collection<Team> getAllTeams() {
        return storage.getAllTeams();
    }

    @Override
    public Team getTeam(final int teamId) {
        return storage.getTeam(teamId)
            .orElseThrow(() -> new NotFoundException(Team.class, teamId));
    }

    @Override
    public Team updateTeam(final Team teamToUpdate) {
        return storage.updateTeam(teamToUpdate);
    }

    @Override
    public void deleteTeam(final Team team) {
        storage.deleteTeam(team.getId());
    }

    @Override
    public User createUser(final User user) {
        if (isUserCaptainAndCaptainExistsOnTeam(user)) {
            removeCaptaincyFromExistingTeamCaptain(user.getTeam());
        }

        final User createdUser = storage.createUser(user);

        try {
            // When adding a new user, we configure the initial stats DB/cache
            final UserStats currentUserStats = foldingStatsRetriever.getTotalStats(createdUser);
            final UserStats initialStats = statsRepository.createInitialStats(currentUserStats);
            LOGGER.info("User '{}' (ID: {}) created with initial stats: {}", createdUser.getDisplayName(), createdUser.getId(), initialStats);
            userStatsParser.parseTcStatsForUsers(Collections.singletonList(createdUser));
        } catch (final ExternalConnectionException e) {
            LOGGER.error("Error retrieving initial stats for user '{}' (ID: {})", createdUser.getDisplayName(), createdUser.getId(), e);
        }

        return createdUser;
    }

    @Override
    public Collection<User> getAllUsersWithPasskeys() {
        return storage.getAllUsers();
    }

    @Override
    public Collection<User> getAllUsersWithoutPasskeys() {
        return getAllUsersWithPasskeys()
            .stream()
            .map(User::hidePasskey)
            .toList();
    }

    @Override
    public User getUserWithPasskey(final int userId) {
        return storage.getUser(userId)
            .orElseThrow(() -> new NotFoundException(User.class, userId));
    }

    @Override
    public User getUserWithoutPasskey(final int userId) {
        final User userWithPasskey = getUserWithPasskey(userId);
        return User.hidePasskey(userWithPasskey);
    }

    @Override
    public User updateUser(final User userToUpdate, final User existingUser) {
        if (isUserCaptainAndCaptainExistsOnTeam(userToUpdate)) {
            final boolean isCaptainChange = userToUpdate.isUserIsCaptain() != existingUser.isUserIsCaptain();
            final boolean isTeamChange = userToUpdate.getTeam().getId() != existingUser.getTeam().getId();

            if (isCaptainChange || isTeamChange) {
                removeCaptaincyFromExistingTeamCaptain(userToUpdate.getTeam());
            }
        }

        // Perform any stats handling before updating the user
        if (isUserTeamChange(userToUpdate, existingUser)) {
            handleTeamChange(userToUpdate, existingUser.getTeam());
        }

        final User updatedUser = storage.updateUser(userToUpdate);

        if (isUserStateChange(updatedUser, existingUser)) {
            handleStateChange(updatedUser);
            LOGGER.trace("User updated with required state change");
        }

        return updatedUser;
    }

    private boolean isUserStateChange(final User updatedUser, final User existingUser) {
        if (existingUser.getHardware().id() != updatedUser.getHardware().id()) {
            LOGGER.debug("User '{}' (ID: {}) had state change to hardware, {} -> {}", existingUser.getDisplayName(),
                existingUser.getId(), existingUser.getHardware(), updatedUser.getHardware());
            return true;
        }

        if (!existingUser.getFoldingUserName().equalsIgnoreCase(updatedUser.getFoldingUserName())) {
            LOGGER.debug("User '{}' (ID: {}) had state change to Folding username, {} -> {}",
                existingUser.getDisplayName(), existingUser.getId(), existingUser.getFoldingUserName(), updatedUser.getFoldingUserName());
            return true;
        }

        if (!existingUser.getPasskey().equalsIgnoreCase(updatedUser.getPasskey())) {
            LOGGER.debug("User '{}' (ID: {}) had state change to passkey, {} -> {}", existingUser.getDisplayName(),
                existingUser.getId(), existingUser.getPasskey(), updatedUser.getPasskey());
            return true;
        }

        LOGGER.debug("No state change required for updated user '{}' (ID: {})", updatedUser.getDisplayName(), updatedUser.getId());
        return false;
    }

    private boolean isHardwareStateChange(final Hardware updatedHardware, final Hardware existingHardware) {
        // Using BigDecimal since equality checks with doubles can be imprecise
        final BigDecimal existingMultiplier = BigDecimal.valueOf(existingHardware.multiplier());
        final BigDecimal updatedMultiplier = BigDecimal.valueOf(updatedHardware.multiplier());
        final boolean isMultiplierChange = !existingMultiplier.equals(updatedMultiplier);

        if (isMultiplierChange) {
            LOGGER.debug("Hardware '{}' (ID: {}) had state change to multiplier, {} -> {}", updatedHardware.id(),
                updatedHardware.hardwareName(), existingHardware.multiplier(), updatedHardware.multiplier());
        }

        return isMultiplierChange;
    }

    private void handleStateChange(final User userWithStateChange) {
        if (ParsingStateManager.current() == ParsingState.DISABLED) {
            LOGGER.info("Received a state change for user '{}' (ID: {}), but system is not currently parsing stats",
                userWithStateChange.getDisplayName(), userWithStateChange.getId());
            return;
        }

        try {
            final UserStats userTotalStats = foldingStatsRetriever.getTotalStats(userWithStateChange);
            LOGGER.debug("Setting initial stats to: {}", userTotalStats);
            statsRepository.createInitialStats(userTotalStats);

            final UserTcStats currentUserTcStats = statsRepository.getHourlyTcStats(userWithStateChange);
            final OffsetTcStats offsetTcStats = OffsetTcStats.create(currentUserTcStats);
            final OffsetTcStats createdOffsetStats = statsRepository.createOffsetStats(userWithStateChange, offsetTcStats);
            LOGGER.debug("Added offset stats of: {}", createdOffsetStats);

            LOGGER.info("Handled state change for user '{}' (ID: {})", userWithStateChange.getDisplayName(), userWithStateChange.getId());
        } catch (final ExternalConnectionException e) {
            LOGGER.error("Unable to update the state of user '{}' (ID: {})", userWithStateChange.getDisplayName(), userWithStateChange.getId(), e);
        }
    }

    private boolean isUserTeamChange(final User updatedUser, final User existingUser) {
        if (updatedUser.getTeam().getId() != existingUser.getTeam().getId()) {
            LOGGER.info("User '{}' (ID: {}) moved from team '{}' -> '{}'", existingUser.getDisplayName(), existingUser.getId(),
                updatedUser.getTeam().getTeamName(), existingUser.getTeam().getTeamName());
            return true;
        }

        return false;
    }

    private void handleTeamChange(final User userWithTeamChange, final Team oldTeam) {
        if (ParsingStateManager.current() == ParsingState.DISABLED) {
            LOGGER.info("Received a team change for user '{}' (ID: {}), but system is not currently parsing stats",
                userWithTeamChange.getDisplayName(), userWithTeamChange.getId());
            return;
        }

        // Add user's current stats as retired stats for old team
        final UserTcStats userStats = statsRepository.getHourlyTcStats(userWithTeamChange);

        if (!userStats.isEmptyStats()) {
            final RetiredUserTcStats retiredUserTcStats =
                RetiredUserTcStats.createWithoutId(oldTeam.getId(), userWithTeamChange.getDisplayName(), userStats);
            final RetiredUserTcStats createdRetiredUserTcStats = statsRepository.createRetiredUserStats(retiredUserTcStats);
            LOGGER.info("User '{}' (ID: {}) retired with retired stats ID: {}", userWithTeamChange.getDisplayName(), userWithTeamChange.getId(),
                createdRetiredUserTcStats.getRetiredUserId());
        }

        // Reset user stats
        final UserStats userTotalStats = statsRepository.getTotalStats(userWithTeamChange);
        statsRepository.createInitialStats(userTotalStats);

        // Pull stats to update teams
        final Collection<User> users = getAllUsersWithPasskeys();
        userStatsParser.parseTcStatsForUsersAndWait(users);

        LOGGER.info("Handled team change for user '{}' (ID: {})", userWithTeamChange.getDisplayName(), userWithTeamChange.getId());
    }

    private boolean isUserCaptainAndCaptainExistsOnTeam(final User user) {
        if (!user.isUserIsCaptain()) {
            return false;
        }

        final Team team = user.getTeam();
        final Optional<User> existingCaptainOptional = getCaptainOfTeam(team);
        if (existingCaptainOptional.isEmpty()) {
            return false;
        }

        final User existingCaptain = existingCaptainOptional.get();
        LOGGER.info("Captain '{} (ID: {})' already exists for team '{}', will be replaced by '{}' (ID: {})",
            existingCaptain.getDisplayName(), existingCaptain.getId(), team.getTeamName(), user.getDisplayName(), user.getId()
        );
        return true;
    }

    private void removeCaptaincyFromExistingTeamCaptain(final Team team) {
        final Optional<User> existingCaptainOptional = getCaptainOfTeam(team);
        if (existingCaptainOptional.isEmpty()) {
            return;
        }

        final User existingCaptain = existingCaptainOptional.get();
        final User userWithCaptaincyRemoved = User.removeCaptaincyFromUser(existingCaptain);

        updateUser(userWithCaptaincyRemoved, existingCaptain);
    }

    private Optional<User> getCaptainOfTeam(final Team team) {
        return getUsersOnTeam(team)
            .stream()
            .filter(User::isUserIsCaptain)
            .findAny();
    }

    @Override
    public void deleteUser(final User user) {
        // Retrieve the user's stats before deleting the user, so we can use the values for the retried user stats
        final UserTcStats userStats = storage.getHourlyTcStats(user.getId())
            .orElse(UserTcStats.empty(user.getId()));
        storage.deleteUser(user.getId());

        if (userStats.isEmptyStats()) {
            LOGGER.warn("User '{}' (ID: {}) has no stats, not saving any retired stats", user.getDisplayName(), user.getId());
            return;
        }

        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.createWithoutId(user, userStats);
        final RetiredUserTcStats createdRetiredUserTcStats = storage.createRetiredUserStats(retiredUserTcStats);
        LOGGER.info("User '{}' (ID: {}) retired with retired stats ID: {}", user.getDisplayName(), user.getId(),
            createdRetiredUserTcStats.getRetiredUserId());
    }

    @Override
    public Collection<User> getUsersOnTeam(final Team team) {
        if (team.getId() == Team.EMPTY_TEAM_ID) {
            return Collections.emptyList();
        }

        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getTeam().getId() == team.getId())
            .map(User::hidePasskey)
            .toList();
    }

    @Override
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        final UserAuthenticationResult userAuthenticationResult = storage.authenticateSystemUser(userName, password);

        if (userAuthenticationResult.userExists() && userAuthenticationResult.passwordMatch()) {
            LOGGER.debug("System user '{}' successfully logged in", userName);
        } else {
            LOGGER.debug("Error authenticating system user '{}': {}", userName, userAuthenticationResult);
        }

        return userAuthenticationResult;
    }

    @Override
    public UserChange createUserChange(final UserChange userChange) {
        return storage.createUserChange(userChange);
    }

    @Override
    public Collection<UserChange> getAllUserChangesWithPasskeys(final Collection<UserChangeState> states, final int numberOfMonths) {
        return storage.getAllUserChanges(states, numberOfMonths);
    }

    @Override
    public Collection<UserChange> getAllUserChangesWithoutPasskeys(final Collection<UserChangeState> states, final int numberOfMonths) {
        return storage.getAllUserChanges(states, numberOfMonths)
            .stream()
            .map(UserChange::hidePasskey)
            .toList();
    }

    @Override
    public Collection<UserChange> getAllUserChangesForNextMonth() {
        return storage.getAllUserChanges(Collections.singletonList(UserChangeState.APPROVED_NEXT_MONTH), 0);
    }

    @Override
    public UserChange getUserChange(final int userChangeId) {
        return storage.getUserChange(userChangeId)
            .orElseThrow(() -> new NotFoundException(UserChange.class, userChangeId));
    }

    @Override
    public UserChange updateUserChange(final UserChange userChangeToUpdate) {
        return storage.updateUserChange(userChangeToUpdate);
    }

    @Override
    public void printCacheContents() {
        storage.printCacheContents();
    }
}
