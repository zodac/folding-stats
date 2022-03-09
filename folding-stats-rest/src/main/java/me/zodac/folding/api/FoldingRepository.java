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

package me.zodac.folding.api;

import java.util.Collection;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.bean.Storage;
import me.zodac.folding.bean.tc.user.UserStatsParser;
import me.zodac.folding.rest.exception.NotFoundException;
import me.zodac.folding.state.ParsingStateManager;

/**
 * Interface used for CRUD operations for <code>folding-stats</code> classes:
 * <ul>
 *     <li>{@link Hardware}</li>
 *     <li>{@link Team}</li>
 *     <li>{@link User}</li>
 * </ul>
 *
 * <p>
 * For the most part, this will serve as a wrapper to {@link Storage}, which knows how to perform CRUD operations on the backend storage and caches.
 * But since some logic is needed for special cases (like retrieving the latest Folding@Home stats for a {@link User} when it is created), we
 * implement that logic here, and delegate any CRUD needs to {@link Storage}.
 */
public interface FoldingRepository {

    /**
     * Creates a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to create
     * @return the created {@link Hardware}, with ID
     */
    Hardware createHardware(final Hardware hardware);

    /**
     * Retrieves all {@link Hardware}s.
     *
     * @return a {@link Collection} of the retrieved {@link Hardware}
     */
    Collection<Hardware> getAllHardware();

    /**
     * Retrieves a {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return the retrieved {@link Hardware}
     * @throws NotFoundException thrown if the {@link Hardware} cannot be found
     */
    Hardware getHardware(final int hardwareId);

    /**
     * Updates an existing {@link Hardware}.
     *
     * <p>
     * Also handles state change to any {@link User}s using this {@link Hardware} if necessary.
     *
     * @param hardwareToUpdate the {@link Hardware} with updated values
     * @param existingHardware the existing {@link Hardware}
     * @return the updated {@link Hardware}
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
     * <p>
     * If the system is not in {@link ParsingState#DISABLED}, we will also update stats for all {@link User}s.
     *
     * @param team the {@link Team} to create
     * @return the created {@link Team}, with ID
     * @see ParsingStateManager
     * @see UserStatsParser#parseTcStatsForUsersAndWait(Collection)
     */
    Team createTeam(final Team team);

    /**
     * Retrieves all {@link Team}.
     *
     * @return a {@link Collection} of the retrieved {@link Team}s
     */
    Collection<Team> getAllTeams();

    /**
     * Retrieves a {@link Team}.
     *
     * @param teamId the ID of the {@link Team} to retrieve
     * @return the retrieved {@link Team}
     * @throws NotFoundException thrown if the {@link Team} cannot be found
     */
    Team getTeam(final int teamId);

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
     */
    User createUser(final User user);

    /**
     * Retrieves all {@link User}s, with passkeys unmodified.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     */
    Collection<User> getAllUsersWithPasskeys();

    /**
     * Retrieves all {@link User}s, with passkeys masked.
     *
     * @return a {@link Collection} of the retrieved {@link User}s
     * @see User#hidePasskey(User)
     */
    Collection<User> getAllUsersWithoutPasskeys();

    /**
     * Retrieves a {@link User}, with the passkey unmodified.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return the retrieved {@link User}
     * @throws NotFoundException thrown if the {@link User} cannot be found
     */
    User getUserWithPasskey(final int userId);

    /**
     * Retrieves a {@link User}, with the passkey masked.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return the retrieved {@link User} with no passkey exposed
     * @throws NotFoundException thrown if the {@link User} cannot be found
     * @see User#hidePasskey(User)
     */
    User getUserWithoutPasskey(final int userId);

    /**
     * Updates an existing {@link User}.
     *
     * <p>
     * Also handles state change to this {@link User} if necessary.
     *
     * @param userToUpdate the {@link User} with updated values
     * @param existingUser the existing {@link User}
     * @return the updated {@link User}
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
     * Creates a {@link UserChange}.
     *
     * @param userChange the {@link UserChange} to create
     * @return the created {@link UserChange}, with ID
     */
    UserChange createUserChange(final UserChange userChange);

    /**
     * Retrieves all {@link UserChange}s, with {@link User} passkeys unmodified.
     *
     * @return a {@link Collection} of the retrieved {@link UserChange}
     */
    Collection<UserChange> getAllUserChangesWithPasskeys();

    /**
     * Retrieves all {@link UserChange}s with any of the given {@link UserChangeState}s, with {@link User} passkeys unmodified.
     *
     * @param states the {@link UserChangeState}s to look for
     * @return a {@link Collection} of the retrieved {@link UserChange}
     */
    Collection<UserChange> getAllUserChangesWithPasskeys(final Collection<UserChangeState> states);

    /**
     * Retrieves all {@link UserChange}s, with {@link User} passkeys masked.
     *
     * @return a {@link Collection} of the retrieved {@link UserChange}
     */
    Collection<UserChange> getAllUserChangesWithoutPasskeys();

    /**
     * Retrieves all {@link UserChange}s with any of the given {@link UserChangeState}s, with {@link User} passkeys masked.
     *
     * @param states the {@link UserChangeState}s to look for
     * @return a {@link Collection} of the retrieved {@link UserChange}
     */
    Collection<UserChange> getAllUserChangesWithoutPasskeys(final Collection<UserChangeState> states);

    /**
     * Retrieves all {@link UserChange}s that have been approved for {@link UserChangeState#APPROVED_NEXT_MONTH}.
     *
     * @return a {@link Collection} of the retrieved {@link UserChange}
     */
    Collection<UserChange> getAllUserChangesForNextMonth();

    /**
     * Retrieves a {@link UserChange}.
     *
     * @param userChangeId the ID of the {@link UserChange} to retrieve
     * @return the retrieved {@link UserChange}
     * @throws NotFoundException thrown if the {@link UserChange} cannot be found
     */
    UserChange getUserChange(final int userChangeId);

    /**
     * Updates an existing {@link UserChange}.
     *
     * @param userChangeToUpdate the {@link UserChange} with updated values
     * @return the updated {@link UserChange}
     */
    UserChange updateUserChange(final UserChange userChangeToUpdate);

    /**
     * Debug function which will print the contents of any caches being used to the system log.
     */
    void printCacheContents();
}
