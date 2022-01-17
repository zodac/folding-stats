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

package me.zodac.folding.db.postgres;

import static java.util.stream.Collectors.toList;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link EmbeddedPostgres} DB to be used for tests.
 */
public final class EmbeddedPostgresDataSource {

    private static final Logger LOGGER = LogManager.getLogger();

    private final DataSource dataSource;

    private EmbeddedPostgresDataSource() {
        try {
            LOGGER.info("Configuring test DB...");
            dataSource = EmbeddedPostgres.builder()
                .start()
                .getPostgresDatabase();
            createDatabaseTables();
        } catch (final Exception e) {
            throw new AssertionError("Unable to start test DB", e);
        }
    }

    /**
     * Creates an instance of {@link EmbeddedPostgresDataSource}.
     *
     * @return the created {@link EmbeddedPostgresDataSource}
     */
    public static DataSource create() {
        return new EmbeddedPostgresDataSource().dataSource;
    }

    private void createDatabaseTables() throws IOException, URISyntaxException, SQLException {
        final List<String> sqlFiles = List.of("init-db.sql", "system-users.sql");

        for (final String sqlFile : sqlFiles) {
            final Path initScript =
                Paths.get(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("db/" + sqlFile)).toURI());

            final List<String> createStatements = Arrays.stream(Files
                    .readString(initScript)
                    .split("((\\n\\r)|(\\r\\n)){2}|(\\r){2}|(\\n){2}")) // Split on blank line(s)
                .map(String::trim)
                .collect(toList());

            try (final Connection connection = dataSource.getConnection();
                 final Statement statement = connection.createStatement()) {
                for (final String createStatement : createStatements) {
                    statement.execute(createStatement);
                }
            }
        }
    }
}
