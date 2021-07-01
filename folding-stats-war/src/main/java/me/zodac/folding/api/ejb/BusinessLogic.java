package me.zodac.folding.api.ejb;

import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;

/**
 * In order to decouple the REST layer from any business requirements, we move that logic into this interface, to be
 * implemented as an EJB. This should simplify the REST layer to simply validate incoming requests and forward to here.
 */
public interface BusinessLogic {

    /**
     * Creates a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to create
     * @return the created {@link Hardware}, with ID
     */
    Hardware createHardware(final Hardware hardware);

    /**
     * Retrieves a {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return an {@link Optional} of the retrieved {@link Hardware}
     */
    Optional<Hardware> getHardware(final int hardwareId);

    /**
     * Retrieves all {@link Hardware}.
     *
     * @return a {@link Collection} of the retrieved {@link Hardware}
     */
    Collection<Hardware> getAllHardware();

    /**
     * Deletes a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to delete
     */
    void deleteHardware(final Hardware hardware);

    /**
     * Creates a {@link Team}.
     *
     * @param team the {@link Team} to create
     * @return the created {@link Team}, with ID
     */
    Team createTeam(final Team team);

    /**
     * Retrieves a {@link Team}.
     *
     * @param teamId the ID of the {@link Team} to retrieve
     * @return an {@link Optional} of the retrieved {@link Team}
     */
    Optional<Team> getTeam(final int teamId);

    /**
     * Retrieves all {@link Team}.
     *
     * @return a {@link Collection} of the retrieved {@link Team}s
     */
    Collection<Team> getAllTeams();

    /**
     * Deletes a {@link Team}.
     *
     * @param team the {@link Team} to delete
     */
    void deleteTeam(final Team team);

    /**
     * Retrieves a {@link User}, with the passkey unmodified.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     */
    Optional<User> getUserWithPasskey(final int userId);

    /**
     * Retrieves a {@link User}, with the passkey masked.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return an {@link Optional} of the retrieved {@link User}
     */
    Optional<User> getUserWithoutPasskey(final int userId);

    /**
     * Retrieves all {@link User}, with the passkey unmodified.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     */
    Collection<User> getAllUsersWithPasskeys();

    /**
     * Retrieves all {@link User}, with the passkey masked.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     */
    Collection<User> getAllUsersWithoutPasskeys();
    
    /**
     * Retrieves a {@link Hardware} with the given name.
     *
     * <p>
     * Checks {@link #getAllHardware()} for a case-insensitive match on the {@link Hardware} name.
     *
     * <p>
     * If there is more than one result (though there should not be!), there is no guarantee on which will be
     * returned.
     *
     * @param hardwareName the name of the {@link Hardware}
     * @return an {@link Optional} of the matching {@link Hardware}
     */
    Optional<Hardware> getHardwareWithName(final String hardwareName);

    /**
     * Retrieves a {@link Team} with the given name.
     *
     * <p>
     * Checks {@link #getAllTeams()} for a case-insensitive match on the {@link Team} name.
     *
     * <p>
     * If there is more than one result (though there should not be!), there is no guarantee on which will be
     * returned.
     *
     * @param teamName the name of the {@link Team}
     * @return an {@link Optional} of the matching {@link Team}
     */
    Optional<Team> getTeamWithName(final String teamName);

    /**
     * Retrieves all {@link User}s currently referencing the provided {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to check for
     * @return a {@link Collection} of {@link User}s using the {@link Hardware}
     */
    Collection<User> getUsersWithHardware(final Hardware hardware);

    /**
     * Retrieves all {@link User}s currently referencing the provided {@link Team}.
     *
     * @param team the {@link Team} to check for
     * @return a {@link Collection} of {@link User}s using the {@link Team}
     */
    Collection<User> getUsersOnTeam(final Team team);

    /**
     * Retrieves a {@link User} with the matching {@code foldingUserName} and {@code passkey}. Since these are unique fields for a
     * {@link User} there should only be a single{@link User} returned (or none).
     *
     * @param foldingUserName the Folding@Home user name of the {@link User} to be found
     * @param passkey         the passkey of the {@link User} to be found
     * @return an {@link Optional} of the matching {@link User}
     */
    Optional<User> getUserWithFoldingUserNameAndPasskey(final String foldingUserName, final String passkey);

}
