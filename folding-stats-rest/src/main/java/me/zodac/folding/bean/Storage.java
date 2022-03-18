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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.time.Duration;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.DatabaseConnectionException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.rest.api.tc.AllTeamsSummary;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * In order to decouple both the REST layer from the persistence solution we use this facade for CRUD operations.
 *
 * <p>
 * Since some persisted data can be cached, we don't want any other modules of the codebase to need to worry about DB vs cache access, and
 * instead encapsulate all of that logic here.
 */
@Component
public class Storage {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int ALL_TEAMS_SUMMARY_ID = 1; // We only ever have one entry

    private static final int STANDARD_CACHE_SIZE = 25;
    private static final Duration STANDARD_CACHE_EXPIRATION_TIME = Duration.ofHours(1);

    // POJO caches
    private final Cache<Integer, Hardware> hardwareCache = Caffeine.newBuilder()
        .maximumSize(300)
        .expireAfterWrite(Duration.ofDays(30))
        .build();
    private final Cache<Integer, Team> teamCache = Caffeine.newBuilder()
        .maximumSize(STANDARD_CACHE_SIZE)
        .build();
    private final Cache<Integer, User> userCache = Caffeine.newBuilder()
        .maximumSize(STANDARD_CACHE_SIZE)
        .build();

    // Stats caches
    private final Cache<Integer, AllTeamsSummary> allTeamsSummaryCache = Caffeine.newBuilder()
        .maximumSize(1)
        .expireAfterWrite(STANDARD_CACHE_EXPIRATION_TIME)
        .build();
    private final Cache<Integer, UserStats> initialStatsCache = Caffeine.newBuilder()
        .maximumSize(STANDARD_CACHE_SIZE)
        .expireAfterWrite(STANDARD_CACHE_EXPIRATION_TIME)
        .build();
    private final Cache<Integer, OffsetTcStats> offsetTcStatsCache = Caffeine.newBuilder()
        .maximumSize(STANDARD_CACHE_SIZE)
        .expireAfterWrite(STANDARD_CACHE_EXPIRATION_TIME)
        .build();
    private final Cache<Integer, RetiredUserTcStats> retiredTcStatsCache = Caffeine.newBuilder()
        .maximumSize(STANDARD_CACHE_SIZE)
        .expireAfterWrite(STANDARD_CACHE_EXPIRATION_TIME)
        .build();
    private final Cache<Integer, UserTcStats> tcStatsCache = Caffeine.newBuilder()
        .maximumSize(STANDARD_CACHE_SIZE)
        .expireAfterWrite(STANDARD_CACHE_EXPIRATION_TIME)
        .build();
    private final Cache<Integer, UserStats> totalStatsCache = Caffeine.newBuilder()
        .maximumSize(STANDARD_CACHE_SIZE)
        .expireAfterWrite(STANDARD_CACHE_EXPIRATION_TIME)
        .build();

    /**
     * Creates a {@link Hardware}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@code hardwareCache}.
     *
     * @param hardware the {@link Hardware} to create
     * @return the created {@link Hardware}, with ID
     * @see DbManager#createHardware(Hardware)
     */
    @Cached
    public Hardware createHardware(final Hardware hardware) {
        return dbManagerFunction(dbManager -> {
            final Hardware hardwareWithId = dbManager.createHardware(hardware);
            hardwareCache.put(hardwareWithId.id(), hardwareWithId);
            return hardwareWithId;
        });
    }

    /**
     * Retrieves all {@link Hardware}s.
     *
     * <p>
     * First attempts to retrieve from {@code hardwareCache}, then if none exist, attempts to retrieve from the {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link Hardware}
     * @see DbManager#getAllHardware()
     */
    @Cached
    public Collection<Hardware> getAllHardware() {
        final Collection<Hardware> fromCache = hardwareCache.asMap().values();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all hardware");
        return dbManagerFunction(dbManager -> {
            final Collection<Hardware> fromDb = dbManager.getAllHardware();

            for (final Hardware hardware : fromDb) {
                hardwareCache.put(hardware.id(), hardware);
            }

            return fromDb;
        });
    }

