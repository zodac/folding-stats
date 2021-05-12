package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.HardwareNotFoundException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.bean.TeamCompetitionResetScheduler;
import me.zodac.folding.bean.TeamCompetitionStatsScheduler;
import me.zodac.folding.cache.CompetitionResultCache;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.okBuilder;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

@Path("/tc_stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsEndpoint.class);

    // Stats updates occur every hour, so we must invalidate responses every hour
    private static final int CACHE_EXPIRATION_TIME = (int) TimeUnit.HOURS.toSeconds(1);

    @EJB
    private StorageFacade storageFacade;

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

        final ExecutionType executionType = async ? ExecutionType.ASYNCHRONOUS : ExecutionType.SYNCHRONOUS;
        teamCompetitionStatsScheduler.manualTeamCompetitionStatsParsing(executionType);
        return ok();
    }

    @GET
    @Path("/reset/")
    public Response resetStats() {
        LOGGER.info("GET request received to manually reset TC stats");

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        SystemStateManager.next(SystemState.RESETTING_STATS);
        teamCompetitionResetScheduler.manualResetTeamCompetitionStats();
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        return ok();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamCompetitionStats(@Context final Request request) {
        LOGGER.debug("GET request received to show TC stats");

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        if (SystemStateManager.current() != SystemState.WRITE_EXECUTED && CompetitionResultCache.hasCachedResult()) {
            LOGGER.debug("System is not in state {} and has a cached TC result, using cache", SystemState.WRITE_EXECUTED);

            final Optional<CompetitionResult> cachedCompetitionResult = CompetitionResultCache.get();
            if (cachedCompetitionResult.isPresent()) {
                final CompetitionResult cachedResult = cachedCompetitionResult.get();
                return ok(cachedResult);
            } else {
                LOGGER.warn("Cache said it had TC result, but none was returned! Calculating new TC result");
            }
        }

        LOGGER.debug("Calculating latest TC result, system state: {}, TC cache populated: {}", SystemStateManager.current(), CompetitionResultCache.hasCachedResult());

        try {
            final List<TeamResult> teamResults = getStatsForTeams();
            LOGGER.debug("Found {} TC teams", teamResults.size());

            if (teamResults.isEmpty()) {
                LOGGER.warn("No TC teams to show");
            }

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(CACHE_EXPIRATION_TIME);

            final EntityTag entityTag = new EntityTag(String.valueOf(teamResults.hashCode()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);

            if (builder == null) {
                LOGGER.debug("Cached resources have changed");

                final CompetitionResult competitionResult = CompetitionResult.create(teamResults);
                CompetitionResultCache.add(competitionResult);
                SystemStateManager.next(SystemState.AVAILABLE);

                builder = okBuilder(competitionResult);
                builder.tag(entityTag);
            }

            builder.cacheControl(cacheControl);
            return builder.build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC stats", e);
            return serverError();
        }
    }

    private List<TeamResult> getStatsForTeams() {
        try {
            final List<Team> teams = storageFacade.getAllTeams();
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

        final List<UserResult> userResults = team.getUserIds()
                .stream()
                .map(this::getTcUser)
                .filter(Objects::nonNull)
                .collect(toList());

        try {
            final User captain = storageFacade.getUser(team.getCaptainUserId());
            final Set<Integer> retiredUserIds = team.getRetiredUserIds();

            final List<UserResult> retiredUserResults = new ArrayList<>(retiredUserIds.size());

            for (final int retiredUserId : retiredUserIds) {
                final RetiredUserTcStats retiredUserTcStats = storageFacade.getRetiredUser(retiredUserId);

                try {
                    final User retiredUser = storageFacade.getUser(retiredUserTcStats.getUserId());
                    final Hardware retiredUserHardware = storageFacade.getHardware(retiredUser.getHardwareId());
                    retiredUserResults.add(UserResult.createForRetiredUser(retiredUser, retiredUserHardware, retiredUserTcStats));
                } catch (final UserNotFoundException e) {
                    LOGGER.debug("Unable to find retired user ID {} with original user ID: {}", retiredUserId, retiredUserTcStats.getUserId(), e);
                    LOGGER.warn("Unable to find retired user ID {} with original user ID: {}", retiredUserId, retiredUserTcStats.getUserId());
                    retiredUserResults.add(UserResult.empty(retiredUserTcStats.getDisplayUserName()));
                }
            }

            return TeamResult.create(team.getTeamName(), team.getTeamDescription(), captain.getDisplayName(), userResults, retiredUserResults);
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to get details for team captain: {}", team, e);
            throw e;
        } catch (final UserNotFoundException e) {
            LOGGER.warn("User ID not found, unexpected error: {}", team, e);
            throw new FoldingException(String.format("User ID not found: %s", team), e);
        } catch (final HardwareNotFoundException e) {
            LOGGER.warn("Hardware ID not found for retired user, unexpected error: {}", team, e);
            throw new FoldingException(String.format("Hardware ID not found for retired user: %s", team), e);
        }
    }

    private UserResult getTcUser(final int userId) {
        if (userId == User.EMPTY_USER_ID) {
            LOGGER.warn("User had invalid ID");
            return null;
        }

        try {
            final User user = storageFacade.getUser(userId);
            return getTcStatsForUser(user);
        } catch (final UserNotFoundException e) {
            LOGGER.warn("Unable to find user ID: {}", userId, e);
            return null;
        } catch (final FoldingException e) {
            LOGGER.warn("Error finding user ID: {}", userId, e.getCause());
            return null;
        }
    }

    private UserResult getTcStatsForUser(final User user) {
        final Hardware hardware;

        try {
            hardware = storageFacade.getHardware(user.getHardwareId());
        } catch (final HardwareNotFoundException e) {
            LOGGER.debug("No hardware found for ID: {}", user.getHardwareId(), e);
            LOGGER.warn("No hardware found for ID: {}", user.getHardwareId());
            return null;
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting TC stats for user: {}", user, e.getCause());
            return null;
        }

        final Category category = Category.get(user.getCategory());
        if (category == Category.INVALID) {
            LOGGER.warn("Unexpectedly got an invalid category '{}' for Folding user: {}", user.getCategory(), user.getDisplayName());
            return null;
        }

        try {
            final UserTcStats userTcStats = storageFacade.getTcStatsForUser(user.getId());
            LOGGER.debug("Results for {}: {} points | {} multiplied points | {} units", user.getDisplayName(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits());
            return UserResult.createWithNoRank(user.getDisplayName(), hardware, category.displayName(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits(), user.getLiveStatsLink(), user.isRetired());
        } catch (final UserNotFoundException e) {
            LOGGER.debug("No stats found for user ID: {}", user.getId(), e);
            LOGGER.warn("No stats found for user ID: {}", user.getId());
            return UserResult.empty(user.getDisplayName());
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting TC stats for user: {}", user, e.getCause());
            return null;
        }
    }
}
