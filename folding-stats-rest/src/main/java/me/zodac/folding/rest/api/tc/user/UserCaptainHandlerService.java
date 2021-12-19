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

import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import org.springframework.stereotype.Service;

/**
 * Handles the scenario where a {@link User} is created as captain, updated to be captain, or moves to a new {@link Team} as a captain, but an
 * existing captain already exists.
 *
 * <p>
 * The old captain {@link User} will be updated to no longer be captain.
 */
@Service
public interface UserCaptainHandlerService {

    /**
     * Checks if the {@link User} is a captain and if their {@link Team} already has a captain.
     *
     * @param user the {@link User} to check
     * @return <code>true</code> if the {@link User} is captain and the {@link Team} already has a captain
     */
    boolean isUserCaptainAndCaptainExistsOnTeam(final User user);

    /**
     * Retrieves the current captain for the provided {@link Team}. If one exists, they are updated to no longer be the captain.
     *
     * @param team the {@link Team} whose captain is to be updated
     */
    void removeCaptaincyFromExistingTeamCaptain(final Team team);
}