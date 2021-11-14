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

package me.zodac.folding.ejb.tc.user;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.state.ParsingStateManager;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.util.ProcessingType;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import me.zodac.folding.ejb.tc.scheduled.StatsScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the stats change for a {@link User}, when it moved from one team to another, to ensure the old team retains their points, and the new teams
 * gets all subsequent stats.
 */
@Singleton
public class UserTeamChangeHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private FoldingStatsCore foldingStatsCore;

    @EJB
    private StatsScheduler statsScheduler;

    /**
     * Checks if the updated {@link User} is changing their {@link Team}.
     *
     * @param updatedUser  the updated {@link User} to check
     * @param existingUser the existing {@link User} to compare against
     * @return <code>true</code> if the {@link User} is changing their {@link Team}
     * @see #handleTeamChange(User, Team)
     */
    public boolean isUserTeamChange(final User updatedUser, final User existingUser) {
        if (updatedUser.getTeam().getId() != existingUser.getTeam().getId()) {
            LOGGER.info("User '{}' (ID: {}) moved from team '{}' -> '{}'", existingUser.getDisplayName(), existingUser.getId(),
                updatedUser.getTeam().getTeamName(), existingUser.getTeam().getTeamName());
            return true;
        }

        return false;
    }

    /**
     * This should be called if {@link #isUserTeamChange(User, User)} is <b>true</b>.
     *
     * <p>
     * We want to keep their <code>Team Competition</code> stats for their original {@link Team} as a retired user, then any future points come in to
     * the new {@link Team}. We will:
     * <ol>
     *     <li>Take their current {@link UserTcStats} and create a {@link RetiredUserTcStats} for their old {@link Team}</li>
     *     <li>Set their initial {@link UserStats} to their current total {@link UserStats}</li>
     * </ol>
     *
     * <p>
     * This allows all new stats to be added to their new {@link Team}, while the old {@link Team} keeps their existing contributions in the
     * <code>Team Competition</code>.
     *
     * <p>
     * <b>NOTE:</b> If the currently {@link ParsingState} is {@link ParsingState#DISABLED}, no changes will be made to the {@link User}.
     *
     * @param userWithTeamChange the {@link User} which had its {@link Team} changed
     * @param oldTeam            the {@link Team} that the {@link User} is moving away from
     */
    public void handleTeamChange(final User userWithTeamChange, final Team oldTeam) {
        if (ParsingStateManager.current() == ParsingState.DISABLED) {
            LOGGER.debug("Received a team change for user '{}' (ID: {}), but system is not currently parsing stats",
                userWithTeamChange.getDisplayName(), userWithTeamChange.getId());
            return;
        }

        // Add user's current stats as retired stats for old team
        final UserTcStats userStats = foldingStatsCore.getHourlyTcStats(userWithTeamChange);
        final RetiredUserTcStats retiredUserTcStats =
            RetiredUserTcStats.createWithoutId(oldTeam.getId(), userWithTeamChange.getDisplayName(), userStats);
        final RetiredUserTcStats createdRetiredUserTcStats = foldingStatsCore.createRetiredUserStats(retiredUserTcStats);
        LOGGER.info("User '{}' (ID: {}) retired with retired stats ID: {}", userWithTeamChange.getDisplayName(), userWithTeamChange.getId(),
            createdRetiredUserTcStats.getRetiredUserId());

        // Reset user stats
        final UserStats userTotalStats = foldingStatsCore.getTotalStats(userWithTeamChange);
        foldingStatsCore.createInitialStats(userTotalStats);

        // Pull stats to update teams
        statsScheduler.manualTeamCompetitionStatsParsing(ProcessingType.SYNCHRONOUS);

        LOGGER.info("Handled team change for user '{}' (ID: {})", userWithTeamChange.getDisplayName(), userWithTeamChange.getId());
    }
}
