package me.zodac.folding.db.postgres;

import me.zodac.folding.api.db.DbConnectionPool;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.db.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.HardwareNotFoundException;
import me.zodac.folding.api.tc.exception.TeamNotFoundException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.DateTimeUtils;
import me.zodac.folding.rest.api.tc.historic.DailyStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PostgresDbManager implements DbManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDbManager.class);
    private static final String VIOLATES_FOREIGN_KEY_CONSTRAINT = "violates foreign key constraint";
    private static final String VIOLATES_UNIQUE_CONSTRAINT = "violates unique constraint";

    private final DbConnectionPool dbConnectionPool;

    private PostgresDbManager(final DbConnectionPool dbConnectionPool) {
        this.dbConnectionPool = dbConnectionPool;
    }

    public static PostgresDbManager create(final DbConnectionPool dbConnectionPool) {
        return new PostgresDbManager(dbConnectionPool);
    }

    @Override
    public Hardware createHardware(final Hardware hardware) throws FoldingException, FoldingConflictException {
        final String insertSqlWithReturnId = "INSERT INTO hardware (hardware_name, display_name, operating_system, multiplier) VALUES (?, ?, ?, ?) RETURNING hardware_id;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(insertSqlWithReturnId)) {

            preparedStatement.setString(1, hardware.getHardwareName());
            preparedStatement.setString(2, hardware.getDisplayName());
            preparedStatement.setString(3, hardware.getOperatingSystem());
            preparedStatement.setDouble(4, hardware.getMultiplier());

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final int hardwareId = resultSet.getInt("hardware_id");
                    return Hardware.updateWithId(hardwareId, hardware);
                }
                throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
            }
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_UNIQUE_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public List<Hardware> getAllHardware() throws FoldingException {
        final String selectSqlStatement = "SELECT * FROM hardware ORDER BY hardware_id ASC;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            final List<Hardware> allHardware = new ArrayList<>();

            while (resultSet.next()) {
                allHardware.add(createHardware(resultSet));
            }

            return allHardware;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Hardware getHardware(final int hardwareId) throws FoldingException, HardwareNotFoundException {
        final String selectSqlStatement = "SELECT * FROM hardware WHERE hardware_id = ?;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, hardwareId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return createHardware(resultSet);
                }
            }

            throw new HardwareNotFoundException(hardwareId);
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void updateHardware(final Hardware hardware) throws FoldingException, FoldingConflictException {
        final String updateSqlStatement = "UPDATE hardware " +
                "SET hardware_name = ?, display_name = ?, operating_system = ?, multiplier = ? " +
                "WHERE hardware_id = ?;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(updateSqlStatement)) {

            preparedStatement.setString(1, hardware.getHardwareName());
            preparedStatement.setString(2, hardware.getDisplayName());
            preparedStatement.setString(3, hardware.getOperatingSystem());
            preparedStatement.setDouble(4, hardware.getMultiplier());
            preparedStatement.setInt(5, hardware.getId());

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            if (preparedStatement.executeUpdate() == 0) {
                throw new FoldingException(String.format("Error executing update for hardware: %s", preparedStatement));
            }
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_UNIQUE_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void deleteHardware(final int hardwareId) throws FoldingException, FoldingConflictException {
        final String deleteSqlStatement = "DELETE FROM hardware WHERE hardware_id = ?;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(deleteSqlStatement)) {

            preparedStatement.setInt(1, hardwareId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_FOREIGN_KEY_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public User createUser(final User user) throws FoldingException, FoldingConflictException {
        final String insertSqlWithReturnId = "INSERT INTO users (folding_username, display_username, passkey, category, hardware_id, live_stats_link, is_retired) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING user_id;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(insertSqlWithReturnId)) {

            preparedStatement.setString(1, user.getFoldingUserName());
            preparedStatement.setString(2, user.getDisplayName());
            preparedStatement.setString(3, user.getPasskey());
            preparedStatement.setString(4, user.getCategory());
            preparedStatement.setInt(5, user.getHardwareId());
            preparedStatement.setString(6, user.getLiveStatsLink());
            preparedStatement.setBoolean(7, user.isRetired());

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final int foldingUserId = resultSet.getInt("user_id");
                    return User.updateWithId(foldingUserId, user);
                }
            }
            throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_UNIQUE_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public List<User> getAllUsers() throws FoldingException {
        final String selectSqlStatement = "SELECT * FROM users ORDER BY user_id ASC;";
        LOGGER.debug("Executing SQL statement: '{}'", selectSqlStatement);

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            final List<User> allUsers = new ArrayList<>();

            while (resultSet.next()) {
                allUsers.add(createUser(resultSet));
            }

            return allUsers;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public User getUser(final int userId) throws FoldingException, UserNotFoundException {
        final String selectSqlStatement = "SELECT * FROM users WHERE user_id = ?;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return createUser(resultSet);
                }

                throw new UserNotFoundException(userId);
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void updateUser(final User user) throws FoldingException, FoldingConflictException {
        final String updateSqlStatement = "UPDATE users " +
                "SET folding_username = ?, display_username = ?, passkey = ?, category = ?, hardware_id = ?, live_stats_link = ?, is_retired = ? " +
                "WHERE user_id = ?;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(updateSqlStatement)) {

            preparedStatement.setString(1, user.getFoldingUserName());
            preparedStatement.setString(2, user.getDisplayName());
            preparedStatement.setString(3, user.getPasskey());
            preparedStatement.setString(4, user.getCategory());
            preparedStatement.setInt(5, user.getHardwareId());
            preparedStatement.setString(6, user.getLiveStatsLink());
            preparedStatement.setBoolean(7, user.isRetired());
            preparedStatement.setInt(8, user.getId());

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            if (preparedStatement.executeUpdate() == 0) {
                throw new FoldingException(String.format("Error executing update for user: %s", preparedStatement));
            }
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_UNIQUE_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void deleteUser(final int userId) throws FoldingException, FoldingConflictException {
        final String deleteSqlStatement = "DELETE FROM users WHERE user_id = ?;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(deleteSqlStatement)) {

            preparedStatement.setInt(1, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_FOREIGN_KEY_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Team createTeam(final Team team) throws FoldingException, FoldingConflictException {
        final String insertSqlWithReturnId = "INSERT INTO teams (team_name, team_description, captain_user_id, user_ids, retired_user_ids) VALUES (?, ?, ?, ?, ?) RETURNING team_id;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(insertSqlWithReturnId)) {

            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setString(2, team.getTeamDescription());
            preparedStatement.setInt(3, team.getCaptainUserId());
            preparedStatement.setArray(4, connection.createArrayOf("INT", team.getUserIds().toArray(new Integer[0])));
            preparedStatement.setArray(5, connection.createArrayOf("INT", team.getRetiredUserIds().toArray(new Integer[0])));

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final int foldingTeamId = resultSet.getInt("team_id");
                    return Team.updateWithId(foldingTeamId, team);
                }

                throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
            }
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_UNIQUE_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public List<Team> getAllTeams() throws FoldingException {
        final String selectSqlStatement = "SELECT * FROM teams ORDER BY team_id ASC;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement); final ResultSet resultSet = preparedStatement.executeQuery()) {

            final List<Team> allTeams = new ArrayList<>();

            while (resultSet.next()) {
                allTeams.add(createTeam(resultSet));
            }

            return allTeams;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Team getTeam(final int teamId) throws FoldingException, TeamNotFoundException {
        final String selectSqlStatement = "SELECT * FROM teams WHERE team_id = ?;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, teamId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return createTeam(resultSet);
                }

                throw new TeamNotFoundException(teamId);
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void updateTeam(final Team team) throws FoldingException, FoldingConflictException {
        final String updateSqlStatement = "UPDATE teams " +
                "SET team_name = ?, team_description = ?, captain_user_id = ?, user_ids = ?, retired_user_ids = ? " +
                "WHERE team_id = ?;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(updateSqlStatement)) {

            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setString(2, team.getTeamDescription());
            preparedStatement.setInt(3, team.getCaptainUserId());
            preparedStatement.setArray(4, connection.createArrayOf("INT", team.getUserIds().toArray(new Integer[0])));
            preparedStatement.setArray(5, connection.createArrayOf("INT", team.getRetiredUserIds().toArray(new Integer[0])));
            preparedStatement.setInt(6, team.getId());

            LOGGER.debug("Executing SQL statement '{}'", preparedStatement);
            if (preparedStatement.executeUpdate() == 0) {
                throw new FoldingException(String.format("Error executing update for team: %s", preparedStatement));
            }
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_UNIQUE_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void deleteTeam(final int teamId) throws FoldingException, FoldingConflictException {
        LOGGER.debug("Deleting team {} from DB", teamId);
        final String deleteSqlStatement = "DELETE FROM teams WHERE team_id = ?;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(deleteSqlStatement)) {

            preparedStatement.setInt(1, teamId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_FOREIGN_KEY_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void persistHourlyTcStats(final UserTcStats userTcStats) throws FoldingException {
        LOGGER.debug("Inserting TC stats for user ID: {}", userTcStats.getUserId());
        final String preparedInsertSqlStatement = "INSERT INTO user_tc_stats_hourly (user_id, utc_timestamp, tc_points, tc_points_multiplied, tc_units) VALUES (?, ?, ?, ?, ?);";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            try {
                preparedStatement.setInt(1, userTcStats.getUserId());
                preparedStatement.setTimestamp(2, userTcStats.getTimestamp());
                preparedStatement.setLong(3, userTcStats.getPoints());
                preparedStatement.setLong(4, userTcStats.getMultipliedPoints());
                preparedStatement.setInt(5, userTcStats.getUnits());

                LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
                preparedStatement.execute();
            } catch (final SQLException e) {
                throw new FoldingException(String.format("Unable to persist TC stats for user: %s", userTcStats), e);
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public UserTcStats getHourlyTcStats(final int userId) throws FoldingException, UserNotFoundException {
        LOGGER.debug("Getting current TC stats for user {}", userId);
        final String preparedSelectSqlStatement = "SELECT utc_timestamp, tc_points, tc_points_multiplied, tc_units " +
                "FROM user_tc_stats_hourly " +
                "WHERE user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedSelectSqlStatement)) {

            preparedStatement.setInt(1, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return UserTcStats.create(userId, resultSet.getTimestamp("utc_timestamp"), resultSet.getLong("tc_points"), resultSet.getLong("tc_points_multiplied"), resultSet.getInt("tc_units"));
                }
            } catch (final SQLException e) {
                LOGGER.warn("Unable to get TC stats for user: {}", userId, e);
            }
            throw new UserNotFoundException(userId);
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public boolean isAnyHourlyTcStats() throws FoldingException {
        LOGGER.debug("Checking if any TC stats exist in the DB");
        final String selectSqlStatement = "SELECT COUNT(*) AS count FROM user_tc_stats_hourly;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("count") > 0;
                }

                return false;
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public List<DailyStats> getTcUserStatsByDay(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        LOGGER.debug("Getting historic daily user TC stats for {}/{} for user {}", DateTimeUtils.formatMonth(month), year, userId);

        final String selectSqlStatement = "SELECT utc_timestamp::DATE AS daily_timestamp, " +
                "COALESCE(MAX(tc_points) - LAG(MAX(tc_points)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points, " +
                "COALESCE(MAX(tc_points_multiplied) - LAG(MAX(tc_points_multiplied)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points_multiplied, " +
                "COALESCE(MAX(tc_units) - LAG(MAX(tc_units)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_units " +
                "FROM user_tc_stats_hourly " +
                "WHERE EXTRACT(MONTH FROM utc_timestamp) = ? " +
                "AND EXTRACT(YEAR FROM utc_timestamp) = ? " +
                "AND user_id = ? " +
                "GROUP BY utc_timestamp::DATE " +
                "ORDER BY utc_timestamp::DATE ASC;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, month.getValue());
            preparedStatement.setInt(2, year.getValue());
            preparedStatement.setInt(3, userId);

            final List<DailyStats> userStats = new ArrayList<>();

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                // First entry will be zeroed, so we need to manually get the first day's stats for the user
                if (resultSet.next()) {
                    final Timestamp timestamp = resultSet.getTimestamp("daily_timestamp");
                    final LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
                    final UserTcStats userTcStats = getTcStatsForDay(localDate, userId);

                    userStats.add(DailyStats.create(
                            timestamp.toLocalDateTime().toLocalDate(),
                            userTcStats.getPoints(),
                            userTcStats.getMultipliedPoints(),
                            userTcStats.getUnits()
                            )
                    );
                }

                // All remaining entries will be diff-ed from the previous entry
                while (resultSet.next()) {
                    final Timestamp timestamp = resultSet.getTimestamp("daily_timestamp");
                    userStats.add(DailyStats.create(
                            timestamp.toLocalDateTime().toLocalDate(),
                            resultSet.getLong("diff_points"),
                            resultSet.getLong("diff_points_multiplied"),
                            resultSet.getInt("diff_units")
                            )
                    );
                }

                if (userStats.isEmpty()) {
                    throw new UserNotFoundException(userId);
                }

                return userStats;
            }
        } catch (final FoldingException | UserNotFoundException e) {
            LOGGER.warn("Unable to get the stats for the first day of {}/{} for user {}", DateTimeUtils.formatMonth(month), year, userId);
            throw e;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Map<LocalDate, UserTcStats> getTcUserStatsByMonth(final int userId, final Year year) throws FoldingException, UserNotFoundException {
        LOGGER.debug("Getting historic monthly user TC stats for {} for user {}", year, userId);

        final String selectSqlStatement = "SELECT MAX(utc_timestamp) AS month_timestamp, " +
                "MAX(tc_points) AS diff_points, " +
                "MAX(tc_points_multiplied) AS diff_points_multiplied, " +
                "MAX(tc_units) AS diff_units " +
                "FROM user_tc_stats_hourly " +
                "WHERE EXTRACT(YEAR FROM utc_timestamp) = ? " +
                "AND user_id = ? " +
                "GROUP BY EXTRACT(MONTH FROM utc_timestamp)::INT " +
                "ORDER BY EXTRACT(MONTH FROM utc_timestamp)::INT ASC;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, year.getValue());
            preparedStatement.setInt(2, userId);

            final Map<LocalDate, UserTcStats> userStatsByDate = new TreeMap<>();

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    final Timestamp timestamp = resultSet.getTimestamp("month_timestamp");
                    userStatsByDate.put(timestamp.toLocalDateTime().toLocalDate(),
                            UserTcStats.create(
                                    userId, timestamp,
                                    resultSet.getLong("diff_points"),
                                    resultSet.getLong("diff_points_multiplied"),
                                    resultSet.getInt("diff_units")
                            )
                    );
                }

                if (userStatsByDate.isEmpty()) {
                    throw new UserNotFoundException(userId);
                }

                return userStatsByDate;
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    private UserTcStats getTcStatsForDay(final LocalDate localDate, final int userId) throws UserNotFoundException, FoldingException {
        LOGGER.debug("Getting TC stats for user {} on {}", userId, localDate);
        final String preparedSelectSqlStatement = "SELECT utc_timestamp, tc_points, tc_points_multiplied, tc_units " +
                "FROM user_tc_stats_hourly " +
                "WHERE EXTRACT(DAY FROM utc_timestamp) = ? " +
                "AND EXTRACT(MONTH FROM utc_timestamp) = ? " +
                "AND EXTRACT(YEAR FROM utc_timestamp) = ? " +
                "AND user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedSelectSqlStatement)) {

            preparedStatement.setInt(1, localDate.getDayOfMonth());
            preparedStatement.setInt(2, localDate.getMonth().getValue());
            preparedStatement.setInt(3, localDate.getYear());
            preparedStatement.setInt(4, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return UserTcStats.create(userId, resultSet.getTimestamp("utc_timestamp"), resultSet.getLong("tc_points"), resultSet.getLong("tc_points_multiplied"), resultSet.getInt("tc_units"));
                }
            } catch (final SQLException e) {
                LOGGER.warn("Unable to get TC stats for user: {}", userId, e);
            }
            throw new UserNotFoundException(userId);
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void persistInitialStats(final UserStats userStats) throws FoldingException {
        LOGGER.debug("Inserting initial stats for user {} to DB", userStats.getUserId());
        final String preparedInsertSqlStatement = "INSERT INTO user_initial_stats (user_id, utc_timestamp, initial_points, initial_units) VALUES (?, ?, ?, ?);";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            try {
                preparedStatement.setInt(1, userStats.getUserId());
                preparedStatement.setTimestamp(2, userStats.getTimestamp());
                preparedStatement.setLong(3, userStats.getPoints());
                preparedStatement.setInt(4, userStats.getUnits());

                LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
                preparedStatement.execute();
            } catch (final SQLException e) {
                LOGGER.warn("Unable to persist initial stats for user: {}", userStats, e);
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public UserStats getInitialStats(final int userId) throws FoldingException {
        LOGGER.debug("Getting initial stats for user ID: {}", userId);
        final String preparedInsertSqlStatement = "SELECT utc_timestamp, initial_points, initial_units " +
                "FROM user_initial_stats " +
                "WHERE user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            preparedStatement.setInt(1, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return UserStats.createWithPointsAndUnits(userId,
                            resultSet.getTimestamp("utc_timestamp"),
                            resultSet.getLong("initial_points"),
                            resultSet.getInt("initial_units")
                    );
                }

            } catch (final SQLException e) {
                throw new FoldingException("Unable to get initial stats for user ID: " + userId, e);
            }

            throw new FoldingException("No initial stats found for user ID: " + userId);
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void persistTotalStats(final UserStats stats) throws FoldingException {
        LOGGER.debug("Inserting total stats for user ID {} to DB", stats.getUserId());
        final String preparedInsertSqlStatement = "INSERT INTO user_total_stats (user_id, utc_timestamp, total_points, total_units) VALUES (?, ?, ?, ?);";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            try {
                preparedStatement.setInt(1, stats.getUserId());
                preparedStatement.setTimestamp(2, stats.getTimestamp());
                preparedStatement.setLong(3, stats.getPoints());
                preparedStatement.setInt(4, stats.getUnits());

                LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
                preparedStatement.execute();
            } catch (final SQLException e) {
                throw new FoldingException(String.format("Unable to persist total stats for user: %s", stats), e);
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public UserStats getTotalStats(final int userId) throws FoldingException {
        LOGGER.debug("Getting total stats for user ID: {}", userId);
        final String preparedSqlStatement = "SELECT utc_timestamp, total_points, total_units " +
                "FROM user_total_stats " +
                "WHERE user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedSqlStatement)) {
            preparedStatement.setInt(1, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            preparedStatement.execute();
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return UserStats.createWithPointsAndUnits(userId,
                            resultSet.getTimestamp("utc_timestamp"),
                            resultSet.getLong("total_points"),
                            resultSet.getInt("total_units")
                    );
                }
                throw new FoldingException("Could not find any total stats for user ID: " + userId);
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void addOffsetStats(final int userId, final OffsetStats offsetStats) throws FoldingException {
        LOGGER.debug("Adding offset stats for user {}", userId);
        final String preparedInsertSqlStatement = "INSERT INTO user_offset_tc_stats (user_id, utc_timestamp, offset_points, offset_multiplied_points, offset_units) VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (user_id) " +
                "DO UPDATE " +
                "SET utc_timestamp = ?, offset_points = ?, offset_multiplied_points = ?, offset_units = ?;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            final Timestamp currentUtcTimestamp = DateTimeUtils.currentUtcTimestamp();

            preparedStatement.setInt(1, userId);
            preparedStatement.setTimestamp(2, currentUtcTimestamp);
            preparedStatement.setLong(3, offsetStats.getPointsOffset());
            preparedStatement.setLong(4, offsetStats.getMultipliedPointsOffset());
            preparedStatement.setInt(5, offsetStats.getUnitsOffset());
            preparedStatement.setTimestamp(6, currentUtcTimestamp);
            preparedStatement.setLong(7, offsetStats.getPointsOffset());
            preparedStatement.setLong(8, offsetStats.getMultipliedPointsOffset());
            preparedStatement.setInt(9, offsetStats.getUnitsOffset());

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            preparedStatement.execute();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public OffsetStats addOrUpdateOffsetStats(final int userId, final OffsetStats offsetStats) throws FoldingException {
        LOGGER.debug("Adding/updating offset stats for user {}", userId);
        final String preparedInsertSqlStatement = "INSERT INTO user_offset_tc_stats (user_id, utc_timestamp, offset_points, offset_multiplied_points, offset_units) VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (user_id) " +
                "DO UPDATE " +
                "SET utc_timestamp = ?, offset_points = user_offset_tc_stats.offset_points + ?, offset_multiplied_points = user_offset_tc_stats.offset_multiplied_points + ?, offset_units = user_offset_tc_stats.offset_units + ? " +
                "RETURNING offset_points, offset_multiplied_points, offset_units;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            final Timestamp currentUtcTimestamp = DateTimeUtils.currentUtcTimestamp();

            preparedStatement.setInt(1, userId);
            preparedStatement.setTimestamp(2, currentUtcTimestamp);
            preparedStatement.setLong(3, offsetStats.getPointsOffset());
            preparedStatement.setLong(4, offsetStats.getMultipliedPointsOffset());
            preparedStatement.setInt(5, offsetStats.getUnitsOffset());
            preparedStatement.setTimestamp(6, currentUtcTimestamp);
            preparedStatement.setLong(7, offsetStats.getPointsOffset());
            preparedStatement.setLong(8, offsetStats.getMultipliedPointsOffset());
            preparedStatement.setInt(9, offsetStats.getUnitsOffset());

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return OffsetStats.create(resultSet.getLong("offset_points"), resultSet.getLong("offset_multiplied_points"), resultSet.getInt("offset_units"));
                }
                throw new FoldingException("Error inserting to offset stats");
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public OffsetStats getOffsetStats(final int userId) throws FoldingException {
        LOGGER.debug("Getting offset stats for user ID: {}", userId);
        final String preparedInsertSqlStatement = "SELECT offset_points, offset_multiplied_points, offset_units " +
                "FROM user_offset_tc_stats " +
                "WHERE user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            preparedStatement.setInt(1, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return OffsetStats.create(
                            resultSet.getLong("offset_points"),
                            resultSet.getLong("offset_multiplied_points"),
                            resultSet.getInt("offset_units"));
                }
            } catch (final SQLException e) {
                LOGGER.warn("Error getting offset stats for user: {}", userId, e);
            }

            LOGGER.debug("No result found for user ID {}, returning empty", userId);
            return OffsetStats.empty();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void clearAllOffsetStats() throws FoldingConflictException, FoldingException {
        LOGGER.debug("Clearing offset stats for all users");
        final String preparedInsertSqlStatement = "DELETE FROM user_offset_tc_stats;";
        LOGGER.debug("Executing prepared statement: '{}'", preparedInsertSqlStatement);

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            preparedStatement.executeUpdate();

        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_FOREIGN_KEY_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public int persistRetiredUserStats(final int teamId, final String displayUserName, final UserTcStats retiredUserStats) throws FoldingException {
        LOGGER.debug("Persisting retired user ID {} for team ID {}", retiredUserStats.getUserId(), teamId);
        final String preparedInsertSqlStatement = "INSERT INTO retired_user_stats (user_id, team_id, display_username, utc_timestamp, final_points, final_multiplied_points, final_units) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            try {
                preparedStatement.setInt(1, retiredUserStats.getUserId());
                preparedStatement.setInt(2, teamId);
                preparedStatement.setString(3, displayUserName);
                preparedStatement.setTimestamp(4, DateTimeUtils.currentUtcTimestamp());
                preparedStatement.setLong(5, retiredUserStats.getPoints());
                preparedStatement.setLong(6, retiredUserStats.getMultipliedPoints());
                preparedStatement.setInt(7, retiredUserStats.getUnits());

                LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                }
                throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
            } catch (final SQLException e) {
                LOGGER.warn("Unable to persist retired stats for user ID {} for team ID {}", retiredUserStats.getUserId(), teamId, e);
                throw new FoldingException("Error persisting retired stats", e);
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public RetiredUserTcStats getRetiredUserStats(final int retiredUserId) throws FoldingException {
        LOGGER.debug("Getting retired user with ID: {} ", retiredUserId);
        final String preparedInsertSqlStatement = "SELECT id, user_id, team_id, display_username, utc_timestamp, final_points, final_multiplied_points, final_units " +
                "FROM retired_user_stats " +
                "WHERE id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            try {
                preparedStatement.setInt(1, retiredUserId);

                LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return RetiredUserTcStats.create(
                                resultSet.getInt("id"),
                                resultSet.getInt("team_id"),
                                resultSet.getString("display_username"),
                                UserTcStats.create(
                                        resultSet.getInt("user_id"),
                                        resultSet.getTimestamp("utc_timestamp"),
                                        resultSet.getLong("final_points"),
                                        resultSet.getLong("final_multiplied_points"),
                                        resultSet.getInt("final_units")
                                )
                        );
                    }
                }
                throw new FoldingException("Unable to find retired stats for retired user ID: " + retiredUserId);
            } catch (final SQLException e) {
                throw new FoldingException("Error getting retired stats for retired user ID: " + retiredUserId, e);
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    private static Hardware createHardware(final ResultSet resultSet) throws SQLException {
        return Hardware.create(
                resultSet.getInt("hardware_id"),
                resultSet.getString("hardware_name"),
                resultSet.getString("display_name"),
                OperatingSystem.get(resultSet.getString("operating_system")),
                resultSet.getDouble("multiplier")
        );
    }

    private static User createUser(final ResultSet resultSet) throws SQLException {
        return User.create(
                resultSet.getInt("user_id"),
                resultSet.getString("folding_username"),
                resultSet.getString("display_username"),
                resultSet.getString("passkey"),
                Category.get(resultSet.getString("category")),
                resultSet.getInt("hardware_id"),
                resultSet.getString("live_stats_link"),
                resultSet.getBoolean("is_retired")
        );
    }

    private static Team createTeam(final ResultSet resultSet) throws SQLException {
        return Team.create(
                resultSet.getInt("team_id"),
                resultSet.getString("team_name"),
                resultSet.getString("team_description"),
                resultSet.getInt("captain_user_id"),
                Set.of((Integer[]) resultSet.getArray("user_ids").getArray()),
                Set.of((Integer[]) resultSet.getArray("retired_user_ids").getArray())
        );
    }
}
