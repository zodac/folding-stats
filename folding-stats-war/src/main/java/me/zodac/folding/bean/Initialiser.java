package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.cache.StatsCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.List;


// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class Initialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initialiser.class);

    private final DbManager dbManager = DbManagerRetriever.get();

    @EJB
    private StorageFacade storageFacade;

    @EJB
    private TeamCompetitionStatsParser teamCompetitionStatsParser;

    @PostConstruct
    public void init() {
        initCaches();
        initTcStatsCache();

        LOGGER.info("System ready for requests");
    }

    private void initCaches() {
        try {
            storageFacade.getAllHardware();
            storageFacade.getAllUsers();
            storageFacade.getAllTeams();
        } catch (final FoldingException e) {
            LOGGER.warn("Error intialising caches", e.getCause());
        }
    }

    private void initTcStatsCache() {
        try {
            if (!dbManager.doTcStatsExist()) {
                LOGGER.warn("No TC stats data exists in the DB");
                teamCompetitionStatsParser.manualStatsParsing();
                return;
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to check DB state", e.getCause());
        }


        final List<User> users = UserCache.get().getAll();

        for (final User user : users) {
            try {
                final Stats initialStatsForUser = dbManager.getInitialUserStats(user.getId());
                LOGGER.debug("Found initial stats for user {}: {}", user, initialStatsForUser);
                StatsCache.get().addInitialStats(user.getId(), initialStatsForUser);
            } catch (final UserNotFoundException e) {
                LOGGER.debug("No initial stats in DB for {}", user, e);
                LOGGER.warn("No initial stats in DB for {}", user);
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get initial stats for user {}", user, e.getCause());
            }

            try {
                final Stats currentStatsForUser = dbManager.getCurrentUserStats(user.getId());
                LOGGER.debug("Found current stats for user {}: {}", user, currentStatsForUser);
                StatsCache.get().addCurrentStats(user.getId(), currentStatsForUser);
            } catch (final UserNotFoundException e) {
                LOGGER.debug("No current stats in DB for {}", user, e);
                LOGGER.warn("No current stats in DB for {}", user);
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get current stats for user {}", user, e.getCause());
            }
        }

        LOGGER.debug("Initialised TC stats cache");
    }
}
