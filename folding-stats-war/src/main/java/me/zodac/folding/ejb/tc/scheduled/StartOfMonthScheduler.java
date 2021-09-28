package me.zodac.folding.ejb.tc.scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import me.zodac.folding.ParsingStateManager;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.ParsingState;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.utils.EnvironmentVariableUtils;
import me.zodac.folding.ejb.tc.UserStatsResetter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Startup} EJB which schedules the beginning of the <code>Team Competition</code> on the first day of the month. The day on which the month
 * starts can be configured using the environment variable:
 * <ul>
 *     <li>STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH</li>
 * </ul>
 *
 * <p>
 * It performs the following actions:
 * <ul>
 *     <li> Resets the stats for all users.
 *
 * <p>
 *          The reset will occur on the day configured to start stats parsing at <b>00:15</b>. This time cannot be changed, but the
 *          reset can be disabled using the environment variable:
 *          <ul>
 *              <li>ENABLE_STATS_MONTHLY_RESET</li>
 *          </ul>
 *      <b>NOTE:</b> The {@link StatsScheduler} <i>can</i> have its schedule changed, but should not be set to conflict with this reset time.
 *      </li>
 * </ul>
 */
@Startup
@Singleton
public class StartOfMonthScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_MONTHLY_RESET_ENABLED = Boolean.parseBoolean(EnvironmentVariableUtils.get("ENABLE_STATS_MONTHLY_RESET", "false"));
    private static final String STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH =
        EnvironmentVariableUtils.get("STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH", "3");

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
        LOGGER.trace("Timer fired at: {}", timer);
        LOGGER.warn("Starting TC stats for new month");

        SystemStateManager.next(SystemState.RESETTING_STATS);
        ParsingStateManager.next(ParsingState.DISABLED);
        userStatsResetter.resetTeamCompetitionStats();
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }
}
