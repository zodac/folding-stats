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

import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.rest.api.tc.AllTeamsSummary;
import me.zodac.folding.rest.api.tc.RetiredUserSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link Component} used for CRUD operations for {@code folding-stats} stats classes:
 * <ul>
 *     <li>{@link AllTeamsSummary}</li>
 *     <li>{@link HistoricStats}</li>
 *     <li>{@link MonthlyResult}</li>
 *     <li>{@link OffsetTcStats}</li>
 *     <li>{@link RetiredUserTcStats}</li>
 *     <li>{@link UserStats} (initial and total)</li>
 *     <li>{@link UserTcStats}</li>
 * </ul>
 */
@Component
public class StatsRepository {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Storage storage;

    /**
     * {@link Autowired} constructor.
     *
     * @param storage the {@link Storage}
     */
    @Autowired
    public StatsRepository(final Storage storage) {
        this.storage = storage;
    }

    /**
     * Creates a {@link MonthlyResult} for the {@code Team Competition}.
     *
     * @param monthlyResult a {@link MonthlyResult} for the {@code Team Competition}
     * @return the {@code Team Competition} {@link MonthlyResult}
     */

    public MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult) {
        return storage.createMonthlyResult(monthlyResult);
    }

    /**
     * Retrieves the {@link MonthlyResult} of the {@code Team Competition} for the provided {@link Month} and {@link Year}.
     *
     * @param month the {@link Month} of the {@link MonthlyResult} to be retrieved
     * @param year  the {@link Year} of the {@link MonthlyResult} to be retrieved
     * @return an {@link Optional} of the {@code Team Competition} {@link MonthlyResult}
     */

    public Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year) {
        return storage.getMonthlyResult(month, year);
    }

    /**
     * Retrieves the {@link HistoricStats} for the provided {@link User} for a specific {@code day}.
     *
     * @param user  the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year  the {@link Year} of the {@link HistoricStats}
     * @param month the {@link Month} of the {@link HistoricStats}
     * @param day   the day of the {@link Month} of the {@link HistoricStats}
     * @return the hourly {@link HistoricStats} for the {@link User}
     */
    public Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month, final int day) {
        final Collection<HistoricStats> historicStats = storage.getHistoricStats(user.id(), year, month, day);
        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}/{}, returning empty", user.id(), year.getValue(), month.getValue(), day);
        }

        return historicStats;
    }

    /**
     * Retrieves the {@link HistoricStats} for the provided {@link User} for a specific {@link Month}.
     *
     * @param user  the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year  the {@link Year} of the {@link HistoricStats}
     * @param month the {@link Month} of the {@link HistoricStats}
     * @return the daily {@link HistoricStats} for the {@link User}
     */
    public Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month) {
        final Collection<HistoricStats> historicStats = storage.getHistoricStats(user.id(), year, month, 0);
        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}, returning empty", user.id(), year.getValue(), month.getValue());
        }

        return historicStats;
    }

    /**
     * Retrieves the {@link HistoricStats} for the provided {@link User} for a specific {@link Year}.
     *
     * @param user the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year the {@link Year} of the {@link HistoricStats}
     * @return the monthly {@link HistoricStats} for the {@link User}
     */
    public Collection<HistoricStats> getHistoricStats(final User user, final Year year) {
        final Collection<HistoricStats> historicStats = storage.getHistoricStats(user.id(), year, null, 0);
        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}, returning empty", user.id(), year.getValue());
        }

        return historicStats;
    }

    /**
     * Creates a {@link UserStats} for the total overall stats for the provided {@link User}.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}
     */
    public UserStats createTotalStats(final UserStats userStats) {
        return storage.createTotalStats(userStats);
    }

    /**
     * Retrieves the total {@link UserStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link UserStats} are to be retrieved
     * @return the {@link UserStats} for the {@link User}, or {@link UserStats#empty()} if none can be found
     */
    public UserStats getTotalStats(final User user) {
        return storage.getTotalStats(user.id())
            .orElse(UserStats.empty());
    }

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
    public OffsetTcStats createOffsetStats(final User user, final OffsetTcStats offsetTcStats) {
        storage.deleteOffsetStats(user.id());
        return storage.createOrUpdateOffsetStats(user.id(), offsetTcStats);
    }

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
    public OffsetTcStats createOrUpdateOffsetStats(final User user, final OffsetTcStats offsetTcStats) {
        return storage.createOrUpdateOffsetStats(user.id(), offsetTcStats);
    }

    /**
     * Retrieves the {@link OffsetTcStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link OffsetTcStats} are to be retrieved
     * @return the {@link OffsetTcStats} for the {@link User}, or {@link OffsetTcStats#empty()} if none can be found
     */
    public OffsetTcStats getOffsetStats(final User user) {
        return storage.getOffsetStats(user.id())
            .orElse(OffsetTcStats.empty());
    }

    /**
     * Creates a {@link UserTcStats} for a {@link User}'s {@code Team Competition} stats for a specific hour.
     *
     * @param userTcStats the {@link UserTcStats} to be created
     * @return the created {@link UserTcStats}
     */
    public UserTcStats createHourlyTcStats(final UserTcStats userTcStats) {
        return storage.createHourlyTcStats(userTcStats);
    }

    /**
     * Retrieves the latest {@link UserTcStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link UserTcStats} are to be retrieved
     * @return the {@link UserTcStats} for the {@link User}, or {@link UserTcStats#empty(int)} if none can be found
     */
    public UserTcStats getHourlyTcStats(final User user) {
        return storage.getHourlyTcStats(user.id())
            .orElse(UserTcStats.empty(user.id()));
    }

    /**
     * Creates a {@link UserStats} for the initial overall stats for the provided {@link User} at the start of the monitoring period.
     *
     * @param userStats the {@link UserStats} to be created
     * @return the created {@link UserStats}
     */
    public UserStats createInitialStats(final UserStats userStats) {
        return storage.createInitialStats(userStats);
    }

    /**
     * Retrieves the initial {@link UserStats} for the provided {@link User}.
     *
     * @param user the {@link User} whose {@link UserStats} are to be retrieved
     * @return the {@link UserStats} for the {@link User}, or {@link UserStats#empty()} if none can be found
     */
    public UserStats getInitialStats(final User user) {
        return storage.getInitialStats(user.id())
            .orElse(UserStats.empty());
    }

    /**
     * Creates a {@link RetiredUserTcStats} for a {@link User} that has been removed from a {@link Team}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} of the removed {@link User}
     * @return the created {@link RetiredUserTcStats}
     */
    public RetiredUserTcStats createRetiredUserStats(final RetiredUserTcStats retiredUserTcStats) {
        return storage.createRetiredUserStats(retiredUserTcStats);
    }

    /**
     * Resets all {@link User}s for the {@code Team Competition}. Performs the following actions:
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
     *          Resets the {@link UserStats}, {@link UserTcStats} and {@link AllTeamsSummary} caches (if any are used).
     *      </li>
     *      <li>
     *          Sets the {@link SystemState} to {@link SystemState#WRITE_EXECUTED}.
     *      </li>
     * </ul>
     */
    public void resetAllTeamCompetitionUserStats() {
        for (final User user : storage.getAllUsers()) {
            LOGGER.info("Resetting TC stats for {}", user.displayName());
            final UserStats totalStats = getTotalStats(user);
            final UserStats totalStatsWithNewTime = UserStats.createNow(totalStats.userId(), totalStats.points(), totalStats.units());
            createInitialStats(totalStatsWithNewTime);
            createHourlyTcStats(UserTcStats.empty(totalStats.userId()));
        }

        LOGGER.info("Deleting retired user TC stats");
        storage.deleteAllRetiredUserTcStats();

        LOGGER.info("Deleting offset TC stats");
        storage.deleteAllOffsetTcStats();

        LOGGER.info("Evicting TC and initial stats caches, and all teams summary cache");
        storage.evictTcStatsCache();
        storage.evictInitialStatsCache();
        storage.evictAllTeamsSummaryCache();
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

    /**
     * Retrieves the current {@link AllTeamsSummary}.
     *
     * <p>
     * If the {@link  me.zodac.folding.api.state.SystemState} is in {@link  me.zodac.folding.api.state.SystemState#WRITE_EXECUTED}, a new
     * {@link AllTeamsSummary} will be created.
     *
     * @return the latest {@link AllTeamsSummary}
     */
    public AllTeamsSummary getAllTeamsSummary() {
        if (SystemStateManager.current() != SystemState.WRITE_EXECUTED) {
            LOGGER.debug("System is not in state {}, retrieving cached TC result", SystemState.WRITE_EXECUTED);

            final Optional<AllTeamsSummary> cachedCompetitionResult = storage.getAllTeamsSummary();
            if (cachedCompetitionResult.isPresent()) {
                return cachedCompetitionResult.get();
            }
        }

        LOGGER.debug("Calculating latest TC result, system state: {}", SystemStateManager.current());
        final AllTeamsSummary allTeamsSummary = constructAllTeamsSummary();
        final AllTeamsSummary createdAllTeamsSummary = storage.createAllTeamsSummary(allTeamsSummary);
        SystemStateManager.next(SystemState.AVAILABLE);

        return createdAllTeamsSummary;
    }

    private AllTeamsSummary constructAllTeamsSummary() {
        final List<TeamSummary> teamSummaries = getStatsForTeams();
        LOGGER.debug("Found {} TC teams", teamSummaries::size);

        if (teamSummaries.isEmpty()) {
            LOGGER.warn("No TC teams to show");
        }

        return AllTeamsSummary.create(teamSummaries);
    }

    private List<TeamSummary> getStatsForTeams() {
        return storage.getAllTeams()
            .stream()
            .map(this::getTcTeamResult)
            .toList();
    }

    private TeamSummary getTcTeamResult(final Team team) {
        LOGGER.debug("Converting team '{}' for TC stats", team::teamName);

        final Collection<User> usersOnTeam = getUsersFromTeam(team);
        LOGGER.debug("Found {} users for team '{}': {}", usersOnTeam.size(), team.teamName(), usersOnTeam);

        final Collection<UserSummary> activeUserSummaries = usersOnTeam
            .stream()
            .map(User::hidePasskey) // Since we are retrieving users from StorageService, the passkeys will need to be explicitly hidden
            .map(this::getTcStatsForUser)
            .toList();

        final Collection<RetiredUserSummary> retiredUserSummaries = getAllRetiredUsersForTeam(team)
            .stream()
            .map(RetiredUserSummary::createWithDefaultRank)
            .toList();

        final String captainDisplayName = getCaptainDisplayName(team.teamName(), usersOnTeam);
        return TeamSummary.createWithDefaultRank(team, captainDisplayName, activeUserSummaries, retiredUserSummaries);
    }

    private Collection<User> getUsersFromTeam(final Team team) {
        return storage.getAllUsers()
            .stream()
            .filter(user -> user.team().id() == team.id())
            .toList();
    }

    private Collection<RetiredUserTcStats> getAllRetiredUsersForTeam(final Team team) {
        return storage.getAllRetiredUsers()
            .stream()
            .filter(retiredUserTcStats -> retiredUserTcStats.teamId() == team.id())
            .toList();
    }

    private UserSummary getTcStatsForUser(final User user) {
        final UserTcStats userTcStats = getHourlyTcStats(user);
        LOGGER.debug("Results for {}: {} points | {} multiplied points | {} units", user::displayName, userTcStats::points,
            userTcStats::multipliedPoints, userTcStats::units);
        return UserSummary.createWithDefaultRank(user, userTcStats.points(), userTcStats.multipliedPoints(), userTcStats.units());
    }

    private static String getCaptainDisplayName(final String teamName, final Iterable<User> usersOnTeam) {
        for (final User user : usersOnTeam) {
            if (user.userIsCaptain()) {
                return user.displayName();
            }
        }

        LOGGER.warn("No captain set for team '{}'", teamName);
        return null;
    }
}
