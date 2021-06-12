package me.zodac.folding.api.db;

import java.sql.Connection;
import me.zodac.folding.api.exception.DatabaseConnectionException;

/**
 * Interface for a DB {@link Connection} pool.
 */
public interface DbConnectionPool {

    /**
     * Retrieve a DB {@link Connection} from the pool.
     *
     * @return a DB {@link Connection}
     * @throws DatabaseConnectionException thrown if an error opening the {@link Connection} occurs
     */
    Connection getConnection();
}
