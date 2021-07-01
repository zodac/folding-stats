package me.zodac.folding.ejb.scheduled;

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
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.utils.EnvironmentVariableUtils;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.ejb.UserTeamCompetitionStatsParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Startup} EJB which schedules the <code>Team Competition</code> stats retrieval for the system. By default, the
 * system will update stats using {@link UserTeamCompetitionStatsParser} every hour at <b>55</b> minutes past the hour.
 *
 * <p>
 * However, this default time can be changed by setting the environment variables:
 * <ul>
 *     <li>STATS_PARSING_SCHEDULE_HOUR</li>
 *     <li>STATS_PARSING_SCHEDULE_MINUTE</li>
 *     <li>STATS_PARSING_SCHEDULE_SECOND</li>
 * </ul>
 *
 * <b>NOTE:</b> The {@link TeamCompetitionResetScheduler} schedule cannot be modified, so you should be careful not to
 * have the stats retrieval conflict with the reset time.
 */
@Startup
@Singleton
public class TeamCompetitionStatsScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_STATS_SCHEDULED_PARSING_ENABLED =
        Boolean.parseBoolean(EnvironmentVariableUtils.get("ENABLE_STATS_SCHEDULED_PARSING", "false"));

    // Default is to run every hour at 55 minutes past the hour
    private static final String STATS_PARSING_SCHEDULE_HOUR = EnvironmentVariableUtils.get("STATS_PARSING_SCHEDULE_HOUR", "*");
    private static final String STATS_PARSING_SCHEDULE_MINUTE = EnvironmentVariableUtils.get("STATS_PARSING_SCHEDULE_MINUTE", "55");
    private static final String STATS_PARSING_SCHEDULE_SECOND = EnvironmentVariableUtils.get("STATS_PARSING_SCHEDULE_SECOND", "0");

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private UserTeamCompetitionStatsParser userTeamCompetitionStatsParser;

    @Resource
    private TimerService timerService;

    /**
     * On system startup, checks if scheduled TC stats parsing is enabled. If so, starts a {@link Timer} for stats parsing.
     */
    @PostConstruct
    public void init() {
        if (!IS_STATS_SCHEDULED_PARSING_ENABLED) {
            LOGGER.error("Scheduled TC stats parsing not enabled");
            return;
        }

        final ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour(STATS_PARSING_SCHEDULE_HOUR);
        schedule.minute(STATS_PARSING_SCHEDULE_MINUTE);
        schedule.second(STATS_PARSING_SCHEDULE_SECOND);
        schedule.timezone("UTC");
        final Timer timer = timerService.createCalendarTimer(schedule);
        LOGGER.info("Starting TC stats parser with schedule: {}", timer.getSchedule());
    }

    /**
     * Scheduled executing for parsing <code>Team Competition</code> stats.
     *
     * @param timer the {@link Timer} for scheduled execution
     */
    @Timeout
    public void scheduledTeamCompetitionStatsParsing(final Timer timer) {
        LOGGER.trace("Timer fired at: {}", timer);
        try {
            SystemStateManager.next(SystemState.UPDATING_STATS);
            parseTeamCompetitionStats(ExecutionType.ASYNCHRONOUS);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating TC stats", e);
        }
    }

    /**
     * Parses <code>Team Competition</code> stats. Sets the {@link SystemState} to {@link SystemState#UPDATING_STATS} while stats are being parsed,
     * then finally to {@link SystemState#WRITE_EXECUTED} when complete.
     *
     * @param executionType the {@link ExecutionType}
     */
    public void manualTeamCompetitionStatsParsing(final ExecutionType executionType) {
        LOGGER.debug("Manual stats parsing execution");
        SystemStateManager.next(SystemState.UPDATING_STATS);
        parseTeamCompetitionStats(executionType);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

    private void parseTeamCompetitionStats(final ExecutionType executionType) {
        LOGGER.info("");
        LOGGER.info("Parsing TC Folding stats:");

        final Collection<Team> tcTeams = businessLogic.getAllTeams();
        if (tcTeams.isEmpty()) {
            LOGGER.warn("No TC teams configured in system!");
            return;
        }

        for (final Team team : tcTeams) {
            parseTcStatsForTeam(team, executionType);
        }
    }

    private void parseTcStatsForTeam(final Team team, final ExecutionType executionType) {
        LOGGER.debug("Getting TC stats for users in team {}", team::getTeamName);
        final Collection<User> teamUsers = businessLogic.getUsersOnTeam(team);

        if (teamUsers.isEmpty()) {
            LOGGER.warn("No users for team '{}'", team.getTeamName());
            return;
        }

        for (final User user : teamUsers) {
            if (executionType == ExecutionType.ASYNCHRONOUS) {
                userTeamCompetitionStatsParser.parseTcStatsForUser(user);
            } else {
                userTeamCompetitionStatsParser.parseTcStatsForUserAndWait(user);
            }
        }
    }
}