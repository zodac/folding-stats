package me.zodac.folding.db.postgres;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import me.zodac.folding.api.db.DbConnectionPool;

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
public class TestDbConnectionPool implements DbConnectionPool {

    private final DataSource dataSource;

    public TestDbConnectionPool() {
        try {
            dataSource = EmbeddedPostgres.builder().start().getPostgresDatabase();
            createDatabaseTables();
        } catch (final Exception e) {
            throw new AssertionError("Unable to start test DB", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    private void createDatabaseTables() throws IOException, URISyntaxException, SQLException {
        final Path initScript = Paths.get(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("db/init-db.sql")).toURI());

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