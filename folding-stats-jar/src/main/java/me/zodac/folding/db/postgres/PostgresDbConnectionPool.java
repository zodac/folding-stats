package me.zodac.folding.db.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.zodac.folding.api.utils.EnvironmentVariables;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Pool of PostgreSQL DB {@link Connection}s.
 */
public final class PostgresDbConnectionPool {

    private static final HikariConfig DATA_SOURCE_CONFIG = new HikariConfig();
    private static final HikariDataSource DATA_SOURCE_POOL;

    static {
        DATA_SOURCE_CONFIG.setJdbcUrl(EnvironmentVariables.get("JDBC_CONNECTION_URL"));
        DATA_SOURCE_CONFIG.setUsername(EnvironmentVariables.get("JDBC_CONNECTION_USER"));
        DATA_SOURCE_CONFIG.setPassword(EnvironmentVariables.get("JDBC_CONNECTION_PASSWORD"));
        DATA_SOURCE_CONFIG.setDriverClassName(EnvironmentVariables.get("JDBC_CONNECTION_DRIVER"));

        DATA_SOURCE_CONFIG.addDataSourceProperty("cachePrepStmts", "true");
        DATA_SOURCE_CONFIG.addDataSourceProperty("prepStmtCacheSize", "250");
        DATA_SOURCE_CONFIG.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        DATA_SOURCE_POOL = new HikariDataSource(DATA_SOURCE_CONFIG);
    }

    private PostgresDbConnectionPool() {

    }

    /**
     * Retrieve a DB {@link Connection} from the pool.
     *
     * @return a DB {@link Connection}
     * @throws SQLException thrown if an error accessing the {@link Connection} occurs
     */
    // TODO: [zodac] Wrap this SQLException with a FoldingConnectionException, remove the connection CATCH block in PostgresDbManager
    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE_POOL.getConnection();
    }
}
