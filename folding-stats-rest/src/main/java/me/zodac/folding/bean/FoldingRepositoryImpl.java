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

package me.zodac.folding.bean;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
import me.zodac.folding.api.util.DecodedLoginCredentials;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.user.UserStatsParser;
import me.zodac.folding.rest.exception.NotFoundException;
import me.zodac.folding.state.ParsingStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link Component} used for CRUD operations for {@code folding-stats} classes:
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
                LOGGER.debug("User '{}' (ID: {}) had state change to hardware", userUsingHardware.displayName(), userUsingHardware.id());
                handleStateChange(userUsingHardware);
            }
        }

        return updatedHardware;
    }

    private Collection<User> getUsersWithHardware(final Hardware hardware) {
        if (hardware.id() == Hardware.EMPTY_HARDWARE_ID) {
            return List.of();
        }

        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.hardware().id() == hardware.id())
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
            userStatsParser.parseTcStatsForUsers(users);
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
        storage.deleteTeam(team.id());
    }

    @Override
    public User createUser(final User user) {
        if (isUserCaptainAndCaptainExistsOnTeam(user)) {
            removeCaptaincyFromExistingTeamCaptain(user.team());
        }

        final User createdUser = storage.createUser(user);

        try {
            // When adding a new user, we configure the initial stats DB/cache
            final UserStats currentUserStats = foldingStatsRetriever.getTotalStats(createdUser);
            final UserStats initialStats = statsRepository.createInitialStats(currentUserStats);
            LOGGER.info("User '{}' (ID: {}) created with initial stats: {}", createdUser.displayName(), createdUser.id(), initialStats);
            userStatsParser.parseTcStatsForUser(createdUser);
        } catch (final ExternalConnectionException e) {
            LOGGER.error("Error retrieving initial stats for user '{}' (ID: {})", createdUser.displayName(), createdUser.id(), e);
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
            final boolean isCaptainChange = userToUpdate.role().isCaptain() != existingUser.role().isCaptain();
            final boolean isTeamChange = userToUpdate.team().id() != existingUser.team().id();

            if (isCaptainChange || isTeamChange) {
                removeCaptaincyFromExistingTeamCaptain(userToUpdate.team());
            }
        }

        // Perform any stats handling before updating the user
        if (isUserTeamChange(userToUpdate, existingUser)) {
            handleTeamChange(userToUpdate, existingUser.team());
        }

        final User updatedUser = storage.updateUser(userToUpdate);

        if (isUserStateChange(updatedUser, existingUser)) {
            handleStateChange(updatedUser);
            LOGGER.trace("User updated with required state change");
        }

        return updatedUser;
    }

    private static boolean isUserStateChange(final User updatedUser, final User existingUser) {
        if (existingUser.hardware().id() != updatedUser.hardware().id()) {
            LOGGER.debug("User '{}' (ID: {}) had state change to hardware, {} -> {}", existingUser.displayName(),
                existingUser.id(), existingUser.hardware(), updatedUser.hardware());
            return true;
        }

        if (!existingUser.foldingUserName().equalsIgnoreCase(updatedUser.foldingUserName())) {
            LOGGER.debug("User '{}' (ID: {}) had state change to Folding username, {} -> {}",
                existingUser.displayName(), existingUser.id(), existingUser.foldingUserName(), updatedUser.foldingUserName());
            return true;
        }

        if (!existingUser.passkey().equalsIgnoreCase(updatedUser.passkey())) {
            LOGGER.debug("User '{}' (ID: {}) had state change to passkey, {} -> {}", existingUser.displayName(),
                existingUser.id(), existingUser.passkey(), updatedUser.passkey());
            return true;
        }

        LOGGER.debug("No state change required for updated user '{}' (ID: {})", updatedUser.displayName(), updatedUser.id());
        return false;
    }

    private static boolean isHardwareStateChange(final Hardware updatedHardware, final Hardware existingHardware) {
        // Using BigDecimal since equality checks with doubles can be imprecise
        final BigDecimal existingMultiplier = BigDecimal.valueOf(existingHardware.multiplier());
        final BigDecimal updatedMultiplier = BigDecimal.valueOf(updatedHardware.multiplier());
        final boolean isMultiplierChange = existingMultiplier.compareTo(updatedMultiplier) != 0;

        if (isMultiplierChange) {
            LOGGER.debug("Hardware '{}' (ID: {}) had state change to multiplier, {} -> {}", updatedHardware.id(),
                updatedHardware.hardwareName(), existingHardware.multiplier(), updatedHardware.multiplier());
        }

        return isMultiplierChange;
    }

    private void handleStateChange(final User userWithStateChange) {
        if (ParsingStateManager.current() == ParsingState.DISABLED) {
            LOGGER.info("Received a state change for user '{}' (ID: {}), but system is not currently parsing stats",
                userWithStateChange.displayName(), userWithStateChange.id());
            return;
        }

        try {
            final UserStats userTotalStats = foldingStatsRetriever.getTotalStats(userWithStateChange);
            LOGGER.debug("Setting initial stats to: {}", userTotalStats);
            statsRepository.createInitialStats(userTotalStats);

            final UserTcStats currentUserTcStats = statsRepository.getHourlyTcStats(userWithStateChange);
            final OffsetTcStats offsetTcStats = OffsetTcStats.createFromUserTcStats(currentUserTcStats);
            final OffsetTcStats createdOffsetStats = statsRepository.createOffsetStats(userWithStateChange, offsetTcStats);
            LOGGER.debug("Added offset stats of: {}", createdOffsetStats);

            LOGGER.info("Handled state change for user '{}' (ID: {})", userWithStateChange.displayName(), userWithStateChange.id());
        } catch (final ExternalConnectionException e) {
            LOGGER.error("Unable to update the state of user '{}' (ID: {})", userWithStateChange.displayName(), userWithStateChange.id(), e);
        }
    }

    private static boolean isUserTeamChange(final User updatedUser, final User existingUser) {
        if (updatedUser.team().id() != existingUser.team().id()) {
            LOGGER.info("User '{}' (ID: {}) moved from team '{}' -> '{}'", existingUser.displayName(), existingUser.id(),
                updatedUser.team().teamName(), existingUser.team().teamName());
            return true;
        }

        return false;
    }

    private void handleTeamChange(final User userWithTeamChange, final Team oldTeam) {
        if (ParsingStateManager.current() == ParsingState.DISABLED) {
            LOGGER.info("Received a team change for user '{}' (ID: {}), but system is not currently parsing stats",
                userWithTeamChange.displayName(), userWithTeamChange.id());
            return;
        }

        // Add user's current stats as retired stats for old team
        final UserTcStats userStats = statsRepository.getHourlyTcStats(userWithTeamChange);

        if (!userStats.isEmptyStats()) {
            final RetiredUserTcStats retiredUserTcStats =
                RetiredUserTcStats.createWithoutId(oldTeam.id(), userWithTeamChange.displayName(), userStats);
            final RetiredUserTcStats createdRetiredUserTcStats = statsRepository.createRetiredUserStats(retiredUserTcStats);
            LOGGER.info("User '{}' (ID: {}) replaced and retired with retired stats ID: {}", userWithTeamChange.displayName(),
                userWithTeamChange.id(), createdRetiredUserTcStats.retiredUserId());
        }

        // Reset user stats
        final UserStats userTotalStats = statsRepository.getTotalStats(userWithTeamChange);
        statsRepository.createInitialStats(userTotalStats);

        // Pull stats to update teams
        final Collection<User> users = getAllUsersWithPasskeys();
        userStatsParser.parseTcStatsForUsers(users);

        LOGGER.info("Handled team change for user '{}' (ID: {})", userWithTeamChange.displayName(), userWithTeamChange.id());
    }

    private boolean isUserCaptainAndCaptainExistsOnTeam(final User user) {
        if (!user.role().isCaptain()) {
            return false;
        }

        final Team team = user.team();
        final Optional<User> existingCaptainOptional = getCaptainOfTeam(team);
        if (existingCaptainOptional.isEmpty()) {
            return false;
        }

        final User existingCaptain = existingCaptainOptional.get();
        LOGGER.info("Captain '{} (ID: {})' already exists for team '{}', will be replaced by '{}' (ID: {})",
            existingCaptain.displayName(), existingCaptain.id(), team.teamName(), user.displayName(), user.id()
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
            .filter(user -> user.role().isCaptain())
            .findAny();
    }

    @Override
    public void deleteUser(final User user) {
        // Retrieve the user's stats before deleting the user, so we can use the values for the retried user stats
        final UserTcStats userStats = storage.getHourlyTcStats(user.id())
            .orElse(UserTcStats.empty(user.id()));
        storage.deleteUser(user.id());

        if (userStats.isEmptyStats()) {
            LOGGER.warn("User '{}' (ID: {}) has no stats, not saving any retired stats", user.displayName(), user.id());
            return;
        }

        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.createWithoutId(user, userStats);
        final RetiredUserTcStats createdRetiredUserTcStats = storage.createRetiredUserStats(retiredUserTcStats);
        LOGGER.info("User '{}' (ID: {}) retired with retired stats ID: {}", user.displayName(), user.id(), createdRetiredUserTcStats.retiredUserId());
    }

    @Override
    public Collection<User> getUsersOnTeam(final Team team) {
        if (team.id() == Team.EMPTY_TEAM_ID) {
            return List.of();
        }

        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.team().id() == team.id())
            .map(User::hidePasskey)
            .toList();
    }

    @Override
    public UserAuthenticationResult authenticateSystemUser(final DecodedLoginCredentials decodedLoginCredentials) {
        final UserAuthenticationResult userAuthenticationResult = storage.authenticateSystemUser(decodedLoginCredentials);

        if (userAuthenticationResult.userExists() && userAuthenticationResult.passwordMatch()) {
            LOGGER.debug("System user '{}' successfully logged in", decodedLoginCredentials.username());
        } else {
            LOGGER.debug("Error authenticating system user '{}': {}", decodedLoginCredentials.username(), userAuthenticationResult);
        }

        return userAuthenticationResult;
    }

    @Override
    public UserChange createUserChange(final UserChange userChange) {
        return storage.createUserChange(userChange);
    }

    @Override
    public Collection<UserChange> getAllUserChangesWithPasskeys(final Collection<UserChangeState> states, final long numberOfMonths) {
        return storage.getAllUserChanges(states, numberOfMonths);
    }

    @Override
    public Collection<UserChange> getAllUserChangesWithoutPasskeys(final Collection<UserChangeState> states, final long numberOfMonths) {
        return storage.getAllUserChanges(states, numberOfMonths)
            .stream()
            .map(UserChange::hidePasskey)
            .toList();
    }

    @Override
    public Collection<UserChange> getAllUserChangesForNextMonth() {
        try {
            return storage.getAllUserChanges(List.of(UserChangeState.APPROVED_NEXT_MONTH), 0L);
        } catch (final Exception e) {
            LOGGER.warn("Error retrieving all user changes for next month", e);
            return List.of();
        }
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
