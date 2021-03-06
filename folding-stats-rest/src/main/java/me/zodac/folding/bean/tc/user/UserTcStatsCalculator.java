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

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.bean.StatsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Calculates a {@link User}'s {@code Team Competition} {@link UserTcStats} and persists.
 */
@Component
public class UserTcStatsCalculator {

    private static final Logger STATS_LOGGER = LogManager.getLogger("stats");

    private final StatsRepository statsRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param statsRepository the {@link StatsRepository}
     */
    @Autowired
    public UserTcStatsCalculator(final StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    /**
     * Calculates the {@link UserTcStats} for a {@link User} for a single update and persists to the DB.
     *
     * <p>
     * The stats for the update are calculated as:
     * <ol>
     *     <li>The initial points are subtracted from the total points to give the {@link User}'s {@code pre-offset TC points}</li>
     *     <li>These TC points are multiplied by the {@link Hardware#multiplier()} for the {@link User}'s {@code TC multiplier points}</li>
     *     <li>The initial units are subtracted from the total units to give the {@link User}'s {@code TC units}</li>
     *     <li>Any {@link OffsetTcStats} are then applied (positive or negative) to give the {@link User}'s {@code final TC stats}</li>
     * </ol>
     *
     * <p>
     * The {@code final TC stats} are then persisted in the DB.
     *
     * @param user          the {@link User}
     * @param initialStats  the {@link User}'s initial {@link Stats}
     * @param offsetTcStats the {@link User}'s {@link OffsetTcStats}
     * @param totalStats    the {@link User}'s current total {@link UserStats}
     * @see StatsRepository#createHourlyTcStats(UserTcStats)
     */
    public void calculateAndPersist(final User user, final Stats initialStats, final OffsetTcStats offsetTcStats, final UserStats totalStats) {
        final double hardwareMultiplier = user.hardware().multiplier();
        final long points = Math.max(Stats.DEFAULT_POINTS, totalStats.getPoints() - initialStats.getPoints());
        final long multipliedPoints = Math.round(points * hardwareMultiplier);
        final int units = Math.max(Stats.DEFAULT_UNITS, totalStats.getUnits() - initialStats.getUnits());

        final UserTcStats statsBeforeOffset = UserTcStats.create(user.id(), totalStats.getTimestamp(), points, multipliedPoints, units);
        final UserTcStats hourlyUserTcStats = statsBeforeOffset.add(offsetTcStats);
        final UserTcStats previousHourlyTcStats = statsRepository.getHourlyTcStats(user);
        final UserTcStats createdHourlyTcStats = statsRepository.createHourlyTcStats(hourlyUserTcStats);

        // Only debug log if user has some points
        if (multipliedPoints != UserTcStats.DEFAULT_MULTIPLIED_POINTS) {
            STATS_LOGGER.debug("{} (ID: {}): {} total points (unmultiplied) | {} total units", user::displayName, user::id,
                () -> formatWithCommas(totalStats.getPoints()), () -> formatWithCommas(totalStats.getUnits()));
            STATS_LOGGER.debug("{} (ID: {}): {} TC multiplied points (pre-offset) | {} TC units (pre-offset)", user::displayName, user::id,
                () -> formatWithCommas(multipliedPoints), () -> formatWithCommas(units));

            final UserTcStats tcStatsForThisUpdate = createdHourlyTcStats.subtract(previousHourlyTcStats);
            STATS_LOGGER.debug("{} (ID: {}): {} TC multiplied points (update) | {} TC units (update)", user::displayName, user::id,
                () -> formatWithCommas(tcStatsForThisUpdate.getMultipliedPoints()), () -> formatWithCommas(tcStatsForThisUpdate.getUnits()));
        }

        STATS_LOGGER.info("{} (ID: {}): {} TC points | {} TC units", user.displayName(), user.id(),
            formatWithCommas(createdHourlyTcStats.getMultipliedPoints()), formatWithCommas(createdHourlyTcStats.getUnits()));
    }
}
