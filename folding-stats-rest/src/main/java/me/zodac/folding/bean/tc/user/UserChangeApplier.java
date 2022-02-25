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
     * Applies all provides {@link UserChange}s.
     *
     * <p>
     * If the application of any {@link UserChange} fails, it will be logged, but will not stop execution of the remaining {@link UserChange}s
     *
     * @param userChanges the {@link UserChange}s to apply
     * @see #apply(UserChange)
     */
    public void apply(final Collection<UserChange> userChanges) {
        for (final UserChange userChange : userChanges) {
            try {
                apply(userChange);
            } catch (final Exception e) {
                LOGGER.warn("Error occurred applying UserChange {}", userChange, e);
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
     * @see FoldingRepository#getUserWithPasskey(int)
     * @see FoldingRepository#updateUser(User, User)
     */
    public void apply(final UserChange userChange) {
        LOGGER.info("Applying for UserChange: {}", userChange);
        final User existingUser = foldingRepository.getUserWithPasskey(userChange.getUser().getId());
        LOGGER.info("Found existing user: {}", existingUser);
        foldingRepository.updateUser(userChange.getUser(), existingUser);
        LOGGER.info("Updated user");
        foldingRepository.updateUserChange(userChange.getId(), UserChangeState.COMPLETED);
        LOGGER.info("Updated UserChange to complete");
    }
}
