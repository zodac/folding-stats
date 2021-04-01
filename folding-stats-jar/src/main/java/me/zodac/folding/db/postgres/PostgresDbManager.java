package me.zodac.folding.db.postgres;

import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.HardwareCategory;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.parsing.FoldingStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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

    public static HardwareCategory createHardwareCategory(final HardwareCategory hardwareCategory) throws FoldingException {
        try {
            final String insertSqlStatement = PostgresSqlQueryBuilder.insertHardwareCategory(hardwareCategory);
            final int hardwareId = executeInsertSqlWithReturnId(insertSqlStatement);
            return HardwareCategory.updateHardwareCategoryWithId(hardwareId, hardwareCategory);
        } catch (final Exception e) {
            throw new FoldingException("Error persisting hardware category", e);
        }
    }

    public static List<HardwareCategory> getAllHardwareCategories() throws FoldingException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getHardwareCategories();
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {

            final List<HardwareCategory> hardwareCategories = new ArrayList<>();

            while (resultSet.next()) {
                hardwareCategories.add(HardwareCategory.create(
                        resultSet.getInt("hardware_id"),
                        resultSet.getString("category_name"),
                        resultSet.getDouble("multiplier")
                ));
            }

            return hardwareCategories;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static HardwareCategory getHardwareCategory(final String hardwareCategoryId) throws FoldingException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getHardwareCategory(hardwareCategoryId);
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {
            if (resultSet.next()) {
                return HardwareCategory.create(
                        resultSet.getInt("hardware_id"),
                        resultSet.getString("category_name"),
                        resultSet.getDouble("multiplier")
                );
            } else {
                throw new NoSuchElementException();
            }
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static FoldingUser createFoldingUser(final FoldingUser foldingUser) throws FoldingException {
        try {
            final String insertSqlStatement = PostgresSqlQueryBuilder.insertFoldingUser(foldingUser);
            final int foldingUserId = executeInsertSqlWithReturnId(insertSqlStatement);
            return FoldingUser.updateFoldingUserWithId(foldingUserId, foldingUser);
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
                        resultSet.getInt("hardware_id"),
                        resultSet.getString("hardware_name")
                ));
            }

            return foldingUsers;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static FoldingUser getFoldingUser(final String foldingUserId) throws FoldingException {
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
                        resultSet.getInt("hardware_id"),
                        resultSet.getString("hardware_name")
                );
            } else {
                throw new NoSuchElementException();
            }
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
}
