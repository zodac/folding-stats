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

package me.zodac.folding.bean.tc.user;

import static me.zodac.folding.api.util.NumberUtils.formatWithCommas;

import java.util.Collection;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.state.ParsingStateManager;
import me.zodac.folding.state.SystemStateManager;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Class that parses {@link Stats} for <code>Team Competition</code> {@link User}s.
 */
@Component
public class UserStatsParser {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    @Autowired
    private StatsRepository statsRepository;

    /**
     * Parses the latest TC stats for the given {@link User}s.
     *
     * <p>
     * Method blocks until the stats have been parsed, calculated and persisted.
     *
     * @param users the {@link User}s whose TC stats are to be parsed
     */
    public void parseTcStatsForUsersAndWait(final Collection<User> users) {
        updateTcStatsForUsers(users);
    }

    /**
     * Parses the latest TC stats for the given {@link User}s.
     *
     * @param users the {@link User}s whose TC stats are to be parsed
     */
    @Async
    public void parseTcStatsForUsers(final Collection<User> users) {
        updateTcStatsForUsers(users);
    }

    private void updateTcStatsForUsers(final Collection<User> users) {
        ParsingStateManager.next(ParsingState.ENABLED_TEAM_COMPETITION);
        SystemStateManager.next(SystemState.UPDATING_STATS);

        LOGGER.info("Parsing Folding stats:");
        for (final User user : users) {
            try {
                updateTcStatsForUser(user);
            } catch (final Exception e) {
                LOGGER.error("Error updating TC stats for user '{}' (ID: {})", user.getDisplayName(), user.getId(), e);
            }
        }
        LOGGER.info("Finished parsing Folding stats");

        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

    private void updateTcStatsForUser(final User user) {
        LOGGER.debug("Updating stats for '{}': {}", user.getDisplayName(), user);
        if (StringUtils.isBlank(user.getPasskey())) {
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
            LOGGER.debug("{}: {} offset points | {} offset units", user::getDisplayName,
                () -> formatWithCommas(offsetTcStats.getMultipliedPointsOffset()), () -> formatWithCommas(offsetTcStats.getUnitsOffset()));
        }

        final UserStats totalStats = getTotalStatsForUserOrEmpty(user);
        if (totalStats.isEmpty()) {
            LOGGER.warn("Retrieved empty total stats for user: {}", user);
            return;
        }

        final UserStats createdTotalStats = statsRepository.createTotalStats(totalStats);
        calculateAndPersistTcStats(user, initialStats, offsetTcStats, createdTotalStats);
    }

    private static UserStats getTotalStatsForUserOrEmpty(final User user) {
        try {
            return FOLDING_STATS_RETRIEVER.getTotalStats(user);
        } catch (final ExternalConnectionException e) {
            LOGGER.warn("Error connecting to Folding@Home API at '{}'", e.getUrl(), e);
        }

        return UserStats.empty();
    }

    private void calculateAndPersistTcStats(final User user,
                                            final Stats initialStats,
                                            final OffsetTcStats offsetTcStats,
                                            final UserStats totalStats) {
        final double hardwareMultiplier = user.getHardware().getMultiplier();
        final long points = Math.max(0, totalStats.getPoints() - initialStats.getPoints());
        final long multipliedPoints = Math.round(points * hardwareMultiplier);
        final int units = Math.max(0, totalStats.getUnits() - initialStats.getUnits());

        final UserTcStats statsBeforeOffset = UserTcStats.create(user.getId(), totalStats.getTimestamp(), points, multipliedPoints, units);
        final UserTcStats hourlyUserTcStats = statsBeforeOffset.updateWithOffsets(offsetTcStats);

        LOGGER.debug("{} (ID: {}): {} total points (unmultiplied) | {} total units", user::getDisplayName, user::getId,
            () -> formatWithCommas(totalStats.getPoints()), () -> formatWithCommas(totalStats.getUnits()));
        LOGGER.debug("{} (ID: {}): {} TC multiplied points (pre-offset) | {} TC units (pre-offset)", user::getDisplayName, user::getId,
            () -> formatWithCommas(multipliedPoints), () -> formatWithCommas(units));

        final UserTcStats createdHourlyTcStats = statsRepository.createHourlyTcStats(hourlyUserTcStats);
        LOGGER.info("{} (ID: {}): {} TC points | {} TC units", user.getDisplayName(), user.getId(),
            formatWithCommas(createdHourlyTcStats.getMultipliedPoints()), formatWithCommas(createdHourlyTcStats.getUnits()));
    }
}