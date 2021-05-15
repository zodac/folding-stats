package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.utils.EnvironmentVariables;
import me.zodac.folding.api.utils.ExecutionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class TeamCompetitionStatsScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsScheduler.class);
    private static final boolean IS_STATS_SCHEDULED_PARSING_ENABLED = Boolean.parseBoolean(EnvironmentVariables.get("ENABLE_STATS_SCHEDULED_PARSING", "false"));

    // Default is to run every hour at 55 minutes past the hour
    private static final String STATS_PARSING_SCHEDULE_HOUR = EnvironmentVariables.get("STATS_PARSING_SCHEDULE_HOUR", "*");
    private static final String STATS_PARSING_SCHEDULE_MINUTE = EnvironmentVariables.get("STATS_PARSING_SCHEDULE_MINUTE", "55");
    private static final String STATS_PARSING_SCHEDULE_SECOND = EnvironmentVariables.get("STATS_PARSING_SCHEDULE_SECOND", "0");

    @EJB
    private StorageFacade storageFacade;

    @EJB
    private UserTeamCompetitionStatsParser userTeamCompetitionStatsParser;

    @Resource
    private TimerService timerService;

    @PostConstruct
    public void init() {
        if (!IS_STATS_SCHEDULED_PARSING_ENABLED) {
            LOGGER.warn("Scheduled TC stats parsing not enabled");
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

    public void manualTeamCompetitionStatsParsing(final ExecutionType executionType) {
        LOGGER.debug("Manual stats parsing execution");
        SystemStateManager.next(SystemState.UPDATING_STATS);
        parseTeamCompetitionStats(executionType);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

    private void parseTeamCompetitionStats(final ExecutionType executionType) {
        LOGGER.info("");
        LOGGER.info("Parsing TC Folding stats:");

        final Collection<Team> tcTeams = getTcTeams();
        if (tcTeams.isEmpty()) {
            LOGGER.warn("No TC teams configured in system!");
            return;
        }


        for (final Team team : tcTeams) {
            parseTcStatsForTeam(team, executionType);
        }
    }

    public void parseTcStatsForTeam(final Team team, final ExecutionType executionType) {
        LOGGER.debug("Getting TC stats for users in team {}", team.getTeamName());

        for (final int userId : team.getUserIds()) {
            final Optional<User> user = getTcUser(userId);
            if (user.isEmpty()) {
                LOGGER.warn("Error finding user with ID: {}", userId);
                continue;
            }

            if (executionType == ExecutionType.ASYNCHRONOUS) {
                userTeamCompetitionStatsParser.parseTcStatsForUser(user.get());
            } else {
                userTeamCompetitionStatsParser.parseTcStatsForUserAndWait(user.get());
            }
        }
    }

    private Optional<User> getTcUser(final int userId) {
        try {
            final User user = storageFacade.getUser(userId);

            if (user.isRetired()) {
                LOGGER.warn("TC user was retired, was expecting active user: {}", user);
                return Optional.empty();
            }
            return Optional.of(user);
        } catch (final Exception e) {
            LOGGER.warn("Unable to get TC user with ID: {}", userId, e);
            return Optional.empty();
        }
    }

    private Collection<Team> getTcTeams() {
        try {
            return storageFacade.getAllTeams();
        } catch (final FoldingException e) {
            LOGGER.warn("Error retrieving TC teams", e);
            return Collections.emptyList();
        }
    }
}