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

package me.zodac.folding.rest.impl.tc.scheduled;

import static java.lang.Boolean.parseBoolean;

import java.util.Collection;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.rest.api.FoldingService;
import me.zodac.folding.rest.api.tc.user.UserStatsParserService;
import me.zodac.folding.state.ParsingStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatsScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_STATS_SCHEDULED_PARSING_ENABLED =
        parseBoolean(EnvironmentVariableUtils.getOrDefault("ENABLE_STATS_SCHEDULED_PARSING", "false"));

    @Autowired
    private FoldingService foldingService;

    @Autowired
    private UserStatsParserService userStatsParser;

    @Scheduled(cron = "0 55 * * * *", zone = "UTC")
    public void scheduledTeamCompetitionStatsParsing() {
        if (!IS_STATS_SCHEDULED_PARSING_ENABLED) {
            LOGGER.error("Scheduled TC stats parsing not enabled");
            return;
        }

        LOGGER.trace("Method #scheduledTeamCompetitionStatsParsing() fired");
        final Collection<User> users = foldingService.getAllUsersWithPasskeys();

        if (users.isEmpty()) {
            LOGGER.warn("No TC users configured in system!");
            return;
        }

        ParsingStateManager.next(ParsingState.ENABLED_TEAM_COMPETITION);
        userStatsParser.parseTcStatsForUser(users);
    }
}
