package me.zodac.folding.ejb.tc.summary;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import javax.ejb.Singleton;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.ejb.api.BusinessLogic;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.RetiredUserSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Singleton} EJB used to generate a {@link CompetitionSummary} for the <code>Team Competition</code>.
 */
final class CompetitionSummaryGenerator {

    private static final Logger LOGGER = LogManager.getLogger();

    private CompetitionSummaryGenerator() {
        
    }

    /**
     * Generates a {@link CompetitionSummary} based on the latest <code>Team Competition</code> {@link UserTcStats}.
     * Will retrieve a cached instance if one exists and the {@link SystemState} is not {@link SystemState#WRITE_EXECUTED}.
     *
     * @param businessLogic the {@link BusinessLogic} to retrieve current system information
     * @return the latest {@link CompetitionSummary}
     */
    static CompetitionSummary generate(final BusinessLogic businessLogic) {
        LOGGER.debug("Calculating latest TC result");

        final List<TeamSummary> teamSummaries = getStatsForTeams(businessLogic);
        LOGGER.debug("Found {} TC teams", teamSummaries::size);

        if (teamSummaries.isEmpty()) {
            LOGGER.warn("No TC teams to show");
        }

        return CompetitionSummary.create(teamSummaries);
    }

    private static List<TeamSummary> getStatsForTeams(final BusinessLogic businessLogic) {
        return businessLogic.getAllTeams()
            .stream()
            .map(team -> getTcTeamResult(team, businessLogic))
            .collect(toList());
    }

    private static TeamSummary getTcTeamResult(final Team team, final BusinessLogic businessLogic) {
        LOGGER.debug("Converting team '{}' for TC stats", team::getTeamName);

        final Collection<User> usersOnTeam = businessLogic.getUsersOnTeam(team);

        final Collection<UserSummary> activeUserSummaries = usersOnTeam
            .stream()
            .map(user -> getTcStatsForUser(user, businessLogic))
            .collect(toList());

        final Collection<RetiredUserSummary> retiredUserSummaries = businessLogic.getAllRetiredUsersForTeam(team)
            .stream()
            .map(RetiredUserSummary::createWithDefaultRank)
            .collect(toList());

        final String captainDisplayName = getCaptainDisplayName(team.getTeamName(), usersOnTeam);
        return TeamSummary.createWithDefaultRank(team, captainDisplayName, activeUserSummaries, retiredUserSummaries);
    }

    private static UserSummary getTcStatsForUser(final User user, final BusinessLogic businessLogic) {
        final Hardware hardware = user.getHardware();
        final Category category = user.getCategory();

        final UserTcStats userTcStats = businessLogic.getHourlyTcStats(user);
        LOGGER.debug("Results for {}: {} points | {} multiplied points | {} units", user::getDisplayName, userTcStats::getPoints,
            userTcStats::getMultipliedPoints, userTcStats::getUnits);
        return UserSummary.createWithDefaultRank(user.getId(), user.getDisplayName(), user.getFoldingUserName(), hardware, category,
            user.getProfileLink(), user.getLiveStatsLink(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits());
    }

    private static String getCaptainDisplayName(final String teamName, final Collection<User> usersOnTeam) {
        for (final User user : usersOnTeam) {
            if (user.isUserIsCaptain()) {
                return user.getDisplayName();
            }
        }

        LOGGER.warn("No captain set for team '{}'", teamName);
        return null;
    }
}
