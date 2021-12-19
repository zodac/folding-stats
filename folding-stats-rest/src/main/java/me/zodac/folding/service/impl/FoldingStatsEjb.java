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

package me.zodac.folding.service.impl;

import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.service.FoldingStatsService;
import me.zodac.folding.service.StorageService;
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
public class FoldingStatsEjb implements FoldingStatsService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private StorageService storageService;
//    private static final Storage STORAGE = Storage.getInstance();
//    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

//    @EJB
//    private StatsScheduler statsScheduler;
//
//    @EJB
//    private UserCaptainHandler userCaptainHandler;
//
//    @EJB
//    private UserStateChangeHandler userStateChangeHandler;
//
//    @EJB
//    private UserStatsParser userStatsParser;
//
//    @EJB
//    private UserTeamChangeHandler userTeamChangeHandler;

    @Override
    public Hardware createHardware(final Hardware hardware) {
        return storageService.createHardware(hardware);
    }


    @Override
    public Optional<Hardware> getHardware(final int hardwareId) {
        return storageService.getHardware(hardwareId);
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return storageService.getAllHardware();
    }

    @Override
    public Hardware updateHardware(final Hardware hardwareToUpdate, final Hardware existingHardware) {
        final Hardware updatedHardware = storageService.updateHardware(hardwareToUpdate);

//        if (userStateChangeHandler.isHardwareStateChange(updatedHardware, existingHardware)) {
//            final Collection<User> usersUsingThisHardware = getUsersWithHardware(updatedHardware);
//
//            for (final User userUsingHardware : usersUsingThisHardware) {
//                LOGGER.debug("User '{}' (ID: {}) had state change to hardware", userUsingHardware.getDisplayName(), userUsingHardware.getId());
//                userStateChangeHandler.handleStateChange(userUsingHardware);
//            }
//        }

        return updatedHardware;
    }

//    private Collection<User> getUsersWithHardware(final Hardware hardware) {
//        if (hardware.getId() == Hardware.EMPTY_HARDWARE_ID) {
//            return Collections.emptyList();
//        }
//
//        return getAllUsersWithPasskeys()
//            .stream()
//            .filter(user -> user.getHardware().getId() == hardware.getId())
//            .collect(toList());
//    }

    @Override
    public void deleteHardware(final Hardware hardware) {
        storageService.deleteHardware(hardware.getId());
    }

    @Override
    public Team createTeam(final Team team) {
        final Team createdTeam = storageService.createTeam(team);
//        statsScheduler.manualTeamCompetitionStatsParsing(ProcessingType.SYNCHRONOUS);
        return createdTeam;
    }

    @Override
    public Optional<Team> getTeam(final int teamId) {
        return storageService.getTeam(teamId);
    }

    @Override
    public Collection<Team> getAllTeams() {
        return storageService.getAllTeams();
    }

    @Override
    public Team updateTeam(final Team teamToUpdate) {
        return storageService.updateTeam(teamToUpdate);
    }

    @Override
    public void deleteTeam(final Team team) {
        storageService.deleteTeam(team.getId());
    }