    /**
     * Retrieves a {@link Hardware}.
     *
     * <p>
     * First attempts to retrieve from {@code hardwareCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return an {@link Optional} of the retrieved {@link Hardware}
     * @see DbManager#getHardware(int)
     */
    @Cached
    public Optional<Hardware> getHardware(final int hardwareId) {
        final Hardware fromCache = hardwareCache.getIfPresent(hardwareId);

        if (fromCache != null) {
            return Optional.of(fromCache);
        }

        LOGGER.trace("Cache miss! Get hardware");
        return dbManagerFunction(dbManager -> {
            final Optional<Hardware> fromDb = dbManager.getHardware(hardwareId);
            fromDb.ifPresent(hardware -> hardwareCache.put(hardwareId, hardware));
            return fromDb;
        });
    }

    /**
     * Updates a {@link Hardware}. Expects the {@link Hardware} to have a valid ID.
     *
     * <p>
     * Persists it with the {@link DbManager}, then updates it in the {@code hardwareCache}.
     *
     * <p>
     * Also updates the {@code userCache} with an updated version of any {@link User} that references this {@link Hardware}.
     *
     * @param hardwareToUpdate the {@link Hardware} to update
     * @return the updated {@link Hardware}
     * @see DbManager#updateHardware(Hardware)
     */
    @Cached
    public Hardware updateHardware(final Hardware hardwareToUpdate) {
        return dbManagerFunction(dbManager -> {
            final Hardware updatedHardware = dbManager.updateHardware(hardwareToUpdate);
            hardwareCache.put(updatedHardware.id(), updatedHardware);

            getAllUsers()
                .stream()
                .filter(user -> user.getHardware().id() == updatedHardware.id())
                .map(user -> User.updateHardware(user, updatedHardware))
                .forEach(updatedUser -> userCache.put(updatedUser.getId(), updatedUser));

            return updatedHardware;
        });
    }

    /**
     * Deletes a {@link Hardware}.
     *
     * <p>
     * Deletes it with the {@link DbManager}, then removes it from the {@code hardwareCache}.
     *
     * @param hardwareId the ID of the {@link Hardware} to delete
     * @see DbManager#deleteHardware(int)
     */
    @Cached
    public void deleteHardware(final int hardwareId) {
        dbManagerConsumer(dbManager -> {
            dbManager.deleteHardware(hardwareId);
            hardwareCache.invalidate(hardwareId);
        });
    }

    /**
     * Creates a {@link Team}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@code teamCache}.
     *
     * @param team the {@link Team} to create
     * @return the created {@link Team}, with ID
     * @see DbManager#createTeam(Team)
     */
    @Cached
    public Team createTeam(final Team team) {
        return dbManagerFunction(dbManager -> {
            final Team teamWithId = dbManager.createTeam(team);
            teamCache.put(teamWithId.id(), teamWithId);
            return teamWithId;
        });
    }

    /**
     * Retrieves all {@link Team}s.
     *
     * <p>
     * First attempts to retrieve from {@code teamCache}, then if none exist, attempts to retrieve from the {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link Team}s
     * @see DbManager#getAllTeams()
     */
    @Cached
    public Collection<Team> getAllTeams() {
        final Collection<Team> fromCache = teamCache.asMap().values();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all teams");
        return dbManagerFunction(dbManager -> {
            final Collection<Team> fromDb = dbManager.getAllTeams();

            for (final Team team : fromDb) {
                teamCache.put(team.id(), team);
            }

            return fromDb;
        });
    }

    /**
     * Retrieves a {@link Team}.
     *
     * <p>
     * First attempts to retrieve from {@code teamCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param teamId the ID of the {@link Team} to retrieve
     * @return an {@link Optional} of the retrieved {@link Team}
     * @see DbManager#getTeam(int)
     */
    @Cached
    public Optional<Team> getTeam(final int teamId) {
        final Team fromCache = teamCache.getIfPresent(teamId);

        if (fromCache != null) {
            return Optional.of(fromCache);
        }

        LOGGER.trace("Cache miss! Get team");
        return dbManagerFunction(dbManager -> {
            final Optional<Team> fromDb = dbManager.getTeam(teamId);
            fromDb.ifPresent(team -> teamCache.put(teamId, team));
            return fromDb;
        });
    }

