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

import static me.zodac.folding.api.util.NumberUtils.formatWithCommas;

import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.state.ParsingStateManager;
import me.zodac.folding.state.SystemStateManager;
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
            } catch (final Exception e) {
                LOGGER.error("Error updating TC stats for user '{}' (ID: {})", user.displayName(), user.id(), e);
            }
        }
        LOGGER.info("Finished Folding stats parsing");

        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

    private void updateTcStatsForUser(final User user) {
        LOGGER.debug("Updating stats for '{}': {}", user.displayName(), user);
        if (StringUtils.isBlank(user.passkey())) {
            LOGGER.warn("Not parsing TC stats for user, missing passkey: {}", user);
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
            LOGGER.debug("{}: {} offset points | {} offset units", user::displayName,
                () -> formatWithCommas(offsetTcStats.getMultipliedPointsOffset()), () -> formatWithCommas(offsetTcStats.getUnitsOffset()));
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