//
//    @Override
//    public User createUser(final User user) {
//        if (userCaptainHandler.isUserCaptainAndCaptainExistsOnTeam(user)) {
//            userCaptainHandler.removeCaptaincyFromExistingTeamCaptain(user.getTeam());
//        }
//
//        final User createdUser = STORAGE.createUser(user);
//
//        try {
//            // When adding a new user, we configure the initial stats DB/cache
//            final UserStats currentUserStats = FOLDING_STATS_RETRIEVER.getTotalStats(createdUser);
//            final UserStats initialStats = createInitialStats(currentUserStats);
//            LOGGER.info("User '{}' (ID: {}) created with initial stats: {}", createdUser.getDisplayName(), createdUser.getId(), initialStats);
//            userStatsParser.parseTcStatsForUser(createdUser);
//        } catch (final ExternalConnectionException e) {
//            LOGGER.error("Error retrieving initial stats for user '{}' (ID: {})", createdUser.getDisplayName(), createdUser.getId(), e);
//        }
//
//        return createdUser;
//    }
//
//    @Override
//    public Optional<User> getUserWithPasskey(final int userId) {
//        return STORAGE.getUser(userId);
//    }
//
//    @Override
//    public Optional<User> getUserWithoutPasskey(final int userId) {
//        final Optional<User> user = getUserWithPasskey(userId);
//
//        if (user.isEmpty()) {
//            return user;
//        }
//
//        return Optional.of(User.hidePasskey(user.get()));
//    }
//
//    @Override
//    public Collection<User> getAllUsersWithPasskeys() {
//        return STORAGE.getAllUsers();
//    }
//
//    @Override
//    public Collection<User> getAllUsersWithoutPasskeys() {
//        return getAllUsersWithPasskeys()
//            .stream()
//            .map(User::hidePasskey)
//            .collect(toList());
//    }
//
//    @Override
//    public User updateUser(final User userToUpdate, final User existingUser) {
//        if (userCaptainHandler.isUserCaptainAndCaptainExistsOnTeam(userToUpdate)) {
//            final boolean isCaptainChange = userToUpdate.isUserIsCaptain() != existingUser.isUserIsCaptain();
//            final boolean isTeamChange = userToUpdate.getTeam().getId() != existingUser.getTeam().getId();
//
//            if (isCaptainChange || isTeamChange) {
//                userCaptainHandler.removeCaptaincyFromExistingTeamCaptain(userToUpdate.getTeam());
//            }
//        }
//
//        // Perform any stats handling before updating the user
//        if (userTeamChangeHandler.isUserTeamChange(userToUpdate, existingUser)) {
//            userTeamChangeHandler.handleTeamChange(userToUpdate, existingUser.getTeam());
//        }
//
//        final User updatedUser = STORAGE.updateUser(userToUpdate);
//
//        if (userStateChangeHandler.isUserStateChange(updatedUser, existingUser)) {
//            userStateChangeHandler.handleStateChange(updatedUser);
//            LOGGER.trace("User updated with required state change");
//        }
//
//        return updatedUser;
//    }
//
//    @Override
//    public void deleteUser(final User user) {
//        // Retrieve the user's stats before deleting the user, so we can use the values for the retried user stats
//        final UserTcStats userStats = getHourlyTcStats(user);
//        STORAGE.deleteUser(user.getId());
//
//        if (userStats.isEmptyStats()) {
//            LOGGER.warn("User '{}' (ID: {}) has no stats, not saving any retired stats", user.getDisplayName(), user.getId());
//            return;
//        }
//
//        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.createWithoutId(user.getTeam().getId(), user.getDisplayName(), userStats);
//        final RetiredUserTcStats createdRetiredUserTcStats = STORAGE.createRetiredUserStats(retiredUserTcStats);
//        LOGGER.info("User '{}' (ID: {}) retired with retired stats ID: {}", user.getDisplayName(), user.getId(),
//            createdRetiredUserTcStats.getRetiredUserId());
//    }
//
//    @Override
//    public Collection<User> getUsersOnTeam(final Team team) {
//        if (team.getId() == Team.EMPTY_TEAM_ID) {
//            return Collections.emptyList();
//        }
//
//        return getUsersOnTeamWithPasskeys(team)
//            .stream()
//            .map(User::hidePasskey)
//            .collect(toList());
//    }
//
//    @Override
//    public Collection<User> getUsersOnTeamWithPasskeys(final Team team) {
//        if (team.getId() == Team.EMPTY_TEAM_ID) {
//            return Collections.emptyList();
//        }
//
//        return getAllUsersWithPasskeys()
//            .stream()
//            .filter(user -> user.getTeam().getId() == team.getId())
//            .collect(toList());
//    }
//
//    @Override
//    public MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult) {
//        return STORAGE.createMonthlyResult(monthlyResult);
//    }
//
//    @Override
//    public Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year) {
//        return STORAGE.getMonthlyResult(month, year);
//    }

    @Override
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        final UserAuthenticationResult userAuthenticationResult = storageService.authenticateSystemUser(userName, password);
//        final UserAuthenticationResult userAuthenticationResult = UserAuthenticationResult.success(Set.of("admin"));

        if (userAuthenticationResult.isUserExists() && userAuthenticationResult.isPasswordMatch()) {
            LOGGER.debug("System user '{}' successfully logged in", userName);
        } else {
            LOGGER.debug("Error authenticating system user '{}': {}", userName, userAuthenticationResult);
        }

        return userAuthenticationResult;
    }