    /**
     * Updates a {@link Team}. Expects the {@link Team} to have a valid ID.
     *
     * <p>
     * Persists it with the {@link DbManager}, then updates it in the {@code teamCache}.
     *
     * <p>
     * Also updates the {@code userCache} with an updated version of any {@link User} that references this {@link Team}.
     *
     * @param teamToUpdate the {@link Team} to update
     * @return the updated {@link Team}
     * @see DbManager#updateTeam(Team)
     */
    @Cached
    public Team updateTeam(final Team teamToUpdate) {
        return dbManagerFunction(dbManager -> {
            final Team updatedTeam = dbManager.updateTeam(teamToUpdate);
            teamCache.put(updatedTeam.id(), updatedTeam);

            getAllUsers()
                .stream()
                .filter(user -> user.getTeam().id() == updatedTeam.id())
                .map(user -> User.updateTeam(user, updatedTeam))
                .forEach(updatedUser -> userCache.put(updatedUser.getId(), updatedUser));

            return updatedTeam;
        });
    }

    /**
     * Deletes a {@link Team}.
     *
     * <p>
     * Deletes it with the {@link DbManager}, then removes it from the {@code teamCache}.
     *
     * @param teamId the ID of the {@link Team} to delete
     * @see DbManager#deleteTeam(int)
     */
    @Cached
    public void deleteTeam(final int teamId) {
        dbManagerConsumer(dbManager -> {
            dbManager.deleteTeam(teamId);
            teamCache.invalidate(teamId);
        });
    }

    /**
     * Creates a {@link User}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@code userCache}.
     *
     * @param user the {@link User} to create
     * @return the created {@link User}, with ID
     * @see DbManager#createUser(User)
     */
    @Cached
    public User createUser(final User user) {
        return dbManagerFunction(dbManager -> {
            final User userWithId = dbManager.createUser(user);
            userCache.put(userWithId.getId(), userWithId);
            return userWithId;
        });
    }

    /**
     * Retrieves all {@link User}s.
     *
     * <p>
     * First attempts to retrieve from {@code userCache}, then if none exist, attempts to retrieve from the {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     * @see DbManager#getAllUsers()
     */
    @Cached
    public Collection<User> getAllUsers() {
        final Collection<User> fromCache = userCache.asMap().values();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all users");
        return dbManagerFunction(dbManager -> {
            final Collection<User> fromDb = dbManager.getAllUsers();

            for (final User user : fromDb) {
                userCache.put(user.getId(), user);
            }

            return fromDb;
        });
    }

    /**
     * Retrieves a {@link User}.
     *
     * <p>
     * First attempts to retrieve from {@code userCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     * @see DbManager#getUser(int)
     */
    @Cached
    public Optional<User> getUser(final int userId) {
        final User fromCache = userCache.getIfPresent(userId);

        if (fromCache != null) {
            return Optional.of(fromCache);
        }

        LOGGER.trace("Cache miss! Get user");
        return dbManagerFunction(dbManager -> {
            final Optional<User> fromDb = dbManager.getUser(userId);
            fromDb.ifPresent(user -> userCache.put(userId, user));
            return fromDb;
        });
    }

    /**
     * Updates a {@link User}. Expects the {@link User} to have a valid ID.
     *
     * <p>
     * Persists it with the {@link DbManager}, then updates it in the {@code userCache}.
     *
     * @param userToUpdate the {@link User} to update
     * @return the updated {@link User}
     * @see DbManager#updateUser(User)
     */
    @Cached
    public User updateUser(final User userToUpdate) {
        return dbManagerFunction(dbManager -> {
            final User updatedUser = dbManager.updateUser(userToUpdate);
            userCache.put(updatedUser.getId(), updatedUser);
            return updatedUser;
        });
    }

