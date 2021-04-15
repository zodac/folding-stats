package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.parsing.FoldingStatsParser;
import me.zodac.folding.util.EnvironmentVariable;
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
import java.util.List;

// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class TeamCompetitionStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsParser.class);

    // Default is to run every hour at 15 minutes past the hour
    private static final String STATS_PARSING_SCHEDULE_HOUR = EnvironmentVariable.get("STATS_PARSING_SCHEDULE_HOUR", "*");
    private static final String STATS_PARSING_SCHEDULE_MINUTE = EnvironmentVariable.get("STATS_PARSING_SCHEDULE_MINUTE", "15");
    private static final String STATS_PARSING_SCHEDULE_SECOND = EnvironmentVariable.get("STATS_PARSING_SCHEDULE_SECOND", "0");

    @EJB
    private StorageFacade storageFacade;

    @Resource
    private TimerService timerService;

    @PostConstruct
    public void init() {
        final ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour(STATS_PARSING_SCHEDULE_HOUR);
        schedule.minute(STATS_PARSING_SCHEDULE_MINUTE);
        schedule.second(STATS_PARSING_SCHEDULE_SECOND);
        schedule.timezone("UTC");
        final Timer timer = timerService.createCalendarTimer(schedule);
        LOGGER.info("Starting TC stats parser with schedule: {}", timer.getSchedule());
    }

    @Timeout
    public void scheduledStatsParsing(final Timer timer) {
        LOGGER.debug("Timer fired at: {}", timer.getInfo());
        getTcStatsForFoldingUsers();
    }

    public void manualStatsParsing() {
        LOGGER.debug("Manual stats parsing execution");
        getTcStatsForFoldingUsers();
    }

    private void getTcStatsForFoldingUsers() {
        LOGGER.info("Parsing TC Folding stats");

        final List<FoldingUser> tcUsers = storageFacade.getTcUsers();

        if (tcUsers.isEmpty()) {
            LOGGER.warn("No TC Folding users configured in system!");
            return;
        }

        FoldingStatsParser.parseTcStatsForAllUsers(tcUsers);
        LOGGER.info("Finished parsing");
        LOGGER.info("");
    }
}