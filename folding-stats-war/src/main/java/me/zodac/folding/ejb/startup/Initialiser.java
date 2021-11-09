package me.zodac.folding.ejb.startup;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.util.ProcessingType;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import me.zodac.folding.ejb.tc.scheduled.StatsScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@link Startup} EJB which initialises the system when it comes online. This involves initalising any caches or
 * any available {@link User} {@link Stats}, and verifying the {@link SystemState} and marking the system as {@link SystemState#AVAILABLE}
 * when intialisation is complete.
 */
@Startup
@Singleton
public class Initialiser {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private FoldingStatsCore foldingStatsCore;

    @EJB
    private StatsScheduler statsScheduler;

    /**
     * On system startup, we execute the following actions to initialise the system for requests:
     * <ol>
     *     <li>Initialise the {@link me.zodac.folding.api.tc.Hardware}, {@link User}, {@link me.zodac.folding.api.tc.Team},
     *     {@link OffsetTcStats} and initial {@link me.zodac.folding.api.tc.stats.UserStats} caches</li>
     *     <li>If no <code>Team Competition</code> stats exist, performs a manual stats update</li>
     *     <li>Sets the {@link SystemState} to {@link SystemState#AVAILABLE} when complete</li>
     * </ol>
     *
     * @see FoldingStatsCore#isAnyHourlyTcStatsExist()
     * @see StatsScheduler#manualTeamCompetitionStatsParsing(ProcessingType)
     */
    @PostConstruct
    public void init() {
        initCaches();
        initTcStats();

        SystemStateManager.next(SystemState.AVAILABLE);
        LOGGER.info("System ready for requests");
    }

    private void initCaches() {
        foldingStatsCore.getAllHardware();
        foldingStatsCore.getAllTeams();
        final Collection<User> users = foldingStatsCore.getAllUsersWithoutPasskeys();

        for (final User user : users) {
            final OffsetTcStats offsetTcStatsForUser = foldingStatsCore.getOffsetStats(user);
            LOGGER.debug("Found offset stats for user {}: {}", user, offsetTcStatsForUser);

            final UserStats initialStatsForUser = foldingStatsCore.getInitialStats(user);
            LOGGER.debug("Found initial stats for user {}: {}", user, initialStatsForUser);
        }

        LOGGER.debug("Initialised stats caches");
    }

    private void initTcStats() {
        if (!foldingStatsCore.isAnyHourlyTcStatsExist()) {
            LOGGER.warn("No TC stats data exists in the DB");
            statsScheduler.manualTeamCompetitionStatsParsing(ProcessingType.ASYNCHRONOUS);
        }
    }
}
