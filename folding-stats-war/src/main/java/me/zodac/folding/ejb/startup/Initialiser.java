package me.zodac.folding.ejb.startup;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.util.ExecutionType;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.ejb.OldFacade;
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
    private static final DbManager DB_MANAGER = DbManagerRetriever.get();

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private OldFacade oldFacade;

    @EJB
    private StatsScheduler statsScheduler;

    /**
     * On system startup, we execute the following actions to initialise the system for requests:
     * <ol>
     *     <li>Initialise the {@link me.zodac.folding.api.tc.Hardware}, {@link User}, {@link me.zodac.folding.api.tc.Team},
     *     {@link me.zodac.folding.api.tc.stats.OffsetStats} and initial {@link me.zodac.folding.api.tc.stats.UserStats} caches</li>
     *     <li>If no <code>Team Competition</code> stats exist, performs a manual stats update</li>
     *     <li>Sets the {@link SystemState} to {@link SystemState#AVAILABLE} when complete</li>
     * </ol>
     *
     * @see DbManager#isAnyHourlyTcStats()
     * @see StatsScheduler#manualTeamCompetitionStatsParsing(ExecutionType)
     */
    @PostConstruct
    public void init() {
        initCaches();
        initTcStats();

        SystemStateManager.next(SystemState.AVAILABLE);
        LOGGER.info("System ready for requests");
    }

    private void initCaches() {
        businessLogic.getAllHardware();
        businessLogic.getAllTeams();
        final Collection<User> users = businessLogic.getAllUsersWithoutPasskeys();

        for (final User user : users) {
            final OffsetStats offsetStatsForUser = oldFacade.getOffsetStatsForUser(user.getId());
            LOGGER.debug("Found offset stats for user {}: {}", user, offsetStatsForUser);

            final Stats initialStatsForUser = oldFacade.getInitialStatsForUser(user.getId());
            LOGGER.debug("Found initial stats for user {}: {}", user, initialStatsForUser);
        }

        LOGGER.debug("Initialised initial stats cache");
    }

    private void initTcStats() {
        if (!DB_MANAGER.isAnyHourlyTcStats()) { // TODO: [zodac] Go through BL
            LOGGER.warn("No TC stats data exists in the DB");
            statsScheduler.manualTeamCompetitionStatsParsing(ExecutionType.ASYNCHRONOUS);
        }
    }
}
