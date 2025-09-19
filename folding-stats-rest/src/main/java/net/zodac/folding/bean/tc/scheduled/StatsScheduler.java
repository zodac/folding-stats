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

package net.zodac.folding.bean.tc.scheduled;

import java.util.Collection;
import net.zodac.folding.api.state.ParsingState;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.bean.tc.user.UserStatsParser;
import net.zodac.folding.state.ParsingStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@link Scheduled} {@link Component} which schedules the {@code Team Competition} stats retrieval for the system. The
 * system will update stats using {@link UserStatsParser} based on the supplied <b>STATS_SCHEDULED_PARSING_CRON</b> environment variable.
 *
 * <p>
 * By default, the stats will run on the 55th minute of every hours, from the 3rd of the month until the end of the month.
 */
@Component
@ConditionalOnProperty(name = "stats.scheduled.parsing.enabled", havingValue = "true")
public class StatsScheduler {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FoldingRepository foldingRepository;
    private final UserStatsParser userStatsParser;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param userStatsParser   the {@link UserStatsParser}
     */
    @Autowired
    public StatsScheduler(final FoldingRepository foldingRepository, final UserStatsParser userStatsParser) {
        this.foldingRepository = foldingRepository;
        this.userStatsParser = userStatsParser;
    }

    /**
     * Scheduled execution to parse {@code Team Competition} stats.
     */
    @Scheduled(cron = "${stats.scheduled.parsing.cron:0 55 * 3-31 * *}", zone = "UTC")
    public void scheduledTeamCompetitionStatsParsing() {
        final Collection<User> users = foldingRepository.getAllUsersWithPasskeys();

        if (users.isEmpty()) {
            LOGGER.warn("No TC users, not parsing stats");
            return;
        }

        ParsingStateManager.next(ParsingState.ENABLED_TEAM_COMPETITION);
        userStatsParser.parseTcStatsForUsers(users);
    }
}
