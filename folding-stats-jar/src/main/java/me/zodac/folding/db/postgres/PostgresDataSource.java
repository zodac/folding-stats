/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
     * @param dataSourceUrl      the JDBC URL for the data source
     * @param dataSourceDriver   the driver for the data source
     * @param dataSourceUsername the username for the data source
     * @param dataSourcePassword the password URL for the data source
     * @return the created {@link PostgresDataSource}
     */
    public static PostgresDataSource create(final String dataSourceUrl,
                                            final String dataSourceDriver,
                                            final String dataSourceUsername,
                                            final String dataSourcePassword) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dataSourceUrl);
        hikariConfig.setDriverClassName(dataSourceDriver);
        hikariConfig.setUsername(dataSourceUsername);
        hikariConfig.setPassword(dataSourcePassword);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        LOGGER.debug("Connecting to DB...");
        return new PostgresDataSource(hikariConfig);
    }
}
