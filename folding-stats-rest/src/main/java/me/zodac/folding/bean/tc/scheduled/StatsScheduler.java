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

package me.zodac.folding.bean.tc.scheduled;

import java.util.Collection;
import me.zodac.folding.api.FoldingRepository;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.bean.tc.user.UserStatsParser;
import me.zodac.folding.state.ParsingStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@link Scheduled} {@link Component} which schedules the <code>Team Competition</code> stats retrieval for the system. The
 * system will update stats using {@link UserStatsParser} every hour at <b>55</b> minutes past the hour.
 * It will also only run from the 3rd of the month until the end of the month.
 */
@Component
public class StatsScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_STATS_SCHEDULED_PARSING_ENABLED = EnvironmentVariableUtils.getBoolean("ENABLE_STATS_SCHEDULED_PARSING");

    @Autowired
    private FoldingRepository foldingRepository;

    @Autowired
    private UserStatsParser userStatsParser;

    /**
     * Scheduled execution to parse <code>Team Competition</code> stats.
     */
    @Scheduled(cron = "0 55 * 3-31 * *", zone = "UTC")
    public void scheduledTeamCompetitionStatsParsing() {
        if (!IS_STATS_SCHEDULED_PARSING_ENABLED) {
            LOGGER.error("Scheduled TC stats parsing not enabled");
            return;
        }

        final Collection<User> users = foldingRepository.getAllUsersWithPasskeys();

        if (users.isEmpty()) {
            LOGGER.warn("No TC users, not parsing stats");
            return;
        }

        ParsingStateManager.next(ParsingState.ENABLED_TEAM_COMPETITION);
        userStatsParser.parseTcStatsForUsers(users);
    }
}
