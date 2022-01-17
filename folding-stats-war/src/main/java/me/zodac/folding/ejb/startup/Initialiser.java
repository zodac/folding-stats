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

package me.zodac.folding.ejb.startup;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import me.zodac.folding.state.SystemStateManager;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.util.ProcessingType;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import me.zodac.folding.ejb.tc.scheduled.StatsScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@link Startup} EJB which initialises the system when it comes online. This involves initalising any caches or
 * any available {@link User} {@link Stats}, and verifying the {@link SystemState} and marking the system as {@link SystemState#AVAILABLE}
 * when intialisation is complete.
 */
@Startup
@Singleton
public class Initialiser {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private FoldingStatsCore foldingStatsCore;

    @EJB
    private StatsScheduler statsScheduler;

    /**
     * On system startup, we execute the following actions to initialise the system for requests:
     * <ol>
     *     <li>Initialise the {@link me.zodac.folding.api.tc.Hardware}, {@link User}, {@link me.zodac.folding.api.tc.Team},
     *     {@link OffsetTcStats} and initial {@link me.zodac.folding.api.tc.stats.UserStats} caches</li>
     *     <li>If no <code>Team Competition</code> stats exist, performs a manual stats update</li>
     *     <li>Sets the {@link SystemState} to {@link SystemState#AVAILABLE} when complete</li>
     * </ol>
     *
     * @see FoldingStatsCore#isAnyHourlyTcStatsExist()
     * @see StatsScheduler#manualTeamCompetitionStatsParsing(ProcessingType)
     */
    @PostConstruct
    public void init() {
        initCaches();
        initTcStats();

        SystemStateManager.next(SystemState.AVAILABLE);
        LOGGER.info("System ready for requests");
    }

    private void initCaches() {
        foldingStatsCore.getAllHardware();
        foldingStatsCore.getAllTeams();
        final Collection<User> users = foldingStatsCore.getAllUsersWithoutPasskeys();

        for (final User user : users) {
            final OffsetTcStats offsetTcStatsForUser = foldingStatsCore.getOffsetStats(user);
            LOGGER.debug("Found offset stats for user {}: {}", user, offsetTcStatsForUser);

            final UserStats initialStatsForUser = foldingStatsCore.getInitialStats(user);
            LOGGER.debug("Found initial stats for user {}: {}", user, initialStatsForUser);
        }

        LOGGER.debug("Initialised stats caches");
    }

    private void initTcStats() {
        if (!foldingStatsCore.isAnyHourlyTcStatsExist()) {
            LOGGER.warn("No TC stats data exists in the DB");
            statsScheduler.manualTeamCompetitionStatsParsing(ProcessingType.ASYNCHRONOUS);
        }
    }
}
