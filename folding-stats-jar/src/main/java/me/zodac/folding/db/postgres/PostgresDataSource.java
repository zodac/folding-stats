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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <b>PostgreSQL</b> implementation of {@link HikariDataSource}.
 */
public final class PostgresDataSource extends HikariDataSource {

    private static final Logger LOGGER = LogManager.getLogger();

    private PostgresDataSource(final HikariConfig configuration) {
        super(configuration);
    }

    /**
     * Creates an instance of {@link PostgresDataSource}.
     *
     * <p>
     * Uses the following environment variables:
     * <ul>
     *     <li>JDBC_CONNECTION_URL</li>
     *     <li>JDBC_CONNECTION_USER</li>
     *     <li>JDBC_CONNECTION_PASSWORD</li>
     *     <li>JDBC_CONNECTION_DRIVER</li>
     * </ul>
     *
     * @return the created {@link PostgresDataSource}
     */
    public static PostgresDataSource create() {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(EnvironmentVariableUtils.get("JDBC_CONNECTION_URL"));
        hikariConfig.setUsername(EnvironmentVariableUtils.get("JDBC_CONNECTION_USER"));
        hikariConfig.setPassword(EnvironmentVariableUtils.get("JDBC_CONNECTION_PASSWORD"));
        hikariConfig.setDriverClassName(EnvironmentVariableUtils.get("JDBC_CONNECTION_DRIVER"));
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        LOGGER.debug("Connecting to DB...");
        return new PostgresDataSource(hikariConfig);
    }
}