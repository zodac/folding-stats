package me.zodac.folding.db.postgres;

import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.parsing.FoldingStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static me.zodac.folding.util.EnvironmentUtils.getEnvironmentValue;

public class PostgresDbManager implements DbManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDbManager.class);

    private static final String JDBC_CONNECTION_URL = getEnvironmentValue("JDBC_CONNECTION_URL");
    private static final Properties JDBC_CONNECTION_PROPERTIES = new Properties();

    static {
        JDBC_CONNECTION_PROPERTIES.setProperty("user", getEnvironmentValue("JDBC_CONNECTION_USER"));
        JDBC_CONNECTION_PROPERTIES.setProperty("password", getEnvironmentValue("JDBC_CONNECTION_PASSWORD"));
        JDBC_CONNECTION_PROPERTIES.setProperty("driver", getEnvironmentValue("JDBC_CONNECTION_DRIVER"));
    }

    public static Hardware createHardware(final Hardware hardware) throws FoldingException {
        try {
            final String insertSqlStatement = PostgresSqlQueryBuilder.insertHardware(hardware);
            final int hardwareId = executeInsertSqlWithReturnId(insertSqlStatement);
            return Hardware.updateWithId(hardwareId, hardware);
        } catch (final Exception e) {
            throw new FoldingException("Error persisting hardware category", e);
        }
    }

    public static List<Hardware> getAllHardware() throws FoldingException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getHardware();
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {

            final List<Hardware> allHardware = new ArrayList<>();

            while (resultSet.next()) {
                allHardware.add(Hardware.create(
                        resultSet.getInt("hardware_id"),
                        resultSet.getString("hardware_name"),
                        resultSet.getString("display_name"),
                        resultSet.getDouble("multiplier")
                ));
            }

            return allHardware;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static Hardware getHardware(final String hardwareId) throws FoldingException, NotFoundException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getHardware(hardwareId);
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {
            if (resultSet.next()) {
                return Hardware.create(
                        resultSet.getInt("hardware_id"),
                        resultSet.getString("hardware_name"),
                        resultSet.getString("display_name"),
                        resultSet.getDouble("multiplier")
                );
            }

            throw new NotFoundException();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static FoldingUser createFoldingUser(final FoldingUser foldingUser) throws FoldingException {
        try {
            final String insertSqlStatement = PostgresSqlQueryBuilder.insertFoldingUser(foldingUser);
            final int foldingUserId = executeInsertSqlWithReturnId(insertSqlStatement);
            return FoldingUser.updateWithId(foldingUserId, foldingUser);
        } catch (final Exception e) {
            throw new FoldingException("Error persisting Folding user", e);
        }
    }

    public static List<FoldingUser> getAllFoldingUsers() throws FoldingException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getFoldingUsers();
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {

            final List<FoldingUser> foldingUsers = new ArrayList<>();

            while (resultSet.next()) {
                foldingUsers.add(FoldingUser.create(
                        resultSet.getInt("user_id"),
                        resultSet.getString("folding_username"),
                        resultSet.getString("display_username"),
                        resultSet.getString("passkey"),
                        resultSet.getString("category"),
                        resultSet.getInt("hardware_id"),
                        resultSet.getInt("folding_team_number")
                ));
            }

            return foldingUsers;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static FoldingUser getFoldingUser(final String foldingUserId) throws FoldingException, NotFoundException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getFoldingUser(foldingUserId);
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {
            if (resultSet.next()) {
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

            throw new NotFoundException();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static FoldingTeam createFoldingTeam(final FoldingTeam foldingTeam) throws FoldingException {
        try {
            final String insertSqlStatement = PostgresSqlQueryBuilder.insertFoldingTeam(foldingTeam);
            final int foldingTeamId = executeInsertSqlWithReturnId(insertSqlStatement);
            return FoldingTeam.updateWithId(foldingTeamId, foldingTeam);
        } catch (final Exception e) {
            throw new FoldingException("Error persisting Folding team", e);
        }
    }

    public static List<FoldingTeam> getAllFoldingTeams() throws FoldingException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getFoldingTeams();
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {

            final List<FoldingTeam> foldingTeams = new ArrayList<>();

            while (resultSet.next()) {
                foldingTeams.add(FoldingTeam.create(
                        resultSet.getInt("team_id"),
                        resultSet.getString("team_name"),
                        resultSet.getInt("captain_user_id"),
                        resultSet.getInt("nvidia_gpu_user_id"),
                        resultSet.getInt("amd_gpu_user_id"),
                        resultSet.getInt("wildcard_user_id")
                ));
            }

            return foldingTeams;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static FoldingTeam getFoldingTeam(final String foldingTeamId) throws FoldingException, NotFoundException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getFoldingTeam(foldingTeamId);
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {
            if (resultSet.next()) {
                return FoldingTeam.create(
                        resultSet.getInt("team_id"),
                        resultSet.getString("team_name"),
                        resultSet.getInt("captain_user_id"),
                        resultSet.getInt("nvidia_gpu_user_id"),
                        resultSet.getInt("amd_gpu_user_id"),
                        resultSet.getInt("wildcard_user_id")
                );
            }

            throw new NotFoundException();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    private static int executeInsertSqlWithReturnId(final String insertSqlWithReturnId) throws SQLException {
        LOGGER.debug("Executing SQL statement '{}'", insertSqlWithReturnId);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(insertSqlWithReturnId)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }

        throw new IllegalStateException("No ID was returned from the DB, but no exception was raised");
    }

    public static void persistStats(final List<FoldingStats> foldingStats) throws FoldingException {
        LOGGER.info("Inserting stats for {} Folding users to DB", foldingStats.size());
        final List<String> insertSqlStatements = PostgresSqlQueryBuilder.insertFoldingStats(foldingStats);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement()) {
            for (final String insertSqlStatement : insertSqlStatements) {
                try {
                    statement.execute(insertSqlStatement);
                } catch (final SQLException e) {
                    LOGGER.warn("Unable to INSERT folding stats for user: {}", insertSqlStatement, e);
                }
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    private static String getPointsForUserInMonthQuery(final FoldingUser foldingUser, final Month month, final boolean first) {
        final String order = first ? "ASC" : "DESC";
        return String.format(
                "SELECT total_points AS points, total_wus AS wus " +
                        "FROM individual_points " +
                        "WHERE utc_timestamp BETWEEN '2021-%s-01' AND NOW() " +
                        "AND user_id = '%s' " +
                        "ORDER BY utc_timestamp %s " +
                        "LIMIT 1;",
                month.getValue(), foldingUser.getId(), order
        );
    }

    public static UserStats getFirstPointsForUserInMonth(final FoldingUser foldingUser, final Month month) throws FoldingException, NotFoundException {
        LOGGER.debug("Getting first points in month {} for user {}", month, foldingUser);
        final String selectSqlStatement = getPointsForUserInMonthQuery(foldingUser, month, true);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {

            if (resultSet.next()) {
                return new UserStats(resultSet.getLong("points"), resultSet.getLong("wus"));
            }

            throw new NotFoundException();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static UserStats getCurrentPointsForUserInMonth(final FoldingUser foldingUser, final Month month) throws FoldingException, NotFoundException {
        LOGGER.debug("Getting current points in month {} for user {}", month, foldingUser);
        final String selectSqlStatement = getPointsForUserInMonthQuery(foldingUser, month, false);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {

            if (resultSet.next()) {
                return new UserStats(resultSet.getLong("points"), resultSet.getLong("wus"));
            }

            throw new NotFoundException();
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }
}
