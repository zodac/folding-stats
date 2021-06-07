package me.zodac.folding.ejb;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.utils.EnvironmentVariableUtils;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TotalStatsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collection;
import java.util.Collections;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionResetScheduler.class);
    private static final boolean IS_MONTHLY_RESET_ENABLED = Boolean.parseBoolean(EnvironmentVariableUtils.get("ENABLE_STATS_MONTHLY_RESET", "false"));

    @EJB
    private transient OldFacade oldFacade;

    @EJB
    private transient TeamCompetitionStatsScheduler teamCompetitionStatsScheduler;

    @PostConstruct
    public void init() {
        if (!IS_MONTHLY_RESET_ENABLED) {
            LOGGER.warn("Monthly TC stats reset not enabled");
        }
    }

    @Schedule(dayOfMonth = "1", minute = "15", info = "Monthly cache reset for TC teams")
    public void resetTeamCompetitionStats() {
        if (!IS_MONTHLY_RESET_ENABLED) {
            LOGGER.warn("Monthly TC stats reset not enabled");
            return;
        }

        LOGGER.info("Resetting TC stats for new month");

        SystemStateManager.next(SystemState.RESETTING_STATS);
        manualResetTeamCompetitionStats();
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
    }

    public void manualResetTeamCompetitionStats() {
        final Collection<Team> teams;
        try {
            teams = oldFacade.getAllTeams();
            if (teams.isEmpty()) {
                LOGGER.error("No TC teams configured in system!");
                return;
            }
        } catch (final FoldingException e) {
            LOGGER.error("Unable to get teams!");
            return;
        }

        final Collection<User> users = getAllUsers();
        if (users.isEmpty()) {
            LOGGER.error("No TC users configured in system to reset!");
            return;
        }

        resetStats(users);
        clearOffsets();
        resetCaches();

        try {
            LOGGER.info("Deleting retired users");
            oldFacade.deleteRetiredUserStats();
        } catch (final FoldingException e) {
            LOGGER.error("Unable to reset retired stats", e);
        }

        teamCompetitionStatsScheduler.manualTeamCompetitionStatsParsing(ExecutionType.SYNCHRONOUS);
    }

    // TODO: [zodac] Go through Storage/BL, not direct to caches
    private void resetCaches() {
        LOGGER.info("Resetting caches");
        TcStatsCache.get().clear();
        TotalStatsCache.get().clear();
        RetiredTcStatsCache.get().clear();
    }

    private Collection<User> getAllUsers() {
        try {
            return oldFacade.getAllUsers();
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting all users to reset stats", e);
            return Collections.emptyList();
        }
    }

    private void clearOffsets() {
        try {
            LOGGER.info("Clearing offsets");
            oldFacade.clearOffsetStats();
        } catch (final FoldingException e) {
            LOGGER.warn("Error clearing offset stats for users", e.getCause());
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error clearing offset stats for users", e);
        }
    }

    private void resetStats(final Collection<User> usersToReset) {
        LOGGER.info("Resetting all TC stats");
        for (final User user : usersToReset) {
            try {
                LOGGER.info("Resetting TC stats for {}", user.getDisplayName());
                oldFacade.updateInitialStatsForUser(user);
            } catch (final UserNotFoundException e) {
                LOGGER.warn("No user found to reset TC stats: {}", user);
            } catch (final FoldingException e) {
                LOGGER.warn("Error resetting TC stats for user: {}", user);
            } catch (final Exception e) {
                LOGGER.warn("Unexpected error resetting TC stats for user: {}", user);
            }
        }
    }
}
