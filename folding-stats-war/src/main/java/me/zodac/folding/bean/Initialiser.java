package me.zodac.folding.bean;

import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.utils.TimeUtils;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.StatsCache;
import me.zodac.folding.cache.TeamCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Locale;


// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class Initialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initialiser.class);

    private final DbManager dbManager = DbManagerRetriever.get();

    @EJB
    private TeamCompetitionStatsParser teamCompetitionStatsParser;

    @PostConstruct
    public void init() {
        initPojoCaches();
        initTcStatsCache();

        LOGGER.info("System ready for requests");
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
        final Month currentMonth = TimeUtils.getCurrentUtcMonth();
        final Year currentYear = TimeUtils.getCurrentUtcYear();

        for (final User user : users) {
            try {
                final Stats initialStatsForUser = dbManager.getFirstStatsForUser(user.getId(), currentMonth, currentYear);
                LOGGER.debug("Found initial stats for {}/{} for user {}: {}", StringUtils.capitalize(currentMonth.toString().toLowerCase(Locale.UK)), currentYear, user, initialStatsForUser);
                StatsCache.get().addInitialStats(user.getId(), initialStatsForUser);
            } catch (final NotFoundException e) {
                LOGGER.debug("No initial stats in DB for {}", user, e);
                LOGGER.warn("No initial stats in DB for {}", user);
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get initial stats for {}/{} for user {}", StringUtils.capitalize(currentMonth.toString().toLowerCase(Locale.UK)), currentYear, user, e.getCause());
            }

            try {
                final Stats currentStatsForUser = dbManager.getLatestStatsForUser(user.getId(), currentMonth, currentYear);
                LOGGER.debug("Found current stats for {}/{} for user {}: {}", StringUtils.capitalize(currentMonth.toString().toLowerCase(Locale.UK)), currentYear, user, currentStatsForUser);
                StatsCache.get().addCurrentStats(user.getId(), currentStatsForUser);
            } catch (final NotFoundException e) {
                LOGGER.debug("No current stats in DB for {}", user, e);
                LOGGER.warn("No current stats in DB for {}", user);
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get current stats for {}/{} for user {}", StringUtils.capitalize(currentMonth.toString().toLowerCase(Locale.UK)), currentYear, user, e.getCause());
            }
        }

        LOGGER.debug("Initialised TC stats cache");
    }

    private void initPojoCaches() {
        try {
            LOGGER.debug("Initialising hardware cache with DB data");
            HardwareCache.get().addAll(dbManager.getAllHardware());
        } catch (final FoldingException e) {
            LOGGER.warn("Error initialising hardware cache", e.getCause());
        }

        try {
            LOGGER.debug("Initialising Folding user cache with DB data");
            UserCache.get().addAll(dbManager.getAllUsers());
        } catch (final FoldingException e) {
            LOGGER.warn("Error initialising Folding user cache", e.getCause());
        }

        try {
            LOGGER.debug("Initialising Folding team cache with DB data");
            TeamCache.get().addAll(dbManager.getAllTeams());
        } catch (final FoldingException e) {
            LOGGER.warn("Error initialising Folding team cache", e.getCause());
        }

        LOGGER.debug("Caches initialised");
    }
}
