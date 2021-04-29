package me.zodac.folding.test.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class for cleaning database tables for integration tests.
 */
public final class DatabaseCleaner {

    private static final String JDBC_CONNECTION_URL = "jdbc:postgresql://192.168.99.100:5433/folding_db";
    private static final Properties JDBC_CONNECTION_PROPERTIES = new Properties();

    static {
        JDBC_CONNECTION_PROPERTIES.setProperty("user", "folding_user");
        JDBC_CONNECTION_PROPERTIES.setProperty("password", "shroot");
        JDBC_CONNECTION_PROPERTIES.setProperty("driver", "org.postgresql.Driver");
    }

    private DatabaseCleaner() {

    }

    /**
     * Deletes all entries in the given {@code tableNames} and resets the serial count for the identity to 0.
     *
     * @param tableNames the tables to clean/truncate
     * @throws SQLException thrown if there was an error connecting to the DB
     */
    public static void truncateTableAndResetId(final String... tableNames) throws SQLException {
        for (final String tableName : tableNames) {
            final String truncateQuery = String.format("TRUNCATE TABLE %s RESTART IDENTITY CASCADE;", tableName);

            try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
                 final PreparedStatement preparedStatement = connection.prepareStatement(truncateQuery)) {
                preparedStatement.execute();
            }
        }
    }
}
