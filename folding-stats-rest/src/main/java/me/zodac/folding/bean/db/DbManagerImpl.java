/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.bean.db;

import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.db.DbManager;
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
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link DbManager} which essentially wraps the response from {@link DbManagerRetriever#get()}. Used so we can inject an instance
 * instead of creating an instance of {@link DbManager}.
 */
@Component
public class DbManagerImpl implements DbManager {

    private static final DbManager DB_MANAGER = DbManagerRetriever.get();

    @Override
    public Hardware createHardware(final Hardware hardware) {
        return DB_MANAGER.createHardware(hardware);
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return DB_MANAGER.getAllHardware();
    }

    @Override
    public Optional<Hardware> getHardware(final int hardwareId) {
        return DB_MANAGER.getHardware(hardwareId);
    }

    @Override
    public Hardware updateHardware(final Hardware hardwareToUpdate) {
        return DB_MANAGER.updateHardware(hardwareToUpdate);
    }

    @Override
    public void deleteHardware(final int hardwareId) {
        DB_MANAGER.deleteHardware(hardwareId);
    }

    @Override
    public Team createTeam(final Team team) {
        return DB_MANAGER.createTeam(team);
    }

    @Override
    public Collection<Team> getAllTeams() {
        return DB_MANAGER.getAllTeams();
    }

    @Override
    public Optional<Team> getTeam(final int teamId) {
        return DB_MANAGER.getTeam(teamId);
    }

    @Override
    public Team updateTeam(final Team teamToUpdate) {
        return DB_MANAGER.updateTeam(teamToUpdate);
    }

    @Override
    public void deleteTeam(final int teamId) {
        DB_MANAGER.deleteTeam(teamId);
    }

    @Override
    public User createUser(final User user) {
        return DB_MANAGER.createUser(user);
    }

    @Override
    public Collection<User> getAllUsers() {
        return DB_MANAGER.getAllUsers();
    }

    @Override
    public Optional<User> getUser(final int userId) {
        return DB_MANAGER.getUser(userId);
    }

    @Override
    public User updateUser(final User userToUpdate) {
        return DB_MANAGER.updateUser(userToUpdate);
    }

    @Override
    public void deleteUser(final int userId) {
        DB_MANAGER.deleteUser(userId);
    }

    @Override
    public UserChange createUserChange(final UserChange userChange) {
        return DB_MANAGER.createUserChange(userChange);
    }

    @Override
    public Collection<UserChange> getAllUserChanges(final Collection<UserChangeState> states, final long numberOfMonths) {
        return DB_MANAGER.getAllUserChanges(states, numberOfMonths);
    }

    @Override
    public Optional<UserChange> getUserChange(final int userChangeId) {
        return DB_MANAGER.getUserChange(userChangeId);
    }

    @Override
    public UserChange updateUserChange(final UserChange userChangeToUpdate) {
        return DB_MANAGER.updateUserChange(userChangeToUpdate);
    }

    @Override
    public UserTcStats createHourlyTcStats(final UserTcStats userTcStats) {
        return DB_MANAGER.createHourlyTcStats(userTcStats);
    }

    @Override
    public Optional<UserTcStats> getHourlyTcStats(final int userId) {
        return DB_MANAGER.getHourlyTcStats(userId);
    }

    @Override
    public Collection<HistoricStats> getHistoricStatsHourly(final int userId, final Year year, final Month month, final int day) {
        return DB_MANAGER.getHistoricStatsHourly(userId, year, month, day);
    }

    @Override
    public Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Year year, final Month month) {
        return DB_MANAGER.getHistoricStatsDaily(userId, year, month);
    }

    @Override
    public Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year) {
        return DB_MANAGER.getHistoricStatsMonthly(userId, year);
    }

    @Override
    public UserStats createInitialStats(final UserStats userStats) {
        return DB_MANAGER.createInitialStats(userStats);
    }

    @Override
    public Optional<UserStats> getInitialStats(final int userId) {
        return DB_MANAGER.getInitialStats(userId);
    }

    @Override
    public UserStats createTotalStats(final UserStats userStats) {
        return DB_MANAGER.createTotalStats(userStats);
    }

    @Override
    public Optional<UserStats> getTotalStats(final int userId) {
        return DB_MANAGER.getTotalStats(userId);
    }

    @Override
    public OffsetTcStats createOrUpdateOffsetStats(final int userId, final OffsetTcStats offsetTcStats) {
        return DB_MANAGER.createOrUpdateOffsetStats(userId, offsetTcStats);
    }

    @Override
    public Optional<OffsetTcStats> getOffsetStats(final int userId) {
        return DB_MANAGER.getOffsetStats(userId);
    }

    @Override
    public void deleteOffsetStats(final int userId) {
        DB_MANAGER.deleteOffsetStats(userId);
    }

    @Override
    public void deleteAllOffsetStats() {
        DB_MANAGER.deleteAllOffsetStats();
    }

    @Override
    public RetiredUserTcStats createRetiredUserStats(final RetiredUserTcStats retiredUserTcStats) {
        return DB_MANAGER.createRetiredUserStats(retiredUserTcStats);
    }

    @Override
    public Collection<RetiredUserTcStats> getAllRetiredUserStats() {
        return DB_MANAGER.getAllRetiredUserStats();
    }

    @Override
    public void deleteAllRetiredUserStats() {
        DB_MANAGER.deleteAllRetiredUserStats();
    }

    @Override
    public MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult) {
        return DB_MANAGER.createMonthlyResult(monthlyResult);
    }

    @Override
    public Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year) {
        return DB_MANAGER.getMonthlyResult(month, year);
    }

    @Override
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        return DB_MANAGER.authenticateSystemUser(userName, password);
    }
}