    /**
     * Deletes a {@link User}.
     *
     * <p>
     * Deletes it with the {@link DbManager}, then removes it from the {@code userCache}.
     *
     * <p>
     * Also removes the {@link User}'s values from the stats caches:
     * <ul>
     *     <li>{@code initialStatsCache}</li>
     *     <li>{@code offsetTcStatsCache}</li>
     *     <li>{@code tcStatsCache}</li>
     *     <li>{@code totalStatsCache}</li>
     * </ul>
     *
     * @param userId the ID of the {@link User} to delete
     * @see DbManager#deleteUser(int)
     */
    @Cached
    public void deleteUser(final int userId) {
        dbManagerConsumer(dbManager -> {
            dbManager.deleteUser(userId);
            userCache.invalidate(userId);

            // Remove the user entry from all stats caches
            offsetTcStatsCache.invalidate(userId);
            totalStatsCache.invalidate(userId);
            initialStatsCache.invalidate(userId);
            tcStatsCache.invalidate(userId);
        });
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
    public MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult) {
        return dbManagerFunction(dbManager -> dbManager.createMonthlyResult(monthlyResult));
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
    public Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year) {
        return dbManagerFunction(dbManager -> dbManager.getMonthlyResult(month, year));
    }

    /**
     * Creates a {@link RetiredUserTcStats} for a {@link User} that has been deleted from a {@link Team}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@code retiredTcStatsCache}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} for the deleted {@link User}
     * @return the {@link RetiredUserTcStats}
     */
    @Cached
    public RetiredUserTcStats createRetiredUserStats(final RetiredUserTcStats retiredUserTcStats) {
        return dbManagerFunction(dbManager -> {
            final RetiredUserTcStats createdRetiredUserTcStats = dbManager.createRetiredUserStats(retiredUserTcStats);
            retiredTcStatsCache.put(createdRetiredUserTcStats.getRetiredUserId(), createdRetiredUserTcStats);
            return createdRetiredUserTcStats;
        });
    }

    /**
     * Retrieves all {@link RetiredUserTcStats}.
     *
     * <p>
     * First attempts to retrieve from {@code retiredTcStatsCache}, then if none exist, attempts to retrieve from the {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link RetiredUserTcStats}
     * @see DbManager#getAllRetiredUserStats()
     */
    @Cached
    public Collection<RetiredUserTcStats> getAllRetiredUsers() {
        final Collection<RetiredUserTcStats> fromCache = retiredTcStatsCache.asMap().values();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all retired users");
        return dbManagerFunction(dbManager -> {
            final Collection<RetiredUserTcStats> fromDb = dbManager.getAllRetiredUserStats();

            for (final RetiredUserTcStats retiredUserTcStats : fromDb) {
                retiredTcStatsCache.put(retiredUserTcStats.getRetiredUserId(), retiredUserTcStats);
            }

            return fromDb;
        });
    }

    /**
     * Deletes all {@link RetiredUserTcStats} for all {@link Team}s.
     *
     * <p>
     * Also evicts the {@code retiredTcStatsCache}.
     */
    @Cached
    public void deleteAllRetiredUserTcStats() {
        dbManagerConsumer(dbManager -> {
            dbManager.deleteAllRetiredUserStats();
            retiredTcStatsCache.invalidateAll();
        });
    }

    /**
     * Authenticates a system user with {@link DbManager}.
     *
     * @param userName the system user username
     * @param password the system user password
     * @return the {@link UserAuthenticationResult}
     */
    @NotCached
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        return dbManagerFunction(dbManager -> dbManager.authenticateSystemUser(userName, password));
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
    public Collection<HistoricStats> getHistoricStats(final int userId, final Year year, final Month month, final int day) {
        if (year == null) {
            return Collections.emptyList();
        }

        return dbManagerFunction(dbManager -> {
            if (month == null) {
                return dbManager.getHistoricStatsMonthly(userId, year);
            }

            if (day == 0) {
                return dbManager.getHistoricStatsDaily(userId, year, month);
            }

            return dbManager.getHistoricStatsHourly(userId, year, month, day);
        });
    }

