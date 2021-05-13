package me.zodac.folding.api.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for a DB {@link Connection} pool.
 */
public interface DbConnectionPool {

    /**
     * Retrieve a DB {@link Connection} from the pool.
     *
     * @return a DB {@link Connection}
     * @throws SQLException thrown if an error accessing the {@link Connection} occurs
     */
    Connection getConnection() throws SQLException;
}
