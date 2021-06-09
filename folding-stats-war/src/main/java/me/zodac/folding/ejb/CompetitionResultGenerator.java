package me.zodac.folding.ejb;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.cache.CompetitionResultCache;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.RetiredUserSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * {@link Singleton} EJB used to generate a {@link CompetitionSummary} for the <code>Team Competition</code>.
 */
@Singleton
public class CompetitionResultGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompetitionResultGenerator.class);

    @EJB
    private transient OldFacade oldFacade;

    /**
     * Generates a {@link CompetitionSummary} based on the latest <code>Team Competition</code> {@link UserTcStats}.
     * Will retrieve a cached instance if one exists and the {@link SystemState} is not {@link SystemState#WRITE_EXECUTED}.
     *
     * @return the latest {@link CompetitionSummary}
     */
    public CompetitionSummary generate() {
        // TODO: [zodac] This cache logic should not be here, should be in the Storage access layer (whatever that will be)
        final CompetitionResultCache competitionResultCache = CompetitionResultCache.get();
        if (SystemStateManager.current() != SystemState.WRITE_EXECUTED && competitionResultCache.hasCachedResult()) {
            LOGGER.debug("System is not in state {} and has a cached TC result, using cache", SystemState.WRITE_EXECUTED);


            final Optional<CompetitionSummary> cachedCompetitionResult = competitionResultCache.getResult();
            if (cachedCompetitionResult.isPresent()) {
                return cachedCompetitionResult.get();
            } else {
                LOGGER.warn("Cache said it had TC result, but none was returned! Calculating new TC result");
            }
        }

        LOGGER.debug("Calculating latest TC result, system state: {}, TC cache populated: {}", SystemStateManager.current(), competitionResultCache.hasCachedResult());

        final List<TeamSummary> teamSummaries = getStatsForTeams();
        LOGGER.debug("Found {} TC teams", teamSummaries.size());

        if (teamSummaries.isEmpty()) {
            LOGGER.warn("No TC teams to show");
        }

        final CompetitionSummary competitionSummary = CompetitionSummary.create(teamSummaries);
        competitionResultCache.add(competitionSummary);
        SystemStateManager.next(SystemState.AVAILABLE);
        return competitionSummary;
    }

    private List<TeamSummary> getStatsForTeams() {
        return oldFacade.getAllTeams()
                .stream()
                .map(this::getTcTeamResult)
                .collect(toList());
    }

    private TeamSummary getTcTeamResult(final Team team) {
        LOGGER.debug("Converting team '{}' for TC stats", team.getTeamName());

        final Collection<User> usersOnTeam = oldFacade.getUsersOnTeam(team);

        final Collection<UserSummary> activeUserSummaries = usersOnTeam
                .stream()
                .map(this::getTcStatsForUser)
                .collect(toList());

        final Collection<RetiredUserSummary> retiredUserSummaries = oldFacade.getRetiredUsersForTeam(team)
                .stream()
                .map(RetiredUserSummary::create)
                .collect(toList());

        final String captainDisplayName = getCaptainDisplayName(team.getTeamName(), usersOnTeam);
        return TeamSummary.create(team.getTeamName(), team.getTeamDescription(), team.getForumLink(), captainDisplayName, activeUserSummaries, retiredUserSummaries);
    }

    private String getCaptainDisplayName(final String teamName, final Collection<User> usersOnTeam) {
        for (final User user : usersOnTeam) {
            if (user.isUserIsCaptain()) {
                return user.getDisplayName();
            }
        }

        LOGGER.warn("No captain set for team '{}'", teamName);
        return null;
    }

    private UserSummary getTcStatsForUser(final User user) {
        final Hardware hardware = user.getHardware();
        final Category category = user.getCategory();

        final UserTcStats userTcStats = oldFacade.getTcStatsForUser(user.getId());
        LOGGER.debug("Results for {}: {} points | {} multiplied points | {} units", user.getDisplayName(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits());
        return UserSummary.create(user.getId(), user.getDisplayName(), user.getFoldingUserName(), hardware, category, userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits(), user.getProfileLink(), user.getLiveStatsLink());
    }
}