    /**
     * Creates a {@link UserStats} for the total overall stats for a {@link User}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@code totalStatsCache}.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}
     */
    @Cached
    public UserStats createTotalStats(final UserStats userStats) {
        return dbManagerFunction(dbManager -> {
            final UserStats fromDb = dbManager.createTotalStats(userStats);
            totalStatsCache.put(fromDb.getUserId(), fromDb);
            return fromDb;
        });
    }

    /**
     * Retrieves the {@link UserStats} for a {@link User} with the provided ID.
     *
     * <p>
     * First attempts to retrieve from {@code totalStatsCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} to whose {@link UserStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserStats}
     * @see DbManager#getTotalStats(int)
     */
    @Cached
    public Optional<UserStats> getTotalStats(final int userId) {
        final UserStats fromCache = totalStatsCache.getIfPresent(userId);

        if (fromCache != null) {
            return Optional.of(fromCache);
        }

        LOGGER.trace("Cache miss! Total stats");
        return dbManagerFunction(dbManager -> {
            final Optional<UserStats> fromDb = dbManager.getTotalStats(userId);
            fromDb.ifPresent(userStats -> totalStatsCache.put(userId, userStats));
            return fromDb;
        });
    }

    /**
     * Creates an {@link OffsetTcStats}, defining the offset points/units for the provided {@link User}.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@code offsetTcStatsCache}.
     *
     * <p>
     * If an {@link OffsetTcStats} already exists for the {@link User}, the existing values are updated to be the addition of both
     * {@link OffsetTcStats}.
     *
     * @param userId        the ID of the {@link User} for whom the {@link OffsetTcStats} are being created
     * @param offsetTcStats the {@link OffsetTcStats} to be created
     * @return the created/updated {@link OffsetTcStats}, or {@link OffsetTcStats#empty()}
     */
    @Cached
    public OffsetTcStats createOrUpdateOffsetStats(final int userId, final OffsetTcStats offsetTcStats) {
        return dbManagerFunction(dbManager -> {
            final OffsetTcStats fromDb = dbManager.createOrUpdateOffsetStats(userId, offsetTcStats);
            offsetTcStatsCache.put(userId, fromDb);
            return fromDb;
        });
    }

    /**
     * Retrieves the {@link OffsetTcStats} for a {@link User} with the provided ID.
     *
     * <p>
     * First attempts to retrieve from {@code offsetTcStatsCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} to whose {@link OffsetTcStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link OffsetTcStats}
     * @see DbManager#getOffsetStats(int)
     */
    @Cached
    public Optional<OffsetTcStats> getOffsetStats(final int userId) {
        final OffsetTcStats fromCache = offsetTcStatsCache.getIfPresent(userId);

        if (fromCache != null) {
            return Optional.of(fromCache);
        }

        LOGGER.trace("Cache miss! Offset stats");
        return dbManagerFunction(dbManager -> {
            final Optional<OffsetTcStats> fromDb = dbManager.getOffsetStats(userId);
            fromDb.ifPresent(offsetTcStats -> offsetTcStatsCache.put(userId, offsetTcStats));
            return fromDb;
        });
    }

    /**
     * Deletes the {@link OffsetTcStats} for a {@link User} with the provided ID.
     *
     * <p>
     * Also evicts the {@link User} ID from the {@code offsetTcStatsCache}.
     *
     * @param userId the ID of the {@link User} to whose {@link OffsetTcStats} are to be deleted
     */
    @Cached
    public void deleteOffsetStats(final int userId) {
        dbManagerConsumer(dbManager -> {
            dbManager.deleteOffsetStats(userId);
            offsetTcStatsCache.invalidate(userId);
        });
    }

    /**
     * Deletes the {@link OffsetTcStats} for all {@link User}s in the system.
     *
     * <p>
     * Also evicts the {@code offsetTcStatsCache}.
     */
    @Cached
    public void deleteAllOffsetTcStats() {
        dbManagerConsumer(dbManager -> {
            dbManager.deleteAllOffsetStats();
            offsetTcStatsCache.invalidateAll();
        });
    }

