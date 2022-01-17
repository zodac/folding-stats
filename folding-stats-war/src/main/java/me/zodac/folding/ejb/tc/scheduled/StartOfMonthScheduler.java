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

package me.zodac.folding.ejb.tc.scheduled;

import static java.lang.Boolean.parseBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import me.zodac.folding.state.ParsingStateManager;
import me.zodac.folding.state.SystemStateManager;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.ejb.tc.user.UserStatsResetter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Startup} EJB which schedules the beginning of the <code>Team Competition</code> on the first day of the month. The day on which the month
 * starts can be configured using the environment variable:
 * <ul>
 *     <li>{@code STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH}</li>
 * </ul>
 *
 * <p>
 * It Resets the stats for all users.
 *
 * <p>
 * The reset will occur on the day configured to start stats parsing at <b>00:15</b>. This time cannot be changed, but the reset can be disabled using
 * the environment variable:
 * <ul>
 *     <li>{@code ENABLE_STATS_MONTHLY_RESET}</li>
 * </ul>
 * <b>NOTE:</b> The {@link StatsScheduler} <i>can</i> have its schedule changed, but should not be set to conflict with this reset time.
 */
@Startup
@Singleton
public class StartOfMonthScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_MONTHLY_RESET_ENABLED =
        parseBoolean(EnvironmentVariableUtils.getOrDefault("ENABLE_STATS_MONTHLY_RESET", "false"));
    private static final String STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH =
        EnvironmentVariableUtils.getOrDefault("STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH", "3");

    @EJB
    private UserStatsResetter userStatsResetter;

    @Resource
    private TimerService timerService;

    /**
     * On system startup, checks if the reset is enabled. If so, starts a {@link Timer} for stats reset.
     */
    @PostConstruct
    public void init() {
        if (!IS_MONTHLY_RESET_ENABLED) {
            LOGGER.error("Start of month reset not enabled");
            return;
        }

        final ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour("0");
        schedule.minute("15");
        schedule.second("0");
        schedule.dayOfMonth(STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH);
        schedule.timezone("UTC");
        final Timer timer = timerService.createCalendarTimer(schedule);
        LOGGER.info("Scheduling start of TC stats: {}", timer.getSchedule());
    }

    /**
     * Scheduled execution to reset the <code>Team Competition</code>
     * stats.
     *
     * @param timer the {@link Timer} for scheduled execution
     * @see UserStatsResetter#resetTeamCompetitionStats()
     */
    @Timeout
    public void startOfTeamCompetition(final Timer timer) {
        try {
            LOGGER.trace("Timer fired at: {}", timer);
            LOGGER.warn("Starting TC stats for new month");

            SystemStateManager.next(SystemState.RESETTING_STATS);
            ParsingStateManager.next(ParsingState.DISABLED);
            userStatsResetter.resetTeamCompetitionStats();
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
        } catch (final Exception e) {
            LOGGER.error("Error with start of team schedule", e);
        }
    }
}
