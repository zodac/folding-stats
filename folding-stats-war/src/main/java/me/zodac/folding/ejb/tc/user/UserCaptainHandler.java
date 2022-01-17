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

package me.zodac.folding.ejb.tc.user;

import java.util.Optional;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the scenario where a {@link User} is created as captain, updated to be captain, or moves to a new {@link Team} as a captain, but an
 * existingcaptain already exists.
 *
 * <p>
 * The old captain {@link User} will be updated to no longer be captain.
 */
@Singleton
public class UserCaptainHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private FoldingStatsCore foldingStatsCore;

    /**
     * Checks if the {@link User} is a captain and if their {@link Team} already has a captain.
     *
     * @param user the {@link User} to check
     * @return <code>true</code> if the {@link User} is captain and the {@link Team} already has a captain
     */
    public boolean isUserCaptainAndCaptainExistsOnTeam(final User user) {
        if (!user.isUserIsCaptain()) {
            return false;
        }

        final Team team = user.getTeam();
        final Optional<User> existingCaptainOptional = getCaptainOfTeam(team);
        if (existingCaptainOptional.isEmpty()) {
            return false;
        }

        final User existingCaptain = existingCaptainOptional.get();
        LOGGER.info("Captain '{} (ID: {})' already exists for team '{}', will be replaced by '{}' (ID: {})",
            existingCaptain.getDisplayName(), existingCaptain.getId(), team.getTeamName(), user.getDisplayName(), user.getId()
        );
        return true;
    }

    /**
     * Retrieves the current captain for the provided {@link Team}. If one exists, they are updated to no longer be the captain.
     *
     * @param team the {@link Team} whose captain is to be updated
     * @see FoldingStatsCore#updateUser(User, User)
     */
    public void removeCaptaincyFromExistingTeamCaptain(final Team team) {
        final Optional<User> existingCaptainOptional = getCaptainOfTeam(team);
        if (existingCaptainOptional.isEmpty()) {
            return;
        }

        final User existingCaptain = existingCaptainOptional.get();
        final User userWithCaptaincyRemoved = User.removeCaptaincyFromUser(existingCaptain);

        foldingStatsCore.updateUser(userWithCaptaincyRemoved, existingCaptain);
    }

    private Optional<User> getCaptainOfTeam(final Team team) {
        return foldingStatsCore.getUsersOnTeam(team)
            .stream()
            .filter(User::isUserIsCaptain)
            .findAny();
    }
}
