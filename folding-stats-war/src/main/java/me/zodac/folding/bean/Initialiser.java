package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.db.DbManagerRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collection;


// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class Initialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initialiser.class);

    private final DbManager dbManager = DbManagerRetriever.get();

    @EJB
    private StorageFacade storageFacade;

    @EJB
    private TeamCompetitionStatsScheduler teamCompetitionStatsScheduler;

    @PostConstruct
    public void init() {
        initCaches();
        initTcStats();

        SystemStateManager.next(SystemState.AVAILABLE);
        LOGGER.info("System ready for requests");
    }

    private void initCaches() {
        try {
            storageFacade.getAllHardware();
            storageFacade.getAllTeams();

            final Collection<User> users = storageFacade.getAllUsers();
            storageFacade.initialiseOffsetStats();

            for (final User user : users) {
                try {
                    final Stats initialStatsForUser = storageFacade.getInitialStatsForUser(user.getId());
                    LOGGER.debug("Found initial stats for user {}: {}", user, initialStatsForUser);
                } catch (final FoldingException e) {
                    LOGGER.warn("Unable to get initial stats for user {}", user, e.getCause());
                }
            }

            LOGGER.debug("Initialised initial stats cache");
        } catch (final FoldingException e) {
            LOGGER.warn("Error intialising caches", e.getCause());
        }
    }

    private void initTcStats() {
        try {
            if (!dbManager.isAnyHourlyTcStats()) {
                LOGGER.warn("No TC stats data exists in the DB");
                teamCompetitionStatsScheduler.manualTeamCompetitionStatsParsing(ExecutionType.ASYNCHRONOUS);
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to check DB state", e.getCause());
        }
    }
}
