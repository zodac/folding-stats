package me.zodac.folding.ejb.scheduled.tc;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.utils.EnvironmentVariableUtils;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.ejb.OldFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Startup} EJB which schedules the monthly reset of the <code>Team Competition</code>. The reset will occur once
 * a month on the day configured to start stats parsing at <b>00:15</b>. This time cannot be changed, but the reset can be disabled and day of the
 * month configured using the environment variables:
 * <ul>
 *     <li>ENABLE_STATS_MONTHLY_RESET</li>
 *     <li>STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH</li>
 * </ul>
 *
 * <b>NOTE:</b> The {@link StatsScheduler} <i>can</i> have its schedule changed, but should not be set to conflict
 * with this reset time.
 */
@Startup
@Singleton
public class StartOfMonthResetScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_MONTHLY_RESET_ENABLED = Boolean.parseBoolean(EnvironmentVariableUtils.get("ENABLE_STATS_MONTHLY_RESET", "false"));
    private static final String STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH =
        EnvironmentVariableUtils.get("STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH", "3");

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private OldFacade oldFacade;

    @EJB
    private StatsScheduler statsScheduler;

    @Resource
    private TimerService timerService;

    // TODO: [zodac] Go through Storage/BL, not direct to caches
    private static void resetCaches() {
        LOGGER.info("Resetting caches");
        TcStatsCache.getInstance().removeAll();
        TotalStatsCache.getInstance().removeAll();
        RetiredTcStatsCache.getInstance().removeAll();
    }

    /**
     * On system startup, checks if the reset is enabled. If so, starts a {@link Timer} for stats reset.
     */
    @PostConstruct
    public void init() {
        if (!IS_MONTHLY_RESET_ENABLED) {
            LOGGER.error("Start of TC stats parsing reset not enabled");
        }

        final ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour("0");
        schedule.minute("15");
        schedule.second("0");
        schedule.dayOfMonth(STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH);
        schedule.timezone("UTC");
        final Timer timer = timerService.createCalendarTimer(schedule);
        LOGGER.info("Starting start of month TC stats reset with schedule: {}", timer.getSchedule());
    }

    /**
     * Scheduled execution to reset the <code>Team Competition</code>
     * stats.
     *
     * @param timer the {@link Timer} for scheduled execution
     * @see #resetTeamCompetitionStats()
     */
    @Timeout
    public void scheduleTeamCompetitionStatsReset(final Timer timer) {
        LOGGER.trace("Timer fired at: {}", timer);
        LOGGER.warn("Resetting TC stats for new month");

        SystemStateManager.next(SystemState.RESETTING_STATS);
        resetTeamCompetitionStats();
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

    /**
     * Resets the <code>Team Competition</code> stats for all {@link User}s.
     *
     * <p>
     * Actions performed:
     * <ol>
     * <li>Retrieves the current stats for all {@link User}s and updates their initial stats to these current values</li>
     * <li>Remove offset stats for all users</li>
     * <li>Delete all retired user stats</li>
     * <li>Invalidate all stats caches ({@link TcStatsCache}/{@link TotalStatsCache}/{@link RetiredTcStatsCache})</li>
     * <li>Execute a new stats update to set all values to <b>0</b></li>
     * </ol>
     *
     * @see OldFacade#setCurrentStatsAsInitialStatsForUser(User)
     * @see OldFacade#clearOffsetStats()
     * @see OldFacade#deleteRetiredUserStats()
     * @see StatsScheduler#manualTeamCompetitionStatsParsing(ExecutionType)
     */
    public void resetTeamCompetitionStats() {
        try {
            final Collection<User> users = businessLogic.getAllUsersWithoutPasskeys();
            if (users.isEmpty()) {
                LOGGER.error("No TC users configured in system to reset!");
            } else {
                // Pull stats one more time to get the latest values
                statsScheduler.manualTeamCompetitionStatsParsing(ExecutionType.SYNCHRONOUS);
                resetStats(users);
                LOGGER.info("Clearing offsets");
                oldFacade.clearOffsetStats();
            }

            LOGGER.info("Deleting retired users");
            oldFacade.deleteRetiredUserStats();
            resetCaches();
            statsScheduler.manualTeamCompetitionStatsParsing(ExecutionType.SYNCHRONOUS);
        } catch (final Exception e) {
            LOGGER.debug("Unexpected error manually resetting TC stats", e);
            LOGGER.warn("Unexpected error manually resetting TC stats"); // TODO: [zodac] Should be logging exception message too
        }
    }

    private void resetStats(final Collection<User> usersToReset) {
        LOGGER.info("Resetting user stats");
        for (final User user : usersToReset) {
            LOGGER.info("Resetting TC stats for {}", user.getDisplayName());
            oldFacade.setCurrentStatsAsInitialStatsForUser(user);
        }
    }
}
