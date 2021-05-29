package me.zodac.folding.ejb;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.HardwareNotFoundException;
import me.zodac.folding.api.tc.exception.NoStatsAvailableException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.cache.CompetitionResultCache;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.RetiredUserResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * {@link Singleton} EJB used to generate a {@link CompetitionResult} for the <code>Team Competition</code>.
 * <p>
 * Currently an EJB since we need to inject an instance of {@link BusinessLogic}, but if we can split that up with an
 * interface, we could move this into the JAR module as a simple class.
 */
@Singleton
public class CompetitionResultGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompetitionResultGenerator.class);

    @EJB
    private BusinessLogic businessLogic;

    /**
     * Generates a {@link CompetitionResult} based on the latest <code>Team Competition</code> {@link UserTcStats}.
     * Will retrieve a cached instance if one exists and the {@link SystemState} is not {@link SystemState#WRITE_EXECUTED}.
     *
     * @return the latest {@link CompetitionResult}
     */
    public CompetitionResult generate() {
        // TODO: [zodac] This cache logic should not be here, should be in the Storage access layer (whatever that will be)
        if (SystemStateManager.current() != SystemState.WRITE_EXECUTED && CompetitionResultCache.hasCachedResult()) {
            LOGGER.debug("System is not in state {} and has a cached TC result, using cache", SystemState.WRITE_EXECUTED);

            final Optional<CompetitionResult> cachedCompetitionResult = CompetitionResultCache.get();
            if (cachedCompetitionResult.isPresent()) {
                return cachedCompetitionResult.get();
            } else {
                LOGGER.warn("Cache said it had TC result, but none was returned! Calculating new TC result");
            }
        }

        LOGGER.debug("Calculating latest TC result, system state: {}, TC cache populated: {}", SystemStateManager.current(), CompetitionResultCache.hasCachedResult());

        final List<TeamResult> teamResults = getStatsForTeams();
        LOGGER.debug("Found {} TC teams", teamResults.size());

        if (teamResults.isEmpty()) {
            LOGGER.warn("No TC teams to show");
        }

        final CompetitionResult competitionResult = CompetitionResult.create(teamResults);
        CompetitionResultCache.add(competitionResult);
        SystemStateManager.next(SystemState.AVAILABLE);
        return competitionResult;
    }

    private List<TeamResult> getStatsForTeams() {
        try {
            final Collection<Team> teams = businessLogic.getAllTeams();
            final List<TeamResult> teamResults = new ArrayList<>(teams.size());

            for (final Team team : teams) {
                teamResults.add(getTcTeamResult(team));
            }

            return teamResults;
        } catch (final FoldingException e) {
            LOGGER.warn("Error retrieving TC team stats", e.getCause());
            return Collections.emptyList();
        }
    }

    private TeamResult getTcTeamResult(final Team team) throws FoldingException {
        LOGGER.debug("Converting team '{}' for TC stats", team.getTeamName());

        final Collection<User> usersOnTeam = businessLogic.getUsersOnTeam(team);

        final Collection<UserResult> activeUserResults = usersOnTeam
                .stream()
                .map(this::getTcStatsForUser)
                .collect(toList());

        final Collection<RetiredUserResult> retiredUserResults = businessLogic.getRetiredUsersForTeam(team)
                .stream()
                .map(RetiredUserResult::createFromRetiredStats)
                .collect(toList());

        final String captainDisplayName = getCaptainDisplayName(usersOnTeam);
        return TeamResult.create(team.getTeamName(), team.getTeamDescription(), team.getForumLink(), captainDisplayName, activeUserResults, retiredUserResults);
    }

    private String getCaptainDisplayName(final Collection<User> usersOnTeam) {
        for (final User user : usersOnTeam) {
            if (user.isUserIsCaptain()) {
                return user.getDisplayName();
            }
        }

        LOGGER.warn("No captain set for team");
        return null;
    }

    private UserResult getTcStatsForUser(final User user) {
        final Hardware hardware;

        try {
            hardware = businessLogic.getHardware(user.getHardwareId());
        } catch (final HardwareNotFoundException e) {
            LOGGER.debug("No hardware found for ID: {}", user.getHardwareId(), e);
            LOGGER.warn("No hardware found for ID: {}", user.getHardwareId());
            return UserResult.empty(user.getDisplayName());
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting TC stats for user: {}", user, e.getCause());
            return UserResult.empty(user.getDisplayName());
        }

        final Category category = Category.get(user.getCategory());
        if (category == Category.INVALID) {
            LOGGER.warn("Unexpectedly got an invalid category '{}' for Folding user: {}", user.getCategory(), user.getDisplayName());
            return UserResult.empty(user.getDisplayName());
        }

        try {
            final UserTcStats userTcStats = businessLogic.getTcStatsForUser(user.getId());
            LOGGER.debug("Results for {}: {} points | {} multiplied points | {} units", user.getDisplayName(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits());
            return UserResult.create(user.getId(), user.getDisplayName(), user.getFoldingUserName(), hardware, category.displayName(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits(), user.getProfileLink(), user.getLiveStatsLink());
        } catch (final UserNotFoundException | NoStatsAvailableException e) {
            LOGGER.debug("No stats found for user ID: {}", user.getId(), e);
            LOGGER.warn("No stats found for user ID: {}", user.getId());
            return UserResult.empty(user.getDisplayName());
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting TC stats for user: {}", user, e.getCause());
            return UserResult.empty(user.getDisplayName());
        }
    }
}
