package me.zodac.folding.api.db;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;

import java.util.Collection;
import java.util.Optional;

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
     * @throws FoldingException thrown on error persisting the {@link Hardware}
     */
    Hardware createHardware(final Hardware hardware) throws FoldingException;

    Collection<Hardware> getAllHardware() throws FoldingException;

    Optional<Hardware> getHardware(final int hardwareId) throws FoldingException;

    void updateHardware(final Hardware hardware) throws FoldingException;

    void deleteHardware(final int hardwareId) throws FoldingException;

    Team createTeam(final Team team) throws FoldingException;

    Collection<Team> getAllTeams() throws FoldingException;

    Optional<Team> getTeam(final int foldingTeamId) throws FoldingException;

    void updateTeam(final Team team) throws FoldingException;

    void deleteTeam(final int teamId) throws FoldingException;

    User createUser(final User user) throws FoldingException;

    Collection<User> getAllUsers() throws FoldingException;

    Optional<User> getUser(final int userId) throws FoldingException;

    void updateUser(final User user) throws FoldingException;

    void deleteUser(final int userId) throws FoldingException;

    SystemUserAuthentication authenticateSystemUser(final String userName, final String password) throws FoldingException;
}
