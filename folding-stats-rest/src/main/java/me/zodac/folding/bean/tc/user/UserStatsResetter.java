/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.StatsRepository;
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

    private final StatsRepository statsRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param statsRepository the {@link StatsRepository}
     */
    @Autowired
    public UserStatsResetter(final StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    /**
     * Resets the {@code Team Competition} stats for all {@link User}s.
     *
     * @see StatsRepository#resetAllTeamCompetitionUserStats()
     */
    public void resetTeamCompetitionStats() {
        try {
            // Not pulling the latest stats due to rate-limiting from Folding@Home API
            // Technically means we could have some points drop in the two minutes between last update and reset,
            // But I think we can live with that. :)
            // userStatsParser.parseTcStatsForUsers(users);

            LOGGER.info("Resetting Team Competition stats");
            statsRepository.resetAllTeamCompetitionUserStats();
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error manually resetting TC stats", e);
        }
    }
}
