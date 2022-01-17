/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import me.zodac.folding.state.SystemStateManager;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.util.ProcessingType;
import me.zodac.folding.ejb.api.FoldingStatsCore;
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

/**
 * REST endpoint for <code>Team Competition</code> stats.
 *
 * @see me.zodac.folding.client.java.request.TeamCompetitionStatsRequestSender
 * @see me.zodac.folding.client.java.response.TeamCompetitionStatsResponseParser
 */
@Path("/stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private UriInfo uriContext;

    @EJB
    private FoldingStatsCore foldingStatsCore;

    @EJB
    private LeaderboardStatsGenerator leaderboardStatsGenerator;

    @EJB
    private StatsScheduler statsScheduler;

    @EJB
    private UserStatsParser userStatsParser;

    @EJB
    private UserStatsResetter userStatsResetter;

    /**
     * {@link GET} request to retrieve the <code>Team Competition</code> overall {@link CompetitionSummary}.
     *
     * @return {@link Response.Status#OK} containing the {@link CompetitionSummary}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamCompetitionStats() {
        LOGGER.debug("GET request received to show TC stats");

        try {
            final CompetitionSummary competitionSummary = foldingStatsCore.getCompetitionSummary();
            return ok(competitionSummary);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving full TC stats", e);
            return serverError();
        }
    }

    /**
     * {@link GET} request to retrieve the <code>Team Competition</code> {@link UserSummary} for the given {@link User}.
     *
     * @param userId the ID of the {@link User} whose {@link UserSummary} is to be retrieved
     * @return {@link Response.Status#OK} containing the {@link UserSummary}
     */
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

            final Optional<User> optionalUser = foldingStatsCore.getUserWithoutPasskey(parsedId);
            if (optionalUser.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }
            final User user = optionalUser.get();

            final CompetitionSummary competitionSummary = foldingStatsCore.getCompetitionSummary();
            final Collection<UserSummary> userSummaries = competitionSummary.getTeams()
                .stream()
                .flatMap(teamResult -> teamResult.getActiveUsers().stream())
                .collect(toList());

            for (final UserSummary userSummary : userSummaries) {
                if (userSummary.getUser().getId() == user.getId()) {
                    return ok(userSummary);
                }
            }

            return notFound();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC stats for users", e);
            return serverError();
        }
    }

    /**
     * {@link PATCH} request to update an existing {@link UserSummary} with an {@link OffsetTcStats}.
     *
     * @param userId        the ID of the {@link UserSummary} to be updated
     * @param offsetTcStats the {@link OffsetTcStats} to be applied
     * @return {@link Response.Status#OK}
     */
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

            final Optional<User> optionalUser = foldingStatsCore.getUserWithPasskey(parsedId);
            if (optionalUser.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final User user = optionalUser.get();
            final Hardware hardware = user.getHardware();
            final OffsetTcStats offsetTcStatsToPersist = OffsetTcStats.updateWithHardwareMultiplier(offsetTcStats, hardware.getMultiplier());
            final OffsetTcStats createdOffsetStats = foldingStatsCore.createOrUpdateOffsetStats(user, offsetTcStatsToPersist);

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

    /**
     * {@link GET} request to retrieve the <code>Team Competition</code> {@link TeamLeaderboardEntry}s.
     *
     * @return {@link Response.Status#OK} containing the {@link TeamLeaderboardEntry}s
     */
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

    /**
     * {@link GET} request to retrieve the <code>Team Competition</code> {@link UserCategoryLeaderboardEntry}s by {@link Category}.
     *
     * @return {@link Response.Status#OK} containing the {@link UserCategoryLeaderboardEntry}s
     */
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

    /**
     * {@link POST} request to manually update the <code>Team Competition</code> stats.
     *
     * @param async whether the execution should be performed asynchronously or synchronously
     * @return {@link Response.Status#OK}
     */
    @POST
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

    /**
     * {@link POST} request to manually reset the <code>Team Competition</code> stats.
     *
     * @return {@link Response.Status#OK}
     */
    @POST
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
