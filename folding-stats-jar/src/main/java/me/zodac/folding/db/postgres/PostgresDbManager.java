package me.zodac.folding.db.postgres;

import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.HardwareNotFoundException;
import me.zodac.folding.api.exception.TeamNotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserStatsOffset;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.EnvironmentVariables;
import me.zodac.folding.api.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

// TODO: [zodac] Add some DB pooling here, can be made a bit less shit
public class PostgresDbManager implements DbManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDbManager.class);

    private static final String JDBC_CONNECTION_URL = EnvironmentVariables.get("JDBC_CONNECTION_URL");
    private static final Properties JDBC_CONNECTION_PROPERTIES = new Properties();
    private static final String VIOLATES_FOREIGN_KEY_CONSTRAINT = "violates foreign key constraint";
    private static final String VIOLATES_UNIQUE_CONSTRAINT = "violates unique constraint";

    static {
        JDBC_CONNECTION_PROPERTIES.setProperty("user", EnvironmentVariables.get("JDBC_CONNECTION_USER"));
        JDBC_CONNECTION_PROPERTIES.setProperty("password", EnvironmentVariables.get("JDBC_CONNECTION_PASSWORD"));
        JDBC_CONNECTION_PROPERTIES.setProperty("driver", EnvironmentVariables.get("JDBC_CONNECTION_DRIVER"));
    }

    @Override
    public Hardware createHardware(final Hardware hardware) throws FoldingException, FoldingConflictException {
        final String insertSqlWithReturnId = "INSERT INTO hardware (hardware_name, display_name, operating_system, multiplier) VALUES (?, ?, ?, ?) RETURNING hardware_id;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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
        final String selectSqlStatement = "SELECT * FROM hardware;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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
        final String selectSqlStatement = "SELECT * FROM users;";
        LOGGER.debug("Executing SQL statement: '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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
        final String selectSqlStatement = "SELECT * FROM teams;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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
    public void persistHourlyTcUserStats(final List<UserTcStats> userStats) throws FoldingException {
        LOGGER.debug("Inserting TC stats for {} users to DB", userStats.size());
        final String preparedInsertSqlStatement = "INSERT INTO user_tc_stats_hourly (user_id, utc_timestamp, tc_points, tc_points_multiplied, tc_units) VALUES (?, ?, ?, ?, ?);";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            for (final UserTcStats tcUserStatsForUser : userStats) {
                try {
                    preparedStatement.setInt(1, tcUserStatsForUser.getUserId());
                    preparedStatement.setTimestamp(2, tcUserStatsForUser.getTimestamp());
                    preparedStatement.setLong(3, tcUserStatsForUser.getPoints());
                    preparedStatement.setLong(4, tcUserStatsForUser.getMultipliedPoints());
                    preparedStatement.setInt(5, tcUserStatsForUser.getUnits());

                    LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
                    preparedStatement.execute();
                } catch (final SQLException e) {
                    LOGGER.warn("Unable to persist TC stats for user: {}", tcUserStatsForUser, e);
                }
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public boolean doTcStatsExist() throws FoldingException {
        LOGGER.debug("Checking if any TC stats exist in the DB");
        final String selectSqlStatement = "SELECT COUNT(*) AS count FROM user_tc_stats_hourly;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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
    public Map<LocalDate, UserTcStats> getDailyUserTcStats(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        LOGGER.debug("Getting historic daily user TC stats for {}/{} for user {}", StringUtils.capitalize(month.toString().toLowerCase(Locale.UK)), year, userId);

        final String selectSqlStatement = "SELECT utc_timestamp::DATE AS TIMESTAMP, " +
                "COALESCE(MAX(tc_points) - LAG(MAX(tc_points)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points, " +
                "COALESCE(MAX(tc_points_multiplied) - LAG(MAX(tc_points_multiplied)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points_multiplied, " +
                "COALESCE(MAX(tc_units) - LAG(MAX(tc_units)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_units " +
                "FROM user_tc_stats_hourly " +
                "WHERE EXTRACT(MONTH FROM utc_timestamp) = ? " +
                "AND EXTRACT(YEAR FROM utc_timestamp) = ? " +
                "AND user_id = ? " +
                "GROUP BY utc_timestamp::DATE " +
                "ORDER BY utc_timestamp::DATE ASC;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, month.getValue());
            preparedStatement.setInt(2, year.getValue());
            preparedStatement.setInt(3, userId);

            final Map<LocalDate, UserTcStats> userStatsByDate = new TreeMap<>();

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                // First entry will be zeroed, so we need to manually get the first day's stats for the user
                if (resultSet.next()) {
                    final Timestamp timestamp = resultSet.getTimestamp("timestamp");
                    final LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
                    final UserTcStats userTcStats = getTcStatsForDay(localDate, userId);

                    userStatsByDate.put(timestamp.toLocalDateTime().toLocalDate(),
                            UserTcStats.create(
                                    userId, timestamp,
                                    userTcStats.getPoints(),
                                    userTcStats.getMultipliedPoints(),
                                    userTcStats.getUnits()
                            )
                    );
                }

                // All remaining stats will be diff-ed from the previous entry
                while (resultSet.next()) {
                    final Timestamp timestamp = resultSet.getTimestamp("timestamp");
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
        } catch (final FoldingException | UserNotFoundException e) {
            LOGGER.warn("Unable to get the stats for the first day of {}/{} for user {}", StringUtils.capitalize(month.toString().toLowerCase(Locale.UK)), year, userId);
            throw e;
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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
    public void persistInitialUserStats(final UserStats userStats) throws FoldingException {
        LOGGER.debug("Inserting initial stats for user {} to DB", userStats.getUserId());
        final String preparedInsertSqlStatement = "INSERT INTO user_initial_stats (user_id, utc_timestamp, initial_points, initial_units) VALUES (?, ?, ?, ?);";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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
    public Stats getInitialUserStats(final int userId) throws FoldingException, UserNotFoundException {
        LOGGER.debug("Getting initial stats for user {}", userId);
        final Map<Integer, Stats> initialUserStats = getInitialUserStats(List.of(userId));

        if (initialUserStats.isEmpty() || !initialUserStats.containsKey(userId)) {
            throw new UserNotFoundException(userId);
        }

        return initialUserStats.get(userId);
    }

    @Override
    public Map<Integer, Stats> getInitialUserStats(final List<Integer> userIds) throws FoldingException {
        LOGGER.debug("Getting initial stats for {} users", userIds.size());
        final String preparedInsertSqlStatement = "SELECT utc_timestamp, initial_points, initial_units " +
                "FROM user_initial_stats " +
                "WHERE user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            final Map<Integer, Stats> initialStats = new HashMap<>(userIds.size());

            for (final int userId : userIds) {
                preparedStatement.setInt(1, userId);

                LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        initialStats.put(userId, Stats.create(resultSet.getLong("initial_points"), resultSet.getInt("initial_units")));
                    }
                } catch (final SQLException e) {
                    LOGGER.warn("Unable to get initial stats for user: {}", userId, e);
                }
            }
            return initialStats;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public UserTcStats getCurrentTcStats(final int userId) throws FoldingException, UserNotFoundException {
        LOGGER.debug("Getting current TC stats for user {}", userId);
        final String preparedSelectSqlStatement = "SELECT utc_timestamp, tc_points, tc_points_multiplied, tc_units " +
                "FROM user_tc_stats_hourly " +
                "WHERE user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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
    public void persistTotalUserStats(final List<UserStats> totalUserStats) throws FoldingException {
        LOGGER.debug("Inserting total stats for {} users to DB", totalUserStats.size());
        final String preparedInsertSqlStatement = "INSERT INTO user_total_stats (user_id, utc_timestamp, total_points, total_units) VALUES (?, ?, ?, ?);";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            for (final UserStats totalStatsForUser : totalUserStats) {
                try {
                    preparedStatement.setInt(1, totalStatsForUser.getUserId());
                    preparedStatement.setTimestamp(2, totalStatsForUser.getTimestamp());
                    preparedStatement.setLong(3, totalStatsForUser.getPoints());
                    preparedStatement.setInt(4, totalStatsForUser.getUnits());

                    LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
                    preparedStatement.execute();
                } catch (final SQLException e) {
                    LOGGER.warn("Unable to persist total stats for user: {}", totalStatsForUser, e);
                }
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Stats getTotalStats(final int userId) throws FoldingException {
        LOGGER.debug("Getting total stats for user ID: {}", userId);
        final String preparedSqlStatement = "SELECT total_points, total_units " +
                "FROM user_total_stats " +
                "WHERE user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedSqlStatement)) {
            preparedStatement.setInt(1, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            preparedStatement.execute();
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Stats.create(resultSet.getLong("total_points"), resultSet.getInt("total_units"));
                }
                throw new FoldingException("Error inserting to offset stats");
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void addOffsetStats(final int userId, final UserStatsOffset userStatsOffset) throws FoldingException {
        LOGGER.debug("Adding offset stats for user {}", userId);
        final String preparedInsertSqlStatement = "INSERT INTO user_offset_tc_stats (user_id, utc_timestamp, offset_multiplied_points, offset_units) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (user_id) " +
                "DO UPDATE " +
                "SET utc_timestamp = ?, offset_multiplied_points = ?, offset_units = ? " +
                "RETURNING offset_multiplied_points, offset_units;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            final Timestamp currentUtcTimestamp = TimeUtils.getCurrentUtcTimestamp();

            preparedStatement.setInt(1, userId);
            preparedStatement.setTimestamp(2, currentUtcTimestamp);
            preparedStatement.setLong(3, userStatsOffset.getPointsOffset());
            preparedStatement.setInt(4, userStatsOffset.getUnitsOffset());
            preparedStatement.setTimestamp(5, currentUtcTimestamp);
            preparedStatement.setLong(6, userStatsOffset.getPointsOffset());
            preparedStatement.setInt(7, userStatsOffset.getUnitsOffset());

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            preparedStatement.execute();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }


    @Override
    public UserStatsOffset addOrUpdateOffsetStats(final int userId, final UserStatsOffset userStatsOffset) throws FoldingException {
        LOGGER.debug("Adding/updating offset stats for user {}", userId);
        final String preparedInsertSqlStatement = "INSERT INTO user_offset_tc_stats (user_id, utc_timestamp, offset_multiplied_points, offset_units) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (user_id) " +
                "DO UPDATE " +
                "SET utc_timestamp = ?, offset_multiplied_points = user_offset_tc_stats.offset_multiplied_points + ?, offset_units = user_offset_tc_stats.offset_units + ? " +
                "RETURNING offset_multiplied_points, offset_units;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            final Timestamp currentUtcTimestamp = TimeUtils.getCurrentUtcTimestamp();

            preparedStatement.setInt(1, userId);
            preparedStatement.setTimestamp(2, currentUtcTimestamp);
            preparedStatement.setLong(3, userStatsOffset.getPointsOffset());
            preparedStatement.setInt(4, userStatsOffset.getUnitsOffset());
            preparedStatement.setTimestamp(5, currentUtcTimestamp);
            preparedStatement.setLong(6, userStatsOffset.getPointsOffset());
            preparedStatement.setInt(7, userStatsOffset.getUnitsOffset());

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return UserStatsOffset.create(resultSet.getLong("offset_multiplied_points"), resultSet.getInt("offset_units"));
                }
                throw new FoldingException("Error inserting to offset stats");
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Map<Integer, UserStatsOffset> getOffsetStats(final List<Integer> userIds) throws FoldingException {
        LOGGER.debug("Getting offset stats for {} users", userIds.size());
        final String preparedInsertSqlStatement = "SELECT offset_multiplied_points, offset_units " +
                "FROM user_offset_tc_stats " +
                "WHERE user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            final Map<Integer, UserStatsOffset> offsetsByUserId = new HashMap<>(userIds.size());

            for (final int userId : userIds) {
                preparedStatement.setInt(1, userId);

                LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
                try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        offsetsByUserId.put(userId, UserStatsOffset.create(resultSet.getLong("offset_multiplied_points"), resultSet.getInt("offset_units")));
                    } else {
                        offsetsByUserId.put(userId, UserStatsOffset.empty());
                    }
                } catch (final SQLException e) {
                    LOGGER.warn("Error getting offset stats for user: {}", userId, e);
                    offsetsByUserId.put(userId, UserStatsOffset.empty());
                }
            }
            return offsetsByUserId;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void clearOffsetStats() throws FoldingConflictException, FoldingException {
        LOGGER.debug("Clearing offset stats for all users");
        final String preparedInsertSqlStatement = "DELETE FROM user_offset_tc_stats;";
        LOGGER.debug("Executing prepared statement: '{}'", preparedInsertSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            try {
                preparedStatement.setInt(1, retiredUserStats.getUserId());
                preparedStatement.setInt(2, teamId);
                preparedStatement.setString(3, displayUserName);
                preparedStatement.setTimestamp(4, TimeUtils.getCurrentUtcTimestamp());
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

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
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
                throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
            } catch (final SQLException e) {
                LOGGER.warn("Unable to get retired stats for retired user ID {}", retiredUserId, e);
                throw new FoldingException("Error persisting retired stats", e);
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
                resultSet.getString("operating_system"),
                resultSet.getDouble("multiplier")
        );
    }

    private static User createUser(final ResultSet resultSet) throws SQLException {
        return User.create(
                resultSet.getInt("user_id"),
                resultSet.getString("folding_username"),
                resultSet.getString("display_username"),
                resultSet.getString("passkey"),
                resultSet.getString("category"),
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
