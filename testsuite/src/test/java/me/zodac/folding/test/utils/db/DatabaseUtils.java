package me.zodac.folding.test.utils.db;

import me.zodac.folding.test.utils.Stats;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Utility class for database tables for tests.
 */
public final class DatabaseUtils {

    private static final String JDBC_CONNECTION_URL = "jdbc:postgresql://192.168.99.100:5433/folding_db";
    private static final Properties JDBC_CONNECTION_PROPERTIES = new Properties();

    static {
        JDBC_CONNECTION_PROPERTIES.setProperty("user", "folding_user");
        JDBC_CONNECTION_PROPERTIES.setProperty("password", "shroot");
        JDBC_CONNECTION_PROPERTIES.setProperty("driver", "org.postgresql.Driver");
    }

    private DatabaseUtils() {

    }

    /**
     * Deletes all entries in the given {@code tableNames} and resets the serial count for the identity to <b>0</b>.
     *
     * @param tableNames the tables to clean/truncate
     */
    public static void truncateTableAndResetId(final String... tableNames) {
        for (final String tableName : tableNames) {
            final String truncateQuery = String.format("TRUNCATE TABLE %s RESTART IDENTITY CASCADE;", tableName);

            try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
                 final PreparedStatement preparedStatement = connection.prepareStatement(truncateQuery)) {
                preparedStatement.execute();
            } catch (final SQLException e) {
                throw new AssertionError(String.format("Error cleaning table: '%s'", tableName), e);
            }
        }
    }

    /**
     * Insert {@link Stats} into the provided {@code tableName}.
     *
     * @param tableName the name of the table
     * @param stats     the {@link Stats} to be persisted
     */
    public static void insertStats(final String tableName, final Stats... stats) {
        final String insertStatement = String.format("INSERT INTO %s VALUES %s;", tableName,
                Arrays.stream(stats).map(Stats::toString).collect(Collectors.joining(",")));

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(insertStatement)) {
            preparedStatement.execute();
        } catch (final SQLException e) {
            throw new AssertionError(String.format("Error inserting TC stats: '%s'", insertStatement), e);
        }
    }
}