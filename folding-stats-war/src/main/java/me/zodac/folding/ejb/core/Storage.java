package me.zodac.folding.ejb.core;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TeamCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
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
    Hardware createHardware(final Hardware hardware) {
        final Hardware hardwareWithId = DB_MANAGER.createHardware(hardware);
        hardwareCache.add(hardwareWithId.getId(), hardwareWithId);
        return hardwareWithId;
    }

    /**
     * Retrieves a {@link Hardware}.
     *
     * <p>
     * First attempts to retrieve from {@link HardwareCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return an {@link Optional} of the retrieved {@link Hardware}
     * @see DbManager#getHardware(int)
     */
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
     * First attempts to retrieve from {@link HardwareCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link Hardware}
     * @see DbManager#getAllHardware()
     */
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
     * @param updatedHardware the {@link Hardware} to update
     * @see DbManager#updateHardware(Hardware)
     */
    void updateHardware(final Hardware updatedHardware) {
        DB_MANAGER.updateHardware(updatedHardware);
        hardwareCache.add(updatedHardware.getId(), updatedHardware);
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
    Team createTeam(final Team team) {
        final Team teamWithId = DB_MANAGER.createTeam(team);
        teamCache.add(teamWithId.getId(), teamWithId);
        return teamWithId;
    }

    /**
     * Retrieves a {@link Team}.
     *
     * <p>
     * First attempts to retrieve from {@link TeamCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @param teamId the ID of the {@link Team} to retrieve
     * @return an {@link Optional} of the retrieved {@link Team}
     * @see DbManager#getTeam(int)
     */
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
     * First attempts to retrieve from {@link TeamCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link Team}s
     * @see DbManager#getAllTeams()
     */
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
    void deleteTeam(final int teamId) {
        DB_MANAGER.deleteTeam(teamId);
        teamCache.remove(teamId);
    }

    /**
     * Retrieves a {@link User}.
     *
     * <p>
     * First attempts to retrieve from {@link UserCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     * @see DbManager#getUser(int)
     */
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
     * First attempts to retrieve from {@link UserCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     * @see DbManager#getAllUsers()
     */
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
     * Evicts a {@link User} from the {@link UserCache}.
     *
     * <p>
     * Used in scenarios where something a {@link User} references, like a {@link Hardware} or {@link Team} has been
     * updated. The {@link User} itself does not require an update in the DB (since we only store a reference to the ID of the
     * {@link Hardware}/{@link Team}). This will force the next retrieval to go directly to the DB and retrieve the updated
     * {@link Hardware}/{@link Team} details for the {@link User}.
     *
     * @param userId the ID of the {@link User} to evict
     */
    void evictUserFromCache(final int userId) {
        userCache.remove(userId);
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
    public void createMonthlyResult(final String monthlyResult, final LocalDateTime utcTimestamp) {
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
    public Optional<String> getMonthlyResult(final Month month, final Year year) {
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
    public RetiredUserTcStats createRetiredUser(final int teamId, final int userId, final String userDisplayName, final UserTcStats userTcStats) {
        final int retiredUserId = DB_MANAGER.persistRetiredUserStats(teamId, userId, userDisplayName, userTcStats);
        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.create(retiredUserId, teamId, userDisplayName, userTcStats);

        retiredStatsCache.add(retiredUserId, retiredUserTcStats);
        return retiredUserTcStats;
    }

    /**
     * Retrieves all {@link RetiredUserTcStats}.
     *
     * @return a {@link Collection} of the retrieved {@link RetiredUserTcStats}
     */
    public Collection<RetiredUserTcStats> getAllRetiredUsers() {
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
     */
    public void deleteAllRetiredUserStats() {
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
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        return DB_MANAGER.authenticateSystemUser(userName, password);
    }
}
