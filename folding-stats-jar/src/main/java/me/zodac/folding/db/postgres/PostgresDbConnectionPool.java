package me.zodac.folding.db.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.zodac.folding.api.db.DbConnectionPool;
import me.zodac.folding.api.utils.EnvironmentVariableUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link DbConnectionPool} for PostgreSQL DB {@link Connection}s.
 */
public final class PostgresDbConnectionPool implements DbConnectionPool {

    private static final HikariConfig DATA_SOURCE_CONFIG = new HikariConfig();
    private static final HikariDataSource DATA_SOURCE_POOL;

    static {
        DATA_SOURCE_CONFIG.setJdbcUrl(EnvironmentVariableUtils.get("JDBC_CONNECTION_URL"));
        DATA_SOURCE_CONFIG.setUsername(EnvironmentVariableUtils.get("JDBC_CONNECTION_USER"));
        DATA_SOURCE_CONFIG.setPassword(EnvironmentVariableUtils.get("JDBC_CONNECTION_PASSWORD"));
        DATA_SOURCE_CONFIG.setDriverClassName(EnvironmentVariableUtils.get("JDBC_CONNECTION_DRIVER"));

        DATA_SOURCE_CONFIG.addDataSourceProperty("cachePrepStmts", "true");
        DATA_SOURCE_CONFIG.addDataSourceProperty("prepStmtCacheSize", "250");
        DATA_SOURCE_CONFIG.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        DATA_SOURCE_POOL = new HikariDataSource(DATA_SOURCE_CONFIG);
    }

    private PostgresDbConnectionPool() {

    }

    public static PostgresDbConnectionPool create() {
        return new PostgresDbConnectionPool();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DATA_SOURCE_POOL.getConnection();
    }
}
