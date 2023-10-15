/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.test.integration.util.db;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;
import me.zodac.folding.test.integration.util.DummyStats;
import me.zodac.folding.test.integration.util.TestConstants;
import org.postgresql.util.PSQLException;

/**
 * Utility class for database tables for tests.
 */
// TODO: Replace AssertionErrors, not the right place for them
public final class DatabaseUtils {

    private static final String JDBC_CONNECTION_URL = "jdbc:postgresql://" + TestConstants.TEST_IP_ADDRESS + ":5433/folding_db";
    private static final Properties JDBC_CONNECTION_PROPERTIES = new Properties();

    static {
        JDBC_CONNECTION_PROPERTIES.setProperty("user", "folding_user");
        JDBC_CONNECTION_PROPERTIES.setProperty("password", "shroot");
        JDBC_CONNECTION_PROPERTIES.setProperty("driver", "org.postgresql.Driver");
    }

    private DatabaseUtils() {

    }

    /**
     * Deletes all entries in the given {@code tableNames}.
     *
     * @param tableNames the tables to truncate
     */
    public static void truncateTable(final String... tableNames) {
        for (final String tableName : tableNames) {
            final String truncateQuery = String.format("TRUNCATE TABLE %s;", tableName);

            try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
                 final PreparedStatement preparedStatement = connection.prepareStatement(truncateQuery)) {
                preparedStatement.execute();
            } catch (final SQLException e) {
                throw new AssertionError(String.format("Error truncating table: '%s'", tableName), e);
            }
        }
    }

    /**
     * Deletes all entries in the given {@code tableNames} and resets the serial count for the identity to <b>0</b>.
     *
     * @param tableNames the tables to truncate and reset
     */
    public static void truncateTableAndResetId(final String... tableNames) {
        for (final String tableName : tableNames) {
            final String truncateQuery = String.format("TRUNCATE TABLE %s RESTART IDENTITY CASCADE;", tableName);

            try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
                 final PreparedStatement preparedStatement = connection.prepareStatement(truncateQuery)) {
                preparedStatement.execute();
            } catch (final PSQLException e) {
                if (e.getCause() instanceof ConnectException) {
                    throw new AssertionError("Unable to connect to test database"); // NOPMD: PreserveStackTrace - Not interested in trace
                }
                throw new AssertionError(String.format("Error truncating or resetting table: '%s'", tableName), e);
            } catch (final SQLException e) {
                throw new AssertionError(String.format("Error truncating or resetting table: '%s'", tableName), e);
            }
        }
    }

    /**
     * Insert {@link DummyStats} into the provided {@code tableName}.
     *
     * @param tableName the name of the table
     * @param stats     the {@link DummyStats} to be persisted
     */
    public static void insertStats(final String tableName, final DummyStats... stats) {
        final String insertStatement = String.format("INSERT INTO %s VALUES %s;", tableName,
            Arrays.stream(stats).map(DummyStats::toString).collect(Collectors.joining(",")));

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(insertStatement)) {
            preparedStatement.execute();
        } catch (final SQLException e) {
            throw new AssertionError(String.format("Error inserting TC stats: '%s'", insertStatement), e);
        }
    }
}
