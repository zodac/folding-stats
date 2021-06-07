package me.zodac.folding.rest.endpoint;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.FoldingIdInvalidException;
import me.zodac.folding.api.tc.exception.FoldingIdOutOfRangeException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.ejb.BusinessLogic;
import me.zodac.folding.ejb.CompetitionResultGenerator;
import me.zodac.folding.ejb.TeamCompetitionResetScheduler;
import me.zodac.folding.ejb.TeamCompetitionStatsScheduler;
import me.zodac.folding.ejb.UserTeamCompetitionStatsParser;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;
import me.zodac.folding.rest.api.tc.leaderboard.TeamSummary;
import me.zodac.folding.rest.api.tc.leaderboard.UserSummary;
import me.zodac.folding.rest.util.IdentityParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.rest.util.response.Responses.badRequest;
import static me.zodac.folding.rest.util.response.Responses.notFound;
import static me.zodac.folding.rest.util.response.Responses.nullRequest;
import static me.zodac.folding.rest.util.response.Responses.ok;
import static me.zodac.folding.rest.util.response.Responses.serverError;
import static me.zodac.folding.rest.util.response.Responses.serviceUnavailable;

@Path("/stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsEndpoint.class);

    @Context
    private transient UriInfo uriContext;

    @EJB
    private transient BusinessLogic businessLogic;

    @EJB
    private transient CompetitionResultGenerator competitionResultGenerator;

    @EJB
    private transient TeamCompetitionResetScheduler teamCompetitionResetScheduler;

    @EJB
    private transient TeamCompetitionStatsScheduler teamCompetitionStatsScheduler;

    @EJB
    private transient UserTeamCompetitionStatsParser userTeamCompetitionStatsParser;

    @GET
    @PermitAll
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
    @PermitAll
    @Path("/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamCompetitionStatsForUser(@PathParam("userId") final String userId) {
        LOGGER.debug("GET request received to show TC stats for user received at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final int parsedId = IdentityParser.parse(userId);
            final User user = businessLogic.getUserWithPasskey(parsedId, false);

            final CompetitionResult competitionResult = competitionResultGenerator.generate();
            final Collection<UserResult> userResults = competitionResult.getTeams()
                    .stream()
                    .flatMap(teamResult -> teamResult.getActiveUsers().stream())
                    .collect(toList());

            for (final UserResult userResult : userResults) {
                if (userResult.getId() == user.getId()) {
                    return ok(userResult);
                }
            }

            return notFound();
        } catch (final FoldingIdInvalidException e) {
            final String errorMessage = String.format("The user ID '%s' is not a valid format", e.getId());
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingIdOutOfRangeException e) {
            final String errorMessage = String.format("The user ID '%s' is out of range", e.getId());
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final UserNotFoundException e) {
            LOGGER.debug("Error getting {} with ID {}", e.getType(), e.getId(), e);
            LOGGER.error("Error getting {} with ID {}", e.getType(), e.getId());
            return notFound();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC stats for users", e);
            return serverError();
        }
    }

    @PATCH
    @RolesAllowed("admin")
    @Path("/users/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserWithOffset(@PathParam("userId") final String userId, final OffsetStats offsetStats) {
        LOGGER.debug("PATCH request to update offset for user received at '{}': {}", uriContext.getAbsolutePath(), offsetStats);

        if (SystemStateManager.current().isWriteBlocked()) {
            LOGGER.warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        if (offsetStats == null) {
            LOGGER.error("Payload is null");
            return nullRequest();
        }

        try {
            final int parsedId = IdentityParser.parse(userId);
            final User user = businessLogic.getUser(parsedId);

            final OffsetStats offsetStatsToUse = getValidUserStatsOffset(user, offsetStats);
            businessLogic.addOrUpdateOffsetStats(parsedId, offsetStatsToUse);
            SystemStateManager.next(SystemState.UPDATING_STATS);
            userTeamCompetitionStatsParser.parseTcStatsForUserAndWait(businessLogic.getUser(parsedId));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok();
        } catch (final FoldingIdInvalidException e) {
            final String errorMessage = String.format("The user ID '%s' is not a valid format", e.getId());
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingIdOutOfRangeException e) {
            final String errorMessage = String.format("The user ID '%s' is out of range", e.getId());
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final UserNotFoundException e) {
            LOGGER.error("Error finding user with ID: {}", userId, e.getCause());
            return notFound();
        } catch (final FoldingException e) {
            LOGGER.error("Error updating user with ID: {}", userId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating user with ID: {}", userId, e);
            return serverError();
        }
    }

    private OffsetStats getValidUserStatsOffset(final User user, final OffsetStats offsetStats) throws FoldingException, UserNotFoundException {
        final Hardware hardware = user.getHardware();
        return OffsetStats.updateWithHardwareMultiplier(offsetStats, hardware.getMultiplier());
    }

    @GET
    @PermitAll
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
                    .collect(toList());

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
    @PermitAll
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
            if (competitionResult.getTeams().isEmpty()) {
                LOGGER.warn("No TC teams to show");
                return ok(Collections.emptyList());
            }

            final Map<Category, List<UserResult>> userResultsByCategory = new EnumMap<>(Category.class);
            final Map<String, String> teamNameForFoldingUserName = new HashMap<>(); // Convenient way to determine the team name of a user

            for (final TeamResult teamResult : competitionResult.getTeams()) {
                final String teamName = teamResult.getTeamName();

                for (final UserResult userResult : teamResult.getActiveUsers()) {
                    final Category category = Category.get(userResult.getCategory());

                    final List<UserResult> existingUsersInCategory = userResultsByCategory.getOrDefault(category, new ArrayList<>(0));
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
                        .collect(toList());

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

    @GET
    @RolesAllowed("admin")
    @Path("/manual/update")
    public Response updateStats(@QueryParam("async") final boolean async) {
        LOGGER.info("GET request received to manually update TC stats");

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
    @RolesAllowed("admin")
    @Path("/manual/reset/")
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
}
