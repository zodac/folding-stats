package me.zodac.folding.db;

import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.utils.EnvironmentVariables;
import me.zodac.folding.db.postgres.PostgresDbManager;

import java.util.Locale;

/**
 * Utility class used to retrieve an instance of {@link DbManager} for the system.
 */
public class DbManagerRetriever {

    private static final String DATABASE_VARIABLE_NAME = "DEPLOYED_DATABASE";

    private DbManagerRetriever() {

    }

    /**
     * Returns a concrete implementation of {@link DbManager}, based on the value of the <b>DEPLOYED_DATABASE</b> environment variable.
     *
     * @return the {@link DbManager} instance
     */
    public static DbManager get() {
        final String deployedDatabase = EnvironmentVariables.get(DATABASE_VARIABLE_NAME);

        if ("postgres".equals(deployedDatabase.toLowerCase(Locale.UK))) {
            return new PostgresDbManager();
        }
        throw new IllegalStateException(String.format("Unable to find database of type using variable '%s': %s", DATABASE_VARIABLE_NAME, deployedDatabase));
    }
}
