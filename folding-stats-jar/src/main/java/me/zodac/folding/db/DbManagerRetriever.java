/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.db;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.sql.DataSource;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.db.postgres.PostgresDataSource;
import me.zodac.folding.db.postgres.PostgresDbManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to retrieve an instance of {@link DbManager} for the system.
 */
// TODO: Get rid of this, inject the DbManager in folding-stats-rest instead
// TODO: Also introduce TestContainers for testing
public final class DbManagerRetriever {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String DATABASE_VARIABLE_NAME = "DEPLOYED_DATABASE";
    private static final Map<DatabaseType, DbManager> DB_MANAGER_BY_TYPE = new EnumMap<>(DatabaseType.class);

    private DbManagerRetriever() {

    }

    /**
     * Returns a concrete implementation of {@link DbManager}, based on the value of the <b>DEPLOYED_DATABASE</b> environment variable.
     *
     * <p>
     * Supported DBs are defined by {@link DatabaseType}.
     *
     * @return the {@link DbManager} instance
     */
    public static DbManager get() {
        final String deployedDatabase = EnvironmentVariableUtils.get(DATABASE_VARIABLE_NAME);
        final DatabaseType databaseType = DatabaseType.get(deployedDatabase);

        if (DB_MANAGER_BY_TYPE.containsKey(databaseType)) {
            LOGGER.trace("Found existing {} of type '{}'", DbManager.class.getSimpleName(), databaseType);
            return DB_MANAGER_BY_TYPE.get(databaseType);
        }

        if (databaseType == DatabaseType.POSTGRESQL) {
            final Supplier<PostgresDataSource> supplier = Retry.decorateSupplier(retry(), PostgresDataSource::create);
            final DataSource postgresDataSource = supplier.get();
            final PostgresDbManager postgresDbManager = PostgresDbManager.create(postgresDataSource);
            DB_MANAGER_BY_TYPE.put(DatabaseType.POSTGRESQL, postgresDbManager);
            return postgresDbManager;
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