package me.zodac.folding.db;

import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.db.postgres.PostgresDbManager;
import me.zodac.folding.util.EnvironmentUtils;

import java.util.Locale;

/**
 * Utility class used to retrieve an instance of {@link DbManager} for the system.
 */
public class DbManagerRetriever {

    private DbManagerRetriever() {

    }

    /**
     * Returns a concrete implementation of {@link DbManager}, based on the value of the <b>DEPLOYED_DATABASE</b> environment variable.
     *
     * @return the {@link DbManager} instance
     */
    public static DbManager get() {
        final String deployedDatabase = EnvironmentUtils.getEnvironmentValue("DEPLOYED_DATABASE");

        if ("postgres".equals(deployedDatabase.toLowerCase(Locale.UK))) {
            return new PostgresDbManager();
        }
        throw new IllegalStateException(String.format("Unable to find database of type: %s", deployedDatabase));
    }
}
