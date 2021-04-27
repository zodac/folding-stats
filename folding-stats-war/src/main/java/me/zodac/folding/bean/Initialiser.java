package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.db.DbManagerRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.List;

import static java.util.stream.Collectors.toList;


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
        initTcStats();

        LOGGER.info("System ready for requests");
    }

    private void initCaches() {
        try {
            storageFacade.getAllHardware();
            storageFacade.getAllTeams();

            final List<User> users = storageFacade.getAllUsers();
            storageFacade.getOffsetStatsForUsers(users.stream().map(User::getId).collect(toList()));

            for (final User user : users) {
                try {
                    final Stats initialStatsForUser = storageFacade.getInitialStatsForUser(user.getId());
                    LOGGER.debug("Found initial stats for user {}: {}", user, initialStatsForUser);
                } catch (final UserNotFoundException e) {
                    LOGGER.debug("No initial stats in DB for {}", user, e);
                    LOGGER.warn("No initial stats in DB for {}", user);
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
            if (!dbManager.doTcStatsExist()) {
                LOGGER.warn("No TC stats data exists in the DB");
                teamCompetitionStatsParser.manualTcStatsParsing();
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to check DB state", e.getCause());
        }
    }
}
