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

package me.zodac.folding.configuration;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.function.Supplier;
import javax.sql.DataSource;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.db.DatabaseType;
import me.zodac.folding.db.postgres.PostgresDataSource;
import me.zodac.folding.db.postgres.PostgresDbManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
     * @return the {@link DbManager} implementation
     */
    @Bean
    public DbManager getDbManager() {
        final String deployedDatabase = EnvironmentVariableUtils.get(DATABASE_VARIABLE_NAME);
        final DatabaseType databaseType = DatabaseType.get(deployedDatabase);

        if (databaseType == DatabaseType.POSTGRESQL) {
            LOGGER.info("Initialising {} of type '{}'", DbManager.class.getSimpleName(), DatabaseType.POSTGRESQL);
            final Supplier<PostgresDataSource> supplier = Retry.decorateSupplier(retry(), PostgresDataSource::create);
            final DataSource postgresDataSource = supplier.get();
            return PostgresDbManager.create(postgresDataSource);
        }

        throw new IllegalStateException(String.format("Unable to find database of type using variable '%s': %s",
            DATABASE_VARIABLE_NAME, deployedDatabase));
    }

    private static Retry retry() {
        final RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(10)
            .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(10L)))
            .build();
        return Retry.of("DatabaseConnection", retryConfig);
    }
}
