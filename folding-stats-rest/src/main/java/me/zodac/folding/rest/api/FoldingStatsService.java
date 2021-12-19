/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.rest.api;

import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.springframework.stereotype.Service;

/**
 * Interface defining the core business logic of the system.
 *
 * <p>
 * In order to decouple the REST layer from any business requirements, we move that logic into this interface, to be
 * implemented as an EJB. This should simplify the REST layer to simply validate incoming requests and forward to here.
 */
@Service
public interface FoldingStatsService {

    /**
     * Creates a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to create
     * @return the created {@link Hardware}, with ID
     */
    Hardware createHardware(final Hardware hardware);

    /**
     * Retrieves a {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return an {@link Optional} of the retrieved {@link Hardware}
     */
    Optional<Hardware> getHardware(final int hardwareId);

    /**
     * Retrieves all {@link Hardware}.
     *
     * @return a {@link Collection} of the retrieved {@link Hardware}
     */
    Collection<Hardware> getAllHardware();

    /**
     * Updates an existing {@link Hardware}.
     *
     * <p>
     * Also handles state change to any {@link User}s using this {@link Hardware} if necessary.
     *
     * @param hardwareToUpdate the {@link Hardware} with updated values
     * @param existingHardware the existing {@link Hardware}
     * @return the updated {@link Hardware}
     * //     * @see me.zodac.folding.core.tc.user.UserStateChangeHandler#isHardwareStateChange(Hardware, Hardware)
     */
    Hardware updateHardware(final Hardware hardwareToUpdate, final Hardware existingHardware);

    /**
     * Deletes a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to delete
     */
    void deleteHardware(final Hardware hardware);

    /**
     * Creates a {@link Team}.
     *
     * @param team the {@link Team} to create
     * @return the created {@link Team}, with ID
     */
    Team createTeam(final Team team);

    /**
     * Retrieves a {@link Team}.
     *
     * @param teamId the ID of the {@link Team} to retrieve
     * @return an {@link Optional} of the retrieved {@link Team}
     */
    Optional<Team> getTeam(final int teamId);

    /**
     * Retrieves all {@link Team}.
     *
     * @return a {@link Collection} of the retrieved {@link Team}s
     */
    Collection<Team> getAllTeams();

    /**
     * Updates an existing {@link Team}.
     *
     * @param teamToUpdate the {@link Team} with updated values
     * @return the updated {@link Team}
     */
    Team updateTeam(final Team teamToUpdate);

    /**
     * Deletes a {@link Team}.
     *
     * @param team the {@link Team} to delete
     */
    void deleteTeam(final Team team);

    /**
     * Creates a {@link User}.
     *
     * <p>
     * Creates initial {@link UserStats} on creation. Also triggers a new <code>Team Competition</code> stats parse.
     *
     * @param user the {@link User} to create
     * @return the created {@link User}, with ID
     * //     * @see me.zodac.folding.core.tc.user.UserStatsParser#parseTcStatsForUser(User)
     */
    User createUser(final User user);

    /**
     * Retrieves a {@link User}, with the passkey unmodified.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     */
    Optional<User> getUserWithPasskey(final int userId);

    /**
     * Retrieves a {@link User}, with the passkey masked.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     */
    Optional<User> getUserWithoutPasskey(final int userId);

    /**
     * Retrieves all {@link User}, with the passkey unmodified.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     */
    Collection<User> getAllUsersWithPasskeys();

    /**
     * Retrieves all {@link User}, with the passkey masked.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     */
    Collection<User> getAllUsersWithoutPasskeys();

    /**
     * Updates an existing {@link User}.
     *
     * <p>
     * Also handles state change to this {@link User} if necessary.
     *
     * @param userToUpdate the {@link User} with updated values
     * @param existingUser the existing {@link User}
     * @return the updated {@link User}
     * //     * @see me.zodac.folding.core.tc.user.UserStateChangeHandler#isUserStateChange(User, User)
     */
    User updateUser(final User userToUpdate, final User existingUser);

