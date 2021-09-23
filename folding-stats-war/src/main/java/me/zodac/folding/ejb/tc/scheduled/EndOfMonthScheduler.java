package me.zodac.folding.ejb.tc.scheduled;

import com.google.gson.Gson;
import java.time.LocalDateTime;
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
import me.zodac.folding.ParsingStateManager;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.ParsingState;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.utils.DateTimeUtils;
import me.zodac.folding.api.utils.EnvironmentVariableUtils;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.ejb.OldFacade;
import me.zodac.folding.ejb.tc.LeaderboardStatsGenerator;
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
    private static final Gson GSON = new Gson();

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private LeaderboardStatsGenerator leaderboardStatsGenerator;

    @EJB
    private OldFacade oldFacade;

    @EJB
    private StatsScheduler statsScheduler;

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
     * @see #resetTeamCompetitionStats()
     */
    @Timeout
    public void endOfTeamCompetition(final Timer timer) {
        LOGGER.trace("Timer fired at: {}", timer);

        if (IS_MONTHLY_RESET_ENABLED) {
            LOGGER.warn("Resetting TC stats for end of month");
            SystemStateManager.next(SystemState.RESETTING_STATS);
            ParsingStateManager.next(ParsingState.NOT_PARSING_STATS);
            resetTeamCompetitionStats();
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
        }

        if (IS_MONTHLY_RESULT_ENABLED) {
            LOGGER.info("Storing TC stats for new month");
            storeMonthlyResult();
        }
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
            LOGGER.warn("Unexpected error manually resetting TC stats", e);
        }
    }

    private void resetStats(final Collection<User> usersToReset) {
        LOGGER.info("Resetting user stats");
        for (final User user : usersToReset) {
            LOGGER.info("Resetting TC stats for {}", user.getDisplayName());
            oldFacade.setCurrentStatsAsInitialStatsForUser(user);
        }
    }

    // TODO: [zodac] Go through Storage/BL, not direct to caches
    private static void resetCaches() {
        LOGGER.info("Resetting caches");
        TcStatsCache.getInstance().removeAll();
        TotalStatsCache.getInstance().removeAll();
        RetiredTcStatsCache.getInstance().removeAll();
    }

    /**
     * Stores the {@link MonthlyResult} for the current {@link java.time.ZoneOffset#UTC} date-time.
     *
     * <p> The {@link MonthlyResult} will be generated using {@link LeaderboardStatsGenerator#generateTeamLeaderboards()} and
     * {@link LeaderboardStatsGenerator#generateUserCategoryLeaderboards()}.
     *
     * <p> The JSON output will be in the form:
     * <pre>
     * {
     *   "teamLeaderboard": [
     *     {
     *       "teamName": "Team1",
     *       "teamPoints": 25054094,
     *       "teamMultipliedPoints": 857328234,
     *       "teamUnits": 183,
     *       "rank": 1,
     *       "diffToLeader": 0,
     *       "diffToNext": 0
     *     },
     *     {
     *       "teamName": "Team2",
     *       "teamPoints": 139969822,
     *       "teamMultipliedPoints": 451436284,
     *       "teamUnits": 525,
     *       "rank": 2,
     *       "diffToLeader": 405891950,
     *       "diffToNext": 405891950
     *     }
     *   ],
     *   "userCategoryLeaderboard": {
     *     "AMD_GPU": [
     *       {
     *         "displayName": "User1",
     *         "foldingName": "User1",
     *         "hardware": "Hardware1",
     *         "teamName": "Team1",
     *         "points": 7707061,
     *         "multipliedPoints": 547817896,
     *         "units": 50,
     *         "rank": 1,
     *         "diffToLeader": 0,
     *         "diffToNext": 0
     *       },
     *       {
     *         "displayName": "User2",
     *         "foldingName": "User2",
     *         "hardware": "Hardware2",
     *         "teamName": "Team2",
     *         "points": 11587322,
     *         "multipliedPoints": 189800334,
     *         "units": 68,
     *         "rank": 2,
     *         "diffToLeader": 358017562,
     *         "diffToNext": 358017562
     *       }
     *     ],
     *     "NVIDIA_GPU": [
     *       {
     *         "displayName": "User3",
     *         "foldingName": "User3",
     *         "hardware": "Hardware3",
     *         "teamName": "Team1",
     *         "points": 124648353,
     *         "multipliedPoints": 181986595,
     *         "units": 349,
     *         "rank": 1,
     *         "diffToLeader": 0,
     *         "diffToNext": 0
     *       }
     *     ],
     *     "WILDCARD": [
     *       {
     *         "displayName": "User4",
     *         "foldingName": "User4",
     *         "hardware": "Hardware4",
     *         "teamName": "Team2",
     *         "points": 11633984,
     *         "multipliedPoints": 190564658,
     *         "units": 72,
     *         "rank": 1,
     *         "diffToLeader": 0,
     *         "diffToNext": 0
     *       }
     *     ]
     *   }
     * }
     * </pre>
     */
    public void storeMonthlyResult() {
        final MonthlyResult monthlyResult = MonthlyResult.create(leaderboardStatsGenerator.generateTeamLeaderboards(),
            leaderboardStatsGenerator.generateUserCategoryLeaderboards());

        final String result = GSON.toJson(monthlyResult);
        final LocalDateTime currentUtcDateTime = DateTimeUtils.currentUtcDateTime().toLocalDateTime();

        businessLogic.createMonthlyResult(result, currentUtcDateTime);
    }
}
