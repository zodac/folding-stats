/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.bean.tc.user;

import java.util.Collection;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.api.tc.change.UserChange;
import net.zodac.folding.api.tc.change.UserChangeState;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.db.postgres.DatabaseConnectionException;
import net.zodac.folding.rest.exception.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class used to apply a {@link UserChange} to the related {@link User}.
 */
@Component
public class UserChangeApplier {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FoldingRepository foldingRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     */
    @Autowired
    public UserChangeApplier(final FoldingRepository foldingRepository) {
        this.foldingRepository = foldingRepository;
    }

    /**
     * Applies all {@link UserChange}s in {@link UserChangeState#APPROVED_NEXT_MONTH}.
     *
     * <p>
     * If the application of any {@link UserChange} fails, it will be logged, but will not stop execution of the remaining {@link UserChange}s
     *
     * @see #apply(UserChange)
     * @see FoldingRepository#getAllUserChangesForNextMonth()
     */
    public void applyAllForNextMonth() {
        final Collection<UserChange> nextMonthUserChanges = foldingRepository.getAllUserChangesForNextMonth();

        for (final UserChange userChange : nextMonthUserChanges) {
            try {
                apply(userChange);
            } catch (final NotFoundException e) {
                LOGGER.warn("User does not exist for change '{}'", userChange, e);
            } catch (final DatabaseConnectionException e) {
                LOGGER.warn("Error updating user change '{}'", userChange, e);
            } catch (final Exception e) {
                LOGGER.warn("Unexpected error occurred applying user change '{}'", userChange, e);
            }
        }
    }

    /**
     * Applies a {@link UserChange}.
     *
     * <p>
     * First we retrieve the {@link User} defined in the {@link UserChange}. Then we update the {@link User} with the new details from the
     * {@link UserChange}. Finally, we update the {@link UserChange} to {@link UserChangeState#COMPLETED}.
     *
     * @param userChange the {@link UserChange} to apply
     * @return the applied {@link UserChange}
     * @see FoldingRepository#getUserWithPasskey(int)
     * @see FoldingRepository#updateUser(User, User)
     */
    public UserChange apply(final UserChange userChange) {
        LOGGER.info("Applying user change '{}' for: {}", userChange.id(), userChange.newUser().displayName());

        // Explicitly retrieve the user, in case it was deleted after the change was requested
        final User existingUser = foldingRepository.getUserWithPasskey(userChange.newUser().id());

        foldingRepository.updateUser(userChange.newUser(), existingUser);
        LOGGER.debug("Updated user");

        final UserChange userChangeToUpdate = UserChange.updateWithState(UserChangeState.COMPLETED, userChange);
        LOGGER.debug("Updating user change to complete");
        return foldingRepository.updateUserChange(userChangeToUpdate);
    }
}
