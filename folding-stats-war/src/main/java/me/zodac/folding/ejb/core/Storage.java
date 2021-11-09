package me.zodac.folding.ejb.core;

import static me.zodac.folding.cache.CompetitionSummaryCache.COMPETITION_SUMMARY_ID;

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
 * In order to decouple both the REST layer and the {@link FoldingStatsEjb} from the persistence solution we use this interface for CRUD operations.
 *
 * <p>
 * Since some persisted data can be cached, we don't want any other modules of the codebase to need to worry about DB vs cache access, and
 * instead encapsulate all of that logic here.
 *
 * <p>
 * <b>NOTE:</b> Should only be used by {@link FoldingStatsEjb}, other classes should not go use this class.
 */
final class Storage {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final DbManager DB_MANAGER = DbManagerRetriever.get();
    private static final Storage INSTANCE = new Storage();

    // POJO caches
    private final HardwareCache hardwareCache = HardwareCache.getInstance();
    private final TeamCache teamCache = TeamCache.getInstance();
    private final UserCache userCache = UserCache.getInstance();

    // Stats caches
    private final CompetitionSummaryCache competitionSummaryCache = CompetitionSummaryCache.getInstance();
    private final InitialStatsCache initialStatsCache = InitialStatsCache.getInstance();
    private final OffsetTcStatsCache offsetTcStatsCache = OffsetTcStatsCache.getInstance();
    private final RetiredTcStatsCache retiredTcStatsCache = RetiredTcStatsCache.getInstance();
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
    @Cached(HardwareCache.class)
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
    @Cached(HardwareCache.class)
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
    @Cached(HardwareCache.class)
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
        hardwareCache.add(updatedHardware.getId(), updatedHardware);

        getAllUsers()
            .stream()
            .filter(user -> user.getHardware().getId() == updatedHardware.getId())
            .map(user -> User.updateHardware(user, updatedHardware))
            .forEach(updatedUser -> userCache.add(updatedUser.getId(), updatedUser));

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
    @Cached(TeamCache.class)
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
    @Cached(TeamCache.class)
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
    @Cached(TeamCache.class)
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
    @Cached({TeamCache.class, UserCache.class})
    public Team updateTeam(final Team teamToUpdate) {
        final Team updatedTeam = DB_MANAGER.updateTeam(teamToUpdate);
        teamCache.add(updatedTeam.getId(), updatedTeam);

        getAllUsers()
            .stream()
            .filter(user -> user.getTeam().getId() == updatedTeam.getId())
            .map(user -> User.updateTeam(user, updatedTeam))
            .forEach(updatedUser -> userCache.add(updatedUser.getId(), updatedUser));

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
        teamCache.remove(teamId);
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
        userCache.add(userWithId.getId(), userWithId);
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
    @Cached(UserCache.class)
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
        userCache.add(updatedUser.getId(), updatedUser);
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
        userCache.remove(userId);

        // Remove the user entry from all stats caches
        offsetTcStatsCache.remove(userId);
        totalStatsCache.remove(userId);
        initialStatsCache.remove(userId);
        tcStatsCache.remove(userId);
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
        retiredTcStatsCache.add(createdRetiredUserTcStats.getRetiredUserId(), createdRetiredUserTcStats);
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
        final Collection<RetiredUserTcStats> fromCache = retiredTcStatsCache.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all retired users");
        final Collection<RetiredUserTcStats> fromDb = DB_MANAGER.getAllRetiredUserStats();

        for (final RetiredUserTcStats retiredUserTcStats : fromDb) {
            retiredTcStatsCache.add(retiredUserTcStats.getRetiredUserId(), retiredUserTcStats);
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
        retiredTcStatsCache.removeAll();
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
        totalStatsCache.add(fromDb.getUserId(), fromDb);
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
        offsetTcStatsCache.add(userId, fromDb);
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
    @Cached(OffsetTcStatsCache.class)
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
    @Cached(OffsetTcStatsCache.class)
    void deleteAllOffsetTcStats() {
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
     * @return the created {@link UserTcStats}
     */
    @Cached(TcStatsCache.class)
    UserTcStats createHourlyTcStats(final UserTcStats userTcStats) {
        final UserTcStats fromDb = DB_MANAGER.createHourlyTcStats(userTcStats);
        tcStatsCache.add(userTcStats.getUserId(), fromDb);
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
        final Optional<UserTcStats> fromCache = tcStatsCache.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Hourly TC stats");
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
        initialStatsCache.add(fromDb.getUserId(), fromDb);
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
        final Optional<UserStats> fromCache = initialStatsCache.get(userId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Initial stats");
        final Optional<UserStats> fromDb = DB_MANAGER.getInitialStats(userId);
        fromDb.ifPresent(userStats -> initialStatsCache.add(userId, userStats));
        return fromDb;
    }

    /**
     * Creates a {@link CompetitionSummary}.
     *
     * @param competitionSummary the {@link CompetitionSummary} to be created
     * @return the created {@link CompetitionSummary}
     */
    @Cached(CompetitionSummaryCache.class)
    CompetitionSummary createCompetitionSummary(final CompetitionSummary competitionSummary) {
        this.competitionSummaryCache.add(COMPETITION_SUMMARY_ID, competitionSummary);
        return competitionSummary;
    }

    /**
     * Creates the latest {@link CompetitionSummary}.
     *
     * @return an {@link Optional} of the latest {@link CompetitionSummary}
     */
    @Cached(CompetitionSummaryCache.class)
    Optional<CompetitionSummary> getCompetitionSummary() {
        return competitionSummaryCache.get(COMPETITION_SUMMARY_ID);
    }

    /**
     * Evicts all {@link User}s from the {@link TotalStatsCache}.
     */
    @Cached(TotalStatsCache.class)
    void evictTcStatsCache() {
        tcStatsCache.removeAll();
    }

    /**
     * Evicts all {@link User}s from the {@link InitialStatsCache}.
     */
    @Cached(InitialStatsCache.class)
    void evictInitialStatsCache() {
        initialStatsCache.removeAll();
    }

    /**
     * Prints the contents of caches to the system log.
     */
    @Cached({
        CompetitionSummaryCache.class,
        HardwareCache.class,
        InitialStatsCache.class,
        OffsetTcStatsCache.class,
        RetiredTcStatsCache.class,
        TcStatsCache.class,
        TeamCache.class,
        TotalStatsCache.class,
        UserCache.class
    })
    void printCacheContents() {
        // POJOs
        LOGGER.info("HardwareCache: {}", hardwareCache.getCacheContents());
        LOGGER.info("TeamCache: {}", teamCache.getCacheContents());
        LOGGER.info("UserCache: {}", userCache.getCacheContents());

        // Stats
        LOGGER.info("InitialStatsCache: {}", initialStatsCache.getCacheContents());
        LOGGER.info("OffsetStatsCache: {}", offsetTcStatsCache.getCacheContents());
        LOGGER.info("RetiredTcStatsCache: {}", retiredTcStatsCache.getCacheContents());
        LOGGER.info("TcStatsCache: {}", tcStatsCache.getCacheContents());
        LOGGER.info("TotalStatsCache: {}", totalStatsCache.getCacheContents());

        // TC overall
        LOGGER.info("CompetitionSummaryCache: {}", competitionSummaryCache.getCacheContents());
    }
}
