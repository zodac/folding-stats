package me.zodac.folding.db.postgres;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import me.zodac.folding.api.db.DbConnectionPool;
import me.zodac.folding.api.exception.DatabaseConnectionException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import javax.sql.DataSource;
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

import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link DbConnectionPool} to use an {@link EmbeddedPostgres} DB for tests.
 */
public final class TestDbConnectionPool implements DbConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDbConnectionPool.class);

    private transient final DataSource dataSource;

    private TestDbConnectionPool() {
        try {
            LOGGER.info(() -> "Configuring test DB...");
            dataSource = EmbeddedPostgres.builder().start().getPostgresDatabase();
            createDatabaseTables();
        } catch (final Exception e) {
            throw new AssertionError("Unable to start test DB", e);
        }
    }

    /**
     * Creates an instance of {@link TestDbConnectionPool}.
     *
     * @return the created {@link TestDbConnectionPool}
     */
    public static TestDbConnectionPool create() {
        return new TestDbConnectionPool();
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new DatabaseConnectionException("Error opening connection", e);
        }
    }

    private void createDatabaseTables() throws IOException, URISyntaxException, SQLException {
        final List<String> sqlFiles = List.of("init-db.sql", "system-users.sql");

        for (final String sqlFile : sqlFiles) {
            final Path initScript = Paths.get(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("db/" + sqlFile)).toURI());

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
