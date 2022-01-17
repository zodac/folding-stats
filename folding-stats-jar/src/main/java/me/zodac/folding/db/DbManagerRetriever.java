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

package me.zodac.folding.db;

import java.util.EnumMap;
import java.util.Map;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.db.postgres.PostgresDataSource;
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
                DB_MANAGER_BY_DATABASE.putIfAbsent(DatabaseType.POSTGRESQL, PostgresDbManager.create(PostgresDataSource.create()));
                return DB_MANAGER_BY_DATABASE.get(DatabaseType.POSTGRESQL);
            }
            case INVALID:
            default:
                throw new IllegalStateException(String.format("Unable to find database of type using variable '%s': %s",
                    DATABASE_VARIABLE_NAME, deployedDatabase));
        }
    }
}
