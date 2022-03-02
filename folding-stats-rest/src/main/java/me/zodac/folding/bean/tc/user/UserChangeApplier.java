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

package me.zodac.folding.bean.tc.user;

import java.util.Collection;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.bean.FoldingRepository;
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

    @Autowired
    private FoldingRepository foldingRepository;

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
        try {
            final Collection<UserChange> nextMonthUserChanges = foldingRepository.getAllUserChangesForNextMonth();

            for (final UserChange userChange : nextMonthUserChanges) {
                try {
                    apply(userChange);
                } catch (final Exception e) {
                    LOGGER.warn("Error occurred applying user change '{}'", userChange, e);
                }
            }
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error applying next month's user changes", e);
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
        LOGGER.info("Applying user change '{}' for: {}", userChange.getId(), userChange.getNewUser().getDisplayName());
        foldingRepository.updateUser(userChange.getNewUser(), userChange.getPreviousUser());
        LOGGER.debug("Updated user");

        final UserChange userChangeToUpdate = UserChange.updateWithState(UserChangeState.COMPLETED, userChange);
        LOGGER.debug("Updating user change to complete");
        return foldingRepository.updateUserChange(userChangeToUpdate);
    }
}
