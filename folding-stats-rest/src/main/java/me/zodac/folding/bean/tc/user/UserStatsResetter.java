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
import me.zodac.folding.bean.FoldingRepository;
import me.zodac.folding.bean.StatsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Resets the stats for the <code>Team Competition</code>.
 */
@Component
public class UserStatsResetter {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private FoldingRepository foldingRepository;

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private UserStatsParser userStatsParser;

    /**
     * Resets the <code>Team Competition</code> stats for all {@link User}s.
     *
     * <p>
     * Actions performed:
     * <ol>
     * <li>Retrieves the current stats for all {@link User}s and updates their initial stats to these current values</li>
     * <li>Remove offset stats for all users</li>
     * <li>Delete all retired user stats</li>
     * <li>Invalidate all stats caches</li>
     * <li>Execute a new stats update to set all values to <b>0</b></li>
     * </ol>
     *
     * @see StatsRepository#resetAllTeamCompetitionUserStats()
     * @see UserStatsParser#parseTcStatsForUsersAndWait(Collection)
     */
    public void resetTeamCompetitionStats() {
        try {
            final Collection<User> users = foldingRepository.getAllUsersWithPasskeys();

            // Pull stats one more time to get the latest values
            userStatsParser.parseTcStatsForUsersAndWait(users);

            LOGGER.info("Resetting Team Competition stats");
            statsRepository.resetAllTeamCompetitionUserStats();

            // Pull stats for new month
            userStatsParser.parseTcStatsForUsersAndWait(users);
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error manually resetting TC stats", e);
        }
    }
}