//    @Override
//    public Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month, final int day) {
//        final Collection<HistoricStats> historicStats = STORAGE.getHistoricStats(user.getId(), year, month, day);
//        if (historicStats.isEmpty()) {
//            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}/{}, returning empty", user.getId(), year.getValue(), month.getValue(), day);
//        }
//
//        return historicStats;
//    }
//
//    @Override
//    public Collection<HistoricStats> getHistoricStats(final User user, final Year year, final Month month) {
//        final Collection<HistoricStats> historicStats = STORAGE.getHistoricStats(user.getId(), year, month, 0);
//        if (historicStats.isEmpty()) {
//            LOGGER.warn("No stats retrieved for user with ID {} on {}/{}, returning empty", user.getId(), year.getValue(), month.getValue());
//        }
//
//        return historicStats;
//    }
//
//    @Override
//    public Collection<HistoricStats> getHistoricStats(final User user, final Year year) {
//        final Collection<HistoricStats> historicStats = STORAGE.getHistoricStats(user.getId(), year, null, 0);
//        if (historicStats.isEmpty()) {
//            LOGGER.warn("No stats retrieved for user with ID {} on {}, returning empty", user.getId(), year.getValue());
//        }
//
//        return historicStats;
//    }
//
//    @Override
//    public UserStats createTotalStats(final UserStats userStats) {
//        return STORAGE.createTotalStats(userStats);
//    }
//
//    @Override
//    public UserStats getTotalStats(final User user) {
//        return STORAGE.getTotalStats(user.getId())
//            .orElse(UserStats.empty());
//    }
//
//    @Override
//    public OffsetTcStats createOffsetStats(final User user, final OffsetTcStats offsetTcStats) {
//        STORAGE.deleteOffsetStats(user.getId());
//        return STORAGE.createOrUpdateOffsetStats(user.getId(), offsetTcStats);
//    }
//
//    @Override
//    public OffsetTcStats createOrUpdateOffsetStats(final User user, final OffsetTcStats offsetTcStats) {
//        return STORAGE.createOrUpdateOffsetStats(user.getId(), offsetTcStats);
//    }
//
//    @Override
//    public OffsetTcStats getOffsetStats(final User user) {
//        return STORAGE.getOffsetStats(user.getId())
//            .orElse(OffsetTcStats.empty());
//    }
//
//    @Override
//    public UserTcStats createHourlyTcStats(final UserTcStats userTcStats) {
//        return STORAGE.createHourlyTcStats(userTcStats);
//    }
//
//    @Override
//    public UserTcStats getHourlyTcStats(final User user) {
//        return STORAGE.getHourlyTcStats(user.getId())
//            .orElse(UserTcStats.empty(user.getId()));
//    }
//
//    @Override
//    public boolean isAnyHourlyTcStatsExist() {
//        return STORAGE.getFirstHourlyTcStats().isPresent();
//    }
//
//    @Override
//    public UserStats createInitialStats(final UserStats userStats) {
//        return STORAGE.createInitialStats(userStats);
//    }
//
//    @Override
//    public UserStats getInitialStats(final User user) {
//        return STORAGE.getInitialStats(user.getId())
//            .orElse(UserStats.empty());
//    }
//
//    @Override
//    public RetiredUserTcStats createRetiredUserStats(final RetiredUserTcStats retiredUserTcStats) {
//        return STORAGE.createRetiredUserStats(retiredUserTcStats);
//    }
//
//    @Override
//    public void resetAllTeamCompetitionUserStats() {
//        for (final User user : getAllUsersWithoutPasskeys()) {
//            LOGGER.info("Resetting TC stats for {}", user.getDisplayName());
//            final UserStats totalStats = getTotalStats(user);
//            createInitialStats(totalStats);
//        }
//
//        LOGGER.info("Deleting retired user TC stats");
//        STORAGE.deleteAllRetiredUserTcStats();
//
//        LOGGER.info("Deleting offset TC stats");
//        STORAGE.deleteAllOffsetTcStats();
//
//        LOGGER.info("Evicting TC and initial stats caches");
//        STORAGE.evictTcStatsCache();
//        STORAGE.evictInitialStatsCache();
//    }
//
//    @Override
//    public CompetitionSummary getCompetitionSummary() {
//        if (SystemStateManager.current() != SystemState.WRITE_EXECUTED) {
//            LOGGER.debug("System is not in state {}, retrieving competition summary", SystemState.WRITE_EXECUTED);
//
//            final Optional<CompetitionSummary> cachedCompetitionResult = STORAGE.getCompetitionSummary();
//            if (cachedCompetitionResult.isPresent()) {
//                return cachedCompetitionResult.get();
//            }
//        }
//
//        LOGGER.debug("Calculating latest TC result, system state: {}", SystemStateManager.current());
//        final CompetitionSummary competitionSummary = createCompetitionSummary();
//        final CompetitionSummary createdCompetitionSummary = STORAGE.createCompetitionSummary(competitionSummary);
//        SystemStateManager.next(SystemState.AVAILABLE);
//
//        return createdCompetitionSummary;
//    }
//
//    private CompetitionSummary createCompetitionSummary() {
//        final List<TeamSummary> teamSummaries = getStatsForTeams();
//        LOGGER.debug("Found {} TC teams", teamSummaries::size);
//
//        if (teamSummaries.isEmpty()) {
//            LOGGER.warn("No TC teams to show");
//        }
//
//        return CompetitionSummary.create(teamSummaries);
//    }
//
//    private List<TeamSummary> getStatsForTeams() {
//        return getAllTeams()
//            .stream()
//            .map(this::getTcTeamResult)
//            .collect(toList());
//    }
//
//    private TeamSummary getTcTeamResult(final Team team) {
//        LOGGER.debug("Converting team '{}' for TC stats", team::getTeamName);
//
//        final Collection<User> usersOnTeam = getUsersOnTeam(team);
//
//        final Collection<UserSummary> activeUserSummaries = usersOnTeam
//            .stream()
//            .map(this::getTcStatsForUser)
//            .collect(toList());
//
//        final Collection<RetiredUserSummary> retiredUserSummaries = getAllRetiredUsersForTeam(team)
//            .stream()
//            .map(RetiredUserSummary::createWithDefaultRank)
//            .collect(toList());
//
//        final String captainDisplayName = getCaptainDisplayName(team.getTeamName(), usersOnTeam);
//        return TeamSummary.createWithDefaultRank(team, captainDisplayName, activeUserSummaries, retiredUserSummaries);
//    }
//
//    private Collection<RetiredUserTcStats> getAllRetiredUsersForTeam(final Team team) {
//        return STORAGE.getAllRetiredUsers()
//            .stream()
//            .filter(retiredUserTcStats -> retiredUserTcStats.getTeamId() == team.getId())
//            .collect(toList());
//    }
//
//    private UserSummary getTcStatsForUser(final User user) {
//        final UserTcStats userTcStats = getHourlyTcStats(user);
//        LOGGER.debug("Results for {}: {} points | {} multiplied points | {} units", user::getDisplayName, userTcStats::getPoints,
//            userTcStats::getMultipliedPoints, userTcStats::getUnits);
//        return UserSummary.createWithDefaultRank(user, userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits());
//    }
//
//    private static String getCaptainDisplayName(final String teamName, final Collection<User> usersOnTeam) {
//        for (final User user : usersOnTeam) {
//            if (user.isUserIsCaptain()) {
//                return user.getDisplayName();
//            }
//        }
//
//        LOGGER.warn("No captain set for team '{}'", teamName);
//        return null;
//    }
//
//    @Override
//    public void printCacheContents() {
//        STORAGE.printCacheContents();
//    }
}
