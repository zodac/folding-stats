/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.bean.tc.user;

import java.util.Collection;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.api.FoldingRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Resets the stats for the {@code Team Competition}.
 */
@Component
public class UserStatsResetter {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FoldingRepository foldingRepository;
    private final StatsRepository statsRepository;
    private final UserStatsParser userStatsParser;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param statsRepository   the {@link StatsRepository}
     * @param userStatsParser   the {@link UserStatsParser}
     */
    @Autowired
    public UserStatsResetter(final FoldingRepository foldingRepository,
                             final StatsRepository statsRepository,
                             final UserStatsParser userStatsParser) {
        this.foldingRepository = foldingRepository;
        this.statsRepository = statsRepository;
        this.userStatsParser = userStatsParser;
    }

    /**
     * Resets the {@code Team Competition} stats for all {@link User}s.
     *
     * <p>
     * If the reset is for the start of a new month, we will also pull the latest stats for each {@link User}.
     *
     * @param isStartOfCompetition if the reset is for the start of a new month of the {@code Team Competition}
     * @see StatsRepository#resetAllTeamCompetitionUserStats()
     * @see UserStatsParser#parseTcStatsForUsers(Iterable)
     */
    public void resetTeamCompetitionStats(final boolean isStartOfCompetition) {
        try {
            final Collection<User> users = foldingRepository.getAllUsersWithPasskeys();

            // Only pulling the latest stats if we are starting the competition for a new month.
            // If it's the reset of the current month, we don't pull the latest stats due to rate-limiting from Folding@Home API.
            // Technically means we could have some points drop in the two minutes between last update and reset, but I can live with that. :)
            if (isStartOfCompetition) {
                userStatsParser.parseTcStatsForUsers(users);
            }

            LOGGER.info("Resetting Team Competition stats");
            statsRepository.resetAllTeamCompetitionUserStats();
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error manually resetting TC stats", e);
        }
    }
}
