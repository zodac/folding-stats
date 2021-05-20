package me.zodac.folding.rest;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.ejb.BusinessLogic;
import me.zodac.folding.ejb.CompetitionResultGenerator;
import me.zodac.folding.ejb.TeamCompetitionResetScheduler;
import me.zodac.folding.ejb.TeamCompetitionStatsScheduler;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;
import me.zodac.folding.rest.api.tc.leaderboard.TeamSummary;
import me.zodac.folding.rest.api.tc.leaderboard.UserSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

@Path("/tc_stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsEndpoint.class);

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private CompetitionResultGenerator competitionResultGenerator;

    @EJB
    private TeamCompetitionResetScheduler teamCompetitionResetScheduler;

    @EJB
    private TeamCompetitionStatsScheduler teamCompetitionStatsScheduler;

    @Context
    private UriInfo uriContext;

    @GET
    @Path("/manual/")
    public Response manualStats(@QueryParam("async") final boolean async) {
        LOGGER.info("GET request received to manually parse TC stats");

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final ExecutionType executionType = async ? ExecutionType.ASYNCHRONOUS : ExecutionType.SYNCHRONOUS;
            teamCompetitionStatsScheduler.manualTeamCompetitionStatsParsing(executionType);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error manually parsing TC stats", e);
            return serverError();
        }
    }

    @GET
    @Path("/reset/")
    public Response resetStats() {
        LOGGER.info("GET request received to manually reset TC stats");

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            SystemStateManager.next(SystemState.RESETTING_STATS);
            teamCompetitionResetScheduler.manualResetTeamCompetitionStats();
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error manually resetting TC stats", e);
            return serverError();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamCompetitionStats() {
        LOGGER.debug("GET request received to show TC stats");

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final CompetitionResult competitionResult = competitionResultGenerator.generate();
            return ok(competitionResult);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving full TC stats", e);
            return serverError();
        }
    }

    @GET
    @Path("/leaderboard/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamLeaderboard() {
        LOGGER.debug("GET request received to show TC leaderboard");

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final CompetitionResult competitionResult = competitionResultGenerator.generate();
            final List<TeamResult> teamResults = competitionResult.getTeams()
                    .stream()
                    .sorted(Comparator.comparingLong(TeamResult::getTeamMultipliedPoints).reversed())
                    .collect(Collectors.toList());

            if (teamResults.isEmpty()) {
                LOGGER.warn("No TC teams to show");
                return ok(Collections.emptyList());
            }

            final TeamSummary leader = TeamSummary.createLeader(teamResults.get(0));

            final List<TeamSummary> teamSummaries = new ArrayList<>(teamResults.size());
            teamSummaries.add(leader);

            for (int i = 1; i < teamResults.size(); i++) {
                final TeamResult teamResult = teamResults.get(i);
                final TeamResult teamAhead = teamResults.get(i - 1);

                final long diffToLeader = leader.getTeamMultipliedPoints() - teamResult.getTeamMultipliedPoints();
                final long diffToNext = teamAhead.getTeamMultipliedPoints() - teamResult.getTeamMultipliedPoints();

                final int rank = i + 1;
                final TeamSummary teamSummary = TeamSummary.create(teamResult, rank, diffToLeader, diffToNext);
                teamSummaries.add(teamSummary);
            }

            return ok(teamSummaries);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC leaderboard", e);
            return serverError();
        }
    }

    @GET
    @Path("/category/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategoryLeaderboard() {
        LOGGER.debug("GET request received to show TC category leaderboard");

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final CompetitionResult competitionResult = competitionResultGenerator.generate();
            final Map<Category, List<UserResult>> userResultsByCategory = new EnumMap<>(Category.class);
            final Map<String, String> teamNameForFoldingUserName = new HashMap<>(); // Convenient way to determine the team name of a user

            if (competitionResult.getTeams().isEmpty()) {
                LOGGER.warn("No TC teams to show");
                return ok(Collections.emptyList());
            }

            for (final TeamResult teamResult : competitionResult.getTeams()) {
                final String teamName = teamResult.getTeamName();

                for (final UserResult userResult : teamResult.getActiveUsers()) {
                    final Category category = Category.get(userResult.getCategory());

                    final List<UserResult> existingUsersInCategory = userResultsByCategory.getOrDefault(category, new ArrayList<>());
                    existingUsersInCategory.add(userResult);

                    userResultsByCategory.put(category, existingUsersInCategory);
                    teamNameForFoldingUserName.put(userResult.getFoldingName(), teamName);
                }
            }

            final Map<String, List<UserSummary>> categoryLeaderboard = new TreeMap<>();

            for (final var entry : userResultsByCategory.entrySet()) {
                final Category category = entry.getKey();
                final List<UserResult> userResults = entry.getValue()
                        .stream()
                        .sorted(Comparator.comparingLong(UserResult::getMultipliedPoints).reversed())
                        .collect(Collectors.toList());

                final UserResult firstResult = userResults.get(0);
                final UserSummary categoryLeader = UserSummary.createLeader(firstResult, teamNameForFoldingUserName.get(firstResult.getFoldingName()));

                final List<UserSummary> userSummariesInCategory = new ArrayList<>(userResults.size());
                userSummariesInCategory.add(categoryLeader);

                for (int i = 1; i < userResults.size(); i++) {
                    final UserResult userResult = userResults.get(i);
                    final UserResult userAhead = userResults.get(i - 1);

                    final long diffToLeader = categoryLeader.getMultipliedPoints() - userResult.getMultipliedPoints();
                    final long diffToNext = userAhead.getMultipliedPoints() - userResult.getMultipliedPoints();

                    final String teamName = teamNameForFoldingUserName.get(userResult.getFoldingName());
                    final int rank = i + 1;
                    final UserSummary userSummary = UserSummary.create(userResult, teamName, rank, diffToLeader, diffToNext);
                    userSummariesInCategory.add(userSummary);
                }

                categoryLeaderboard.put(category.displayName(), userSummariesInCategory);
            }

            return ok(categoryLeaderboard);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC stats", e);
            return serverError();
        }
    }
}
