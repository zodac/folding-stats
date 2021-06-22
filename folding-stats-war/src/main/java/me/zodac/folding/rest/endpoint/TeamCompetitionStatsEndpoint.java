package me.zodac.folding.rest.endpoint;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.nullRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.ejb.CompetitionResultGenerator;
import me.zodac.folding.ejb.OldFacade;
import me.zodac.folding.ejb.UserTeamCompetitionStatsParser;
import me.zodac.folding.ejb.scheduled.TeamCompetitionResetScheduler;
import me.zodac.folding.ejb.scheduled.TeamCompetitionStatsScheduler;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import me.zodac.folding.rest.parse.IntegerParser;
import me.zodac.folding.rest.parse.ParseResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private UriInfo uriContext;

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private OldFacade oldFacade;

    @EJB
    private CompetitionResultGenerator competitionResultGenerator;

    @EJB
    private TeamCompetitionResetScheduler teamCompetitionResetScheduler;

    @EJB
    private TeamCompetitionStatsScheduler teamCompetitionStatsScheduler;

    @EJB
    private UserTeamCompetitionStatsParser userTeamCompetitionStatsParser;

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
            final CompetitionSummary competitionSummary = competitionResultGenerator.generate();
            return ok(competitionSummary);
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
            final ParseResult parseResult = IntegerParser.parsePositive(userId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The user ID '%s' is not a valid format", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The user ID '%s' is out of range", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();
            final Optional<User> optionalUser = businessLogic.getUserWithoutPasskey(parsedId);

            if (optionalUser.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }
            final User user = optionalUser.get();

            final CompetitionSummary competitionSummary = competitionResultGenerator.generate();
            final Collection<UserSummary> userSummaries = competitionSummary.getTeams()
                .stream()
                .flatMap(teamResult -> teamResult.getActiveUsers().stream())
                .collect(toList());

            for (final UserSummary userSummary : userSummaries) {
                if (userSummary.getId() == user.getId()) {
                    return ok(userSummary);
                }
            }

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
            final ParseResult parseResult = IntegerParser.parsePositive(userId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The user ID '%s' is not a valid format", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The user ID '%s' is out of range", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();
            final Optional<User> optionalUser = businessLogic.getUserWithPasskey(parsedId);

            if (optionalUser.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }
            final User user = optionalUser.get();

            final Hardware hardware = user.getHardware();
            final OffsetStats offsetStatsToUse = OffsetStats.updateWithHardwareMultiplier(offsetStats, hardware.getMultiplier());

            oldFacade.addOrUpdateOffsetStats(parsedId, offsetStatsToUse);
            SystemStateManager.next(SystemState.UPDATING_STATS);
            userTeamCompetitionStatsParser.parseTcStatsForUserAndWait(user);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating user with ID: {}", userId, e);
            return serverError();
        }
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
            final CompetitionSummary competitionSummary = competitionResultGenerator.generate();
            final List<TeamSummary> teamResults = competitionSummary.getTeams()
                .stream()
                .sorted(Comparator.comparingLong(TeamSummary::getTeamMultipliedPoints).reversed())
                .collect(toList());

            if (teamResults.isEmpty()) {
                LOGGER.warn("No TC teams to show");
                return ok(Collections.emptyList());
            }

            final TeamLeaderboardEntry leader = TeamLeaderboardEntry.createLeader(teamResults.get(0));

            final List<TeamLeaderboardEntry> teamSummaries = new ArrayList<>(teamResults.size());
            teamSummaries.add(leader);

            for (int i = 1; i < teamResults.size(); i++) {
                final TeamSummary teamSummary = teamResults.get(i);
                final TeamSummary teamAhead = teamResults.get(i - 1);

                final long diffToLeader = leader.getTeamMultipliedPoints() - teamSummary.getTeamMultipliedPoints();
                final long diffToNext = teamAhead.getTeamMultipliedPoints() - teamSummary.getTeamMultipliedPoints();

                final int rank = i + 1;
                final TeamLeaderboardEntry teamLeaderboardEntry = TeamLeaderboardEntry.create(teamSummary, rank, diffToLeader, diffToNext);
                teamSummaries.add(teamLeaderboardEntry);
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
            final CompetitionSummary competitionSummary = competitionResultGenerator.generate();
            if (competitionSummary.getTeams().isEmpty()) {
                LOGGER.warn("No TC teams to show");
                return ok(Collections.emptyList());
            }

            final Map<Category, List<UserSummary>> userResultsByCategory = new EnumMap<>(Category.class);
            final Map<String, String> teamNameForFoldingUserName = new HashMap<>(); // Convenient way to determine the team name of a user

            for (final TeamSummary teamSummary : competitionSummary.getTeams()) {
                final String teamName = teamSummary.getTeamName();

                for (final UserSummary userSummary : teamSummary.getActiveUsers()) {
                    final Category category = Category.get(userSummary.getCategory());

                    final List<UserSummary> existingUsersInCategory = userResultsByCategory.getOrDefault(category, new ArrayList<>(0));
                    existingUsersInCategory.add(userSummary);

                    userResultsByCategory.put(category, existingUsersInCategory);
                    teamNameForFoldingUserName.put(userSummary.getFoldingName(), teamName);
                }
            }

            final Map<String, List<UserCategoryLeaderboardEntry>> categoryLeaderboard = new TreeMap<>();

            for (final var entry : userResultsByCategory.entrySet()) {
                final Category category = entry.getKey();
                final List<UserSummary> userSummaries = entry.getValue()
                    .stream()
                    .sorted(Comparator.comparingLong(UserSummary::getMultipliedPoints).reversed())
                    .collect(toList());

                final UserSummary firstResult = userSummaries.get(0);
                final UserCategoryLeaderboardEntry categoryLeader =
                    UserCategoryLeaderboardEntry.createLeader(firstResult, teamNameForFoldingUserName.get(firstResult.getFoldingName()));

                final List<UserCategoryLeaderboardEntry> userSummariesInCategory = new ArrayList<>(userSummaries.size());
                userSummariesInCategory.add(categoryLeader);

                for (int i = 1; i < userSummaries.size(); i++) {
                    final UserSummary userSummary = userSummaries.get(i);
                    final UserSummary userAhead = userSummaries.get(i - 1);

                    final long diffToLeader = categoryLeader.getMultipliedPoints() - userSummary.getMultipliedPoints();
                    final long diffToNext = userAhead.getMultipliedPoints() - userSummary.getMultipliedPoints();

                    final String teamName = teamNameForFoldingUserName.get(userSummary.getFoldingName());
                    final int rank = i + 1;
                    final UserCategoryLeaderboardEntry userCategoryLeaderboardEntry =
                        UserCategoryLeaderboardEntry.create(userSummary, teamName, rank, diffToLeader, diffToNext);
                    userSummariesInCategory.add(userCategoryLeaderboardEntry);
                }

                categoryLeaderboard.put(category.toString(), userSummariesInCategory);
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
            teamCompetitionResetScheduler.resetTeamCompetitionStats();
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error manually resetting TC stats", e);
            return serverError();
        }
    }
}
