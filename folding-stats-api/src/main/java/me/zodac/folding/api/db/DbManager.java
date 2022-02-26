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

package me.zodac.folding.api.db;

import java.io.Closeable;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
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
import me.zodac.folding.rest.api.tc.historic.HistoricStats;

/**
 * Interface used to interact with the storage backend and perform CRUD operations.
 *
 * <p>
 * Any method can throw a {@link DatabaseConnectionException} if an error occurs connecting to the underlying DB.
 */
public interface DbManager extends Closeable {

    /**
     * Creates a {@link Hardware} instance in the DB.
     *
     * @param hardware the {@link Hardware} to persist
     * @return the {@link Hardware} updated with an ID
     */
    Hardware createHardware(final Hardware hardware);

    /**
     * Retrieves all {@link Hardware}s from the DB.
     *
     * @return all {@link Hardware}s
     */
    Collection<Hardware> getAllHardware();

    /**
     * Retrieves a {@link Hardware} with the given ID from the DB.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return an {@link Optional} of the retrieved {@link Hardware}
     */
    Optional<Hardware> getHardware(final int hardwareId);

    /**
     * Updates an existing {@link Hardware} in the system, matching on ID.
     *
     * @param hardwareToUpdate the updated {@link Hardware} to be persisted in the DB
     * @return the updated {@link Hardware}
     */
    Hardware updateHardware(final Hardware hardwareToUpdate);

    /**
     * Deletes an existing {@link Hardware} from the system.
     *
     * @param hardwareId the ID of the {@link Hardware} to delete
     */
    void deleteHardware(final int hardwareId);

    /**
     * Creates a {@link Team} instance in the DB.
     *
     * @param team the {@link Team} to persist
     * @return the {@link Team} updated with an ID
     */
    Team createTeam(final Team team);

    /**
     * Retrieves all {@link Team}s from the DB.
     *
     * @return all {@link Team}s
     */
    Collection<Team> getAllTeams();

    /**
     * Retrieves a {@link Team} with the given ID from the DB.
     *
     * @param teamId the ID of the {@link Team} to retrieve
     * @return an {@link Optional} of the retrieved {@link Team}
     */
    Optional<Team> getTeam(final int teamId);

    /**
     * Updates an existing {@link Team} in the system, matching on ID.
     *
     * @param teamToUpdate the updated {@link Team} to be persisted in the DB
     * @return the updated {@link Team}
     */
    Team updateTeam(final Team teamToUpdate);

    /**
     * Deletes an existing {@link Team} from the system.
     *
     * @param teamId the ID of the {@link Team} to delete
     */
    void deleteTeam(final int teamId);

    /**
     * Creates a {@link User} instance in the DB.
     *
     * @param user the {@link User} to persist
     * @return the {@link User} updated with an ID
     */
    User createUser(final User user);

    /**
     * Retrieves all {@link User}s from the DB.
     *
     * @return all {@link User}s
     */
    Collection<User> getAllUsers();

    /**
     * Retrieves a {@link User} with the given ID from the DB.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     */
    Optional<User> getUser(final int userId);

    /**
     * Updates an existing {@link User} in the system, matching on ID.
     *
     * @param userToUpdate the updated {@link User} to be persisted in the DB
     * @return the updated {@link User}
     */
    User updateUser(final User userToUpdate);

    /**
     * Deletes an existing {@link User} from the system.
     *
     * @param userId the ID of the {@link User} to delete
     */
    void deleteUser(final int userId);

    /**
     * Creates a {@link UserTcStats} for a {@link User}'s <code>Team Competition</code> stats for a specific hour.
     *
     * @param userTcStats the {@link UserTcStats} to be created
     * @return the created {@link UserTcStats}
     */
    UserTcStats createHourlyTcStats(final UserTcStats userTcStats);

    /**
     * Retrieves the latest {@link UserTcStats} for the provided {@link User}.
     *
     * @param userId the ID of the {@link User} whose {@link UserTcStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserTcStats}
     */
    Optional<UserTcStats> getHourlyTcStats(final int userId);

    /**
     * Retrieves the {@link HistoricStats} for a given {@link User} ID for a specific {@code day}/{@link Month}/{@link Year}.
     *
     * @param userId the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year   the {@link Year} of the {@link HistoricStats}
     * @param month  the {@link Month} of the {@link HistoricStats}
     * @param day    the day of the {@link Month} of the {@link HistoricStats}
     * @return the hourly {@link HistoricStats} for the {@link User} for the given {@code day}
     */
    Collection<HistoricStats> getHistoricStatsHourly(final int userId, final Year year, final Month month, final int day);

