package me.zodac.folding.ejb.core;

import static java.util.stream.Collectors.toList;

import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.ejb.tc.user.UserStateChangeHandler;
import me.zodac.folding.ejb.tc.user.UserStatsParser;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Singleton} EJB implementation of {@link BusinessLogic}.
 *
 * <p>
 * For the most part, this will serve as a wrapper to {@link Storage}, which knows how to perform CRUD operations on the backend storage and caches.
 * But since some logic is needed for special cases (like retrieving the latest Folding@Home stats for a {@link User} when it is created), we
 * implement that logic here, and delegate any CRUD needs to {@link Storage}.
 */
@Singleton
public class BusinessLogicEjb implements BusinessLogic {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Storage STORAGE = Storage.getInstance();
    // TODO: [zodac] After AbstractCrud and UserValidator are sorted, make this the only place to retrieve stats?
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    @EJB
    private UserStateChangeHandler userStateChangeHandler;

    @EJB
    private UserStatsParser userStatsParser;

    @Override
    public Hardware createHardware(final Hardware hardware) {
        return STORAGE.createHardware(hardware);
    }

    @Override
    public Optional<Hardware> getHardware(final int hardwareId) {
        return STORAGE.getHardware(hardwareId);
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return STORAGE.getAllHardware();
    }

    @Override
    public Hardware updateHardware(final Hardware hardwareToUpdate, final Hardware existingHardware) {
        final Hardware updatedHardware = STORAGE.updateHardware(hardwareToUpdate);

        if (userStateChangeHandler.isHardwareStateChange(updatedHardware, existingHardware)) {
            final Collection<User> usersUsingThisHardware = getUsersWithHardware(updatedHardware);

            for (final User user : usersUsingThisHardware) {
                LOGGER.debug("User '{}' (ID: {}) had state change to hardware", user.getDisplayName(), user.getId());
                userStateChangeHandler.handleStateChange(user);
            }
        }

        return updatedHardware;
    }

    @Override
    public void deleteHardware(final Hardware hardware) {
        STORAGE.deleteHardware(hardware.getId());
    }

    @Override
    public Team createTeam(final Team team) {
        return STORAGE.createTeam(team);
    }

    @Override
    public Optional<Team> getTeam(final int teamId) {
        return STORAGE.getTeam(teamId);
    }

    @Override
    public Collection<Team> getAllTeams() {
        return STORAGE.getAllTeams();
    }

    @Override
    public Team updateTeam(final Team teamToUpdate) {
        return STORAGE.updateTeam(teamToUpdate);
    }

    @Override
    public void deleteTeam(final Team team) {
        STORAGE.deleteTeam(team.getId());
    }

    @Override
    public User createUser(final User user) {
        final User createdUser = STORAGE.createUser(user);

        try {
            // When adding a new user, we configure the initial stats DB/cache
            final UserStats currentUserStats = FOLDING_STATS_RETRIEVER.getTotalStats(createdUser);
            final UserStats initialStats = createInitialStats(currentUserStats);
            LOGGER.info("User '{}' (ID: {}) created with initial stats: {}", createdUser.getDisplayName(), createdUser.getId(), initialStats);
            userStatsParser.parseTcStatsForUser(createdUser);
        } catch (final ExternalConnectionException e) {
            LOGGER.error("Error retrieving initial stats for user '{}' (ID: {})", createdUser.getDisplayName(), createdUser.getId(), e);
        }

        return createdUser;
    }

    @Override
    public Optional<User> getUserWithPasskey(final int userId) {
        return STORAGE.getUser(userId);
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
        return STORAGE.getAllUsers();
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
        final User updatedUser = STORAGE.updateUser(userToUpdate);

        if (userStateChangeHandler.isUserStateChange(updatedUser, existingUser)) {
            userStateChangeHandler.handleStateChange(updatedUser);
            LOGGER.trace("User updated with required state change");
        }

        return updatedUser;
    }

    @Override
    public void deleteUser(final User user) {
        // Retrieve the user's stats before deleting the user, so we can use the values for the retried user stats
        final UserTcStats userStats = getHourlyTcStats(user);
        STORAGE.deleteUser(user.getId());

        if (userStats.isEmptyStats()) {
            LOGGER.warn("User '{}' (ID: {}) has no stats, not saving any retired stats", user.getDisplayName(), user.getId());
            return;
        }

        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.createWithoutId(user.getTeam().getId(), user.getDisplayName(), userStats);
        final RetiredUserTcStats createdRetiredUserTcStats = STORAGE.createRetiredUserStats(retiredUserTcStats);
        LOGGER.info("User '{}' (ID: {}) retired with retired stats ID: {}", user.getDisplayName(), user.getId(),
            createdRetiredUserTcStats.getRetiredUserId());
    }

    @Override
    public Optional<Hardware> getHardwareWithName(final String hardwareName) {
        return getAllHardware()
            .stream()
            .filter(hardware -> hardware.getHardwareName().equalsIgnoreCase(hardwareName))
            .findAny();
    }

