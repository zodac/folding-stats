/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import me.zodac.folding.test.integration.util.TestConstants;
import me.zodac.folding.test.integration.util.TestStats;
import org.postgresql.util.PSQLException;

/**
 * Utility class for database tables for tests.
 */
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
     * Insert {@link TestStats} into the provided {@code tableName}.
     *
     * @param tableName the name of the table
     * @param stats     the {@link TestStats} to be persisted
     */
    public static void insertStats(final String tableName, final TestStats... stats) {
        final String insertStatement = String.format("INSERT INTO %s VALUES %s;", tableName,
            Arrays.stream(stats).map(TestStats::toString).collect(Collectors.joining(",")));

        try (final Connection connection = DriverManager.getConnection(JDBC_CONNECTION_URL, JDBC_CONNECTION_PROPERTIES);
             final PreparedStatement preparedStatement = connection.prepareStatement(insertStatement)) {
            preparedStatement.execute();
        } catch (final SQLException e) {
            throw new AssertionError(String.format("Error inserting TC stats: '%s'", insertStatement), e);
        }
    }
}
