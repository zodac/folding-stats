package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collections;
import java.util.List;

@Startup
@Singleton
public class TcCacheResetScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcCacheResetScheduler.class);

    @EJB
    private StorageFacade storageFacade;

    @Schedule(dayOfMonth = "1", minute = "55", info = "Monthly cache reset for TC teams")
    public void monthlyTcStatsReset() {
        LOGGER.info("Resetting TC caches for new month");
        manualTcStatsReset();
    }

    public void manualTcStatsReset() {
        final List<User> usersToReset = getTcUsers();

        for (final User user : usersToReset) {
            try {
                LOGGER.info("Resetting stats for {}", user.getDisplayName());
                updateInitialStatsForUser(user);
            } catch (final UserNotFoundException e) {
                LOGGER.warn("No user found to reset stats: {}", user);
            } catch (final FoldingException e) {
                LOGGER.warn("Error resetting stats for user: {}", user);
            } catch (final Exception e) {
                LOGGER.warn("Unexpected error resetting stats for user: {}", user);
            }
        }

        try {
            storageFacade.clearOffsetStats();
        } catch (final FoldingException e) {
            LOGGER.warn("Error clearing offset stats for users", e.getCause());
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error clearing offset stats for users", e);
        }
    }


    // Retrieve the initial stats for the user, and the current month's TC stats. We need both because if the user started
    // the competition but earned no points, we cannot simply use the current month's TC stats (which would both be 0).
    private void updateInitialStatsForUser(final User user) throws UserNotFoundException, FoldingException {
        LOGGER.info("Updating initial stats for user: {}", user);
        LOGGER.info("Current method (initial_stats+tc_stats)");
        final Stats initialStats = storageFacade.getInitialStatsForUser(user.getId());
        final UserTcStats currentTcStats = storageFacade.getTcStatsForUser(user.getId());
        final Stats currentAndInitialStats = Stats.create(initialStats.getPoints() + currentTcStats.getPoints(), initialStats.getUnits() + currentTcStats.getUnits());
        LOGGER.info("Finished current method (initial_stats+tc_stats): {}", currentAndInitialStats);

        // TODO: [zodac] Couldn't I just use the total stats, instead of adding initial+tc (two DB calls)?
        LOGGER.info("Potential method (total_stats)");
        final Stats totalStats = storageFacade.getTotalStatsForUser(user.getId());
        LOGGER.info("Finished new method (total_stats: {}", totalStats);
        
        storageFacade.persistInitialUserStats(UserStats.create(user.getId(), TimeUtils.getCurrentUtcTimestamp(), currentAndInitialStats));
        LOGGER.info("Done updating");
    }

    private List<User> getTcUsers() {
        try {
            final List<Team> tcTeams = storageFacade.getAllTeams();
            if (tcTeams.isEmpty()) {
                LOGGER.warn("No TC teams configured in system!");
                return Collections.emptyList();
            }

            final List<User> tcUsers = storageFacade.getActiveTcUsers(tcTeams);

            if (tcUsers.isEmpty()) {
                LOGGER.warn("No TC users configured in system!");
                return Collections.emptyList();
            }

            return tcUsers;
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to get teams!");
            return Collections.emptyList();
        }
    }
}
