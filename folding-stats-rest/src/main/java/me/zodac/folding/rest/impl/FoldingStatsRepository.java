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

package me.zodac.folding.rest.impl;

import static java.util.stream.Collectors.toList;

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
import me.zodac.folding.rest.api.FoldingStatsService;
import me.zodac.folding.rest.api.StorageService;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.RetiredUserSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

///**
// * {@link Singleton} EJB implementation of {@link FoldingStatsService}.
// *
// * <p>
// * For the most part, this will serve as a wrapper to {@link Storage}, which knows how to perform CRUD operations on the backend storage and caches.
// * But since some logic is needed for special cases (like retrieving the latest Folding@Home stats for a {@link User} when it is created), we
// * implement that logic here, and delegate any CRUD needs to {@link Storage}.
// */
//@Singleton
@Component
public class FoldingStatsRepository implements FoldingStatsService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private StorageService storageService;

    @Override
    public MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult) {
        return storageService.createMonthlyResult(monthlyResult);
    }

    @Override
    public Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year) {
        return storageService.getMonthlyResult(month, year);
    }

    @Override
    public Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month, final int day) {
        final Collection<HistoricStats> historicStats = storageService.getHistoricStats(user.getId(), year, month, day);
        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}/{}, returning empty", user.getId(), year.getValue(), month.getValue(), day);
        }

        return historicStats;
    }

    @Override
    public Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month) {
        final Collection<HistoricStats> historicStats = storageService.getHistoricStats(user.getId(), year, month, 0);
        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}, returning empty", user.getId(), year.getValue(), month.getValue());
        }

        return historicStats;
    }

    @Override
    public Collection<HistoricStats> getHistoricStats(final User user, final Year year) {
        final Collection<HistoricStats> historicStats = storageService.getHistoricStats(user.getId(), year, null, 0);
        if (historicStats.isEmpty()) {
            LOGGER.warn("No stats retrieved for user with ID {} on {}, returning empty", user.getId(), year.getValue());
        }

        return historicStats;
    }

    @Override
    public UserStats createTotalStats(final UserStats userStats) {
        return storageService.createTotalStats(userStats);
    }

    @Override
    public UserStats getTotalStats(final User user) {
        return storageService.getTotalStats(user.getId())
            .orElse(UserStats.empty());
    }

    @Override
    public OffsetTcStats createOffsetStats(final User user, final OffsetTcStats offsetTcStats) {
        storageService.deleteOffsetStats(user.getId());
        return storageService.createOrUpdateOffsetStats(user.getId(), offsetTcStats);
    }

    @Override
    public OffsetTcStats createOrUpdateOffsetStats(final User user, final OffsetTcStats offsetTcStats) {
        return storageService.createOrUpdateOffsetStats(user.getId(), offsetTcStats);
    }

    @Override
    public OffsetTcStats getOffsetStats(final User user) {
        return storageService.getOffsetStats(user.getId())
            .orElse(OffsetTcStats.empty());
    }

    @Override
    public UserTcStats createHourlyTcStats(final UserTcStats userTcStats) {
        return storageService.createHourlyTcStats(userTcStats);
    }

    @Override
    public UserTcStats getHourlyTcStats(final User user) {
        return storageService.getHourlyTcStats(user.getId())
            .orElse(UserTcStats.empty(user.getId()));
    }

    @Override
    public boolean isAnyHourlyTcStatsExist() {
        return storageService.getFirstHourlyTcStats().isPresent();
    }

    @Override
    public UserStats createInitialStats(final UserStats userStats) {
        return storageService.createInitialStats(userStats);
    }

    @Override
    public UserStats getInitialStats(final User user) {
        return storageService.getInitialStats(user.getId())
            .orElse(UserStats.empty());
    }

    @Override
    public RetiredUserTcStats createRetiredUserStats(final RetiredUserTcStats retiredUserTcStats) {
        return storageService.createRetiredUserStats(retiredUserTcStats);
    }

    @Override
    public void resetAllTeamCompetitionUserStats(final Collection<User> usersWithoutPasskeys) {
        for (final User user : usersWithoutPasskeys) {
            LOGGER.info("Resetting TC stats for {}", user.getDisplayName());
            final UserStats totalStats = getTotalStats(user);
            createInitialStats(totalStats);
        }

        LOGGER.info("Deleting retired user TC stats");
        storageService.deleteAllRetiredUserTcStats();

        LOGGER.info("Deleting offset TC stats");
        storageService.deleteAllOffsetTcStats();

        LOGGER.info("Evicting TC and initial stats caches");
        storageService.evictTcStatsCache();
        storageService.evictInitialStatsCache();
    }

    @Override
    public CompetitionSummary getCompetitionSummary(final Collection<Team> teams, final Collection<User> users) {
        if (SystemStateManager.current() != SystemState.WRITE_EXECUTED) {
            LOGGER.debug("System is not in state {}, retrieving competition summary", SystemState.WRITE_EXECUTED);

            final Optional<CompetitionSummary> cachedCompetitionResult = storageService.getCompetitionSummary();
            if (cachedCompetitionResult.isPresent()) {
                return cachedCompetitionResult.get();
            }
        }

        LOGGER.debug("Calculating latest TC result, system state: {}", SystemStateManager.current());
        final CompetitionSummary competitionSummary = createCompetitionSummary(teams, users);
        final CompetitionSummary createdCompetitionSummary = storageService.createCompetitionSummary(competitionSummary);
        SystemStateManager.next(SystemState.AVAILABLE);

        return createdCompetitionSummary;
    }

    private CompetitionSummary createCompetitionSummary(final Collection<Team> teams, final Collection<User> users) {
        final List<TeamSummary> teamSummaries = getStatsForTeams(teams, users);
        LOGGER.debug("Found {} TC teams", teamSummaries::size);

        if (teamSummaries.isEmpty()) {
            LOGGER.warn("No TC teams to show");
        }

        return CompetitionSummary.create(teamSummaries);
    }

    private List<TeamSummary> getStatsForTeams(final Collection<Team> teams, final Collection<User> users) {
        return teams
            .stream()
            .map(team -> getTcTeamResult(team, users))
            .collect(toList());
    }

    private TeamSummary getTcTeamResult(final Team team, final Collection<User> users) {
        LOGGER.debug("Converting team '{}' for TC stats", team::getTeamName);

        final Collection<User> usersOnTeam = getUsersFromTeam(team, users);

        final Collection<UserSummary> activeUserSummaries = usersOnTeam
            .stream()
            .map(this::getTcStatsForUser)
            .collect(toList());

        final Collection<RetiredUserSummary> retiredUserSummaries = getAllRetiredUsersForTeam(team)
            .stream()
            .map(RetiredUserSummary::createWithDefaultRank)
            .collect(toList());

        final String captainDisplayName = getCaptainDisplayName(team.getTeamName(), usersOnTeam);
        return TeamSummary.createWithDefaultRank(team, captainDisplayName, activeUserSummaries, retiredUserSummaries);
    }

    private Collection<User> getUsersFromTeam(final Team team, final Collection<User> users) {
        return users
            .stream()
            .filter(user -> user.getTeam().getId() == team.getId())
            .collect(toList());
    }

    private Collection<RetiredUserTcStats> getAllRetiredUsersForTeam(final Team team) {
        return storageService.getAllRetiredUsers()
            .stream()
            .filter(retiredUserTcStats -> retiredUserTcStats.getTeamId() == team.getId())
            .collect(toList());
    }

    private UserSummary getTcStatsForUser(final User user) {
        final UserTcStats userTcStats = getHourlyTcStats(user);
        LOGGER.debug("Results for {}: {} points | {} multiplied points | {} units", user::getDisplayName, userTcStats::getPoints,
            userTcStats::getMultipliedPoints, userTcStats::getUnits);
        return UserSummary.createWithDefaultRank(user, userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits());
    }

    private static String getCaptainDisplayName(final String teamName, final Collection<User> usersOnTeam) {
        for (final User user : usersOnTeam) {
            if (user.isUserIsCaptain()) {
                return user.getDisplayName();
            }
        }

        LOGGER.warn("No captain set for team '{}'", teamName);
        return null;
    }
}
