package me.zodac.folding.api.db;

import me.zodac.folding.api.SystemUserAuthentication;

/**
 * Interface used to interact with the storage backend and perform {@link SystemUserAuthentication} operations.
 */
public interface DbAuthenticationManager {

    SystemUserAuthentication authenticateSystemUser(final String userName, final String password);
}
