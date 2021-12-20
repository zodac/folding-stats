/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.rest.api;

import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import org.springframework.stereotype.Service;

/**
 * Interface defining the core business logic of the system.
 *
 * <p>
 * In order to decouple the REST layer from any business requirements, we move that logic into this interface, to be
 * implemented as an EJB. This should simplify the REST layer to simply validate incoming requests and forward to here.
 */
@Service
public interface FoldingService {

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
     * Updates an existing {@link Hardware}.
     *
     * <p>
     * Also handles state change to any {@link User}s using this {@link Hardware} if necessary.
     *
     * @param hardwareToUpdate the {@link Hardware} with updated values
     * @param existingHardware the existing {@link Hardware}
     * @return the updated {@link Hardware}
     * //     * @see me.zodac.folding.core.tc.user.UserStateChangeHandler#isHardwareStateChange(Hardware, Hardware)
     */
    Hardware updateHardware(final Hardware hardwareToUpdate, final Hardware existingHardware);

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
     * Updates an existing {@link Team}.
     *
     * @param teamToUpdate the {@link Team} with updated values
     * @return the updated {@link Team}
     */
    Team updateTeam(final Team teamToUpdate);

    /**
     * Deletes a {@link Team}.
     *
     * @param team the {@link Team} to delete
     */
    void deleteTeam(final Team team);

    /**
     * Creates a {@link User}.
     *
     * <p>
     * Creates initial {@link UserStats} on creation. Also triggers a new <code>Team Competition</code> stats parse.
     *
     * @param user the {@link User} to create
     * @return the created {@link User}, with ID
     * //     * @see me.zodac.folding.core.tc.user.UserStatsParser#parseTcStatsForUser(User)
     */
    User createUser(final User user);

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
     * Updates an existing {@link User}.
     *
     * <p>
     * Also handles state change to this {@link User} if necessary.
     *
     * @param userToUpdate the {@link User} with updated values
     * @param existingUser the existing {@link User}
     * @return the updated {@link User}
     * //     * @see me.zodac.folding.core.tc.user.UserStateChangeHandler#isUserStateChange(User, User)
     */
    User updateUser(final User userToUpdate, final User existingUser);

    /**
     * Deletes a {@link User}.
     *
     * <p>
     * If the {@link User} has any <code>Team Competition</code> {@link UserTcStats}, those are retained for their {@link Team} as
     * {@link RetiredUserTcStats}.
     *
     * @param user the {@link User} to delete
     */
    void deleteUser(final User user);

    /**
     * Retrieves all {@link User}s currently referencing the provided {@link Team}.
     *
     * <p>
     * The {@link User} {@code passkey} will be masked with {@link User#hidePasskey(User)}.
     *
     * @param team the {@link Team} to check for
     * @return a {@link Collection} of {@link User}s using the {@link Team}
     */
    Collection<User> getUsersOnTeam(final Team team);

    /**
     * Retrieves all {@link User}s currently referencing the provided {@link Team}.
     *
     * <p>
     * The {@link User} {@code passkey} will be available in plaintext, so should only be used for internal processing.
     *
     * @param team the {@link Team} to check for
     * @return a {@link Collection} of {@link User}s using the {@link Team}
     */
    Collection<User> getUsersOnTeamWithPasskeys(final Team team);

    /**
     * Authenticates a system user and retrieves its roles.
     *
     * <p>
     * The following scenarios are considered:
     * <ul>
     *     <li>The user does not exist</li>
     *     <li>The user exists but the password is incorrect</li>
     *     <li>The user exists, and the password is correct</li>
     * </ul>
     *
     * @param userName the system user username
     * @param password the system user password
     * @return the {@link UserAuthenticationResult}
     */
    UserAuthenticationResult authenticateSystemUser(final String userName, final String password);

    /**
     * Debug function which will print the contents of any caches being used to the system log.
     */
    void printCacheContents();
}
