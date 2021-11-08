package me.zodac.folding.ejb.core;

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
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.cache.CompetitionSummaryCache;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.InitialStatsCache;
import me.zodac.folding.cache.OffsetTcStatsCache;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TeamCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
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
    private static final HardwareCache HARDWARE_CACHE = HardwareCache.getInstance();
    private static final TeamCache TEAM_CACHE = TeamCache.getInstance();
    private static final UserCache USER_CACHE = UserCache.getInstance();

    // Stats caches
    private static final CompetitionSummaryCache COMPETITION_SUMMARY_CACHE = CompetitionSummaryCache.getInstance();
    private static final InitialStatsCache INITIAL_STATS_CACHE = InitialStatsCache.getInstance();
    private static final OffsetTcStatsCache OFFSET_TC_STATS_CACHE = OffsetTcStatsCache.getInstance();
    private static final RetiredTcStatsCache RETIRED_TC_STATS_CACHE = RetiredTcStatsCache.getInstance();
    private static final TcStatsCache TC_STATS_CACHE = TcStatsCache.getInstance();
    private static final TotalStatsCache TOTAL_STATS_CACHE = TotalStatsCache.getInstance();

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
    @Cached(HardwareCache.class)
    Hardware createHardware(final Hardware hardware) {
        final Hardware hardwareWithId = DB_MANAGER.createHardware(hardware);
        HARDWARE_CACHE.add(hardwareWithId.getId(), hardwareWithId);
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
    @Cached(HardwareCache.class)
    Optional<Hardware> getHardware(final int hardwareId) {
        final Optional<Hardware> fromCache = HARDWARE_CACHE.get(hardwareId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get hardware");
        final Optional<Hardware> fromDb = DB_MANAGER.getHardware(hardwareId);
        fromDb.ifPresent(hardware -> HARDWARE_CACHE.add(hardwareId, hardware));
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
    @Cached(HardwareCache.class)
    Collection<Hardware> getAllHardware() {
        final Collection<Hardware> fromCache = HARDWARE_CACHE.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all hardware");
        final Collection<Hardware> fromDb = DB_MANAGER.getAllHardware();

        for (final Hardware hardware : fromDb) {
            HARDWARE_CACHE.add(hardware.getId(), hardware);
        }

        return fromDb;
    }

    /**
     * Updates a {@link Hardware}. Expects the {@link Hardware} to have a valid ID.
     *
     * <p>
     * Persists it with the {@link DbManager}, then updates it in the {@link HardwareCache}.
     *
     * <p>
     * Also updates the {@link UserCache} with an updated version of any {@link User} that references this {@link Hardware}.
     *
     * @param hardwareToUpdate the {@link Hardware} to update
     * @see DbManager#updateHardware(Hardware)
     */
    @Cached({HardwareCache.class, UserCache.class})
    public Hardware updateHardware(final Hardware hardwareToUpdate) {
        final Hardware updatedHardware = DB_MANAGER.updateHardware(hardwareToUpdate);
        HARDWARE_CACHE.add(updatedHardware.getId(), updatedHardware);

        getAllUsers()
            .stream()
            .filter(user -> user.getHardware().getId() == updatedHardware.getId())
            .map(user -> User.updateHardware(user, updatedHardware))
            .forEach(updatedUser -> USER_CACHE.add(updatedUser.getId(), updatedUser));

        return updatedHardware;
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
    @Cached(HardwareCache.class)
    void deleteHardware(final int hardwareId) {
        DB_MANAGER.deleteHardware(hardwareId);
        HARDWARE_CACHE.remove(hardwareId);
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
    @Cached(TeamCache.class)
    Team createTeam(final Team team) {
        final Team teamWithId = DB_MANAGER.createTeam(team);
        TEAM_CACHE.add(teamWithId.getId(), teamWithId);
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
    @Cached(TeamCache.class)
    Optional<Team> getTeam(final int teamId) {
        final Optional<Team> fromCache = TEAM_CACHE.get(teamId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get team");
        final Optional<Team> fromDb = DB_MANAGER.getTeam(teamId);
        fromDb.ifPresent(team -> TEAM_CACHE.add(teamId, team));
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
    @Cached(TeamCache.class)
    Collection<Team> getAllTeams() {
        final Collection<Team> fromCache = TEAM_CACHE.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all teams");
        final Collection<Team> fromDb = DB_MANAGER.getAllTeams();

        for (final Team team : fromDb) {
            TEAM_CACHE.add(team.getId(), team);
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
    @Cached({TeamCache.class, UserCache.class})
    public Team updateTeam(final Team teamToUpdate) {
        final Team updatedTeam = DB_MANAGER.updateTeam(teamToUpdate);
        TEAM_CACHE.add(updatedTeam.getId(), updatedTeam);

        getAllUsers()
            .stream()
            .filter(user -> user.getTeam().getId() == updatedTeam.getId())
            .map(user -> User.updateTeam(user, updatedTeam))
            .forEach(updatedUser -> USER_CACHE.add(updatedUser.getId(), updatedUser));

        return updatedTeam;
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
    @Cached(TeamCache.class)
    void deleteTeam(final int teamId) {
        DB_MANAGER.deleteTeam(teamId);
        TEAM_CACHE.remove(teamId);
    }

    /**
     * Creates a {@link User}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link UserCache}.
     *
     * @param user the {@link User} to create
     * @return the created {@link User}, with ID
     * @see DbManager#createUser(User)
     */
    @Cached(UserCache.class)
    User createUser(final User user) {
        final User userWithId = DB_MANAGER.createUser(user);
        USER_CACHE.add(userWithId.getId(), userWithId);
        return userWithId;
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
    @Cached(UserCache.class)
    Optional<User> getUser(final int userId) {
        final Optional<User> fromCache = USER_CACHE.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get user");
        final Optional<User> fromDb = DB_MANAGER.getUser(userId);
        fromDb.ifPresent(user -> USER_CACHE.add(userId, user));
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
    @Cached(UserCache.class)
    Collection<User> getAllUsers() {
        final Collection<User> fromCache = USER_CACHE.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all users");
        final Collection<User> fromDb = DB_MANAGER.getAllUsers();

        for (final User user : fromDb) {
            USER_CACHE.add(user.getId(), user);
        }

        return fromDb;
    }

    /**
     * Updates a {@link User}. Expects the {@link User} to have a valid ID.
     *
     * <p>
     * Persists it with the {@link DbManager}, then updates it in the {@link UserCache}.
     *
     * @param userToUpdate the {@link User} to update
     * @see DbManager#updateUser(User)
     */
    @Cached(UserCache.class)
    public User updateUser(final User userToUpdate) {
        final User updatedUser = DB_MANAGER.updateUser(userToUpdate);
        USER_CACHE.add(updatedUser.getId(), updatedUser);
        return updatedUser;
    }

    /**
     * Deletes a {@link User}.
     *
     * <p>
     * Deletes it with the {@link DbManager}, then removes it to the {@link UserCache}.
     *
     * <p>
     * Also removes the {@link User}'s values from the stats caches:
     * <ul>
     *     <li>{@link InitialStatsCache}</li>
     *     <li>{@link OffsetTcStatsCache}</li>
     *     <li>{@link TcStatsCache}</li>
     *     <li>{@link TotalStatsCache}</li>
     * </ul>
     *
     * @param userId the ID of the {@link User} to delete
     * @see DbManager#deleteUser(int)
     */
    @Cached({UserCache.class, InitialStatsCache.class, OffsetTcStatsCache.class, TcStatsCache.class, TotalStatsCache.class})
    void deleteUser(final int userId) {
        DB_MANAGER.deleteUser(userId);
        USER_CACHE.remove(userId);

        // Remove the user entry from all stats caches
        OFFSET_TC_STATS_CACHE.remove(userId);
        TOTAL_STATS_CACHE.remove(userId);
        INITIAL_STATS_CACHE.remove(userId);
        TC_STATS_CACHE.remove(userId);
    }

    /**
     * Creates a {@link MonthlyResult} for the <code>Team Competition</code>.
     *
     * <p>
     * Persists it with the {@link DbManager}, but does not cache it.
     *
     * @param monthlyResult a {@link MonthlyResult} for the <code>Team Competition</code>
     * @return the <code>Team Competition</code> {@link MonthlyResult}
     */
    @NotCached
    MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult) {
        return DB_MANAGER.createMonthlyResult(monthlyResult);
    }

    /**
     * Retrieves the {@link MonthlyResult} of the <code>Team Competition</code> for the given {@link Month} and {@link Year}.
     *
     * <p>
     * Since these values are not cached, we go directly to the {@link DbManager} to retrieve it.
     *
     * @param month the {@link Month} of the {@link MonthlyResult} to be retrieved
     * @param year  the {@link Year} of the {@link MonthlyResult} to be retrieved
     * @return an {@link Optional} of the <code>Team Competition</code> {@link MonthlyResult}
     */
    @NotCached
    Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year) {
        return DB_MANAGER.getMonthlyResult(month, year);
    }

    /**
     * Creates a {@link RetiredUserTcStats} for a {@link User} that has been deleted from a {@link Team}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link RetiredTcStatsCache}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} for the deleted {@link User}
     * @return the {@link RetiredUserTcStats}
     */
    @Cached(RetiredTcStatsCache.class)
    RetiredUserTcStats createRetiredUserStats(final RetiredUserTcStats retiredUserTcStats) {
        final RetiredUserTcStats createdRetiredUserTcStats = DB_MANAGER.createRetiredUserStats(retiredUserTcStats);
        RETIRED_TC_STATS_CACHE.add(createdRetiredUserTcStats.getRetiredUserId(), createdRetiredUserTcStats);
        return createdRetiredUserTcStats;
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
    @Cached(RetiredTcStatsCache.class)
    Collection<RetiredUserTcStats> getAllRetiredUsers() {
        final Collection<RetiredUserTcStats> fromCache = RETIRED_TC_STATS_CACHE.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all retired users");
        final Collection<RetiredUserTcStats> fromDb = DB_MANAGER.getAllRetiredUserStats();

        for (final RetiredUserTcStats retiredUserTcStats : fromDb) {
            RETIRED_TC_STATS_CACHE.add(retiredUserTcStats.getRetiredUserId(), retiredUserTcStats);
        }

        return fromDb;
    }

    /**
     * Deletes all {@link RetiredUserTcStats} for all {@link Team}s.
     *
     * <p>
     * Also evicts the {@link RetiredTcStatsCache}.
     */
    @Cached(RetiredTcStatsCache.class)
    void deleteAllRetiredUserTcStats() {
        DB_MANAGER.deleteAllRetiredUserStats();
        RETIRED_TC_STATS_CACHE.removeAll();
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
     * @return the created {@link UserStats}
     */
    @Cached(TotalStatsCache.class)
    UserStats createTotalStats(final UserStats userStats) {
        final UserStats fromDb = DB_MANAGER.createTotalStats(userStats);
        TOTAL_STATS_CACHE.add(fromDb.getUserId(), fromDb);
        return fromDb;
    }

    /**
     * Retrieves the {@link UserStats} for a {@link User} with the provided ID.
     *
     * <p>
     * First attempts to retrieve from {@link TotalStatsCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} to whose {@link UserStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserStats}
     * @see DbManager#getTotalStats(int)
     */
    @Cached(TotalStatsCache.class)
    Optional<UserStats> getTotalStats(final int userId) {
        final Optional<UserStats> fromCache = TOTAL_STATS_CACHE.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Total stats");
        final Optional<UserStats> fromDb = DB_MANAGER.getTotalStats(userId);
        fromDb.ifPresent(userStats -> TOTAL_STATS_CACHE.add(userId, userStats));
        return fromDb;
    }

    /**
     * Creates an {@link OffsetTcStats}, defining the offset points/units for the provided {@link User}.
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
     * @return the created/updated {@link OffsetTcStats}, or {@link OffsetTcStats#empty()}
     */
    @Cached(OffsetTcStatsCache.class)
    OffsetTcStats createOrUpdateOffsetStats(final int userId, final OffsetTcStats offsetTcStats) {
        final OffsetTcStats fromDb = DB_MANAGER.createOrUpdateOffsetStats(userId, offsetTcStats);
        OFFSET_TC_STATS_CACHE.add(userId, fromDb);
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
    @Cached(OffsetTcStatsCache.class)
    Optional<OffsetTcStats> getOffsetStats(final int userId) {
        final Optional<OffsetTcStats> fromCache = OFFSET_TC_STATS_CACHE.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Offset stats");
        final Optional<OffsetTcStats> fromDb = DB_MANAGER.getOffsetStats(userId);
        fromDb.ifPresent(offsetTcStats -> OFFSET_TC_STATS_CACHE.add(userId, offsetTcStats));
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
    @Cached(OffsetTcStatsCache.class)
    void deleteOffsetStats(final int userId) {
        DB_MANAGER.deleteOffsetStats(userId);
        OFFSET_TC_STATS_CACHE.remove(userId);
    }

    /**
     * Deletes the {@link OffsetTcStats} for all {@link User}s in the system.
     *
     * <p>
     * Also evicts the {@link OffsetTcStatsCache}.
     */
    @Cached(OffsetTcStatsCache.class)
    void deleteAllOffsetTcStats() {
        DB_MANAGER.deleteAllOffsetStats();
        OFFSET_TC_STATS_CACHE.removeAll();
    }

    /**
     * Creates a {@link UserTcStats} for a {@link User}'s <code>Team Competition</code> stats for a specific hour.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link TcStatsCache}.
     *
     * @param userTcStats the {@link UserTcStats} to be created
     * @return the created {@link UserTcStats}
     */
    @Cached(TcStatsCache.class)
    UserTcStats createHourlyTcStats(final UserTcStats userTcStats) {
        final UserTcStats fromDb = DB_MANAGER.createHourlyTcStats(userTcStats);
        TC_STATS_CACHE.add(userTcStats.getUserId(), fromDb);
        return fromDb;
    }

    /**
     * Retrieves the latest {@link UserTcStats} for the provided {@link User}.
     *
     * <p>
     * First attempts to retrieve from {@link TcStatsCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} whose {@link UserTcStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserTcStats}
     */
    @Cached(TcStatsCache.class)
    Optional<UserTcStats> getHourlyTcStats(final int userId) {
        final Optional<UserTcStats> fromCache = TC_STATS_CACHE.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Hourly TC stats");
        final Optional<UserTcStats> fromDb = DB_MANAGER.getHourlyTcStats(userId);
        fromDb.ifPresent(userTcStats -> TC_STATS_CACHE.add(userId, userTcStats));
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

    /**
     * Creates a {@link UserStats} for the initial overall stats for the provided {@link User} at the start of the monitoring period.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link InitialStatsCache}.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}
     */
    @Cached(InitialStatsCache.class)
    UserStats createInitialStats(final UserStats userStats) {
        final UserStats fromDb = DB_MANAGER.createInitialStats(userStats);
        INITIAL_STATS_CACHE.add(fromDb.getUserId(), fromDb);
        return fromDb;
    }

    /**
     * Retrieves the initial {@link UserStats} for the provided {@link User} ID.
     *
     * <p>
     * First attempts to retrieve from {@link InitialStatsCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} whose {@link UserStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserStats}
     */
    @Cached(InitialStatsCache.class)
    Optional<UserStats> getInitialStats(final int userId) {
        final Optional<UserStats> fromCache = INITIAL_STATS_CACHE.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Initial stats");
        final Optional<UserStats> fromDb = DB_MANAGER.getInitialStats(userId);
        fromDb.ifPresent(userStats -> INITIAL_STATS_CACHE.add(userId, userStats));
        return fromDb;
    }

    /**
     * Creates a {@link CompetitionSummary}.
     *
     * @param competitionSummary the {@link CompetitionSummary} to be created
     * @return the created {@link CompetitionSummary}
     */
    CompetitionSummary createCompetitionSummary(final CompetitionSummary competitionSummary) {
        COMPETITION_SUMMARY_CACHE.add(competitionSummary);
        return competitionSummary;
    }

    /**
     * Creates the latest {@link CompetitionSummary}.
     *
     * @return an {@link Optional} of the latest {@link CompetitionSummary}
     */
    Optional<CompetitionSummary> getCompetitionSummary() {
        return COMPETITION_SUMMARY_CACHE.get();
    }

    /**
     * Evicts all {@link User}s from the {@link TotalStatsCache}.
     */
    @Cached(TotalStatsCache.class)
    void evictTcStatsCache() {
        TC_STATS_CACHE.removeAll();
    }

    /**
     * Evicts all {@link User}s from the {@link InitialStatsCache}.
     */
    @Cached(InitialStatsCache.class)
    void evictInitialStatsCache() {
        INITIAL_STATS_CACHE.removeAll();
    }
}
