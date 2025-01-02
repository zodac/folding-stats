/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.bean.api;

import java.util.Collection;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.util.DecodedLoginCredentials;
import me.zodac.folding.bean.tc.user.UserStatsParser;
import me.zodac.folding.rest.exception.NotFoundException;
import me.zodac.folding.state.ParsingStateManager;

/**
 * Interface used for CRUD operations for {@code folding-stats} classes:
 * <ul>
 *     <li>{@link Hardware}</li>
 *     <li>{@link Team}</li>
 *     <li>{@link User}</li>
 *     <li>{@link UserChange}</li>
 * </ul>
 */
public interface FoldingRepository {

    /**
     * Creates a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to create
     * @return the created {@link Hardware}, with ID
     */
    Hardware createHardware(Hardware hardware);

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
    Hardware getHardware(int hardwareId);

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
    Hardware updateHardware(Hardware hardwareToUpdate, Hardware existingHardware);

    /**
     * Deletes a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to delete
     */
    void deleteHardware(Hardware hardware);

    /**
     * Creates a {@link Team}.
     *
     * <p>
     * If the system is not in {@link ParsingState#DISABLED}, we will also update stats for all {@link User}s.
     *
     * @param team the {@link Team} to create
     * @return the created {@link Team}, with ID
     * @see ParsingStateManager
     * @see UserStatsParser#parseTcStatsForUsers(Iterable)
     */
    Team createTeam(Team team);

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
    Team getTeam(int teamId);

    /**
     * Updates an existing {@link Team}.
     *
     * @param teamToUpdate the {@link Team} with updated values
     * @return the updated {@link Team}
     */
    Team updateTeam(Team teamToUpdate);

    /**
     * Deletes a {@link Team}.
     *
     * @param team the {@link Team} to delete
     */
    void deleteTeam(Team team);

    /**
     * Creates a {@link User}.
     *
     * <p>
     * Creates initial {@link UserStats} on creation. Also triggers a new {@code Team Competition} stats parse.
     *
     * @param user the {@link User} to create
     * @return the created {@link User}, with ID
     */
    User createUser(User user);

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
    User getUserWithPasskey(int userId);

    /**
     * Retrieves a {@link User}, with the passkey masked.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return the retrieved {@link User} with no passkey exposed
     * @throws NotFoundException thrown if the {@link User} cannot be found
     * @see User#hidePasskey(User)
     */
    User getUserWithoutPasskey(int userId);

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
    User updateUser(User userToUpdate, User existingUser);

    /**
     * Deletes a {@link User}.
     *
     * <p>
     * If the {@link User} has any {@code Team Competition} {@link UserTcStats}, those are retained for their {@link Team} as
     * {@link RetiredUserTcStats}.
     *
     * @param user the {@link User} to delete
     */
    void deleteUser(User user);

    /**
     * Retrieves all {@link User}s currently referencing the provided {@link Team}.
     *
     * <p>
     * The {@link User} {@code passkey} will be masked with {@link User#hidePasskey(User)}.
     *
     * @param team the {@link Team} to check for
     * @return a {@link Collection} of {@link User}s using the {@link Team}
     */
    Collection<User> getUsersOnTeam(Team team);

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
     * @param decodedLoginCredentials the system user credentials
     * @return the {@link UserAuthenticationResult}
     */
    UserAuthenticationResult authenticateSystemUser(DecodedLoginCredentials decodedLoginCredentials);

    /**
     * Creates a {@link UserChange}.
     *
     * @param userChange the {@link UserChange} to create
     * @return the created {@link UserChange}, with ID
     */
    UserChange createUserChange(UserChange userChange);

    /**
     * Retrieves all {@link UserChange}s with any of the given {@link UserChangeState}s, with {@link User} passkeys unmodified.
     *
     * @param states         the {@link UserChangeState}s to look for
     * @param numberOfMonths the number of months back from which to retrieve {@link UserChange}s (<b>0</b> means retrieve all)
     * @return a {@link Collection} of the retrieved {@link UserChange}
     */
    Collection<UserChange> getAllUserChangesWithPasskeys(Collection<UserChangeState> states, long numberOfMonths);

    /**
     * Retrieves all {@link UserChange}s with any of the given {@link UserChangeState}s, with {@link User} passkeys masked.
     *
     * @param states         the {@link UserChangeState}s to look for
     * @param numberOfMonths the number of months back from which to retrieve {@link UserChange}s (<b>0</b> means retrieve all)
     * @return a {@link Collection} of the retrieved {@link UserChange}
     */
    Collection<UserChange> getAllUserChangesWithoutPasskeys(Collection<UserChangeState> states, long numberOfMonths);

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
    UserChange getUserChange(int userChangeId);

    /**
     * Updates an existing {@link UserChange}.
     *
     * @param userChangeToUpdate the {@link UserChange} with updated values
     * @return the updated {@link UserChange}
     */
    UserChange updateUserChange(UserChange userChangeToUpdate);

    /**
     * Debug function which will print the contents of any caches being used to the system log.
     */
    void printCacheContents();
}
