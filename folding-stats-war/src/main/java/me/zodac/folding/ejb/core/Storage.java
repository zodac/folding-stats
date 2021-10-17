package me.zodac.folding.ejb.core;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.OffsetTcStatsCache;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TeamCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * In order to decouple both the REST layer and the {@link BusinessLogicEjb} from the persistence solution we use this interface for CRUD operations.
 *
 * <p>
 * Since some persisted data can be cached, we don't want any other modules of the codebase to need to worry about DB vs cache access, and
 * instead encapsulate all of that logic here.
 *
 * <p>
 * <b>NOTE:</b> Should only be used by {@link BusinessLogicEjb}, other classes should not go use this class.
 */
final class Storage {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final DbManager DB_MANAGER = DbManagerRetriever.get();
    private static final Storage INSTANCE = new Storage();

    // POJO caches
    private final HardwareCache hardwareCache = HardwareCache.getInstance();
    private final UserCache userCache = UserCache.getInstance();
    private final TeamCache teamCache = TeamCache.getInstance();

    // Stat caches
    private final RetiredTcStatsCache retiredStatsCache = RetiredTcStatsCache.getInstance();
    private final OffsetTcStatsCache offsetTcStatsCache = OffsetTcStatsCache.getInstance();
    private final TcStatsCache tcStatsCache = TcStatsCache.getInstance();
    private final TotalStatsCache totalStatsCache = TotalStatsCache.getInstance();

    private Storage() {

    }

