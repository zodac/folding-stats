package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.HardwareNotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.cache.StatsCache;
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
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.rest.response.Responses.noContent;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

@Path("/tc_stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsEndpoint.class);

    private final StatsCache statsCache = StatsCache.get();

    @EJB
    private StorageFacade storageFacade;

    @Context
    private UriInfo uriContext;

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
                .filter(Optional::isPresent)
                .map(Optional::get)
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

    private Optional<UserResult> getTcUser(final int userId) {
        if (userId == User.EMPTY_USER_ID) {
            LOGGER.warn("User had invalid ID");
            return Optional.empty();
        }

        try {
            final User user = storageFacade.getUser(userId);
            return convertFoldingUserToTcUser(user);
        } catch (final UserNotFoundException e) {
            LOGGER.warn("Unable to find user ID: {}", userId, e);
            return Optional.empty();
        } catch (final FoldingException e) {
            LOGGER.warn("Error finding user ID: {}", userId, e.getCause());
            return Optional.empty();
        }
    }

    private Optional<UserResult> convertFoldingUserToTcUser(final User user) {
        try {
            final Hardware hardware = storageFacade.getHardware(user.getHardwareId());

            final Optional<Stats> initialStats = statsCache.getInitialStatsForUser(user.getId());
            if (initialStats.isEmpty()) {
                LOGGER.warn("Could not find initial stats for user: {}", user);
                return Optional.empty();
            }

            final Optional<Stats> currentStats = statsCache.getCurrentStatsForUser(user.getId());
            if (currentStats.isEmpty()) {
                LOGGER.warn("Could not find current stats for user: {}", user);
                return Optional.empty();
            }

            LOGGER.trace("Found initial stats {} and current stats {} for {}", initialStats.get(), currentStats.get(), user);
            final long tcUnits = currentStats.get().getUnits() - initialStats.get().getUnits();
            final long tcPoints = currentStats.get().getPoints() - initialStats.get().getPoints();
            final long tcUnmultipliedPoints = currentStats.get().getUnmultipliedPoints() - initialStats.get().getUnmultipliedPoints();

            final Category category = Category.get(user.getCategory());
            if (category == Category.INVALID) {
                LOGGER.warn("Unexpectedly got an {} category for Folding user {}", Category.INVALID.getDisplayName(), user);
                return Optional.empty();
            }

            LOGGER.debug("Results for {}: {} points | {} unmultiplied points | {} units", user.getDisplayName(), tcPoints, tcUnmultipliedPoints, tcUnits);
            return Optional.of(UserResult.create(user.getDisplayName(), hardware.getDisplayName(), category.getDisplayName(), tcPoints, tcUnmultipliedPoints, tcUnits, user.getLiveStatsLink()));
        } catch (final HardwareNotFoundException e) {
            LOGGER.warn("No hardware found for ID: {}", user.getHardwareId(), e);
            return Optional.empty();
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting hardware for user: {}", user, e.getCause());
            return Optional.empty();
        }
    }
}
