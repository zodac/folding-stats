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

package net.zodac.folding.bean.tc.user;

import static net.zodac.folding.api.util.NumberUtils.formatWithCommas;

import java.util.List;
import net.zodac.folding.api.exception.ExternalConnectionException;
import net.zodac.folding.api.state.ParsingState;
import net.zodac.folding.api.state.SystemState;
import net.zodac.folding.api.stats.FoldingStatsRetriever;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.api.tc.stats.OffsetTcStats;
import net.zodac.folding.api.tc.stats.Stats;
import net.zodac.folding.api.tc.stats.UserStats;
import net.zodac.folding.bean.StatsRepository;
import net.zodac.folding.db.postgres.DatabaseConnectionException;
import net.zodac.folding.state.ParsingStateManager;
import net.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class that parses {@link Stats} for {@code Team Competition} {@link User}s.
 */
@Component
public class UserStatsParser {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FoldingStatsRetriever foldingStatsRetriever;
    private final StatsRepository statsRepository;
    private final UserTcStatsCalculator userTcStatsCalculator;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingStatsRetriever the {@link FoldingStatsRetriever}
     * @param statsRepository       the {@link StatsRepository}
     * @param userTcStatsCalculator the {@link UserTcStatsCalculator}
     */
    @Autowired
    public UserStatsParser(final FoldingStatsRetriever foldingStatsRetriever,
                           final StatsRepository statsRepository,
                           final UserTcStatsCalculator userTcStatsCalculator) {
        this.foldingStatsRetriever = foldingStatsRetriever;
        this.statsRepository = statsRepository;
        this.userTcStatsCalculator = userTcStatsCalculator;
    }

    /**
     * Parses the latest TC stats for a single {@link User}.
     *
     * @param user the {@link User} whose TC stats are to be parsed
     */
    public void parseTcStatsForUser(final User user) {
        parseTcStatsForUsers(List.of(user));
    }

    /**
     * Parses the latest TC stats for the given {@link User}s.
     *
     * @param users the {@link User}s whose TC stats are to be parsed
     */
    public void parseTcStatsForUsers(final Iterable<User> users) {
        ParsingStateManager.next(ParsingState.ENABLED_TEAM_COMPETITION);
        SystemStateManager.next(SystemState.UPDATING_STATS);

        LOGGER.info("Starting Folding stats parsing");

        for (final User user : users) {
            try {
                updateTcStatsForUser(user);
            } catch (final DatabaseConnectionException e) {
                LOGGER.error("Error updating TC stats for user '{}' (ID: {})", user.displayName(), user.id(), e);
            } catch (final Exception e) {
                LOGGER.error("Unexpected error updating TC stats for user '{}' (ID: {})", user.displayName(), user.id(), e);
            }
        }

        LOGGER.info("Finished Folding stats parsing");

        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

    private void updateTcStatsForUser(final User user) {
        LOGGER.debug("Updating stats for '{}': {}", user.displayName(), user);
        if (user.isPasskeyHidden()) {
            LOGGER.warn("Not parsing TC stats for user, missing/masked passkey: {}", user);
            return;
        }

        final Stats initialStats = statsRepository.getInitialStats(user);
        if (initialStats.isEmpty()) {
            LOGGER.warn("Retrieved empty initial stats for user: {}", user);
            return;
        }

        final OffsetTcStats offsetTcStats = statsRepository.getOffsetStats(user);
        if (offsetTcStats.isEmpty()) {
            LOGGER.trace("Retrieved empty stats offset for user: {}", () -> user);
        } else {
            LOGGER.debug("{}: {} offset points | {} offset units", user.displayName(), formatWithCommas(offsetTcStats.multipliedPointsOffset()),
                formatWithCommas(offsetTcStats.unitsOffset()));
        }

        final UserStats totalStats = getTotalStatsForUserOrEmpty(user);
        if (totalStats.isEmpty()) {
            LOGGER.warn("Retrieved empty total stats for user: {}", user);
            return;
        }

        final UserStats createdTotalStats = statsRepository.createTotalStats(totalStats);
        userTcStatsCalculator.calculateAndPersist(user, initialStats, offsetTcStats, createdTotalStats);
    }

    private UserStats getTotalStatsForUserOrEmpty(final User user) {
        try {
            return foldingStatsRetriever.getTotalStats(user);
        } catch (final ExternalConnectionException e) {
            LOGGER.warn("Error connecting to Folding@Home API at '{}'", e.getUrl(), e);
        }

        return UserStats.empty();
    }
}
