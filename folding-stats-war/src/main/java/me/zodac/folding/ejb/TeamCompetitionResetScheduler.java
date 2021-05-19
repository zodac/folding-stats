package me.zodac.folding.ejb;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.db.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.TeamNotFoundException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.utils.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collection;
import java.util.Map;

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
    private static final boolean IS_MONTHLY_RESET_ENABLED = Boolean.parseBoolean(EnvironmentVariables.get("ENABLE_STATS_MONTHLY_RESET", "false"));

    @EJB
    private BusinessLogic businessLogic;

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
            teams = businessLogic.getAllTeams();
            if (teams.isEmpty()) {
                LOGGER.error("No TC teams configured in system!");
                return;
            }
        } catch (final FoldingException e) {
            LOGGER.error("Unable to get teams!");
            return;
        }

        final Map<Integer, User> usersById = businessLogic.getActiveTcUsers(teams);
        if (usersById.isEmpty()) {
            LOGGER.error("No TC users configured in system!");
            return;
        }

        resetStats(usersById.values());
        clearOffsets();
        removeRetiredUsersFromTeams(teams);
    }

    private void removeRetiredUsersFromTeams(final Collection<Team> teams) {
        LOGGER.debug("Removing retired users from teams");
        for (final Team team : teams) {
            try {
                businessLogic.getTeam(team.getId());

                if (team.getRetiredUserIds().isEmpty()) {
                    LOGGER.debug("No retired users in team '{}'", team.getTeamName());
                    continue;
                }

                LOGGER.debug("Removing retired users from team '{}'", team.getTeamName());
                final Team teamWithoutRetiredUsers = Team.removeRetiredUsers(team);
                businessLogic.updateTeam(teamWithoutRetiredUsers);
            } catch (final TeamNotFoundException e) {
                LOGGER.debug("Error removing retired users from team, no team found with ID: {}", team.getId(), e);
                LOGGER.warn("Error removing retired users from team, no team found with ID: {}", team.getId());
            } catch (final FoldingConflictException e) {
                LOGGER.warn("Error removing retired users from team, conflict found for ID: {}", team.getId(), e);
            } catch (final FoldingException e) {
                LOGGER.warn("Error removing retired users from team with ID: {}", team.getId(), e.getCause());
            }
        }
    }


    private void clearOffsets() {
        try {
            businessLogic.clearOffsetStats();
        } catch (final FoldingException e) {
            LOGGER.warn("Error clearing offset stats for users", e.getCause());
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error clearing offset stats for users", e);
        }
    }

    private void resetStats(final Collection<User> usersToReset) {
        for (final User user : usersToReset) {
            try {
                LOGGER.info("Resetting stats for {}", user.getDisplayName());
                businessLogic.updateInitialStatsForUser(user);
            } catch (final UserNotFoundException e) {
                LOGGER.warn("No user found to reset stats: {}", user);
            } catch (final FoldingException e) {
                LOGGER.warn("Error resetting stats for user: {}", user);
            } catch (final Exception e) {
                LOGGER.warn("Unexpected error resetting stats for user: {}", user);
            }
        }
    }
}
