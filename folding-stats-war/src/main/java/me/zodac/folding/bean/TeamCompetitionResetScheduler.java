package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.TeamNotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Startup
@Singleton
public class TeamCompetitionResetScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionResetScheduler.class);

    @EJB
    private StorageFacade storageFacade;

    @Schedule(dayOfMonth = "1", minute = "55", info = "Monthly cache reset for TC teams")
    public void monthlyTcStatsReset() {
        LOGGER.info("Resetting TC caches for new month");
        manualTeamCompetitionStatsReset();
    }

    public void manualTeamCompetitionStatsReset() {
        final List<Team> teams;
        try {
            teams = storageFacade.getAllTeams();
            if (teams.isEmpty()) {
                LOGGER.error("No TC teams configured in system!");
                return;
            }
        } catch (final FoldingException e) {
            LOGGER.error("Unable to get teams!");
            return;
        }

        final Map<Integer, User> usersById = storageFacade.getActiveTcUsers(teams);
        if (usersById.isEmpty()) {
            LOGGER.error("No TC users configured in system!");
            return;
        }

        resetStats(usersById.values());
        clearOffsets();
        removeRetiredUsersFromTeams(teams);
    }

    private void removeRetiredUsersFromTeams(final List<Team> teams) {
        LOGGER.debug("Removing retired users from teams");
        for (final Team team : teams) {
            if (team.getRetiredUserIds().isEmpty()) {
                LOGGER.debug("No retired users in team '{}'", team.getTeamName());
                continue;
            }

            try {
                LOGGER.debug("Removing retired users from team '{}'", team.getTeamName());
                final Team teamWithoutRetiredUsers = Team.removeRetiredUsers(team);
                storageFacade.updateTeam(teamWithoutRetiredUsers);
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
            storageFacade.clearOffsetStats();
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
                storageFacade.updateInitialStatsForUser(user);
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