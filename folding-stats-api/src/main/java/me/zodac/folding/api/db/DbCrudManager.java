package me.zodac.folding.api.db;

import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.SystemUserAuthentication;
import me.zodac.folding.api.exception.DatabaseConnectionException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;

/**
 * Interface used to interact with the storage backend and perform CRUD operations on:
 * <ul>
 *     <li>{@link Hardware}</li>
 *     <li>{@link Team}</li>
 *     <li>{@link User}</li>
 * </ul>
 */
public interface DbCrudManager {

    /**
     * Creates a {@link Hardware} instance in the DB.
     *
     * @param hardware the {@link Hardware} to persist
     * @return the {@link Hardware} updated with an ID
     * @throws DatabaseConnectionException thrown on error persisting the {@link Hardware}
     */
    Hardware createHardware(final Hardware hardware);

    Collection<Hardware> getAllHardware();

    Optional<Hardware> getHardware(final int hardwareId);

    void updateHardware(final Hardware hardware);

    void deleteHardware(final int hardwareId);

    Team createTeam(final Team team);

    Collection<Team> getAllTeams();

    Optional<Team> getTeam(final int foldingTeamId);

    void updateTeam(final Team team);

    void deleteTeam(final int teamId);

    User createUser(final User user);

    Collection<User> getAllUsers();

    Optional<User> getUser(final int userId);

    void updateUser(final User user);

    void deleteUser(final int userId);

    SystemUserAuthentication authenticateSystemUser(final String userName, final String password);
}
