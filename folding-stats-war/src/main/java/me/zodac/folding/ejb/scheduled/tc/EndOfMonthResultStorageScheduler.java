package me.zodac.folding.ejb.scheduled.tc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
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
        LOGGER.warn("Storing TC stats for new month");
        storeMonthlyResult();
    }

    /**
     * Stores the monthly result.
     * // TODO: [zodac] Print JSON format in doc
     */
    public void storeMonthlyResult() {
        final JsonObject result = new JsonObject();
        result.addProperty("teamLeaderboard", new Gson().toJson(leaderboardStatsGenerator.generateTeamLeaderboards()));
        result.addProperty("userCategoryLeaderboard", new Gson().toJson(leaderboardStatsGenerator.generateUserCategoryLeaderboards()));
        oldFacade.persistMonthlyResult(result.toString(), DateTimeUtils.currentUtcMonth(), DateTimeUtils.currentUtcYear());
    }
}