    /**
     * Retrieves the {@link HistoricStats} for a given {@link User} ID for a specific {@link Month}/{@link Year}.
     *
     * @param userId the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year   the {@link Year} of the {@link HistoricStats}
     * @param month  the {@link Month} of the {@link HistoricStats}
     * @return the daily {@link HistoricStats} for the {@link User} for the given {@link Month}
     */
    Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Year year, final Month month);

    /**
     * Retrieves the {@link HistoricStats} for a given {@link User} ID for a specific {@link Year}.
     *
     * @param userId the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year   the {@link Year} of the {@link HistoricStats}
     * @return the monthly {@link HistoricStats} for the {@link User} for the given {@link Year}
     */
    Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year);

    /**
     * Creates a {@link UserStats} for the initial overall stats for the provided {@link User} at the start of the monitoring period.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}
     */
    UserStats createInitialStats(final UserStats userStats);

    /**
     * Retrieves the initial {@link UserStats} for the provided {@link User} ID.
     *
     * @param userId the ID of the {@link User} whose {@link UserStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserStats}
     */
    Optional<UserStats> getInitialStats(final int userId);

    /**
     * Creates a {@link UserStats} for the total overall stats for a {@link User}.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}
     */
    UserStats createTotalStats(final UserStats userStats);

    /**
     * Retrieves the {@link UserStats} for a {@link User} with the provided ID.
     *
     * @param userId the ID of the {@link User} to whose {@link UserStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link UserStats}
     */
    Optional<UserStats> getTotalStats(final int userId);

    /**
     * Creates an {@link OffsetTcStats}, defining the offset points/units for the provided {@link User}.
     *
     * <p>
     * If an {@link OffsetTcStats} already exists for the {@link User}, the existing values are updated to be the addition of both
     * {@link OffsetTcStats}.
     *
     * @param userId        the ID of the {@link User} for whom the {@link OffsetTcStats} are being created
     * @param offsetTcStats the {@link OffsetTcStats} to be created
     * @return the created/updated {@link OffsetTcStats}, or {@link OffsetTcStats#empty()}
     */
    OffsetTcStats createOrUpdateOffsetStats(final int userId, final OffsetTcStats offsetTcStats);

    /**
     * Retrieves the {@link OffsetTcStats} for a {@link User} with the provided ID.
     *
     * @param userId the ID of the {@link User} to whose {@link OffsetTcStats} are to be retrieved
     * @return an {@link Optional} of the retrieved {@link OffsetTcStats}
     */
    Optional<OffsetTcStats> getOffsetStats(final int userId);

    /**
     * Deletes the {@link OffsetTcStats} for a {@link User} with the provided ID.
     *
     * @param userId the ID of the {@link User} to whose {@link OffsetTcStats} are to be deleted
     */
    void deleteOffsetStats(final int userId);

    /**
     * Deletes the {@link OffsetTcStats} in the DB.
     */
    void deleteAllOffsetStats();

    /**
     * Creates a {@link RetiredUserTcStats}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} for the deleted {@link User}
     * @return the {@link RetiredUserTcStats}
     */
    RetiredUserTcStats createRetiredUserStats(final RetiredUserTcStats retiredUserTcStats);

    /**
     * Retrieves all {@link RetiredUserTcStats} from the DB.
     *
     * @return a {@link Collection} of the retrieved {@link RetiredUserTcStats}
     */
    Collection<RetiredUserTcStats> getAllRetiredUserStats();

    /**
     * Deletes all {@link RetiredUserTcStats} in the DB.
     */
    void deleteAllRetiredUserStats();

    /**
     * Creates a {@link MonthlyResult} for the <code>Team Competition</code> in the DB.
     *
     * @param monthlyResult a {@link MonthlyResult} for the <code>Team Competition</code>
     * @return the <code>Team Competition</code> {@link MonthlyResult}
     */
    MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult);

    /**
     * Retrieves the {@link MonthlyResult} of the <code>Team Competition</code> for the given {@link Month} and {@link Year} from the DB.
     *
     * @param month the {@link Month} of the {@link MonthlyResult} to be retrieved
     * @param year  the {@link Year} of the {@link MonthlyResult} to be retrieved
     * @return an {@link Optional} of the <code>Team Competition</code> {@link MonthlyResult}
     */
    Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year);

    /**
     * Authenticates a system user against the DB.
     *
     * <p>
     * The provided {@code password} will be hashed in the DB, so we verify the hashes match.
     *
     * @param userName the system user username
     * @param password the system user password
     * @return the {@link UserAuthenticationResult}
     */
    UserAuthenticationResult authenticateSystemUser(final String userName, final String password);

    /**
     * Creates a {@link UserChange} instance in the DB.
     *
     * @param userChange the {@link UserChange} to persist
     * @return the {@link UserChange} updated with an ID
     */
    UserChange createUserChange(UserChange userChange);

    /**
     * Retrieves all {@link UserChange}s from the DB.
     *
     * @return all {@link UserChange}s
     */
    Collection<UserChange> getAllUserChanges();

    /**
     * Retrieves any {@link UserChange}s that contain one of the provided {@code states} from the DB.
     *
     * @param states the {@link UserChangeState}s to look for
     * @return all {@link UserChange}s with any of the provided {@link UserChangeState}s
     */
    Collection<UserChange> getAllUserChanges(final Collection<UserChangeState> states);

    /**
     * Retrieves a {@link UserChange} with the given ID from the DB.
     *
     * @param userChangeId the ID of the {@link UserChange} to retrieve
     * @return an {@link Optional} of the retrieved {@link UserChange}
     */
    Optional<UserChange> getUserChange(final int userChangeId);

    /**
     * Updates an existing {@link UserChange} in the system, matching on ID.
     *
     * @param userChangeToUpdate the {@link UserChange} with updated values
     * @return the updated {@link UserChange}
     */
    UserChange updateUserChange(final UserChange userChangeToUpdate);
}