    @Override
    public Optional<Team> getTeamWithName(final String teamName) {
        return getAllTeams()
            .stream()
            .filter(team -> team.getTeamName().equalsIgnoreCase(teamName))
            .findAny();
    }

    @Override
    public Collection<User> getUsersWithHardware(final Hardware hardware) {
        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getHardware().getId() == hardware.getId())
            .collect(toList());
    }

    @Override
    public Collection<User> getUsersOnTeam(final Team team) {
        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getTeam().getId() == team.getId())
            .collect(toList());
    }

    @Override
    public Optional<User> getUserWithFoldingUserNameAndPasskey(final String foldingUserName, final String passkey) {
        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getFoldingUserName().equalsIgnoreCase(foldingUserName) && user.getPasskey().equalsIgnoreCase(passkey))
            .findAny();
    }

    @Override
    public MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult) {
        return STORAGE.createMonthlyResult(monthlyResult);
    }

    @Override
    public Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year) {
        return STORAGE.getMonthlyResult(month, year);
    }

    @Override
    public Collection<RetiredUserTcStats> getAllRetiredUsersForTeam(final Team team) {
        return STORAGE.getAllRetiredUsers()
            .stream()
            .filter(retiredUserTcStats -> retiredUserTcStats.getTeamId() == team.getId())
            .collect(toList());
    }

    @Override
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        final UserAuthenticationResult userAuthenticationResult = STORAGE.authenticateSystemUser(userName, password);

        if (userAuthenticationResult.isUserExists() && userAuthenticationResult.isPasswordMatch()) {
            LOGGER.debug("System user '{}' successfully logged in", userName);
        } else {
            LOGGER.debug("Error authenticating system user '{}': {}", userName, userAuthenticationResult);
        }

        return userAuthenticationResult;
    }

    @Override
    public Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month, final int day) {
        final Collection<HistoricStats> historicStats = STORAGE.getHistoricStats(user.getId(), year, month, day);
        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}/{}, returning empty", user.getId(), year.getValue(), month.getValue(), day);
        }

        return historicStats;
    }

    @Override
    public Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month) {
        final Collection<HistoricStats> historicStats = STORAGE.getHistoricStats(user.getId(), year, month, 0);
        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}, returning empty", user.getId(), year.getValue(), month.getValue());
        }

        return historicStats;
    }

    @Override
    public Collection<HistoricStats> getHistoricStats(final User user, final Year year) {
        final Collection<HistoricStats> historicStats = STORAGE.getHistoricStats(user.getId(), year, null, 0);
        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}, returning empty", user.getId(), year.getValue());
        }

        return historicStats;
    }

    @Override
    public UserStats createTotalStats(final UserStats userStats) {
        return STORAGE.createTotalStats(userStats);
    }

    @Override
    public UserStats getTotalStats(final User user) {
        return STORAGE.getTotalStats(user.getId())
            .orElse(UserStats.empty());
    }

    @Override
    public OffsetTcStats createOffsetStats(final User user, final OffsetTcStats offsetTcStats) {
        STORAGE.deleteOffsetStats(user.getId());
        return STORAGE.createOrUpdateOffsetStats(user.getId(), offsetTcStats);
    }

    @Override
    public OffsetTcStats createOrUpdateOffsetStats(final User user, final OffsetTcStats offsetTcStats) {
        return STORAGE.createOrUpdateOffsetStats(user.getId(), offsetTcStats);
    }

    @Override
    public OffsetTcStats getOffsetStats(final User user) {
        return STORAGE.getOffsetStats(user.getId())
            .orElse(OffsetTcStats.empty());
    }

    @Override
    public UserTcStats createHourlyTcStats(final UserTcStats userTcStats) {
        return STORAGE.createHourlyTcStats(userTcStats);
    }

    @Override
    public UserTcStats getHourlyTcStats(final User user) {
        return STORAGE.getHourlyTcStats(user.getId())
            .orElse(UserTcStats.empty(user.getId()));
    }

    @Override
    public boolean isAnyHourlyTcStatsExist() {
        return STORAGE.getFirstHourlyTcStats().isPresent();
    }

    @Override
    public UserStats createInitialStats(final UserStats userStats) {
        return STORAGE.createInitialStats(userStats);
    }

    @Override
    public UserStats getInitialStats(final User user) {
        return STORAGE.getInitialStats(user.getId())
            .orElse(UserStats.empty());
    }

    @Override
    public void resetAllTeamCompetitionUserStats() {
        for (final User user : getAllUsersWithoutPasskeys()) {
            LOGGER.info("Resetting TC stats for {}", user.getDisplayName());
            final UserStats totalStats = getTotalStats(user);
            createInitialStats(totalStats);
        }

        LOGGER.info("Deleting retired user TC stats");
        STORAGE.deleteAllRetiredUserTcStats();

        LOGGER.info("Deleting offset TC stats");
        STORAGE.deleteAllOffsetTcStats();

        LOGGER.info("Evicting TC and initial stats caches");
        STORAGE.evictTcStatsCache();
        STORAGE.evictInitialStatsCache();
    }
}