    /**
     * Deletes a {@link User}.
     *
     * <p>
     * If the {@link User} has any <code>Team Competition</code> {@link UserTcStats}, those are retained for their {@link Team} as
     * {@link RetiredUserTcStats}.
     *
     * @param user the {@link User} to delete
     */
    void deleteUser(final User user);

    /**
     * Retrieves all {@link User}s currently referencing the provided {@link Team}.
     *
     * <p>
     * The {@link User} {@code passkey} will be masked with {@link User#hidePasskey(User)}.
     *
     * @param team the {@link Team} to check for
     * @return a {@link Collection} of {@link User}s using the {@link Team}
     */
    Collection<User> getUsersOnTeam(final Team team);

    /**
     * Retrieves all {@link User}s currently referencing the provided {@link Team}.
     *
     * <p>
     * The {@link User} {@code passkey} will be available in plaintext, so should only be used for internal processing.
     *
     * @param team the {@link Team} to check for
     * @return a {@link Collection} of {@link User}s using the {@link Team}
     */
    Collection<User> getUsersOnTeamWithPasskeys(final Team team);

    /**
     * Creates a {@link MonthlyResult} for the <code>Team Competition</code>.
     *
     * @param monthlyResult a {@link MonthlyResult} for the <code>Team Competition</code>
     * @return the <code>Team Competition</code> {@link MonthlyResult}
     */
    MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult);

    /**
     * Retrieves the {@link MonthlyResult} of the <code>Team Competition</code> for the provided {@link Month} and {@link Year}.
     *
     * @param month the {@link Month} of the {@link MonthlyResult} to be retrieved
     * @param year  the {@link Year} of the {@link MonthlyResult} to be retrieved
     * @return an {@link Optional} of the <code>Team Competition</code> {@link MonthlyResult}
     */
    Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year);

    /**
     * Authenticates a system user and retrieves its roles.
     *
     * <p>
     * The following scenarios are considered:
     * <ul>
     *     <li>The user does not exist</li>
     *     <li>The user exists but the password is incorrect</li>
     *     <li>The user exists, and the password is correct</li>
     * </ul>
     *
     * @param userName the system user username
     * @param password the system user password
     * @return the {@link UserAuthenticationResult}
     */
    UserAuthenticationResult authenticateSystemUser(final String userName, final String password);

    /**
     * Retrieves the {@link HistoricStats} for the provided {@link User} for a specific {@code day}.
     *
     * @param user  the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year  the {@link Year} of the {@link HistoricStats}
     * @param month the {@link Month} of the {@link HistoricStats}
     * @param day   the day of the {@link Month} of the {@link HistoricStats}
     * @return the hourly {@link HistoricStats} for the {@link User}
     */
    Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month, final int day);

    /**
     * Retrieves the {@link HistoricStats} for the provided {@link User} for a specific {@link Month}.
     *
     * @param user  the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year  the {@link Year} of the {@link HistoricStats}
     * @param month the {@link Month} of the {@link HistoricStats}
     * @return the daily {@link HistoricStats} for the {@link User}
     */
    Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month);

    /**
     * Retrieves the {@link HistoricStats} for the provided {@link User} for a specific {@link Year}.
     *
     * @param user the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year the {@link Year} of the {@link HistoricStats}
     * @return the monthly {@link HistoricStats} for the {@link User}
     */
    Collection<HistoricStats> getHistoricStats(final User user, final Year year);

    /**
     * Creates a {@link UserStats} for the total overall stats for the provided {@link User}.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}
     */
    UserStats createTotalStats(final UserStats userStats);

    /**
     * Retrieves the total {@link UserStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link UserStats} are to be retrieved
     * @return the {@link UserStats} for the {@link User}, or {@link UserStats#empty()} if none can be found
     */
    UserStats getTotalStats(final User user);

    /**
     * Creates an {@link OffsetTcStats}, defining the offset points/units for the provided {@link User}.
     *
     * <p>
     * If an {@link OffsetTcStats} already exists for the {@link User}, the existing values are overwritten.
     *
     * @param user          the {@link User} for whom the {@link OffsetTcStats} are being created
     * @param offsetTcStats the {@link OffsetTcStats} to be created
     * @return the created {@link OffsetTcStats}, or {@link OffsetTcStats#empty()}
     */
    OffsetTcStats createOffsetStats(final User user, final OffsetTcStats offsetTcStats);

    /**
     * Creates an {@link OffsetTcStats}, defining the offset points/units for the provided {@link User}.
     *
     * <p>
     * If an {@link OffsetTcStats} already exists for the {@link User}, the existing values are updated to be the addition of both
     * {@link OffsetTcStats}.
     *
     * @param user          the {@link User} for whom the {@link OffsetTcStats} are being created
     * @param offsetTcStats the {@link OffsetTcStats} to be created
     * @return the created/updated {@link OffsetTcStats}, or {@link OffsetTcStats#empty()}
     */
    OffsetTcStats createOrUpdateOffsetStats(final User user, final OffsetTcStats offsetTcStats);

    /**
     * Retrieves the {@link OffsetTcStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link OffsetTcStats} are to be retrieved
     * @return the {@link OffsetTcStats} for the {@link User}, or {@link OffsetTcStats#empty()} if none can be found
     */
    OffsetTcStats getOffsetStats(final User user);

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
     * @param user the {@link User} whose {@link UserTcStats} are to be retrieved
     * @return the {@link UserTcStats} for the {@link User}, or {@link UserTcStats#empty(int)} if none can be found
     */
    UserTcStats getHourlyTcStats(final User user);

    /**
     * Checks if any {@link UserTcStats} for the <code>Team Competition</code> exist in the system.
     *
     * @return <code>true</code> if any {@link UserTcStats} have been created
     */
    boolean isAnyHourlyTcStatsExist();

    /**
     * Creates a {@link UserStats} for the initial overall stats for the provided {@link User} at the start of the monitoring period.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}
     */
    UserStats createInitialStats(final UserStats userStats);

    /**
     * Retrieves the initial {@link UserStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link UserStats} are to be retrieved
     * @return the {@link UserStats} for the {@link User}, or {@link UserStats#empty()} if none can be found
     */
    UserStats getInitialStats(final User user);

    /**
     * Creates a {@link RetiredUserTcStats} for a {@link User} that has been removed from a {@link Team}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} of the removed {@link User}
     * @return the created {@link RetiredUserTcStats}
     */
    RetiredUserTcStats createRetiredUserStats(final RetiredUserTcStats retiredUserTcStats);

    /**
     * Resets all {@link User}s for the <code>Team Competition</code>. Performs the following actions:
     *
     * <ul>
     *      <li>
     *          Zeroes each {@link User}'s {@link UserTcStats} by setting their initial {@link UserStats} to their current total {@link UserStats}.
     * <p>
     *          <b>NOTE:</b> This does not retrieve the latest online {@link UserStats} for the {@link User}, it simply retrieves the latest total
     *          {@link UserStats} available on the system.
     *      </li>
     *      <li>
     *          Deletes any {@link OffsetTcStats} for the {@link User}s.
     *      </li>
     *      <li>
     *          Deletes any {@link RetiredUserTcStats}.
     *      </li>
     *      <li>
     *          Resets the {@link UserStats} and {@link UserTcStats} caches (if any are used).
     *      </li>
     * </ul>
     */
    void resetAllTeamCompetitionUserStats();

    /**
     * Retrieves the current {@link CompetitionSummary}.
     *
     * <p>
     * If the {@link  me.zodac.folding.api.state.SystemState} is in {@link  me.zodac.folding.api.state.SystemState#WRITE_EXECUTED}, a new
     * {@link CompetitionSummary} will be created.
     *
     * @return the latest {@link CompetitionSummary}
     */
    CompetitionSummary getCompetitionSummary();

    /**
     * Debug function which will print the contents of any caches being used to the system log.
     */
    void printCacheContents();
}
