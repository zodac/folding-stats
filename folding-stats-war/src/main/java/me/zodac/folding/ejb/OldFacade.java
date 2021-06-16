package me.zodac.folding.ejb;

import static java.util.stream.Collectors.toList;

import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.api.SystemUserAuthentication;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
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

@Deprecated
@Singleton
public class OldFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(OldFacade.class);
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    private final transient DbManager dbManager = DbManagerRetriever.get();
    private final transient TeamCache teamCache = TeamCache.getInstance();
    private final transient UserCache userCache = UserCache.getInstance();
    private final transient HardwareCache hardwareCache = HardwareCache.getInstance();

    private final transient InitialStatsCache initialStatsCache = InitialStatsCache.get();
    private final transient OffsetStatsCache offsetStatsCache = OffsetStatsCache.get();
    private final transient RetiredTcStatsCache retiredStatsCache = RetiredTcStatsCache.get();
    private final transient TcStatsCache tcStatsCache = TcStatsCache.get();
    private final transient TotalStatsCache totalStatsCache = TotalStatsCache.get();

    @EJB
    private transient UserTeamCompetitionStatsParser userTeamCompetitionStatsParser;

    public void updateHardware(final Hardware updatedHardware, final Hardware existingHardware) throws ExternalConnectionException {
        dbManager.updateHardware(updatedHardware);
        hardwareCache.add(updatedHardware);

        final List<User> usersUsingThisHardware = getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getHardware().getId() == updatedHardware.getId())
            .collect(toList());

        final boolean isHardwareMultiplierChange = existingHardware.getMultiplier() != updatedHardware.getMultiplier();

        for (final User user : usersUsingThisHardware) {
            // All users are referencing the old hardware, rather than recreating them, just evict them from the cache
            UserCache.getInstance().remove(user.getId()); // TODO: Don't use cache directly, use Storage#evictUserFromCache()

            if (isHardwareMultiplierChange) {
                LOGGER.debug("User {} had state change to hardware multiplier", user.getFoldingUserName());
                handleStateChangeForUser(user);
            }
        }
    }

    public User createUser(final User user) throws ExternalConnectionException {
        final User userWithId = dbManager.createUser(user);
        userCache.add(userWithId);

        // When adding a new user, we configure the initial stats DB/cache
        persistInitialUserStats(userWithId);
        // When adding a new user, we give an empty offset to the offset cache
        offsetStatsCache.add(userWithId.getId(), OffsetStats.empty());

        userTeamCompetitionStatsParser.parseTcStatsForUser(user);

        return userWithId;
    }

    public User getUserWithPasskey(final int userId) throws UserNotFoundException {
        try {
            return userCache.getOrError(userId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find user with ID {} in cache", userId, e);
        }

        LOGGER.trace("Cache miss! Get user");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final User userFromDb = dbManager.getUser(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userCache.add(userFromDb);

        return userFromDb;
    }

    public User getUserWithoutPasskey(final int userId) throws UserNotFoundException {
        try {
            final User user = userCache.getOrError(userId);
            return User.hidePasskey(user);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find user with ID {} in cache", userId, e);
        }

        LOGGER.trace("Cache miss! Get user");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final User userFromDb = dbManager.getUser(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userCache.add(userFromDb);

        return User.hidePasskey(userFromDb);
    }

    public Collection<User> getAllUsersWithPasskeys() {
        final Collection<User> allUsers = userCache.getAll();

        if (!allUsers.isEmpty()) {
            return allUsers;
        }

        LOGGER.trace("Cache miss! Get all users");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Collection<User> allUsersFromDb = dbManager.getAllUsers();
        userCache.addAll(allUsersFromDb);
        return allUsersFromDb;
    }

    public Collection<User> getAllUsersWithoutPasskeys() {
        final Collection<User> allUsers = userCache.getAll();

        if (!allUsers.isEmpty()) {
            return allUsers.stream()
                .map(User::hidePasskey)
                .collect(toList());
        }

        LOGGER.trace("Cache miss! Get all users");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Collection<User> allUsersFromDb = dbManager.getAllUsers();
        userCache.addAll(allUsersFromDb);

        return allUsersFromDb.stream()
            .map(User::hidePasskey)
            .collect(toList());
    }

    public void updateUser(final User updatedUser) throws UserNotFoundException, ExternalConnectionException {
        final User existingUser = getUserWithPasskey(updatedUser.getId());
        dbManager.updateUser(updatedUser);
        userCache.add(updatedUser);

        if (!existingUser.getHardware().equals(updatedUser.getHardware())) {
            LOGGER.debug("User had state change to hardware, {} -> {}, recalculating initial stats", existingUser.getHardware(),
                updatedUser.getHardware());
            handleStateChangeForUser(updatedUser);
            userCache.add(updatedUser);
            return;
        }

        if (!existingUser.getTeam().equals(updatedUser.getTeam())) {
            LOGGER.debug("User had state change to team, {} -> {}, recalculating initial stats", existingUser.getTeam(), updatedUser.getTeam());
            handleStateChangeForUser(updatedUser);
            userCache.add(updatedUser);
            return;
        }

        if (!existingUser.getFoldingUserName().equalsIgnoreCase(updatedUser.getFoldingUserName())) {
            LOGGER.debug("User had state change to Folding username, {} -> {}, recalculating initial stats", existingUser.getFoldingUserName(),
                updatedUser.getFoldingUserName());
            handleStateChangeForUser(updatedUser);
            userCache.add(updatedUser);
            return;
        }

        if (!existingUser.getPasskey().equalsIgnoreCase(updatedUser.getPasskey())) {
            LOGGER.debug("User had state change to passkey, {} -> {}, recalculating initial stats", existingUser.getPasskey(),
                updatedUser.getPasskey());
            handleStateChangeForUser(updatedUser);
            userCache.add(updatedUser);
            return;
        }

        LOGGER.trace("User updated with any required state changes");
    }

    // If a user is updated and their Folding username, hardware ID or passkey is changed, we need to update their initial offset again
    // Also occurs if the hardware multiplier for a hardware used by a user is changed
    // We set the new initial stats to the user's current total stats, then give an offset of their current TC stats (multiplied)
    private void handleStateChangeForUser(final User userWithStateChange) throws ExternalConnectionException {
        final UserStats userTotalStats = FOLDING_STATS_RETRIEVER.getTotalStats(userWithStateChange);
        final UserTcStats currentUserTcStats = getTcStatsForUser(userWithStateChange.getId());

        LOGGER.debug("Setting initial stats to: {}", userTotalStats);
        dbManager.persistInitialStats(userTotalStats);
        initialStatsCache.add(userWithStateChange.getId(), userTotalStats.getStats());

        final OffsetStats offsetStats =
            OffsetStats.create(currentUserTcStats.getPoints(), currentUserTcStats.getMultipliedPoints(), currentUserTcStats.getUnits());
        LOGGER.debug("Adding offset stats of: {}", offsetStats);
        addOffsetStats(userWithStateChange.getId(), offsetStats);
    }

    public void deleteUser(final User user) {
        final int userId = user.getId();
        dbManager.deleteUser(userId);
        userCache.remove(userId);

        final UserTcStats userStats = getTcStatsForUser(userId);

        if (userStats.isEmpty()) {
            LOGGER.warn("User '{}' has no stats, not saving any retired stats", user.getDisplayName());
            return;
        }

        final Team team = user.getTeam();
        final int retiredUserId = dbManager.persistRetiredUserStats(team.getId(), user.getId(), user.getDisplayName(), userStats);
        retiredStatsCache.add(RetiredUserTcStats.create(retiredUserId, team.getId(), user.getDisplayName(), userStats));
    }

    public void updateTeam(final Team team) {
        dbManager.updateTeam(team);
        teamCache.add(team);
    }

    public void persistInitialUserStats(final User user) throws ExternalConnectionException {
        final UserStats currentUserStats = FOLDING_STATS_RETRIEVER.getTotalStats(user);
        persistInitialUserStats(currentUserStats);
    }

    public void persistInitialUserStats(final UserStats userStats) {
        dbManager.persistInitialStats(userStats);
        initialStatsCache.add(userStats.getUserId(), userStats.getStats());
    }

    public Stats getInitialStatsForUser(final int userId) {
        final Optional<Stats> initialStats = initialStatsCache.get(userId);
        if (initialStats.isPresent()) {
            return initialStats.get();
        }

        LOGGER.trace("Cache miss! getInitialStatsForUser");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Stats initialStatsFromDb = dbManager.getInitialStats(userId)
            .orElse(UserStats.empty())
            .getStats();
        initialStatsCache.add(userId, initialStatsFromDb);
        return initialStatsFromDb;
    }

    public void persistHourlyTcStatsForUser(final UserTcStats userTcStats) {
        dbManager.persistHourlyTcStats(userTcStats);
        tcStatsCache.add(userTcStats.getUserId(), userTcStats);
    }

    public UserTcStats getTcStatsForUser(final int userId) {
        final Optional<UserTcStats> optionalUserTcStats = tcStatsCache.get(userId);

        if (optionalUserTcStats.isPresent()) {
            return optionalUserTcStats.get();
        }

        LOGGER.trace("Cache miss! Current TC stats");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final UserTcStats userTcStatsFromDb = dbManager.getHourlyTcStats(userId).orElse(UserTcStats.empty(userId));
        tcStatsCache.add(userId, userTcStatsFromDb);
        return userTcStatsFromDb;
    }

    public Collection<HistoricStats> getHistoricStatsHourly(final int userId, final int day, final Month month, final Year year)
        throws UserNotFoundException {
        final Collection<HistoricStats> historicStats = dbManager.getHistoricStatsHourly(userId, day, month, year);

        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}/{}, returning empty", userId, year.getValue(), month.getValue(), day);
        }

        return historicStats;
    }

    public Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Month month, final Year year) throws UserNotFoundException {
        final Collection<HistoricStats> historicStats = dbManager.getHistoricStatsDaily(userId, month, year);

        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}, returning empty", userId, year.getValue(), month.getValue());
        }

        return historicStats;
    }

    public Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year) {
        final Collection<HistoricStats> historicStats = dbManager.getHistoricStatsMonthly(userId, year);

        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}, returning empty", userId, year.getValue());
        }

        return historicStats;
    }

    public void addOffsetStats(final int userId, final OffsetStats offsetStats) {
        dbManager.addOffsetStats(userId, offsetStats);
        offsetStatsCache.add(userId, offsetStats);
    }

    public void addOrUpdateOffsetStats(final int userId, final OffsetStats offsetStats) {
        final Optional<OffsetStats> offsetStatsFromDb = dbManager.addOrUpdateOffsetStats(userId, offsetStats);
        offsetStatsFromDb.ifPresent(stats -> offsetStatsCache.add(userId, stats));
    }

    public OffsetStats getOffsetStatsForUser(final int userId) {
        final Optional<OffsetStats> offsetStats = offsetStatsCache.get(userId);
        if (offsetStats.isPresent()) {
            return offsetStats.get();
        }

        LOGGER.trace("Cache miss! getOffsetStatsForUser");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final OffsetStats offsetStatsFromDb = dbManager.getOffsetStats(userId).orElse(OffsetStats.empty());
        offsetStatsCache.add(userId, offsetStatsFromDb);
        return offsetStatsFromDb;
    }


    public void initialiseOffsetStats() {
        for (final User user : getAllUsersWithPasskeys()) {
            final OffsetStats offsetStats = dbManager.getOffsetStats(user.getId()).orElse(OffsetStats.empty());
            offsetStatsCache.add(user.getId(), offsetStats);
        }
    }

    public void clearOffsetStats() {
        dbManager.clearAllOffsetStats();
        offsetStatsCache.clearOffsets();
    }

    public void persistTotalStatsForUser(final UserStats stats) {
        dbManager.persistTotalStats(stats);
        totalStatsCache.add(stats.getUserId(), stats.getStats());
    }

    public Stats getTotalStatsForUser(final int userId) {
        final Optional<Stats> optionalTotalStats = totalStatsCache.get(userId);

        if (optionalTotalStats.isPresent()) {
            return optionalTotalStats.get();
        }

        LOGGER.trace("Cache miss! Total stats");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Stats userTotalStatsFromDb = dbManager.getTotalStats(userId)
            .orElse(UserStats.empty())
            .getStats();
        totalStatsCache.add(userId, userTotalStatsFromDb);
        return userTotalStatsFromDb;
    }

    public void setCurrentStatsAsInitialStatsForUser(final User user) {
        LOGGER.info("Updating initial stats for user: {}", user.getDisplayName());
        final Stats totalStats = getTotalStatsForUser(user.getId());
        persistInitialUserStats(UserStats.create(user.getId(), DateTimeUtils.currentUtcTimestamp(), totalStats.getPoints(), totalStats.getUnits()));
        initialStatsCache.add(user.getId(), totalStats);
    }

    @SuppressWarnings("PMD.ConfusingTernary") // False positive
    public SystemUserAuthentication authenticateSystemUser(final String userName, final String password) {
        final SystemUserAuthentication systemUserAuthentication = dbManager.authenticateSystemUser(userName, password);

        if (systemUserAuthentication.isUserExists() && systemUserAuthentication.isPasswordMatch()) {
            LOGGER.debug("System user '{}' successfully logged in", userName);
        } else if (!systemUserAuthentication.isUserExists()) {
            LOGGER.debug("No system user with name: '{}'", userName);
        } else if (!systemUserAuthentication.isPasswordMatch()) {
            LOGGER.debug("Invalid password supplied for user: '{}'", userName);
        }

        return systemUserAuthentication;
    }

    public Collection<User> getUsersOnTeam(final Team team) {
        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getTeam().getId() == team.getId())
            .collect(toList());
    }

    public Collection<RetiredUserTcStats> getRetiredUsersForTeam(final Team team) {
        return dbManager.getRetiredUserStatsForTeam(team);
    }

    public void deleteRetiredUserStats() {
        dbManager.deleteRetiredUserStats();
    }

    public Optional<User> getUserWithFoldingUserNameAndPasskey(final String foldingUserName, final String passkey) {
        return getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.getFoldingUserName().equalsIgnoreCase(foldingUserName) && user.getPasskey().equalsIgnoreCase(passkey))
            .findAny();
    }

    public Collection<User> getUsersWithHardware(final Hardware hardware) {
        return getAllUsersWithoutPasskeys()
            .stream()
            .filter(user -> user.getHardware().getId() == hardware.getId())
            .collect(toList());
    }

    public Collection<User> getUsersWithTeam(final Team team) {
        return getAllUsersWithoutPasskeys()
            .stream()
            .filter(user -> user.getTeam().getId() == team.getId())
            .collect(toList());
    }
}
