package me.zodac.folding.ejb.startup;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.ejb.OldFacade;
import me.zodac.folding.ejb.scheduled.TeamCompetitionStatsScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collection;

/**
 * The {@link Startup} EJB which initialises the system when it comes online. This involves initalising any caches or
 * any available {@link User} {@link Stats}, and verifying the {@link SystemState} and marking the system as {@link SystemState#AVAILABLE}
 * when intialisation is complete.
 */
@Startup
@Singleton
public class Initialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initialiser.class);

    private transient final DbManager dbManager = DbManagerRetriever.get();

    @EJB
    private transient BusinessLogic businessLogic;

    @EJB
    private transient OldFacade oldFacade;

    @EJB
    private transient TeamCompetitionStatsScheduler teamCompetitionStatsScheduler;

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
     * @see TeamCompetitionStatsScheduler#manualTeamCompetitionStatsParsing(ExecutionType)
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
        oldFacade.getAllTeams();

        final Collection<User> users = oldFacade.getAllUsers();

        oldFacade.initialiseOffsetStats();

        for (final User user : users) {
            final Stats initialStatsForUser = oldFacade.getInitialStatsForUser(user.getId());
            LOGGER.debug("Found initial stats for user {}: {}", user, initialStatsForUser);
        }

        LOGGER.debug("Initialised initial stats cache");
    }

    private void initTcStats() {
        if (!dbManager.isAnyHourlyTcStats()) {
            LOGGER.warn("No TC stats data exists in the DB");
            teamCompetitionStatsScheduler.manualTeamCompetitionStatsParsing(ExecutionType.ASYNCHRONOUS);
        }
    }
}
