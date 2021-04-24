package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.HardwareNotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.bean.TcCacheResetScheduler;
import me.zodac.folding.bean.TeamCompetitionStatsParser;
import me.zodac.folding.rest.tc.CompetitionResult;
import me.zodac.folding.rest.tc.TeamResult;
import me.zodac.folding.rest.tc.UserResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.rest.response.Responses.noContent;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

@Path("/tc_stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsEndpoint.class);

    @EJB
    private StorageFacade storageFacade;

    @EJB
    private TcCacheResetScheduler tcCacheResetScheduler;

    @EJB
    private TeamCompetitionStatsParser teamCompetitionStatsParser;

    @Context
    private UriInfo uriContext;

    @GET
    @Path("/manual/")
    public Response manualStats() {
        LOGGER.info("GET request received to manually parse TC stats at '{}'", uriContext.getAbsolutePath());
        teamCompetitionStatsParser.manualTcStatsParsing();
        return ok();
    }

    @GET
    @Path("/reset/")
    public Response resetStats() {
        LOGGER.info("GET request received to manually reset TC stats at '{}'", uriContext.getAbsolutePath());
        tcCacheResetScheduler.manualTcStatsReset();
        return ok();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamCompetitionStats() {
        LOGGER.info("GET request received to show TC stats at '{}'", uriContext.getAbsolutePath());

        try {
            final List<TeamResult> teamResults = getTeams();
            LOGGER.info("Found {} TC teams", teamResults.size());

            if (teamResults.isEmpty()) {
                return noContent();
            }

            return ok(CompetitionResult.create(teamResults));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC stats", e);
            return serverError();
        }
    }

    private List<TeamResult> getTeams() {
        try {
            final List<Team> teams = storageFacade.getAllTeams();
            final List<TeamResult> teamResults = new ArrayList<>(teams.size());

            for (final Team team : teams) {
                teamResults.add(convertFoldingTeamToTcTeam(team));
            }

            return teamResults;
        } catch (final FoldingException e) {
            LOGGER.warn("Error retrieving TC team stats", e.getCause());
            return Collections.emptyList();
        }
    }

    private TeamResult convertFoldingTeamToTcTeam(final Team team) throws FoldingException {
        LOGGER.info("Converting team '{}' for TC stats", team.getTeamName());

        final List<UserResult> userResults = team.getUserIds()
                .stream()
                .map(this::getTcUser)
                .filter(Objects::nonNull)
                .collect(toList());

        try {
            final User captain = storageFacade.getUser(team.getCaptainUserId());
            return TeamResult.create(team.getTeamName(), captain.getDisplayName(), userResults);
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to get details for team captain: {}", team, e);
            throw e;
        } catch (final UserNotFoundException e) {
            LOGGER.warn("Captain user ID not found, unexpected error: {}", team, e);
            throw new FoldingException(String.format("Captain user ID not found: %s", team), e);
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
            return UserResult.create(user.getDisplayName(), hardware.getDisplayName(), category.getDisplayName(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits(), user.getLiveStatsLink());
        } catch (final UserNotFoundException e) {
            LOGGER.debug("No stats found for user ID: {}", user.getId(), e);
            LOGGER.warn("No stats found for user ID: {}", user.getId());
            return UserResult.empty(user.getDisplayName(), hardware.getDisplayName(), category.getDisplayName(), user.getLiveStatsLink());
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting TC stats for user: {}", user, e.getCause());
            return null;
        }
    }
}
