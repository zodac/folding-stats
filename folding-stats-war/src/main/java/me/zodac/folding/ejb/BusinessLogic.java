package me.zodac.folding.ejb;


import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.db.SystemUserAuthentication;
import me.zodac.folding.api.db.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.HardwareNotFoundException;
import me.zodac.folding.api.tc.exception.NoStatsAvailableException;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.api.tc.exception.TeamNotFoundException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.DateTimeUtils;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.InitialStatsCache;
import me.zodac.folding.cache.OffsetStatsCache;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TeamCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Singleton;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * In order to decouple the REST layer from any business requirements, we move that logic into this {@link Singleton} EJB.
 * <p>
 * This should simplify the REST layer to simply validate incoming requests and forward to here.
 */
// TODO: [zodac] Should replace the cache miss warnings with some metrics instead?
// TODO: [zodac] Split into one Facade for POJOs and one for stats?
// TODO: [zodac] I really don't like how much logic is in here now, originally I planned for this just to avoid needing to specify
//  both DB and cache in the REST/EJB layer. I think it's gotten too big and needs to be scaled back...
// TODO: [zodac] Also don't like how the #get() methods don't use Optional, why am I relying on *NotFoundException?
@Singleton
public class BusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessLogic.class);
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    private final DbManager dbManager = DbManagerRetriever.get();
    private final TeamCache teamCache = TeamCache.get();
    private final UserCache userCache = UserCache.get();
    private final HardwareCache hardwareCache = HardwareCache.get();

    private final InitialStatsCache initialStatsCache = InitialStatsCache.get();
    private final OffsetStatsCache offsetStatsCache = OffsetStatsCache.get();
    private final RetiredTcStatsCache retiredStatsCache = RetiredTcStatsCache.get();
    private final TcStatsCache tcStatsCache = TcStatsCache.get();
    private final TotalStatsCache totalStatsCache = TotalStatsCache.get();

    public Hardware createHardware(final Hardware hardware) throws FoldingException, FoldingConflictException {
        final Hardware hardwareWithId = dbManager.createHardware(hardware);
        hardwareCache.add(hardwareWithId);
        return hardwareWithId;
    }

    public Hardware getHardware(final int hardwareId) throws FoldingException, HardwareNotFoundException {
        try {
            return hardwareCache.get(hardwareId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find hardware with ID {} in cache", hardwareId, e);
        }

        LOGGER.trace("Cache miss! Get hardware");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Hardware hardwareFromDb = dbManager.getHardware(hardwareId);
        hardwareCache.add(hardwareFromDb);
        return hardwareFromDb;
    }

    public Collection<Hardware> getAllHardware() throws FoldingException {
        final Collection<Hardware> allHardware = hardwareCache.getAll();

        if (!allHardware.isEmpty()) {
            return allHardware;
        }

        LOGGER.trace("Cache miss! Get all hardware");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Collection<Hardware> allHardwareFromDb = dbManager.getAllHardware();
        hardwareCache.addAll(allHardwareFromDb);
        return allHardwareFromDb;
    }

    public Optional<Hardware> getHardwareForUser(final User user) {
        try {
            return getAllHardware()
                    .stream()
                    .filter(hardware -> hardware.getId() == user.getHardware().getId())
                    .findAny();
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting all hardware to retrieve hardware for user", e.getCause());
            return Optional.empty();
        }
    }

    public void updateHardware(final Hardware updatedHardware) throws FoldingException, HardwareNotFoundException, FoldingConflictException, FoldingExternalServiceException {
        final Hardware existingHardware = getHardware(updatedHardware.getId());
        dbManager.updateHardware(updatedHardware);
        hardwareCache.add(updatedHardware);

        // If the multiplier is changed then any users that use this hardware must have their initial stats updated
        if (existingHardware.getMultiplier() != updatedHardware.getMultiplier()) {
            final List<User> usersWithUpdatedHardware = getAllUsers()
                    .stream()
                    .filter(user -> user.getHardware().getId() == updatedHardware.getId())
                    .collect(toList());
            LOGGER.debug("Hardware had state change to multiplier {} -> {}, recalculating initial stats for {} users", existingHardware.getMultiplier(), updatedHardware.getMultiplier(), usersWithUpdatedHardware.size());

            for (final User user : usersWithUpdatedHardware) {
                LOGGER.debug("User {} had state change to hardware multiplier", user.getFoldingUserName());
                handleStateChangeForUser(user);
            }
        }
    }

    public void deleteHardware(final int hardwareId) throws FoldingException, FoldingConflictException {
        dbManager.deleteHardware(hardwareId);
        hardwareCache.remove(hardwareId);
    }

    public User createUser(final User user) throws FoldingException, FoldingConflictException, FoldingExternalServiceException {
        final User userWithId = dbManager.createUser(user);
        userCache.add(userWithId);

        // TODO: [zodac] Should the StorageFacade be responsible for making this stats call? Or should it be the caller requesting it?
        // When adding a new user, we configure the initial stats DB/cache
        persistInitialUserStats(userWithId);
        // When adding a new user, we give an empty offset to the offset cache
        offsetStatsCache.add(userWithId.getId(), OffsetStats.empty());

        return userWithId;
    }

    public User getUser(final int userId) throws FoldingException, UserNotFoundException {
        return getUserWithPasskey(userId, true);
    }

    public User getUserWithPasskey(final int userId, final boolean showFullPasskeys) throws FoldingException, UserNotFoundException {
        try {
            final User user = userCache.get(userId);
            return showFullPasskeys ? user : User.hidePasskey(user);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find user with ID {} in cache", userId, e);
        }

        LOGGER.trace("Cache miss! Get user");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final User userFromDb = dbManager.getUser(userId);
        userCache.add(userFromDb);

        return showFullPasskeys ? userFromDb : User.hidePasskey(userFromDb);
    }

    public Collection<User> getAllUsers() throws FoldingException {
        return getAllUsersWithPasskeys(true);
    }

    public Collection<User> getAllUsersWithPasskeys(final boolean showFullPasskeys) throws FoldingException {
        final Collection<User> allUsers = userCache.getAll();

        if (!allUsers.isEmpty()) {
            if (showFullPasskeys) {
                return allUsers;
            }

            return allUsers.stream()
                    .map(User::hidePasskey)
                    .collect(toList());
        }

        LOGGER.trace("Cache miss! Get all users");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Collection<User> allUsersFromDb = dbManager.getAllUsers();
        userCache.addAll(allUsersFromDb);

        if (showFullPasskeys) {
            return allUsersFromDb;
        }

        return allUsersFromDb.stream()
                .map(User::hidePasskey)
                .collect(toList());
    }

    public void updateUser(final User updatedUser) throws FoldingException, UserNotFoundException, FoldingConflictException, FoldingExternalServiceException {
        final User existingUser = getUser(updatedUser.getId());
        dbManager.updateUser(updatedUser);
        userCache.add(updatedUser);

        if (!existingUser.getFoldingUserName().equalsIgnoreCase(updatedUser.getFoldingUserName())) {
            LOGGER.debug("User had state change to Folding username {} -> {}, recalculating initial stats", existingUser.getFoldingUserName(), updatedUser.getFoldingUserName());
            handleStateChangeForUser(updatedUser);
        } else if (!existingUser.getPasskey().equalsIgnoreCase(updatedUser.getPasskey())) {
            LOGGER.debug("User had state change to passkey {} -> {}, recalculating initial stats", existingUser.getPasskey(), updatedUser.getPasskey());
            handleStateChangeForUser(updatedUser);
        } else if (existingUser.getHardware() != updatedUser.getHardware()) {
            LOGGER.debug("User had state change to hardware ID {} -> {}, recalculating initial stats", existingUser.getHardware(), updatedUser.getHardware());
            handleStateChangeForUser(updatedUser);
        }
    }

    // If a user is updated and their Folding username, hardware ID or passkey is changed, we need to update their initial offset again
    // Also occurs if the hardware multiplier for a hardware used by a user is changed
    // We set the new initial stats to the user's current total stats, then give an offset of their current TC stats (multiplied)
    private void handleStateChangeForUser(final User updatedUser) throws FoldingException, FoldingExternalServiceException {
        final UserStats userTotalStats = FOLDING_STATS_RETRIEVER.getTotalStats(updatedUser);
        final UserTcStats currentUserTcStats = getCurrentTcStatsForUserOrDefault(updatedUser);

        LOGGER.debug("Setting initial stats to: {}", userTotalStats);
        dbManager.persistInitialStats(userTotalStats);
        initialStatsCache.add(updatedUser.getId(), userTotalStats.getStats());

        final OffsetStats offsetStats = OffsetStats.create(currentUserTcStats.getPoints(), currentUserTcStats.getMultipliedPoints(), currentUserTcStats.getUnits());
        LOGGER.debug("Adding offset stats of: {}", offsetStats);
        addOffsetStats(updatedUser.getId(), offsetStats);
    }

    private UserTcStats getCurrentTcStatsForUserOrDefault(final User updatedUser) throws FoldingException {
        try {
            return getTcStatsForUser(updatedUser.getId());
        } catch (final UserNotFoundException e) {
            LOGGER.debug("Unable to find {} with ID: {}, using 0 values", e.getType(), e.getId(), e);
            return UserTcStats.empty(updatedUser.getId());
        } catch (final NoStatsAvailableException e) {
            LOGGER.debug("No stats found for user with ID: {}, using 0 values", updatedUser.getId(), e);
            return UserTcStats.empty(updatedUser.getId());
        }
    }

    public void deleteUser(final int userId) throws FoldingConflictException, FoldingException {
        try {
            final User user = getUser(userId);
            final Team team = user.getTeam();

            dbManager.deleteUser(userId);
            userCache.remove(userId);

            final UserTcStats userStats = getTcStatsForUser(userId);

            if (userStats.isEmpty()) {
                LOGGER.warn("User '{}' has no stats, not saving any retired stats", user.getDisplayName());
                return;
            }

            final int retiredUserId = dbManager.persistRetiredUserStats(team.getId(), user.getId(), user.getDisplayName(), userStats);
            retiredStatsCache.add(RetiredUserTcStats.create(retiredUserId, team.getId(), user.getDisplayName(), userStats));
        } catch (final UserNotFoundException | NoStatsAvailableException e) {
            LOGGER.debug("Error getting final stats for deleted user with ID: {}", userId, e);
            LOGGER.warn("Error getting final stats for deleted user with ID: {}", userId);
        }
    }

    public Team createTeam(final Team team) throws FoldingException, FoldingConflictException {
        final Team teamWithId = dbManager.createTeam(team);
        teamCache.add(teamWithId);
        return teamWithId;
    }

    public Team getTeam(final int teamId) throws FoldingException, TeamNotFoundException {
        try {
            return teamCache.get(teamId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find team with ID {} in cache", teamId, e);
        }

        LOGGER.trace("Cache miss! Get team");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Team teamFromDb = dbManager.getTeam(teamId);
        teamCache.add(teamFromDb);
        return teamFromDb;
    }

    public Collection<Team> getAllTeams() throws FoldingException {
        final Collection<Team> allTeams = teamCache.getAll();

        if (!allTeams.isEmpty()) {
            return allTeams;
        }

        LOGGER.trace("Cache miss! Get all teams");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Collection<Team> allTeamsFromDb = dbManager.getAllTeams();
        teamCache.addAll(allTeamsFromDb);
        return allTeamsFromDb;
    }

    public void updateTeam(final Team team) throws FoldingException, FoldingConflictException {
        dbManager.updateTeam(team);
        teamCache.add(team);
    }

    public void deleteTeam(final int teamId) throws FoldingException, FoldingConflictException {
        dbManager.deleteTeam(teamId);
        teamCache.remove(teamId);
    }

    public void persistInitialUserStats(final User user) throws FoldingException, FoldingExternalServiceException {
        final UserStats currentUserStats = FOLDING_STATS_RETRIEVER.getTotalStats(user);
        persistInitialUserStats(currentUserStats);
    }

    public void persistInitialUserStats(final UserStats userStats) throws FoldingException {
        dbManager.persistInitialStats(userStats);
        initialStatsCache.add(userStats.getUserId(), userStats.getStats());
    }

    public Stats getInitialStatsForUser(final int userId) throws FoldingException {
        final Optional<Stats> initialStats = initialStatsCache.get(userId);
        if (initialStats.isPresent()) {
            return initialStats.get();
        }

        LOGGER.trace("Cache miss! getInitialStatsForUser");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Stats initialStatsFromDb = dbManager.getInitialStats(userId).getStats();
        initialStatsCache.add(userId, initialStatsFromDb);
        return initialStatsFromDb;
    }

    public void persistHourlyTcStatsForUser(final UserTcStats userTcStats) throws FoldingException {
        dbManager.persistHourlyTcStats(userTcStats);
        tcStatsCache.add(userTcStats.getUserId(), userTcStats);
    }

    public UserTcStats getTcStatsForUser(final int userId) throws UserNotFoundException, FoldingException, NoStatsAvailableException {
        final Optional<UserTcStats> optionalUserTcStats = tcStatsCache.get(userId);

        if (optionalUserTcStats.isPresent()) {
            return optionalUserTcStats.get();
        }

        LOGGER.trace("Cache miss! Current TC stats");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final UserTcStats userTcStatsFromDb = dbManager.getHourlyTcStats(userId);
        tcStatsCache.add(userId, userTcStatsFromDb);
        return userTcStatsFromDb;
    }

    public Collection<HistoricStats> getHistoricStatsHourly(final int userId, final int day, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        try {
            return dbManager.getHistoricStatsHourly(userId, day, month, year);
        } catch (final NoStatsAvailableException e) {
            LOGGER.debug("No stats retrieved for user with ID {} on {}/{}/{}, returning empty", userId, year.getValue(), month.getValue(), day, e);
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}/{}, returning empty", userId, year.getValue(), month.getValue(), day);
            return Collections.emptyList();
        }
    }

    public Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        try {
            return dbManager.getHistoricStatsDaily(userId, month, year);
        } catch (final NoStatsAvailableException e) {
            LOGGER.debug("No stats retrieved for user with ID {} on {}/{}, returning empty", userId, year.getValue(), month.getValue(), e);
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}, returning empty", userId, year.getValue(), month.getValue());
            return Collections.emptyList();
        }
    }

    public Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year) throws FoldingException, UserNotFoundException {
        try {
            return dbManager.getHistoricStatsMonthly(userId, year);
        } catch (final NoStatsAvailableException e) {
            LOGGER.debug("No stats retrieved for user with ID {} on {}, returning empty", userId, year.getValue(), e);
            LOGGER.warn("No stats retrieved for user with ID {} on {}, returning empty", userId, year.getValue());
            return Collections.emptyList();
        }
    }

    public void addOffsetStats(final int userId, final OffsetStats offsetStats) throws FoldingException {
        dbManager.addOffsetStats(userId, offsetStats);
        offsetStatsCache.add(userId, offsetStats);
    }

    public void addOrUpdateOffsetStats(final int userId, final OffsetStats offsetStats) throws FoldingException {
        final OffsetStats offsetStatsFromDb = dbManager.addOrUpdateOffsetStats(userId, offsetStats);
        offsetStatsCache.add(userId, offsetStatsFromDb);
    }

    public OffsetStats getOffsetStatsForUser(final int userId) throws FoldingException {
        final Optional<OffsetStats> offsetStats = offsetStatsCache.get(userId);
        if (offsetStats.isPresent()) {
            return offsetStats.get();
        }

        LOGGER.trace("Cache miss! getOffsetStatsForUser");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final OffsetStats offsetStatsFromDb = dbManager.getOffsetStats(userId);
        offsetStatsCache.add(userId, offsetStatsFromDb);
        return offsetStatsFromDb;
    }


    public void initialiseOffsetStats() throws FoldingException {
        for (final User user : getAllUsers()) {
            final OffsetStats offsetStats = dbManager.getOffsetStats(user.getId());
            offsetStatsCache.add(user.getId(), offsetStats);
        }
    }

    public void clearOffsetStats() throws FoldingConflictException, FoldingException {
        dbManager.clearAllOffsetStats();
        offsetStatsCache.clearOffsets();
    }

    public void persistTotalStatsForUser(final UserStats stats) throws FoldingException {
        dbManager.persistTotalStats(stats);
        totalStatsCache.add(stats.getUserId(), stats.getStats());
    }

    public Stats getTotalStatsForUser(final int userId) throws FoldingException {
        final Optional<Stats> optionalTotalStats = totalStatsCache.get(userId);

        if (optionalTotalStats.isPresent()) {
            return optionalTotalStats.get();
        }

        LOGGER.trace("Cache miss! Total stats");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Stats userTotalStatsFromDb = dbManager.getTotalStats(userId).getStats();
        totalStatsCache.add(userId, userTotalStatsFromDb);
        return userTotalStatsFromDb;
    }

    public void updateInitialStatsForUser(final User user) throws UserNotFoundException, FoldingException {
        LOGGER.info("Updating initial stats for user: {}", user.getDisplayName());
        final Stats totalStats = getTotalStatsForUser(user.getId());
        persistInitialUserStats(UserStats.create(user.getId(), DateTimeUtils.currentUtcTimestamp(), totalStats));
        initialStatsCache.add(user.getId(), totalStats);
    }

    public boolean doesNotContainHardware(final int hardwareId) {
        try {
            getHardware(hardwareId);
            return false;
        } catch (final FoldingException | HardwareNotFoundException e) {
            LOGGER.debug("Unable to find hardware with ID: {}", hardwareId, e);
            return true;
        }
    }

    public SystemUserAuthentication isValidUser(final String userName, final String password) throws FoldingException {
        return dbManager.isValidSystemUser(userName, password);
    }

    public boolean doesNotContainTeam(final int teamId) {
        try {
            getTeam(teamId);
            return false;
        } catch (final FoldingException | TeamNotFoundException e) {
            LOGGER.debug("Unable to find team with ID: {}", teamId, e);
            return true;
        }
    }

    public Collection<User> getUsersOnTeam(final Team team) throws FoldingException {
        return getAllUsers().stream()
                .filter(user -> user.getTeam().getId() == team.getId())
                .collect(toList());
    }

    public Collection<RetiredUserTcStats> getRetiredUsersForTeam(final Team team) throws FoldingException {
        return dbManager.getRetiredUserStatsForTeam(team);
    }

    public void deleteRetiredUserStats() throws FoldingConflictException, FoldingException {
        dbManager.deleteRetiredUserStats();
    }
}
