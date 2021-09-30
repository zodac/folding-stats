package me.zodac.folding.ejb.tc;

import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.ExecutionType;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.ejb.OldFacade;
import me.zodac.folding.ejb.tc.scheduled.StatsScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Resets the stats for the <code>Team Competition</code>.
 */
@Singleton
public class UserStatsResetter {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private OldFacade oldFacade;

    @EJB
    private StatsScheduler statsScheduler;

    /**
     * Resets the <code>Team Competition</code> stats for all {@link User}s.
     *
     * <p>
     * Actions performed:
     * <ol>
     * <li>Retrieves the current stats for all {@link User}s and updates their initial stats to these current values</li>
     * <li>Remove offset stats for all users</li>
     * <li>Delete all retired user stats</li>
     * <li>Invalidate all stats caches ({@link TcStatsCache}/{@link TotalStatsCache}/{@link RetiredTcStatsCache})</li>
     * <li>Execute a new stats update to set all values to <b>0</b></li>
     * </ol>
     *
     * @see OldFacade#setCurrentStatsAsInitialStatsForUser(User)
     * @see OldFacade#deleteAllOffsetStats()
     * @see BusinessLogic#deleteAllRetiredUserStats()
     * @see StatsScheduler#manualTeamCompetitionStatsParsing(ExecutionType)
     */
    public void resetTeamCompetitionStats() {
        try {
            final Collection<User> users = businessLogic.getAllUsersWithoutPasskeys();
            if (users.isEmpty()) {
                LOGGER.error("No TC users configured in system to reset!");
            } else {
                // Pull stats one more time to get the latest values
                statsScheduler.manualTeamCompetitionStatsParsing(ExecutionType.SYNCHRONOUS);
                resetStats(users);
                LOGGER.info("Clearing offsets");
                oldFacade.deleteAllOffsetStats();
            }

            LOGGER.info("Deleting retired users");
            businessLogic.deleteAllRetiredUserStats();
            resetCaches();
            statsScheduler.manualTeamCompetitionStatsParsing(ExecutionType.SYNCHRONOUS);
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error manually resetting TC stats", e);
        }
    }

    private void resetStats(final Collection<User> usersToReset) {
        LOGGER.info("Resetting user stats");
        for (final User user : usersToReset) {
            LOGGER.info("Resetting TC stats for {}", user.getDisplayName());
            oldFacade.setCurrentStatsAsInitialStatsForUser(user);
        }
    }

    // TODO: [zodac] Go through Storage/BL, not direct to caches
    private static void resetCaches() {
        LOGGER.info("Resetting caches");
        TcStatsCache.getInstance().removeAll();
        TotalStatsCache.getInstance().removeAll();
    }
}
