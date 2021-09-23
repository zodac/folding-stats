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
import me.zodac.folding.ejb.tc.UserStatsStorer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Startup} EJB which schedules the end of the <code>Team Competition</code>. The day on which the month ends
 * can be configured using the environment variable:
 * <ul>
 *     <li>STATS_PARSING_SCHEDULE_LAST_DAY_OF_MONTH</li>
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
 *      <li> Persists the result of that month.
 *
 * <p>
 *          The monthly result time cannot be configured, but it can be disabled using the environment variable:
 *          <ul>
 *              <li>ENABLE_MONTHLY_RESULT_STORAGE</li>
 *          </ul>
 *      </li>
 * </ul>
 */
@Startup
@Singleton
public class EndOfMonthScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String STATS_PARSING_SCHEDULE_LAST_DAY_OF_MONTH =
        EnvironmentVariableUtils.get("STATS_PARSING_SCHEDULE_LAST_DAY_OF_MONTH", "31");
    private static final boolean IS_MONTHLY_RESET_ENABLED = Boolean.parseBoolean(EnvironmentVariableUtils.get("ENABLE_STATS_MONTHLY_RESET", "false"));
    private static final boolean IS_MONTHLY_RESULT_ENABLED =
        Boolean.parseBoolean(EnvironmentVariableUtils.get("ENABLE_MONTHLY_RESULT_STORAGE", "false"));

    @EJB
    private UserStatsResetter userStatsResetter;

    @EJB
    private UserStatsStorer userStatsStorer;

    @Resource
    private TimerService timerService;

    /**
     * On system startup, checks if the reset is enabled. If so, starts a {@link Timer} for stats reset.
     */
    @PostConstruct
    public void init() {
        if (!IS_MONTHLY_RESET_ENABLED && !IS_MONTHLY_RESULT_ENABLED) {
            LOGGER.error("End of month reset and storage not enabled");
            return;
        }

        final ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour("23");
        schedule.minute("57"); // Stats are assumed to run at 23:55
        schedule.second("0");
        schedule.dayOfMonth(STATS_PARSING_SCHEDULE_LAST_DAY_OF_MONTH);
        schedule.timezone("UTC");
        final Timer timer = timerService.createCalendarTimer(schedule);
        LOGGER.info("Scheduling end of TC stats: {}", timer.getSchedule());
    }

    /**
     * Scheduled execution to reset the <code>Team Competition</code> stats.
     *
     * @param timer the {@link Timer} for scheduled execution
     * @see UserStatsResetter#resetTeamCompetitionStats()
     */
    @Timeout
    public void endOfTeamCompetition(final Timer timer) {
        LOGGER.trace("Timer fired at: {}", timer);

        if (IS_MONTHLY_RESET_ENABLED) {
            LOGGER.warn("Resetting TC stats for end of month");
            SystemStateManager.next(SystemState.RESETTING_STATS);
            ParsingStateManager.next(ParsingState.NOT_PARSING_STATS);
            userStatsResetter.resetTeamCompetitionStats();
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
        }

        if (IS_MONTHLY_RESULT_ENABLED) {
            LOGGER.info("Storing TC stats for new month");
            userStatsStorer.storeMonthlyResult();
        }
    }
}
