package me.zodac.folding.ejb.scheduled.tc;

import com.google.gson.Gson;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.utils.DateTimeUtils;
import me.zodac.folding.api.utils.EnvironmentVariableUtils;
import me.zodac.folding.ejb.LeaderboardStatsGenerator;
import me.zodac.folding.ejb.OldFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Startup} EJB which schedules the persistence of the monthly results of the <code>Team Competition</code>. The result will be
 * saved once a month on the 1st day of the month at <b>00:00</b>. This time cannot be changed, but can be disabled using the
 * environment variable:
 * <ul>
 *     <li>ENABLE_MONTHLY_RESULT_STORAGE</li>
 * </ul>
 *
 * <b>NOTE:</b> The {@link StatsScheduler} <i>can</i> have its schedule changed, but should not be set to conflict
 * with this time.
 */
@Startup
@Singleton
public class EndOfMonthResultStorageScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_MONTHLY_RESULT_STORAGE_ENABLED =
        Boolean.parseBoolean(EnvironmentVariableUtils.get("ENABLE_MONTHLY_RESULT_STORAGE", "false"));
    private static final Gson GSON = new Gson();

    @EJB
    private OldFacade oldFacade;

    @EJB
    private LeaderboardStatsGenerator leaderboardStatsGenerator;

    @Resource
    private TimerService timerService;

    /**
     * On system startup, checks if the storage is enabled. If so, starts a {@link Timer} for result storage.
     */
    @PostConstruct
    public void init() {
        if (!IS_MONTHLY_RESULT_STORAGE_ENABLED) {
            LOGGER.error("Monthly TC stats storage not enabled");
        }

        final ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour("0");
        schedule.minute("0");
        schedule.second("0");
        schedule.dayOfMonth("1");
        schedule.timezone("UTC");
        final Timer timer = timerService.createCalendarTimer(schedule);
        LOGGER.info("Starting end of month TC stats result storage with schedule: {}", timer.getSchedule());
    }

    /**
     * Scheduled execution to store the <code>Team Competition</code> result.
     *
     * @param timer the {@link Timer} for scheduled execution
     * @see #storeMonthlyResult()
     */
    @Timeout
    public void scheduleTeamCompetitionResultStorage(final Timer timer) {
        LOGGER.trace("Timer fired at: {}", timer);
        LOGGER.info("Storing TC stats for new month");
        storeMonthlyResult();
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

        oldFacade.persistMonthlyResult(result, currentUtcDateTime);
    }
}
