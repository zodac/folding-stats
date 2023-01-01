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

package me.zodac.folding.db.postgres;

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
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link EmbeddedPostgres} DB to be used for tests.
 */
final class EmbeddedPostgresDataSource {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern BLANK_LINE_PATTERN = Pattern.compile("((\\n\\r)|(\\r\\n)){2}|(\\r){2}|(\\n){2}");

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
    static DataSource create() {
        return new EmbeddedPostgresDataSource().dataSource;
    }

    private void createDatabaseTables() throws IOException, URISyntaxException, SQLException {
        final List<String> sqlFiles = List.of("init-db.sql", "system-users.sql");

        for (final String sqlFile : sqlFiles) {
            final Path initScript =
                Paths.get(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("db/" + sqlFile)).toURI());

            final List<String> createStatements = Arrays.stream(BLANK_LINE_PATTERN.split(Files.readString(initScript)))
                .map(String::trim)
                .toList();

            try (final Connection connection = dataSource.getConnection();
                 final Statement statement = connection.createStatement()) {
                for (final String createStatement : createStatements) {
                    statement.execute(createStatement);
                }
            }
        }
    }
}
