package me.zodac.folding.rest.endpoint;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.nullRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.util.ProcessingType;
import me.zodac.folding.ejb.tc.CompetitionResultGenerator;
import me.zodac.folding.ejb.tc.LeaderboardStatsGenerator;
import me.zodac.folding.ejb.tc.scheduled.StatsScheduler;
import me.zodac.folding.ejb.tc.user.UserStatsParser;
import me.zodac.folding.ejb.tc.user.UserStatsResetter;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import me.zodac.folding.rest.endpoint.util.IdResult;
import me.zodac.folding.rest.endpoint.util.IntegerParser;
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
    private CompetitionResultGenerator competitionResultGenerator;

    @EJB
    private LeaderboardStatsGenerator leaderboardStatsGenerator;

    @EJB
    private StatsScheduler statsScheduler;

    @EJB
    private UserStatsParser userStatsParser;

    @EJB
    private UserStatsResetter userStatsResetter;

    @GET
    @ReadRequired
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamCompetitionStats() {
        LOGGER.debug("GET request received to show TC stats");

        try {
            final CompetitionSummary competitionSummary = competitionResultGenerator.generate();
            return ok(competitionSummary);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving full TC stats", e);
            return serverError();
        }
    }

    @GET
    @ReadRequired
    @PermitAll
    @Path("/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamCompetitionStatsForUser(@PathParam("userId") final String userId) {
        LOGGER.debug("GET request received to show TC stats for user received at '{}'", uriContext.getAbsolutePath());

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

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
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/users/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserWithOffset(@PathParam("userId") final String userId, final OffsetTcStats offsetTcStats) {
        LOGGER.debug("PATCH request to update offset for user received at '{}': {}", uriContext.getAbsolutePath(), offsetTcStats);

        if (offsetTcStats == null || offsetTcStats.isEmpty()) {
            LOGGER.error("Payload is null");
            return nullRequest();
        }

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalUser = businessLogic.getUserWithPasskey(parsedId);
            if (optionalUser.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final User user = optionalUser.get();
            final Hardware hardware = user.getHardware();
            final OffsetTcStats offsetTcStatsToPersist = OffsetTcStats.updateWithHardwareMultiplier(offsetTcStats, hardware.getMultiplier());
            final OffsetTcStats createdOffsetStats = businessLogic.createOrUpdateOffsetStats(user, offsetTcStatsToPersist);

            SystemStateManager.next(SystemState.UPDATING_STATS);
            userStatsParser.parseTcStatsForUserAndWait(user);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Updated user with ID {} with points offset: {}", userId, createdOffsetStats);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating user with ID: {}", userId, e);
            return serverError();
        }
    }

    @GET
    @ReadRequired
    @PermitAll
    @Path("/leaderboard/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamLeaderboard() {
        LOGGER.debug("GET request received to show TC leaderboard");

        try {
            final List<TeamLeaderboardEntry> teamSummaries = leaderboardStatsGenerator.generateTeamLeaderboards();
            return ok(teamSummaries);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC leaderboard", e);
            return serverError();
        }
    }

    @GET
    @ReadRequired
    @PermitAll
    @Path("/category/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCategoryLeaderboard() {
        LOGGER.debug("GET request received to show TC category leaderboard");

        try {
            final Map<Category, List<UserCategoryLeaderboardEntry>> categoryLeaderboard =
                leaderboardStatsGenerator.generateUserCategoryLeaderboards();
            return ok(categoryLeaderboard);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC stats", e);
            return serverError();
        }
    }

    @GET
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/manual/update")
    public Response updateStats(@QueryParam("async") final boolean async) {
        LOGGER.info("GET request received to manually update TC stats");

        try {
            final ProcessingType processingType = async ? ProcessingType.ASYNCHRONOUS : ProcessingType.SYNCHRONOUS;
            statsScheduler.manualTeamCompetitionStatsParsing(processingType);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error manually parsing TC stats", e);
            return serverError();
        }
    }

    @GET
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/manual/reset/")
    public Response resetStats() {
        LOGGER.info("GET request received to manually reset TC stats");

        try {
            SystemStateManager.next(SystemState.RESETTING_STATS);
            userStatsResetter.resetTeamCompetitionStats();
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error manually resetting TC stats", e);
            return serverError();
        }
    }
}
