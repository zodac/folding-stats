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
 *
 */

package me.zodac.folding.rest.api.tc.user;

import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import org.springframework.stereotype.Service;

/**
 * Handles the state change for a {@link User}, when it (or something it relies on) is modified and their <code>Team Competition</code> stats need to
 * be adjusted.
 */
@Service
public interface UserStateChangeHandlerService {

    /**
     * Checks if the updated {@link User} requires a state change for the <code>Team Competition</code>.
     *
     * <p>
     * A state change is required if the updated {@link User} has changed:
     * <ul>
     *     <li>{@link User}'s Folding@Home username</li>
     *     <li>{@link User}'s Folding@Home passkey</li>
     *     <li>{@link User}'s {@link Hardware}</li>
     *     <li>{@link User}'s {@link Team}</li>
     * </ul>
     *
     * @param updatedUser  the updated {@link User} to check
     * @param existingUser the existing {@link User} to compare against
     * @return <code>true</code> if the {@link User} requires a state change
     * @see #handleStateChange(User)
     */
    boolean isUserStateChange(final User updatedUser, final User existingUser);

    /**
     * Checks if the updated {@link Hardware} requires a state change to any {@link User}s that use it for the <code>Team Competition</code>.
     *
     * <p>
     * A state change is required if the updated {@link Hardware} has changed:
     * <ul>
     *     <li>{@link Hardware}'s multiplier</li>
     * </ul>
     *
     * @param updatedHardware  the updated {@link Hardware} to check
     * @param existingHardware the existing {@link Hardware} to compare against
     * @return <code>true</code> if any {@link User}s using the {@link Hardware} require a state change
     * @see #handleStateChange(User)
     */
    boolean isHardwareStateChange(final Hardware updatedHardware, final Hardware existingHardware);

    /**
     * This should be called if either {@link #isUserStateChange(User, User)} or {@link #isHardwareStateChange(Hardware, Hardware)} is <b>true</b>.
     *
     * <p>
     * We want to restart their <code>Team Competition</code> calculation with their new information, so we do the following:
     * <ol>
     *     <li>Take their current {@link UserTcStats} and create some {@link OffsetTcStats} for the same amount</li>
     *     <li>Set their initial {@link UserStats} to their current total {@link UserStats}</li>
     * </ol>
     *
     * <p>
     * This allows all new stats to be collected based off their changed information, while retaining their existing points for the
     * <code>Team Competition</code>.
     *
     * <p>
     * <b>NOTE:</b> If the currently {@link ParsingState} is {@link ParsingState#DISABLED}, no changes will be made to the {@link User}.
     *
     * @param userWithStateChange the {@link User} which had its state change
     */
    void handleStateChange(final User userWithStateChange);
}