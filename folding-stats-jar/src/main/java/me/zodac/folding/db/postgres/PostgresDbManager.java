package me.zodac.folding.db.postgres;

import me.zodac.folding.api.FoldingStats;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.TeamStats;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.db.OrderBy;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.util.EnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PostgresDbManager implements DbManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDbManager.class);

    private static final String JDBC_CONNECTION_URL = EnvironmentVariable.get("JDBC_CONNECTION_URL");
    private static final Properties JDBC_CONNECTION_PROPERTIES = new Properties();

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
        } catch (final IllegalStateException e) {
            throw e;
        } catch (final Exception e) {
            throw new FoldingException("Error persisting hardware", e);
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
    public Hardware getHardware(final int hardwareId) throws FoldingException, NotFoundException {
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

            throw new NotFoundException();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public FoldingUser createFoldingUser(final FoldingUser foldingUser) throws FoldingException {
        final String insertSqlWithReturnId = "INSERT INTO folding_users (folding_username, display_username, passkey, category, hardware_id, folding_team_number) VALUES (?, ?, ?, ?, ?, ?) RETURNING user_id;";
        LOGGER.debug("Executing SQL statement '{}'", insertSqlWithReturnId);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(insertSqlWithReturnId)) {

            preparedStatement.setString(1, foldingUser.getFoldingUserName());
            preparedStatement.setString(2, foldingUser.getDisplayName());
            preparedStatement.setString(3, foldingUser.getPasskey());
            preparedStatement.setString(4, foldingUser.getCategory());
            preparedStatement.setInt(5, foldingUser.getHardwareId());
            preparedStatement.setInt(6, foldingUser.getFoldingTeamNumber());

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final int foldingUserId = resultSet.getInt("user_id");
                    return FoldingUser.updateWithId(foldingUserId, foldingUser);
                }

                throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
            }
        } catch (final IllegalStateException e) {
            throw e;
        } catch (final Exception e) {
            throw new FoldingException("Error persisting Folding user", e);
        }
    }

    @Override
    public List<FoldingUser> getAllFoldingUsers() throws FoldingException {
        final String selectSqlStatement = "SELECT * FROM folding_users;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<FoldingUser> foldingUsers = new ArrayList<>();

                while (resultSet.next()) {
                    foldingUsers.add(createFoldingUser(resultSet));
                }

                return foldingUsers;
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public FoldingUser getFoldingUser(final int foldingUserId) throws FoldingException, NotFoundException {
        final String selectSqlStatement = "SELECT * FROM folding_users WHERE user_id = ?;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, foldingUserId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return createFoldingUser(resultSet);
                }

                throw new NotFoundException();
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public FoldingTeam createFoldingTeam(final FoldingTeam foldingTeam) throws FoldingException {
        final String insertSqlWithReturnId = "INSERT INTO folding_teams (team_name, team_description, captain_user_id, user_ids) VALUES (?, ?, ?, ?) RETURNING team_id;";
//                foldingTeam.getTeamName(), foldingTeam.getTeamDescription(), foldingTeam.getCaptainUserId(),
//                foldingTeam.getUserIds().stream().map(String::valueOf).collect(joining(", ")));
        LOGGER.debug("Executing SQL statement '{}'", insertSqlWithReturnId);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(insertSqlWithReturnId)) {

            preparedStatement.setString(1, foldingTeam.getTeamName());
            preparedStatement.setString(2, foldingTeam.getTeamDescription());
            preparedStatement.setInt(3, foldingTeam.getCaptainUserId());
            preparedStatement.setArray(4, connection.createArrayOf("INT", foldingTeam.getUserIds().toArray(new Integer[0])));

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    final int foldingTeamId = resultSet.getInt("team_id");
                    return FoldingTeam.updateWithId(foldingTeamId, foldingTeam);
                }

                throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
            }
        } catch (final IllegalStateException e) {
            throw e;
        } catch (final Exception e) {
            throw new FoldingException("Error persisting Folding team", e);
        }
    }

    @Override
    public List<FoldingTeam> getAllFoldingTeams() throws FoldingException {
        final String selectSqlStatement = "SELECT * FROM folding_teams;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                final List<FoldingTeam> foldingTeams = new ArrayList<>();

                while (resultSet.next()) {
                    foldingTeams.add(createFoldingTeam(resultSet));
                }

                return foldingTeams;
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public FoldingTeam getFoldingTeam(final int foldingTeamId) throws FoldingException, NotFoundException {
        final String selectSqlStatement = "SELECT * FROM folding_teams WHERE team_id = ?;";
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, foldingTeamId);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return createFoldingTeam(resultSet);
                }

                throw new NotFoundException();
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void persistHourlyUserTcStats(final List<FoldingStats> tcUserStats) throws FoldingException {
        LOGGER.info("Inserting TC stats for {} users to DB", tcUserStats.size());
        final String preparedInsertSqlStatement = "INSERT INTO individual_tc_points (user_id, utc_timestamp, total_points, total_units) VALUES (?, ?, ?, ?);";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            for (final FoldingStats tcFoldingStatsForUser : tcUserStats) {
                try {
                    preparedStatement.setInt(1, tcFoldingStatsForUser.getUserId());
                    preparedStatement.setTimestamp(2, tcFoldingStatsForUser.getTimestamp());
                    preparedStatement.setLong(3, tcFoldingStatsForUser.getPoints());
                    preparedStatement.setInt(4, tcFoldingStatsForUser.getUnits());

                    preparedStatement.execute();
                } catch (final SQLException e) {
                    LOGGER.warn("Unable to INSERT TC stats for user: {}", tcFoldingStatsForUser, e);
                }
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void persistDailyUserTcStats(final List<FoldingStats> tcFoldingStats) throws FoldingException {
        LOGGER.info("Inserting daily TC stats for {} users to DB", tcFoldingStats.size());
        final String preparedInsertSqlStatement = "INSERT INTO historic_stats_tc_user_daily (user_id, utc_timestamp, daily_points, daily_units) VALUES (?, ?, ?, ?);";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            for (final FoldingStats tcFoldingStatsForUser : tcFoldingStats) {
                try {
                    preparedStatement.setInt(1, tcFoldingStatsForUser.getUserId());
                    preparedStatement.setTimestamp(2, tcFoldingStatsForUser.getTimestamp());
                    preparedStatement.setLong(3, tcFoldingStatsForUser.getPoints());
                    preparedStatement.setInt(4, tcFoldingStatsForUser.getUnits());

                    preparedStatement.execute();
                } catch (final SQLException e) {
                    LOGGER.warn("Unable to INSERT daily TC stats for user: {}", tcFoldingStatsForUser, e);
                }
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void persistDailyTeamTcStats(final List<TeamStats> tcTeamStats) throws FoldingException {
        LOGGER.info("Inserting daily TC stats for {} teams to DB", tcTeamStats.size());
        final String preparedInsertSqlStatement = "INSERT INTO historic_stats_tc_team_daily (team_id, utc_timestamp, daily_team_points, daily_team_units) VALUES (?, ?, ?, ?);";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            for (final TeamStats tcStatsForTeam : tcTeamStats) {
                try {
                    preparedStatement.setInt(1, tcStatsForTeam.getTeamId());
                    preparedStatement.setTimestamp(2, tcStatsForTeam.getTimestamp());
                    preparedStatement.setLong(3, tcStatsForTeam.getPoints());
                    preparedStatement.setInt(4, tcStatsForTeam.getUnits());

                    preparedStatement.execute();
                } catch (final SQLException e) {
                    LOGGER.warn("Unable to INSERT daily TC stats for team: {}", tcStatsForTeam, e);
                }
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public void persistMonthlyTeamTcStats(final List<TeamStats> tcTeamStats) throws FoldingException {
        LOGGER.info("Inserting monthly TC stats for {} teams to DB", tcTeamStats.size());
        final String preparedInsertSqlStatement = "INSERT INTO historic_stats_tc_team_monthly (team_id, utc_timestamp, monthly_team_points, monthly_team_units) VALUES (?, ?, ?, ?);";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(preparedInsertSqlStatement)) {
            for (final TeamStats tcStatsForTeam : tcTeamStats) {
                try {
                    preparedStatement.setInt(1, tcStatsForTeam.getTeamId());
                    preparedStatement.setTimestamp(2, tcStatsForTeam.getTimestamp());
                    preparedStatement.setLong(3, tcStatsForTeam.getPoints());
                    preparedStatement.setInt(4, tcStatsForTeam.getUnits());

                    preparedStatement.execute();
                } catch (final SQLException e) {
                    LOGGER.warn("Unable to INSERT monthly TC stats for team: {}", tcStatsForTeam, e);
                }
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }


    @Override
    public boolean doTcStatsExist() throws FoldingException {
        LOGGER.debug("Checking if any TC stats exist in the DB");
        final String selectSqlStatement = "SELECT COUNT(*) AS count FROM individual_tc_points;";

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {
            if (resultSet.next()) {
                return resultSet.getInt("count") > 0;
            }

            return false;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    @Override
    public UserStats getFirstPointsForUserInMonth(final FoldingUser foldingUser, final Month month, final Year year) throws
            FoldingException, NotFoundException {
        LOGGER.debug("Getting first points in month/year {}/{} for user {}", month, year, foldingUser);
        return getPointsForUserInMonth(foldingUser, month, year, OrderBy.ASCENDING);
    }

    @Override
    public UserStats getCurrentPointsForUserInMonth(final FoldingUser foldingUser, final Month month, final Year year) throws
            FoldingException, NotFoundException {
        LOGGER.debug("Getting current points in month/year {}/{} for user {}", month, year, foldingUser);
        return getPointsForUserInMonth(foldingUser, month, year, OrderBy.DESCENDING);
    }


    public UserStats getPointsForUserInMonth(final FoldingUser foldingUser, final Month month, final Year year, final OrderBy orderBy) throws
            FoldingException, NotFoundException {
        final String selectSqlStatement = String.format(
                "SELECT total_points AS points, total_units AS units " +
                        "FROM individual_tc_points " +
                        "WHERE utc_timestamp BETWEEN ? AND NOW() " +
                        "AND user_id = ? " +
                        "ORDER BY utc_timestamp %s " +
                        "LIMIT 1;",
                orderBy.getSqlValue()
        );

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setDate(1, Date.valueOf(LocalDate.of(year.getValue(), month.getValue(), 1)));
            preparedStatement.setInt(2, foldingUser.getId());

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    return new UserStats(resultSet.getLong("points"), resultSet.getInt("units"));
                }

                throw new NotFoundException();
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

    private static FoldingUser createFoldingUser(final ResultSet resultSet) throws SQLException {
        return FoldingUser.create(
                resultSet.getInt("user_id"),
                resultSet.getString("folding_username"),
                resultSet.getString("display_username"),
                resultSet.getString("passkey"),
                resultSet.getString("category"),
                resultSet.getInt("hardware_id"),
                resultSet.getInt("folding_team_number")
        );
    }

    private static FoldingTeam createFoldingTeam(final ResultSet resultSet) throws SQLException {
        return new FoldingTeam.Builder(resultSet.getString("team_name"))
                .teamId(resultSet.getInt("team_id"))
                .teamDescription(resultSet.getString("team_description"))
                .captainUserId(resultSet.getInt("captain_user_id"))
                .userIds(List.of((Integer[]) resultSet.getArray("user_ids").getArray()))
                .createTeam();
    }
}
