package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingConflictException;
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
                LOGGER.debug("Resetting stats for {}", user.getDisplayName());
                updateInitialStatsForUser(user);
                removeOffsetForUser(user);
            } catch (final UserNotFoundException e) {
                LOGGER.warn("No user found to reset stats: {}", user);
            } catch (final FoldingException e) {
                LOGGER.warn("Error resetting stats for user: {}", user);
            } catch (final FoldingConflictException e) {
                LOGGER.warn("Error removing offset stats for user: {}", user);
            } catch (final Exception e) {
                LOGGER.warn("Unexpected error resetting stats for user: {}", user);
            }
        }
    }


    // Retrieve the initial stats for the user, and the current month's TC stats. We need both because if the user started
    // the competition but earned no points, we cannot simply use the current month's TC stats (which would both be 0).
    private void updateInitialStatsForUser(final User user) throws UserNotFoundException, FoldingException {
        final Stats initialStats = storageFacade.getInitialStatsForUser(user.getId());
        final UserTcStats currentTcStats = storageFacade.getTcStatsForUser(user.getId());
        final Stats currentAndInitialStats = Stats.create(initialStats.getPoints() + currentTcStats.getPoints(), initialStats.getUnits() + currentTcStats.getUnits());
        storageFacade.persistInitialUserStats(UserStats.create(user.getId(), TimeUtils.getCurrentUtcTimestamp(), currentAndInitialStats));
    }

    // Now that the initial stats have been updated based on the current month's TC stats, the offsets are already applied, and can
    // be removed
    // TODO: [zodac] Why don't we do this always? Rather than keep offsets as part of the user, have an endpoint that simply updates
    //   a user's initial stats with an offset? Should be cleaner...
    private void removeOffsetForUser(final User user) throws UserNotFoundException, FoldingConflictException, FoldingException {
        final User userWithNoOffsets = User.updateWithNoOffsets(user);
        storageFacade.updateUser(userWithNoOffsets);
    }

    private List<User> getTcUsers() {
        try {
            final List<Team> tcTeams = storageFacade.getAllTeams();
            if (tcTeams.isEmpty()) {
                LOGGER.warn("No TC teams configured in system!");
                return Collections.emptyList();
            }

            final List<User> tcUsers = storageFacade.getUsersFromTeams(tcTeams);

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
