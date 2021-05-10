package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.HardwareNotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.bean.TeamCompetitionResetScheduler;
import me.zodac.folding.bean.TeamCompetitionStatsScheduler;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

@Path("/tc_stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsEndpoint.class);

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

        final ExecutionType executionType = async ? ExecutionType.ASYNCHRONOUS : ExecutionType.SYNCHRONOUS;
        teamCompetitionStatsScheduler.manualTeamCompetitionStatsParsing(executionType);
        return ok();
    }

    @GET
    @Path("/reset/")
    public Response resetStats() {
        LOGGER.info("GET request received to manually reset TC stats");
        teamCompetitionResetScheduler.manualTeamCompetitionStatsReset();
        return ok();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamCompetitionStats() {
        LOGGER.debug("GET request received to show TC stats");

        try {
            final List<TeamResult> teamResults = getStatsForTeams();
            LOGGER.debug("Found {} TC teams", teamResults.size());

            if (teamResults.isEmpty()) {
                LOGGER.warn("No TC teams to show");
            }

            // TODO: [zodac] Cache this CompetitionResult, and invalidate cache on scheduled/manual update, scheduled/manual reset, user create/update, team create/update
            //   Can't simply invalidate on stats update, because what if a hardware display is changed? Should invalidate on ALL changes
            return ok(CompetitionResult.create(teamResults));
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
            return UserResult.createWithNoRank(user.getDisplayName(), hardware.getDisplayName(), category.displayName(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits(), user.getLiveStatsLink(), user.isRetired());
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
