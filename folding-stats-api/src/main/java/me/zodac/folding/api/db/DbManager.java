package me.zodac.folding.api.db;

import me.zodac.folding.api.db.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.HardwareNotFoundException;
import me.zodac.folding.api.tc.exception.TeamNotFoundException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.rest.api.tc.historic.DailyStats;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface used to interact with the storage backend and perform CRUD operations.
 */
public interface DbManager {

    // CRUD operations

    /**
     * Creates a {@link Hardware} instance in the DB.
     *
     * @param hardware the {@link Hardware} to persist
     * @return the {@link Hardware} updated with an ID
     * @throws FoldingException thrown on error persisting the {@link Hardware}
     */
    Hardware createHardware(final Hardware hardware) throws FoldingException, FoldingConflictException;

    Collection<Hardware> getAllHardware() throws FoldingException;

    Hardware getHardware(final int hardwareId) throws FoldingException, HardwareNotFoundException;

    void updateHardware(final Hardware hardware) throws FoldingException, HardwareNotFoundException, FoldingConflictException;

    void deleteHardware(final int hardwareId) throws FoldingException, FoldingConflictException;

    User createUser(final User user) throws FoldingException, FoldingConflictException;

    Collection<User> getAllUsers() throws FoldingException;

    User getUser(final int userId) throws FoldingException, UserNotFoundException;

    void updateUser(final User user) throws FoldingException, UserNotFoundException, FoldingConflictException;

    void deleteUser(final int userId) throws FoldingException, FoldingConflictException;

    Team createTeam(final Team team) throws FoldingException, FoldingConflictException;

    Collection<Team> getAllTeams() throws FoldingException;

    Team getTeam(final int foldingTeamId) throws FoldingException, TeamNotFoundException;

    void updateTeam(final Team team) throws FoldingException, FoldingConflictException;

    void deleteTeam(final int teamId) throws FoldingException, FoldingConflictException;

    // TC operations

    void persistHourlyTcStats(final UserTcStats userTcStats) throws FoldingException;

    boolean isAnyHourlyTcStats() throws FoldingException;

    // Historic TC operations

    List<DailyStats> getTcUserStatsByDay(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException;

    Map<LocalDate, UserTcStats> getTcUserStatsByMonth(final int userId, final Year year) throws FoldingException, UserNotFoundException;

    void persistInitialStats(final UserStats userStats) throws FoldingException;

    UserStats getInitialStats(final int userId) throws FoldingException;

    UserTcStats getHourlyTcStats(final int userId) throws FoldingException, UserNotFoundException;

    void persistTotalStats(final UserStats stats) throws FoldingException;

    UserStats getTotalStats(final int userId) throws FoldingException;

    void addOffsetStats(int userId, OffsetStats offsetStats) throws FoldingException;

    OffsetStats addOrUpdateOffsetStats(final int userId, final OffsetStats offsetStats) throws FoldingException;

    OffsetStats getOffsetStats(final int userId) throws FoldingException;

    void clearAllOffsetStats() throws FoldingConflictException, FoldingException;

    int persistRetiredUserStats(final int teamId, final String displayUserName, final UserTcStats retiredUserStats) throws FoldingException;

    RetiredUserTcStats getRetiredUserStats(final int retiredUserId) throws FoldingException;
}