    static Storage getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a {@link Hardware}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link HardwareCache}.
     *
     * @param hardware the {@link Hardware} to create
     * @return the created {@link Hardware}, with ID
     * @see DbManager#createHardware(Hardware)
     */
    @Cached
    Hardware createHardware(final Hardware hardware) {
        final Hardware hardwareWithId = DB_MANAGER.createHardware(hardware);
        hardwareCache.add(hardwareWithId.getId(), hardwareWithId);
        return hardwareWithId;
    }

    /**
     * Retrieves a {@link Hardware}.
     *
     * <p>
     * First attempts to retrieve from {@link HardwareCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return an {@link Optional} of the retrieved {@link Hardware}
     * @see DbManager#getHardware(int)
     */
    @Cached
    Optional<Hardware> getHardware(final int hardwareId) {
        final Optional<Hardware> fromCache = hardwareCache.get(hardwareId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get hardware");
        final Optional<Hardware> fromDb = DB_MANAGER.getHardware(hardwareId);
        fromDb.ifPresent(hardware -> hardwareCache.add(hardwareId, hardware));
        return fromDb;
    }

    /**
     * Retrieves all {@link Hardware}.
     *
     * <p>
     * First attempts to retrieve from {@link HardwareCache}, then if none exist, attempts to retrieve from the {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link Hardware}
     * @see DbManager#getAllHardware()
     */
    @Cached
    Collection<Hardware> getAllHardware() {
        final Collection<Hardware> fromCache = hardwareCache.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all hardware");
        final Collection<Hardware> fromDb = DB_MANAGER.getAllHardware();

        for (final Hardware hardware : fromDb) {
            hardwareCache.add(hardware.getId(), hardware);
        }

        return fromDb;
    }

    /**
     * Deletes a {@link Hardware}.
     *
     * <p>
     * Deletes it with the {@link DbManager}, then removes it to the {@link HardwareCache}.
     *
     * @param hardwareId the ID of the {@link Hardware} to delete
     * @see DbManager#deleteHardware(int)
     */
    @Cached
    void deleteHardware(final int hardwareId) {
        DB_MANAGER.deleteHardware(hardwareId);
        hardwareCache.remove(hardwareId);
    }

    /**
     * Creates a {@link Team}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link TeamCache}.
     *
     * @param team the {@link Team} to create
     * @return the created {@link Team}, with ID
     * @see DbManager#createTeam(Team)
     */
    @Cached
    Team createTeam(final Team team) {
        final Team teamWithId = DB_MANAGER.createTeam(team);
        teamCache.add(teamWithId.getId(), teamWithId);
        return teamWithId;
    }

    /**
     * Retrieves a {@link Team}.
     *
     * <p>
     * First attempts to retrieve from {@link TeamCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param teamId the ID of the {@link Team} to retrieve
     * @return an {@link Optional} of the retrieved {@link Team}
     * @see DbManager#getTeam(int)
     */
    @Cached
    Optional<Team> getTeam(final int teamId) {
        final Optional<Team> fromCache = teamCache.get(teamId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get team");
        final Optional<Team> fromDb = DB_MANAGER.getTeam(teamId);
        fromDb.ifPresent(team -> teamCache.add(teamId, team));
        return fromDb;
    }

    /**
     * Retrieves all {@link Team}s.
     *
     * <p>
     * First attempts to retrieve from {@link TeamCache}, then if none exist, attempts to retrieve from the {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link Team}s
     * @see DbManager#getAllTeams()
     */
    @Cached
    Collection<Team> getAllTeams() {
        final Collection<Team> fromCache = teamCache.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all teams");
        final Collection<Team> fromDb = DB_MANAGER.getAllTeams();

        for (final Team team : fromDb) {
            teamCache.add(team.getId(), team);
        }

        return fromDb;
    }

    /**
     * Updates a {@link Team}. Expects the {@link Team} to have a valid ID.
     *
     * <p>
     * Persists it with the {@link DbManager}, then updates it in the {@link TeamCache}.
     *
     * <p>
     * Also updates the {@link UserCache} with an updated version of any {@link User} that references this {@link Team}.
     *
     * @param teamToUpdate the {@link Team} to update
     * @see DbManager#updateTeam(Team)
     */
    @Cached
    public Team updateTeam(final Team teamToUpdate) {
        DB_MANAGER.updateTeam(teamToUpdate);
        teamCache.add(teamToUpdate.getId(), teamToUpdate);

        getAllUsers()
            .stream()
            .filter(user -> user.getTeam().getId() == teamToUpdate.getId())
            .map(user -> User.updateTeam(user, teamToUpdate))
            .forEach(updatedUser -> userCache.add(updatedUser.getId(), updatedUser));

        return teamToUpdate;
    }

    /**
     * Deletes a {@link Team}.
     *
     * <p>
     * Deletes it with the {@link DbManager}, then removes it to the {@link Team}.
     *
     * @param teamId the ID of the {@link Team} to delete
     * @see DbManager#deleteTeam(int)
     */
    @Cached
    void deleteTeam(final int teamId) {
        DB_MANAGER.deleteTeam(teamId);
        teamCache.remove(teamId);
    }

    /**
     * Retrieves a {@link User}.
     *
     * <p>
     * First attempts to retrieve from {@link UserCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     * @see DbManager#getUser(int)
     */
    @Cached
    Optional<User> getUser(final int userId) {
        final Optional<User> fromCache = userCache.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get user");
        final Optional<User> fromDb = DB_MANAGER.getUser(userId);
        fromDb.ifPresent(user -> userCache.add(userId, user));
        return fromDb;
    }

    /**
     * Retrieves all {@link User}s.
     *
     * <p>
     * First attempts to retrieve from {@link UserCache}, then if none exist, attempts to retrieve from the {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     * @see DbManager#getAllUsers()
     */
    @Cached
    Collection<User> getAllUsers() {
        final Collection<User> fromCache = userCache.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all users");
        final Collection<User> fromDb = DB_MANAGER.getAllUsers();

        for (final User user : fromDb) {
            userCache.add(user.getId(), user);
        }

        return fromDb;
    }

    /**
     * Creates a monthly result for the <code>Team Competition</code>.
     *
     * <p>
     * Persists it with the {@link DbManager}, but does not cache it.
     *
     * @param monthlyResult the result for a month of the <code>Team Competition</code>
     * @param utcTimestamp  the {@link java.time.ZoneOffset#UTC} timestamp for the result
     */
    @NotCached
    void createMonthlyResult(final String monthlyResult, final LocalDateTime utcTimestamp) {
        DB_MANAGER.persistMonthlyResult(monthlyResult, utcTimestamp);
    }

    /**
     * Retrieves the result of the <code>Team Competition</code> for the given {@link Month} and {@link Year}.
     *
     * <p>
     * Since these values are not cached, we go directly to the {@link DbManager} to retrieve it.
     *
     * @param month the {@link Month} of the result to be retrieved
     * @param year  the {@link Year} of the result to be retrieved
     * @return an {@link Optional} of the <code>Team Competition</code> result
     */
    @NotCached
    Optional<String> getMonthlyResult(final Month month, final Year year) {
        return DB_MANAGER.getMonthlyResult(month, year);
    }

    /**
     * Creates a {@link RetiredUserTcStats} for a {@link User} that has been deleted from a {@link Team}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link HardwareCache}.
     *
     * @param teamId          the ID of the {@link Team} that the {@link User} has been deleted from
     * @param userId          the ID of the {@link User} who is being deleted
     * @param userDisplayName the display name of the {@link User} who is being deleted
     * @param userTcStats     the {@link UserTcStats} at the time of deletion
     * @return the {@link RetiredUserTcStats}
     */
    @Cached
    RetiredUserTcStats createRetiredUser(final int teamId, final int userId, final String userDisplayName, final UserTcStats userTcStats) {
        final int retiredUserId = DB_MANAGER.persistRetiredUserStats(teamId, userId, userDisplayName, userTcStats);
        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.create(retiredUserId, teamId, userDisplayName, userTcStats);

        retiredStatsCache.add(retiredUserId, retiredUserTcStats);
        return retiredUserTcStats;
    }

    /**
     * Retrieves all {@link RetiredUserTcStats}.
     *
     * <p>
     * First attempts to retrieve from {@link RetiredTcStatsCache}, then if none exist, attempts to retrieve from the {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link RetiredUserTcStats}
     * @see DbManager#getAllRetiredUserStats()
     */
    @Cached
    Collection<RetiredUserTcStats> getAllRetiredUsers() {
        final Collection<RetiredUserTcStats> fromCache = retiredStatsCache.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all retired users");
        final Collection<RetiredUserTcStats> fromDb = DB_MANAGER.getAllRetiredUserStats();

        for (final RetiredUserTcStats retiredUserTcStats : fromDb) {
            retiredStatsCache.add(retiredUserTcStats.getRetiredUserId(), retiredUserTcStats);
        }

        return fromDb;
    }

    /**
     * Deletes all {@link RetiredUserTcStats} for all {@link Team}s.
     *
     * <p>
     * Also evicts the {@link RetiredTcStatsCache}.
     */
    @Cached
    void deleteAllRetiredUserStats() {
        DB_MANAGER.deleteAllRetiredUserStats();
        retiredStatsCache.removeAll();
    }

    /**
     * Authenticates a system user with {@link DbManager}.
     *
     * @param userName the system user username
     * @param password the system user password
     * @return the {@link UserAuthenticationResult}
     */
    @NotCached
    UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        return DB_MANAGER.authenticateSystemUser(userName, password);
    }

    /**
     * Retrieves the {@link HistoricStats} for a given {@link User} ID for a specific {@code day}, {@link Month} or {@link Year}.
     *
     * <p>
     * Based on the values of the input parameters, a different {@link Collection} of {@link HistoricStats} will be returned:
     * <ul>
     *     <li>If the {@code year} is null, an empty {@link Collection} is returned</li>
     *     <li>If the {@code month} is null, the monthly {@link HistoricStats} is returned for the given {@link Year}</li>
     *     <li>If the {@code day} is <b>0</b>, the daily {@link HistoricStats} is returned for the given {@link Year}/{@link Month}</li>
     *     <li>Otherwise, the hourly {@link HistoricStats} is returned for the given {@link Year}/{@link Month}/{@code day}</li>
     * </ul>
     *
     * <p>
     * Since these values are not cached, we go directly to the {@link DbManager} to retrieve it.
     *
     * @param userId the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year   the {@link Year} of the {@link HistoricStats}
     * @param month  the {@link Month} of the {@link HistoricStats}
     * @param day    the day of the {@link Month} of the {@link HistoricStats}
     * @return the {@link HistoricStats} for the {@link User}
     * @see DbManager#getHistoricStatsHourly(int, Year, Month, int)
     * @see DbManager#getHistoricStatsDaily(int, Year, Month)
     * @see DbManager#getHistoricStatsMonthly(int, Year)
     */
    @NotCached
    Collection<HistoricStats> getHistoricStats(final int userId, final Year year, final Month month, final int day) {
        if (year == null) {
            return Collections.emptyList();
        }

        if (month == null) {
            return DB_MANAGER.getHistoricStatsMonthly(userId, year);
        }

        if (day == 0) {
            return DB_MANAGER.getHistoricStatsDaily(userId, year, month);
        }

        return DB_MANAGER.getHistoricStatsHourly(userId, year, month, day);
    }

    /**
     * Creates a {@link UserStats} for the total overall stats for a {@link User}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link TotalStatsCache}.
     *
     * @param userStats the {@link UserStats} to be created
     * @return an {@link Optional} of the created {@link UserStats}
     */
    @Cached
    Optional<UserStats> createTotalStats(final UserStats userStats) {
        final Optional<UserStats> fromDb = DB_MANAGER.createTotalStats(userStats);
        fromDb.ifPresent(stats -> totalStatsCache.add(stats.getUserId(), stats));
        return fromDb;
    }

    /**
     * Retrieves the {@link UserStats} for a {@link User} with the provided ID.
     *
     * <p>
     * First attempts to retrieve from {@link TotalStatsCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @param userId the ID of the {@link User} to whose {@link UserStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserStats}
     * @see DbManager#getTotalStats(int)
     */
    @Cached
    Optional<UserStats> getTotalStats(final int userId) {
        final Optional<UserStats> fromCache = totalStatsCache.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Total stats");
        final Optional<UserStats> fromDb = DB_MANAGER.getTotalStats(userId);
        fromDb.ifPresent(userStats -> totalStatsCache.add(userId, userStats));
        return fromDb;
    }

    /**
     * Creates an {@link OffsetTcStats} defining the offset points/units for the provided {@link User}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link OffsetTcStatsCache}.
     *
     * <p>
     * If an {@link OffsetTcStats} already exists for the {@link User}, the existing values are updated to be the addition of both
     * {@link OffsetTcStats}.
     *
     * @param userId        the ID of the {@link User} for whom the {@link OffsetTcStats} are being created
     * @param offsetTcStats the {@link OffsetTcStats} to be created
     * @return an {@link Optional} of the created/updated {@link OffsetTcStats}
     */
    @Cached
    Optional<OffsetTcStats> createOrUpdateOffsetStats(final int userId, final OffsetTcStats offsetTcStats) {
        final Optional<OffsetTcStats> fromDb = DB_MANAGER.createOrUpdateOffsetStats(userId, offsetTcStats);
        fromDb.ifPresent(stats -> offsetTcStatsCache.add(userId, stats));
        return fromDb;
    }

    /**
     * Retrieves the {@link OffsetTcStats} for a {@link User} with the provided ID.
     *
     * <p>
     * First attempts to retrieve from {@link OffsetTcStatsCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} to whose {@link OffsetTcStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link OffsetTcStats}
     * @see DbManager#getOffsetStats(int)
     */
    @Cached
    Optional<OffsetTcStats> getOffsetStats(final int userId) {
        final Optional<OffsetTcStats> fromCache = offsetTcStatsCache.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Offset stats");
        final Optional<OffsetTcStats> fromDb = DB_MANAGER.getOffsetStats(userId);
        fromDb.ifPresent(offsetTcStats -> offsetTcStatsCache.add(userId, offsetTcStats));
        return fromDb;
    }

    /**
     * Deletes the {@link OffsetTcStats} for a {@link User} with the provided ID.
     *
     * <p>
     * Also evicts the {@link User} ID from the {@link OffsetTcStatsCache}.
     *
     * @param userId the ID of the {@link User} to whose {@link OffsetTcStats} are to be deleted
     */
    @Cached
    void deleteOffsetStats(final int userId) {
        DB_MANAGER.deleteOffsetStats(userId);
        offsetTcStatsCache.remove(userId);
    }

    /**
     * Deletes the {@link OffsetTcStats} for all {@link User}s in the system.
     *
     * <p>
     * Also evicts the {@link OffsetTcStatsCache}.
     */
    @Cached
    void deleteAllOffsetStats() {
        DB_MANAGER.deleteAllOffsetStats();
        offsetTcStatsCache.removeAll();
    }

    /**
     * Creates a {@link UserTcStats} for a {@link User}'s <code>Team Competition</code> stats for a specific hour.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link TcStatsCache}.
     *
     * @param userTcStats the {@link UserTcStats} to be created
     * @return an {@link Optional} of the created {@link UserTcStats}
     */
    @Cached
    Optional<UserTcStats> createHourlyTcStats(final UserTcStats userTcStats) {
        final Optional<UserTcStats> fromDb = DB_MANAGER.persistHourlyTcStats(userTcStats);
        fromDb.ifPresent(stats -> tcStatsCache.add(userTcStats.getUserId(), stats));
        return fromDb;
    }

    /**
     * Retrieves the latest {@link UserTcStats} for the provided {@link User}.
     *
     * <p>
     * First attempts to retrieve from {@link TcStatsCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @param userId the ID of the {@link User} whose {@link UserTcStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserTcStats}
     */
    @Cached
    Optional<UserTcStats> getHourlyTcStats(final int userId) {
        final Optional<UserTcStats> fromCache = tcStatsCache.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Offset stats");
        final Optional<UserTcStats> fromDb = DB_MANAGER.getHourlyTcStats(userId);
        fromDb.ifPresent(userTcStats -> tcStatsCache.add(userId, userTcStats));
        return fromDb;
    }

    /**
     * Retrieves the first {@link UserTcStats} for any users in the system.
     *
     * <p>
     * We want to check the status of the system, so we do not go to the {@link TcStatsCache}, and instead go directly though {@link DbManager}.
     *
     * @return an {@link Optional} of the first {@link UserTcStats}
     */
    @NotCached
    Optional<UserTcStats> getFirstHourlyTcStats() {
        return DB_MANAGER.getFirstHourlyTcStats();
    }
}
