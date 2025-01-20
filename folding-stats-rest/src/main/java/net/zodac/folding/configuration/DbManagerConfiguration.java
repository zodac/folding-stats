/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.configuration;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.function.Supplier;
import javax.sql.DataSource;
import net.zodac.folding.api.db.DbManager;
import net.zodac.folding.db.DatabaseType;
import net.zodac.folding.db.postgres.PostgresDataSource;
import net.zodac.folding.db.postgres.PostgresDbManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} class allowing for the injection of {@link DbManager} instances.
 */
@Configuration
public class DbManagerConfiguration {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DATABASE_VARIABLE_NAME = "DEPLOYED_DATABASE";

    /**
     * Returns an implementation of {@link DbManager} as a {@link Bean} for {@link org.springframework.beans.factory.annotation.Autowired} injection.
     *
     * @param dataSourceUrl          the JDBC URL for the data source
     * @param dataSourceDriver       the driver for the data source
     * @param dataSourceUsername     the username for the data source
     * @param dataSourcePassword     the password URL for the data source
     * @param dataSourceDatabaseType the type of database for the data source
     * @return the {@link DbManager} implementation
     */
    @Bean
    public DbManager getDbManager(@Value("${spring.datasource.url}") final String dataSourceUrl,
                                  @Value("${spring.datasource.driver}") final String dataSourceDriver,
                                  @Value("${spring.datasource.username}") final String dataSourceUsername,
                                  @Value("${spring.datasource.password}") final String dataSourcePassword,
                                  @Value("${spring.datasource.database.type}") final String dataSourceDatabaseType) {
        final DatabaseType databaseType = DatabaseType.get(dataSourceDatabaseType);

        if (databaseType == DatabaseType.POSTGRESQL) {
            LOGGER.info("Initialising {} of type '{}'", DbManager.class.getSimpleName(), DatabaseType.POSTGRESQL);
            final Supplier<PostgresDataSource> supplier = Retry.decorateSupplier(retry(), () -> PostgresDataSource.create(
                dataSourceUrl,
                dataSourceDriver,
                dataSourceUsername,
                dataSourcePassword
            ));
            final DataSource postgresDataSource = supplier.get();
            return PostgresDbManager.create(postgresDataSource);
        }

        throw new IllegalStateException(String.format("Unable to find database of type using variable '%s': %s",
            DATABASE_VARIABLE_NAME, dataSourceDatabaseType));
    }

    private static Retry retry() {
        final RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(10)
            .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(10L)))
            .build();
        return Retry.of("DatabaseConnection", retryConfig);
    }
}
