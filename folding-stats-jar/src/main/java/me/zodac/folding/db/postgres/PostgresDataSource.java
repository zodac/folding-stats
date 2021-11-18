/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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
import javax.sql.DataSource;
import me.zodac.folding.api.util.EnvironmentVariableUtils;

/**
 * <b>PostgreSQL</b> implementation of {@link HikariDataSource}.
 */
public final class PostgresDataSource extends HikariDataSource {

    private static final HikariConfig DATA_SOURCE_CONFIG = new HikariConfig();

    static {
        DATA_SOURCE_CONFIG.setJdbcUrl(EnvironmentVariableUtils.get("JDBC_CONNECTION_URL"));
        DATA_SOURCE_CONFIG.setUsername(EnvironmentVariableUtils.get("JDBC_CONNECTION_USER"));
        DATA_SOURCE_CONFIG.setPassword(EnvironmentVariableUtils.get("JDBC_CONNECTION_PASSWORD"));
        DATA_SOURCE_CONFIG.setDriverClassName(EnvironmentVariableUtils.get("JDBC_CONNECTION_DRIVER"));

        DATA_SOURCE_CONFIG.addDataSourceProperty("cachePrepStmts", "true");
        DATA_SOURCE_CONFIG.addDataSourceProperty("prepStmtCacheSize", "250");
        DATA_SOURCE_CONFIG.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    }

    private PostgresDataSource() {
        super(DATA_SOURCE_CONFIG);
    }

    /**
     * Creates an instance of {@link PostgresDataSource}.
     *
     * @return the created {@link PostgresDataSource}
     */
    public static DataSource create() {
        return new PostgresDataSource();
    }
}
