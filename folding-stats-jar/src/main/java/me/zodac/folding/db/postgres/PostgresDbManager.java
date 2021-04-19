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
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.utils.EnvironmentVariable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class PostgresDbManager implements DbManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDbManager.class);

    private static final String JDBC_CONNECTION_URL = EnvironmentVariable.get("JDBC_CONNECTION_URL");
    private static final Properties JDBC_CONNECTION_PROPERTIES = new Properties();
    private static final String VIOLATES_FOREIGN_KEY_CONSTRAINT = "violates foreign key constraint";

    static {
        JDBC_CONNECTION_PROPERTIES.setProperty("user", EnvironmentVariable.get("JDBC_CONNECTION_USER"));
        JDBC_CONNECTION_PROPERTIES.setProperty("password", EnvironmentVariable.get("JDBC_CONNECTION_PASSWORD"));
        JDBC_CONNECTION_PROPERTIES.setProperty("driver", EnvironmentVariable.get("JDBC_CONNECTION_DRIVER"));
    }

    @Override
    public Hardware createHardware(final Hardware hardware) throws FoldingException {
        final String insertSqlWithReturnId = "INSERT INTO hardware (hardware_name, display_name, operating_system, multiplier) VALUES (?, ?, ?, ?) RETURNING hardware_id;";
        LOGGER.debug("Executing SQL statement '{}'", insertSqlWithReturnId);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(insertSqlWithReturnId)) {

            preparedStatement.setString(1, hardware.getHardwareName());
            preparedStatement.setString(2, hardware.getDisplayName());
            preparedStatement.setString(3, hardware.getOperatingSystem());
            preparedStatement.setDouble(4, hardware.getMultiplier());

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final int hardwareId = resultSet.getInt("hardware_id");
                    return Hardware.updateWithId(hardwareId, hardware);
                }
                throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
            }
        } catch (final SQLException e) {
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

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return createHardware(resultSet);
                }
            }

            throw new HardwareNotFoundException();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void updateHardware(final Hardware hardware) throws FoldingException {
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
            LOGGER.debug("Executing SQL statement '{}'", preparedStatement);

            if (preparedStatement.executeUpdate() == 0) {
                throw new FoldingException(String.format("Error executing update for hardware: %s", preparedStatement));
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void deleteHardware(final int hardwareId) throws FoldingException, FoldingConflictException {
        final String deleteSqlStatement = "DELETE FROM hardware WHERE hardware_id = ?;";
        LOGGER.debug("Executing SQL statement '{}'", deleteSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(deleteSqlStatement)) {

            preparedStatement.setInt(1, hardwareId);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_FOREIGN_KEY_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public User createUser(final User user) throws FoldingException {
        final String insertSqlWithReturnId = "INSERT INTO users (folding_username, display_username, passkey, category, hardware_id, folding_team_number, live_stats_link) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING user_id;";
        LOGGER.debug("Executing SQL statement '{}'", insertSqlWithReturnId);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(insertSqlWithReturnId)) {

            preparedStatement.setString(1, user.getFoldingUserName());
            preparedStatement.setString(2, user.getDisplayName());
            preparedStatement.setString(3, user.getPasskey());
            preparedStatement.setString(4, user.getCategory());
            preparedStatement.setInt(5, user.getHardwareId());
            preparedStatement.setInt(6, user.getFoldingTeamNumber());
            preparedStatement.setString(7, user.getLiveStatsLink());

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final int foldingUserId = resultSet.getInt("user_id");
                    return User.updateWithId(foldingUserId, user);
                }

                throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public List<User> getAllUsers() throws FoldingException {
        final String selectSqlStatement = "SELECT * FROM users;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<User> users = new ArrayList<>();

                while (resultSet.next()) {
                    users.add(createFoldingUser(resultSet));
                }

                return users;
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public User getUser(final int userId) throws FoldingException, UserNotFoundException {
        final String selectSqlStatement = "SELECT * FROM users WHERE user_id = ?;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, userId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return createFoldingUser(resultSet);
                }

                throw new UserNotFoundException();
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void updateUser(final User user) throws FoldingException {
        final String updateSqlStatement = "UPDATE users " +
                "SET folding_username = ?, display_username = ?, passkey = ?, category = ?, hardware_id = ?, folding_team_number = ?, live_stats_link = ? " +
                "WHERE user_id = ?;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(updateSqlStatement)) {

            preparedStatement.setString(1, user.getFoldingUserName());
            preparedStatement.setString(2, user.getDisplayName());
            preparedStatement.setString(3, user.getPasskey());
            preparedStatement.setString(4, user.getCategory());
            preparedStatement.setInt(5, user.getHardwareId());
            preparedStatement.setInt(6, user.getFoldingTeamNumber());
            preparedStatement.setString(7, user.getLiveStatsLink());
            preparedStatement.setInt(8, user.getId());
            LOGGER.debug("Executing SQL statement '{}'", preparedStatement);

            if (preparedStatement.executeUpdate() == 0) {
                throw new FoldingException(String.format("Error executing update for user: %s", preparedStatement));
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void deleteUser(final int userId) throws FoldingException, FoldingConflictException {
        final String deleteSqlStatement = "DELETE FROM folding_user WHERE user_id = ?;";
        LOGGER.debug("Executing SQL statement '{}'", deleteSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(deleteSqlStatement)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_FOREIGN_KEY_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Team createTeam(final Team team) throws FoldingException {
        final String insertSqlWithReturnId = "INSERT INTO teams (team_name, team_description, captain_user_id, user_ids) VALUES (?, ?, ?, ?) RETURNING team_id;";
        LOGGER.debug("Executing SQL statement '{}'", insertSqlWithReturnId);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(insertSqlWithReturnId)) {

            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setString(2, team.getTeamDescription());
            preparedStatement.setInt(3, team.getCaptainUserId());
            preparedStatement.setArray(4, connection.createArrayOf("INT", team.getUserIds().toArray(new Integer[0])));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final int foldingTeamId = resultSet.getInt("team_id");
                    return Team.updateWithId(foldingTeamId, team);
                }

                throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public List<Team> getAllTeams() throws FoldingException {
        final String selectSqlStatement = "SELECT * FROM teams;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                final List<Team> teams = new ArrayList<>();

                while (resultSet.next()) {
                    teams.add(createFoldingTeam(resultSet));
                }

                return teams;
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Team getTeam(final int foldingTeamId) throws FoldingException, TeamNotFoundException {
        final String selectSqlStatement = "SELECT * FROM teams WHERE team_id = ?;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, foldingTeamId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return createFoldingTeam(resultSet);
                }

                throw new TeamNotFoundException();
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void updateTeam(final Team team) throws FoldingException {
        final String updateSqlStatement = "UPDATE teams " +
                "SET team_name = ?, team_description = ?, captain_user_id = ?, user_ids = ? " +
                "WHERE team_id = ?;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(updateSqlStatement)) {

            preparedStatement.setString(1, team.getTeamName());
            preparedStatement.setString(2, team.getTeamDescription());
            preparedStatement.setInt(3, team.getCaptainUserId());
            preparedStatement.setArray(4, connection.createArrayOf("INT", team.getUserIds().toArray(new Integer[0])));
            preparedStatement.setInt(5, team.getId());
            LOGGER.debug("Executing SQL statement '{}'", preparedStatement);

            if (preparedStatement.executeUpdate() == 0) {
                throw new FoldingException(String.format("Error executing update for team: %s", preparedStatement));
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void deleteTeam(final int teamId) throws FoldingException, FoldingConflictException {
        final String deleteSqlStatement = "DELETE FROM teams WHERE team_id = ?;";
        LOGGER.debug("Executing SQL statement '{}'", deleteSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(deleteSqlStatement)) {

            preparedStatement.setInt(1, teamId);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            if (e.getMessage().contains(VIOLATES_FOREIGN_KEY_CONSTRAINT)) {
                throw new FoldingConflictException();
            }
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void persistHourlyUserStats(final List<UserStats> userStats) throws FoldingException {
        LOGGER.info("Inserting TC stats for {} users to DB", userStats.size());
        final String preparedInsertSqlStatement = "INSERT INTO tc_user_stats (user_id, utc_timestamp, total_points, total_unmultiplied_points, total_units) VALUES (?, ?, ?, ?, ?);";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            for (final UserStats tcUserStatsForUser : userStats) {
                try {
                    preparedStatement.setInt(1, tcUserStatsForUser.getUserId());
                    preparedStatement.setTimestamp(2, tcUserStatsForUser.getTimestamp());
                    preparedStatement.setLong(3, tcUserStatsForUser.getPoints());
                    preparedStatement.setLong(4, tcUserStatsForUser.getUnmultipliedPoints());
                    preparedStatement.setInt(5, tcUserStatsForUser.getUnits());

                    preparedStatement.execute();
                } catch (final SQLException e) {
                    LOGGER.warn("Unable to INSERT TC stats for user: {}", tcUserStatsForUser, e);
                }
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public boolean doTcStatsExist() throws FoldingException {
        LOGGER.debug("Checking if any TC stats exist in the DB");
        final String selectSqlStatement = "SELECT COUNT(*) AS count FROM tc_user_stats;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

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
    public Map<LocalDate, Stats> getDailyUserStats(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        LOGGER.debug("Getting historic daily user TC stats for {}/{} for user {}", StringUtils.capitalize(month.toString().toLowerCase(Locale.UK)), year, userId);

        final String selectSqlStatement = "SELECT utc_timestamp::DATE AS TIMESTAMP, " +
                "COALESCE(MAX(total_points) - LAG(MAX(total_points)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points, " +
                "COALESCE(MAX(total_unmultiplied_points) - LAG(MAX(total_unmultiplied_points)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_unmultiplied_points, " +
                "COALESCE(MAX(total_units) - LAG(MAX(total_units)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_units " +
                "FROM tc_user_stats " +
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

            final Map<LocalDate, Stats> userStatsByDate = new TreeMap<>();

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                // First entry will be (0, 0), so we need to manually get the first day's stats for the user
                if (resultSet.next()) {
                    final Stats firstDayTotalStats = getTotalStatsForFirstDayForUser(userId, month, year);
                    final Stats initialStatsForMonth = getFirstStatsForUser(userId, month, year);
                    userStatsByDate.put(resultSet.getTimestamp("timestamp").toLocalDateTime().toLocalDate(),
                            new Stats(firstDayTotalStats.getPoints() - initialStatsForMonth.getPoints(),
                                    firstDayTotalStats.getUnmultipliedPoints() - initialStatsForMonth.getUnmultipliedPoints(),
                                    firstDayTotalStats.getUnits() - initialStatsForMonth.getUnits())
                    );
                }

                // All remaining stats will be diff-ed from the previous entry
                while (resultSet.next()) {
                    userStatsByDate.put(resultSet.getTimestamp("timestamp").toLocalDateTime().toLocalDate(),
                            new Stats(
                                    resultSet.getLong("diff_points"),
                                    resultSet.getLong("diff_unmultiplied_points"),
                                    resultSet.getInt("diff_units")
                            )
                    );
                }

                if (userStatsByDate.isEmpty()) {
                    throw new UserNotFoundException();
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

    private Stats getTotalStatsForFirstDayForUser(final int foldingUserId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        final String selectSqlStatement = "SELECT total_points AS points, total_unmultiplied_points AS unmultiplied_points, total_units AS units " +
                "FROM tc_user_stats " +
                "WHERE utc_timestamp::DATE = (" +
                "   SELECT MIN(utc_timestamp)::DATE " +
                "   FROM tc_user_stats WHERE user_id = ? " +
                "   AND EXTRACT(MONTH FROM utc_timestamp) = ? " +
                "   AND EXTRACT(YEAR FROM utc_timestamp) = ? " +
                ") " +
                "AND user_id = ? " +
                "ORDER BY utc_timestamp DESC " +
                "LIMIT 1;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, foldingUserId);
            preparedStatement.setInt(2, month.getValue());
            preparedStatement.setInt(3, year.getValue());
            preparedStatement.setInt(4, foldingUserId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return new Stats(resultSet.getLong("points"), resultSet.getLong("unmultiplied_points"), resultSet.getInt("units"));
                }

                throw new UserNotFoundException();
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Stats getFirstStatsForUser(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        LOGGER.debug("Getting first points in {}/{} for user {}", StringUtils.capitalize(month.toString().toLowerCase(Locale.UK)), year, userId);
        return getPointsForUser(userId, month, year, OrderBy.ASCENDING);
    }

    @Override
    public Stats getLatestStatsForUser(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        LOGGER.debug("Getting current points in {}/{} for user {}", StringUtils.capitalize(month.toString().toLowerCase(Locale.UK)), year, userId);
        return getPointsForUser(userId, month, year, OrderBy.DESCENDING);
    }

    private Stats getPointsForUser(final int foldingUserId, final Month month, final Year year,
                                   final OrderBy orderBy) throws
            FoldingException, UserNotFoundException {
        final String selectSqlStatement = String.format(
                "SELECT total_points AS points, total_unmultiplied_points AS unmultiplied_points, total_units AS units " +
                        "FROM tc_user_stats " +
                        "WHERE EXTRACT(MONTH FROM utc_timestamp) = ? AND EXTRACT(YEAR FROM utc_timestamp) = ? " +
                        "AND user_id = ? " +
                        "ORDER BY utc_timestamp %s " +
                        "LIMIT 1;", orderBy.getSqlValue()
        );

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, month.getValue());
            preparedStatement.setInt(2, year.getValue());
            preparedStatement.setInt(3, foldingUserId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return new Stats(resultSet.getLong("points"), resultSet.getLong("unmultiplied_points"), resultSet.getInt("units"));
                }

                throw new UserNotFoundException();
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

    private static User createFoldingUser(final ResultSet resultSet) throws SQLException {
        return User.create(
                resultSet.getInt("user_id"),
                resultSet.getString("folding_username"),
                resultSet.getString("display_username"),
                resultSet.getString("passkey"),
                resultSet.getString("category"),
                resultSet.getInt("hardware_id"),
                resultSet.getInt("folding_team_number"),
                resultSet.getString("live_stats_link")
        );
    }

    private static Team createFoldingTeam(final ResultSet resultSet) throws SQLException {
        return new Team.Builder(resultSet.getString("team_name"))
                .teamId(resultSet.getInt("team_id"))
                .teamDescription(resultSet.getString("team_description"))
                .captainUserId(resultSet.getInt("captain_user_id"))
                .userIds(List.of((Integer[]) resultSet.getArray("user_ids").getArray()))
                .createTeam();
    }
}
