package me.zodac.folding.db.postgres;

import me.zodac.folding.api.DbManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.rest.HardwareCategory;
import me.zodac.folding.api.service.HardwareCategoryWithId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
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

    public static HardwareCategoryWithId createHardwareCategory(final HardwareCategory hardwareCategory) throws FoldingException {
        try {
            final String insertSqlStatement = PostgresSqlQueryBuilder.insertHardwareCategory(hardwareCategory);
            final int hardwareId = executeInsertSqlWithReturnId(insertSqlStatement);
            return HardwareCategoryWithId.updateHardwareCategoryWithId(hardwareId, hardwareCategory);
        } catch (final Exception e) {
            throw new FoldingException("Error persisting hardware category", e);
        }
    }

    public static List<HardwareCategoryWithId> getAllHardwareCategories() throws FoldingException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getHardwareCategories();
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {

            final List<HardwareCategoryWithId> hardwareCategoryWithIds = new ArrayList<>();

            while (resultSet.next()) {
                hardwareCategoryWithIds.add(HardwareCategoryWithId.createHardwareCategoryWithId(
                        resultSet.getInt("hardware_id"),
                        resultSet.getString("category_name"),
                        resultSet.getDouble("multiplier")
                ));
            }

            return hardwareCategoryWithIds;
        } catch (final SQLException e) {
            throw new FoldingException("Error opening connection to the DB", e);
        }
    }

    public static HardwareCategoryWithId getHardwareCategory(final String hardwareCategoryId) throws FoldingException {
        final String selectSqlStatement = PostgresSqlQueryBuilder.getHardwareCategory(hardwareCategoryId);
        LOGGER.debug("Executing SQL statement '{}'", selectSqlStatement);

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery(selectSqlStatement)) {
            if (resultSet.next()) {
                return HardwareCategoryWithId.createHardwareCategoryWithId(
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
}
