/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.api.util.ProcessingType;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import me.zodac.folding.ejb.tc.user.UserStatsParser;
import me.zodac.folding.state.ParsingStateManager;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Startup} EJB which schedules the <code>Team Competition</code> stats retrieval for the system. By default, the
 * system will update stats using {@link UserStatsParser} every hour at <b>55</b> minutes past the hour.
 * It will also only run from the 3rd of the month until the end of the month.
 *
 * <p>
 * However, these default dates/times can be changed by setting the environment variables:
 * <ul>
 *     <li>{@code STATS_PARSING_SCHEDULE_HOUR}</li>
 *     <li>{@code STATS_PARSING_SCHEDULE_MINUTE}</li>
 *     <li>{@code STATS_PARSING_SCHEDULE_SECOND}</li>
 *     <li>{@code STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH}</li>
 *     <li>{@code STATS_PARSING_SCHEDULE_LAST_DAY_OF_MONTH}</li>
 * </ul>
 *
 * <b>NOTE:</b> The {@link EndOfMonthScheduler} schedule cannot be modified, so you should be careful not to
 * have the {@link StatsScheduler} conflict with the reset time.
 */
@Startup
@Singleton
public class StatsScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_STATS_SCHEDULED_PARSING_ENABLED =
        parseBoolean(EnvironmentVariableUtils.getOrDefault("ENABLE_STATS_SCHEDULED_PARSING", "false"));

    // Default is to run every hour at 55 minutes past the hour
    private static final String STATS_PARSING_SCHEDULE_HOUR = EnvironmentVariableUtils.getOrDefault("STATS_PARSING_SCHEDULE_HOUR", "*");
    private static final String STATS_PARSING_SCHEDULE_MINUTE = EnvironmentVariableUtils.getOrDefault("STATS_PARSING_SCHEDULE_MINUTE", "55");
    private static final String STATS_PARSING_SCHEDULE_SECOND = EnvironmentVariableUtils.getOrDefault("STATS_PARSING_SCHEDULE_SECOND", "0");
    private static final String STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH =
        EnvironmentVariableUtils.getOrDefault("STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH", "3");
    private static final String STATS_PARSING_SCHEDULE_LAST_DAY_OF_MONTH =
        EnvironmentVariableUtils.getOrDefault("STATS_PARSING_SCHEDULE_LAST_DAY_OF_MONTH", "31");

    @EJB
    private FoldingStatsCore foldingStatsCore;

    @EJB
    private UserStatsParser userStatsParser;

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
        schedule.dayOfMonth(STATS_PARSING_SCHEDULE_FIRST_DAY_OF_MONTH + "-" + STATS_PARSING_SCHEDULE_LAST_DAY_OF_MONTH);
        schedule.timezone("UTC");
        final Timer timer = timerService.createCalendarTimer(schedule);
        LOGGER.info("Starting TC stats parser with schedule: {}", timer.getSchedule());
    }

    /**
     * Scheduled execution to parse <code>Team Competition</code> stats.
     *
     * @param timer the {@link Timer} for scheduled execution
     */
    @Timeout
    public void scheduledTeamCompetitionStatsParsing(final Timer timer) {
        LOGGER.trace("Timer fired at: {}", timer);
        try {
            ParsingStateManager.next(ParsingState.ENABLED_TEAM_COMPETITION);
            SystemStateManager.next(SystemState.UPDATING_STATS);
            parseTeamCompetitionStats(ProcessingType.ASYNCHRONOUS);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating TC stats", e);
        }
    }

    /**
     * Parses <code>Team Competition</code> stats. Sets the {@link SystemState} to {@link SystemState#UPDATING_STATS} while stats are being parsed,
     * then finally to {@link SystemState#WRITE_EXECUTED} when complete.
     *
     * @param processingType the {@link ProcessingType}
     */
    public void manualTeamCompetitionStatsParsing(final ProcessingType processingType) {
        LOGGER.debug("Manual stats parsing execution");
        ParsingStateManager.next(ParsingState.ENABLED_TEAM_COMPETITION);
        SystemStateManager.next(SystemState.UPDATING_STATS);
        parseTeamCompetitionStats(processingType);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

    private void parseTeamCompetitionStats(final ProcessingType processingType) {
        LOGGER.info("");
        LOGGER.info("Parsing TC Folding stats:");

        final Collection<Team> tcTeams = foldingStatsCore.getAllTeams();
        if (tcTeams.isEmpty()) {
            LOGGER.warn("No TC teams configured in system!");
            return;
        }

        LOGGER.debug("Found TC teams: {}", tcTeams);
        for (final Team team : tcTeams) {
            parseTcStatsForTeam(team, processingType);
        }
    }

    private void parseTcStatsForTeam(final Team team, final ProcessingType processingType) {
        LOGGER.debug("Getting TC stats for users in team {}", team::getTeamName);
        final Collection<User> teamUsers = foldingStatsCore.getUsersOnTeamWithPasskeys(team);

        if (teamUsers.isEmpty()) {
            LOGGER.warn("No users for team '{}'", team.getTeamName());
            return;
        }

        LOGGER.debug("Found users TC team {}: {}", team.getTeamName(), teamUsers);
        for (final User user : teamUsers) {
            if (processingType == ProcessingType.ASYNCHRONOUS) {
                userStatsParser.parseTcStatsForUser(user);
            } else {
                userStatsParser.parseTcStatsForUserAndWait(user);
            }
        }
    }
}