    /**
     * Creates a {@link UserTcStats} for a {@link User}'s <code>Team Competition</code> stats for a specific hour.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@code tcStatsCache}.
     *
     * @param userTcStats the {@link UserTcStats} to be created
     * @return the created {@link UserTcStats}
     */
    @Cached
    public UserTcStats createHourlyTcStats(final UserTcStats userTcStats) {
        return dbManagerFunction(dbManager -> {
            final UserTcStats fromDb = dbManager.createHourlyTcStats(userTcStats);
            tcStatsCache.put(userTcStats.getUserId(), fromDb);
            return fromDb;
        });
    }

    /**
     * Retrieves the latest {@link UserTcStats} for the provided {@link User}.
     *
     * <p>
     * First attempts to retrieve from {@code tcStatsCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} whose {@link UserTcStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserTcStats}
     */
    @Cached
    public Optional<UserTcStats> getHourlyTcStats(final int userId) {
        final UserTcStats fromCache = tcStatsCache.getIfPresent(userId);

        if (fromCache != null) {
            return Optional.of(fromCache);
        }

        LOGGER.trace("Cache miss! Hourly TC stats");
        return dbManagerFunction(dbManager -> {
            final Optional<UserTcStats> fromDb = dbManager.getHourlyTcStats(userId);
            fromDb.ifPresent(userTcStats -> tcStatsCache.put(userId, userTcStats));
            return fromDb;
        });
    }

    /**
     * Creates a {@link UserStats} for the initial overall stats for the provided {@link User} at the start of the monitoring period.
     *
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@code initialStatsCache}.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}
     */
    @Cached
    public UserStats createInitialStats(final UserStats userStats) {
        return dbManagerFunction(dbManager -> {
            final UserStats fromDb = dbManager.createInitialStats(userStats);
            initialStatsCache.put(fromDb.getUserId(), fromDb);
            return fromDb;
        });
    }

    /**
     * Retrieves the initial {@link UserStats} for the provided {@link User} ID.
     *
     * <p>
     * First attempts to retrieve from {@code initialStatsCache}, then if none exists, attempts to retrieve from the {@link DbManager}.
     *
     * @param userId the ID of the {@link User} whose {@link UserStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserStats}
     */
    @Cached
    public Optional<UserStats> getInitialStats(final int userId) {
        final UserStats fromCache = initialStatsCache.getIfPresent(userId);

        if (fromCache != null) {
            return Optional.of(fromCache);
        }

        LOGGER.trace("Cache miss! Initial stats");
        return dbManagerFunction(dbManager -> {
            final Optional<UserStats> fromDb = dbManager.getInitialStats(userId);
            fromDb.ifPresent(userStats -> initialStatsCache.put(userId, userStats));
            return fromDb;
        });
    }

    /**
     * Creates a {@link AllTeamsSummary}, then adds it to {@code allTeamsSummaryCache}.
     *
     * @param allTeamsSummary the {@link AllTeamsSummary} to be created
     * @return the created {@link AllTeamsSummary}
     */
    @Cached
    public AllTeamsSummary createAllTeamsSummary(final AllTeamsSummary allTeamsSummary) {
        this.allTeamsSummaryCache.put(ALL_TEAMS_SUMMARY_ID, allTeamsSummary);
        return allTeamsSummary;
    }

    /**
     * Creates the latest {@link AllTeamsSummary}.
     *
     * @return an {@link Optional} of the latest {@link AllTeamsSummary}
     */
    @Cached
    public Optional<AllTeamsSummary> getAllTeamsSummary() {
        return Optional.ofNullable(allTeamsSummaryCache.getIfPresent(ALL_TEAMS_SUMMARY_ID));
    }

    /**
     * Evicts all {@link User}s from the {@code totalStatsCache}.
     */
    @Cached
    public void evictTcStatsCache() {
        tcStatsCache.invalidateAll();
    }

