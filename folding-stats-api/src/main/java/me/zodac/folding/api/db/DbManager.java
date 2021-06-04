package me.zodac.folding.api.db;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.HardwareNotFoundException;
import me.zodac.folding.api.tc.exception.NoStatsAvailableException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;

import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;

/**
 * Interface used to interact with the storage backend and perform CRUD operations.
 */
public interface DbManager {

    /**
     * Creates a {@link Hardware} instance in the DB.
     *
     * @param hardware the {@link Hardware} to persist
     * @return the {@link Hardware} updated with an ID
     * @throws FoldingException thrown on error persisting the {@link Hardware}
     */
    Hardware createHardware(final Hardware hardware) throws FoldingException;

    Collection<Hardware> getAllHardware() throws FoldingException;

    Optional<Hardware> getHardware(final int hardwareId) throws FoldingException;

    void updateHardware(final Hardware hardware) throws FoldingException, HardwareNotFoundException;

    void deleteHardware(final int hardwareId) throws FoldingException;

    Team createTeam(final Team team) throws FoldingException;

    Collection<Team> getAllTeams() throws FoldingException;

    Optional<Team> getTeam(final int foldingTeamId) throws FoldingException;

    void updateTeam(final Team team) throws FoldingException;

    void deleteTeam(final int teamId) throws FoldingException;

    User createUser(final User user) throws FoldingException;

    Collection<User> getAllUsers() throws FoldingException;

    Optional<User> getUser(final int userId) throws FoldingException;

    void updateUser(final User user) throws FoldingException, UserNotFoundException;

    void deleteUser(final int userId) throws FoldingException;

    // TC operations

    void persistHourlyTcStats(final UserTcStats userTcStats) throws FoldingException;

    boolean isAnyHourlyTcStats() throws FoldingException;

    // Historic TC operations

    Collection<HistoricStats> getHistoricStatsHourly(final int userId, final int day, final Month month, final Year year) throws FoldingException, NoStatsAvailableException;

    Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Month month, final Year year) throws FoldingException, NoStatsAvailableException;

    Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year) throws FoldingException;

    void persistInitialStats(final UserStats userStats) throws FoldingException;

    Optional<UserStats> getInitialStats(final int userId) throws FoldingException;

    Optional<UserTcStats> getHourlyTcStats(final int userId) throws FoldingException;

    void persistTotalStats(final UserStats stats) throws FoldingException;

    Optional<UserStats> getTotalStats(final int userId) throws FoldingException;

    void addOffsetStats(int userId, OffsetStats offsetStats) throws FoldingException;

    Optional<OffsetStats> addOrUpdateOffsetStats(final int userId, final OffsetStats offsetStats) throws FoldingException;

    Optional<OffsetStats> getOffsetStats(final int userId) throws FoldingException;

    void clearAllOffsetStats() throws FoldingException;

    int persistRetiredUserStats(final int teamId, final int userId, final String displayUserName, final UserTcStats retiredUserStats) throws FoldingException;

    Collection<RetiredUserTcStats> getRetiredUserStatsForTeam(final Team team) throws FoldingException;

    void deleteRetiredUserStats() throws FoldingException;

    SystemUserAuthentication authenticateSystemUser(final String userName, final String password) throws FoldingException;
}
