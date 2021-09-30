package me.zodac.folding.db;

import java.util.EnumMap;
import java.util.Map;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.db.postgres.PostgresDbConnectionPool;
import me.zodac.folding.db.postgres.PostgresDbManager;

/**
 * Utility class used to retrieve an instance of {@link DbManager} for the system.
 */
public final class DbManagerRetriever {

    private static final String DATABASE_VARIABLE_NAME = "DEPLOYED_DATABASE";
    private static final Map<DatabaseType, DbManager> DB_MANAGER_BY_DATABASE = new EnumMap<>(DatabaseType.class);

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

        switch (databaseType) {
            case POSTGRESQL: {
                DB_MANAGER_BY_DATABASE.putIfAbsent(DatabaseType.POSTGRESQL, PostgresDbManager.create(PostgresDbConnectionPool.create()));
                return DB_MANAGER_BY_DATABASE.get(DatabaseType.POSTGRESQL);
            }
            case INVALID:
            default:
                throw new IllegalStateException(String.format("Unable to find database of type using variable '%s': %s",
                    DATABASE_VARIABLE_NAME, deployedDatabase));
        }
    }
}
