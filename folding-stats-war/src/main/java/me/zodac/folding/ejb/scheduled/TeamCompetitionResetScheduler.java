package me.zodac.folding.ejb.scheduled;

import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.utils.EnvironmentVariableUtils;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.ejb.OldFacade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Startup} EJB which schedules the monthly reset of the <code>Team Competition</code>. The reset will occur once
 * a month on the 1st day of the month at <b>00:15</b>. This time cannot be changed, but the reset can be disabled using the
 * environment variable:
 * <ul>
 *     <li>ENABLE_STATS_MONTHLY_RESET</li>
 * </ul>
 *
 * <b>NOTE:</b> The {@link TeamCompetitionStatsScheduler} <i>can</i> have its schedule changed, but should not be set to conflict
 * with this reset time.
 */
@Startup
@Singleton
public class TeamCompetitionResetScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_MONTHLY_RESET_ENABLED = Boolean.parseBoolean(EnvironmentVariableUtils.get("ENABLE_STATS_MONTHLY_RESET", "false"));

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private OldFacade oldFacade;

    @EJB
    private TeamCompetitionStatsScheduler teamCompetitionStatsScheduler;

    // TODO: [zodac] Go through Storage/BL, not direct to caches
    private static void resetCaches() {
        LOGGER.info("Resetting caches");
        TcStatsCache.getInstance().removeAll();
        TotalStatsCache.getInstance().removeAll();
        RetiredTcStatsCache.getInstance().removeAll();
    }

    /**
     * On system startup, checks if the reset is enabled, and logs a warning if it is not.
     */
    @PostConstruct
    public void init() {
        if (!IS_MONTHLY_RESET_ENABLED) {
            LOGGER.error("Monthly TC stats reset not enabled");
        }
    }

    /**
     * Scheduled to execute at <b>00:15</b> on the 1st day of every month. Will reset the <code>Team Competition</code>
     * stats.
     *
     * @see #resetTeamCompetitionStats()
     */
    @Schedule(dayOfMonth = "1", minute = "15", info = "Monthly cache reset for TC teams")
    public void scheduleTeamCompetitionStatsReset() {
        if (!IS_MONTHLY_RESET_ENABLED) {
            LOGGER.error("Monthly TC stats reset not enabled");
            return;
        }

        LOGGER.warn("Resetting TC stats for new month");

        SystemStateManager.next(SystemState.RESETTING_STATS);
        resetTeamCompetitionStats();
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

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
     * @see OldFacade#clearOffsetStats()
     * @see OldFacade#deleteRetiredUserStats()
     * @see TeamCompetitionStatsScheduler#manualTeamCompetitionStatsParsing(ExecutionType)
     */
    public void resetTeamCompetitionStats() {
        try {
            final Collection<User> users = businessLogic.getAllUsersWithoutPasskeys();
            if (users.isEmpty()) {
                LOGGER.error("No TC users configured in system to reset!");
            } else {
                resetStats(users);
                LOGGER.info("Clearing offsets");
                oldFacade.clearOffsetStats();
            }

            LOGGER.info("Deleting retired users");
            oldFacade.deleteRetiredUserStats();
            resetCaches();
            teamCompetitionStatsScheduler.manualTeamCompetitionStatsParsing(ExecutionType.SYNCHRONOUS);
        } catch (final Exception e) {
            LOGGER.debug("Unexpected error manually resetting TC stats", e);
            LOGGER.warn("Unexpected error manually resetting TC stats"); // TODO: [zodac] Should be logging exception message too
        }
    }

    private void resetStats(final Collection<User> usersToReset) {
        LOGGER.info("Resetting user stats");
        for (final User user : usersToReset) {
            LOGGER.info("Resetting TC stats for {}", user.getDisplayName());
            oldFacade.setCurrentStatsAsInitialStatsForUser(user);
        }
    }
}