    /**
     * Evicts all {@link User}s from the {@code initialStatsCache}.
     */
    @Cached
    public void evictInitialStatsCache() {
        initialStatsCache.invalidateAll();
    }

    /**
     * Creates a {@link UserChange}.
     *
     * <p>
     * Persists it with the {@link DbManager}.
     *
     * @param userChange the {@link UserChange} to create
     * @return the created {@link UserChange}, with ID
     * @see DbManager#createUserChange(UserChange)
     */
    @NotCached
    public UserChange createUserChange(final UserChange userChange) {
        return dbManagerFunction(dbManager -> dbManager.createUserChange(userChange));
    }

    /**
     * Retrieves all {@link UserChange}s with the given {@link UserChangeState}.
     *
     * <p>
     * Attempts to retrieve from the {@link DbManager}.
     *
     * @param states         the {@link UserChangeState}s to look for
     * @param numberOfMonths the number of months back from which to retrieve {@link UserChange}s (<b>0</b> means retrieve all)
     * @return a {@link Collection} of the retrieved {@link UserChange}
     * @see DbManager#getAllUserChanges(Collection, int)
     */
    @NotCached
    public Collection<UserChange> getAllUserChanges(final Collection<UserChangeState> states, final int numberOfMonths) {
        return dbManagerFunction(dbManager -> dbManager.getAllUserChanges(states, numberOfMonths));
    }

    /**
     * Retrieves a {@link UserChange}.
     *
     * <p>
     * Attempts to retrieve from the {@link DbManager}.
     *
     * @param userChangeId the ID of the {@link UserChange} to retrieve
     * @return an {@link Optional} of the retrieved {@link UserChange}
     * @see DbManager#getUserChange(int)
     */
    @NotCached
    public Optional<UserChange> getUserChange(final int userChangeId) {
        return dbManagerFunction(dbManager -> dbManager.getUserChange(userChangeId));
    }

    /**
     * Updates a {@link UserChange}. Expects the {@link UserChange} to have a valid ID.
     *
     * <p>
     * Persists it with the {@link DbManager}.
     *
     * @param userChangeToUpdate the {@link UserChange} with updated values
     * @return the updated {@link UserChange}
     * @see DbManager#updateUserChange(UserChange)
     */
    @NotCached
    public UserChange updateUserChange(final UserChange userChangeToUpdate) {
        return dbManagerFunction(dbManager -> dbManager.updateUserChange(userChangeToUpdate));
    }

    /**
     * Prints the contents of caches to the system log.
     */
    @Cached
    public void printCacheContents() {
        // POJOs
        LOGGER.info("HardwareCache: {}", hardwareCache.asMap());
        LOGGER.info("TeamCache: {}", teamCache.asMap());
        LOGGER.info("UserCache: {}", userCache.asMap());

        // Stats
        LOGGER.info("InitialStatsCache: {}", initialStatsCache.asMap());
        LOGGER.info("OffsetStatsCache: {}", offsetTcStatsCache.asMap());
        LOGGER.info("RetiredTcStatsCache: {}", retiredTcStatsCache.asMap());
        LOGGER.info("TcStatsCache: {}", tcStatsCache.asMap());
        LOGGER.info("TotalStatsCache: {}", totalStatsCache.asMap());

        // TC overall
        LOGGER.info("AllTeamsSummaryCache: {}", allTeamsSummaryCache.asMap());
    }

    private <T> T dbManagerFunction(final Function<DbManager, T> function) {
        try (final DbManager dbManager = DbManagerRetriever.get()) {
            return function.apply(dbManager);
        } catch (final IOException e) {
            throw new DatabaseConnectionException("Error closing connection", e);
        }
    }

    private void dbManagerConsumer(final Consumer<DbManager> consumer) {
        try (final DbManager dbManager = DbManagerRetriever.get()) {
            consumer.accept(dbManager);
        } catch (final IOException e) {
            throw new DatabaseConnectionException("Error closing connection", e);
        }
    }
}
