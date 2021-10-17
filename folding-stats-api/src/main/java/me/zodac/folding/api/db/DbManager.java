package me.zodac.folding.api.db;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.exception.DatabaseConnectionException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;

/**
 * Interface used to interact with the storage backend and perform CRUD operations.
 */
// TODO: [zodac] Should #create*() functions return an object, or the ID and let the caller build the object?
public interface DbManager {

    /**
     * Creates a {@link Hardware} instance in the DB.
     *
     * @param hardware the {@link Hardware} to persist
     * @return the {@link Hardware} updated with an ID
     * @throws DatabaseConnectionException thrown on error persisting the {@link Hardware}
     */
    Hardware createHardware(final Hardware hardware);

    Collection<Hardware> getAllHardware();

    Optional<Hardware> getHardware(final int hardwareId);

    void updateHardware(final Hardware hardware);

    void deleteHardware(final int hardwareId);

    Team createTeam(final Team team);

    Collection<Team> getAllTeams();

    Optional<Team> getTeam(final int foldingTeamId);

    void updateTeam(final Team team);

    void deleteTeam(final int teamId);

    User createUser(final User user);

    Collection<User> getAllUsers();

    Optional<User> getUser(final int userId);

    void updateUser(final User user);

    void deleteUser(final int userId);

    // TC operations

    Optional<UserTcStats> persistHourlyTcStats(final UserTcStats userTcStats);

    Optional<UserTcStats> getHourlyTcStats(final int userId);

    Optional<UserTcStats> getFirstHourlyTcStats();

    // Historic TC operations

    Collection<HistoricStats> getHistoricStatsHourly(final int userId, final Year year, final Month month, final int day);

    Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Year year, final Month month);

    Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year);

    Optional<UserStats> createInitialStats(final UserStats userStats);

    Optional<UserStats> getInitialStats(final int userId);

    Optional<UserStats> createTotalStats(final UserStats stats);

    Optional<UserStats> getTotalStats(final int userId);

    Optional<OffsetTcStats> createOrUpdateOffsetStats(final int userId, final OffsetTcStats offsetTcStats);

    Optional<OffsetTcStats> getOffsetStats(final int userId);

    void deleteOffsetStats(final int userId);

    void deleteAllOffsetStats();

    int createRetiredUserStats(final int teamId, final int userId, final String displayUserName, final UserTcStats retiredUserStats);

    Collection<RetiredUserTcStats> getAllRetiredUserStats();

    void deleteAllRetiredUserStats();

    void createMonthlyResult(final String result, final LocalDateTime utcTimestamp);

    Optional<String> getMonthlyResult(final Month month, final Year year);

    UserAuthenticationResult authenticateSystemUser(final String userName, final String password);
}
