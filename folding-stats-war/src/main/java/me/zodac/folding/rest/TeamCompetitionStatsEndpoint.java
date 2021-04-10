package me.zodac.folding.rest;

import com.google.gson.Gson;
import me.zodac.folding.StorageFacade;
import me.zodac.folding.TcStats;
import me.zodac.folding.TcTeam;
import me.zodac.folding.TcUser;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.cache.tc.TcStatsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Path("/tc_stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsEndpoint.class);
    private static final Gson GSON = new Gson();

    private final TcStatsCache tcStatsCache = TcStatsCache.getInstance();

    @EJB
    private StorageFacade storageFacade;

    @Context
    private UriInfo uriContext;

    @GET
    public Response getTeamCompetitionStats() {
        LOGGER.info("GET request received to show TC stats at '{}'", this.uriContext.getAbsolutePath());

        try {
            final List<TcTeam> tcTeams = getTeams();
            LOGGER.info("Found {} TC teams", tcTeams.size());
            LOGGER.info("");

            if (tcTeams.isEmpty()) {
                return Response
                        .serverError()
                        .build();
            }

            final TcStats tcStats = new TcStats(tcTeams);

            return Response
                    .ok()
                    .entity(GSON.toJson(tcStats))
                    .build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC stats", e);
            return Response
                    .serverError()
                    .build();
        }
    }

    private List<TcTeam> getTeams() {
        try {
            final List<FoldingTeam> foldingTeams = storageFacade.getAllFoldingTeams();
            final List<TcTeam> tcTeams = new ArrayList<>(foldingTeams.size());

            for (final FoldingTeam foldingTeam : foldingTeams) {
                tcTeams.add(convertFoldingTeamToTcTeam(foldingTeam));
            }

            return tcTeams;
        } catch (final FoldingException e) {
            LOGGER.warn("Error retrieving TC team stats", e.getCause());
            return Collections.emptyList();
        }
    }

    private TcUser getTcUserOrNull(final int userId, final String userType) {
        if (userId == FoldingUser.EMPTY_USER_ID) {
            LOGGER.warn("No {} user", userType);
            return null;
        }

        try {
            final FoldingUser foldingUser = storageFacade.getFoldingUser(userId);
            return convertFoldingUserToTcUser(foldingUser);
        } catch (final NotFoundException e) {
            LOGGER.warn("Unable to find user ID: {}", userId, e);
            return null;
        } catch (final FoldingException e) {
            LOGGER.warn("Error finding user ID: {}", userId, e.getCause());
            return null;
        }
    }

    private TcTeam convertFoldingTeamToTcTeam(final FoldingTeam foldingTeam) throws FoldingException {
        LOGGER.info("Converting team {} for TC stats", foldingTeam.getTeamName());

        final TcUser nvidiaGpuTcUser = getTcUserOrNull(foldingTeam.getNvidiaGpuUserId(), "nVidia GPU");
        final TcUser amdGpuTcUser = getTcUserOrNull(foldingTeam.getAmdGpuUserId(), "AMD GPU");
        final TcUser wildcardTcUser = getTcUserOrNull(foldingTeam.getWildcardUserId(), "Wildcard");

        try {
            final FoldingUser captain = storageFacade.getFoldingUser(foldingTeam.getCaptainUserId());
            return new TcTeam(foldingTeam.getTeamName(), captain.getDisplayName(), nvidiaGpuTcUser, amdGpuTcUser, wildcardTcUser);
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to get details for team captain: {}", foldingTeam, e);
            throw e;
        } catch (final NotFoundException e) {
            LOGGER.warn("Captain user ID not found, unexpected error: {}", foldingTeam, e);
            throw new FoldingException(String.format("Captain user ID not found: %s", foldingTeam), e);
        }
    }

    private TcUser convertFoldingUserToTcUser(final FoldingUser foldingUser) {
        try {
            final Hardware hardware = storageFacade.getHardware(foldingUser.getHardwareId());

            final Optional<UserStats> initialStats = tcStatsCache.getInitialStatsForUser(foldingUser.getId());
            if (initialStats.isEmpty()) {
                LOGGER.warn("Could not find initial stats for user: {}", foldingUser);
                return new TcUser(foldingUser.getDisplayName(), hardware.getDisplayName());
            }

            final Optional<UserStats> currentStats = tcStatsCache.getCurrentStatsForUser(foldingUser.getId());
            if (currentStats.isEmpty()) {
                LOGGER.warn("Could not find current stats for user: {}", foldingUser);
                return new TcUser(foldingUser.getDisplayName(), hardware.getDisplayName());
            }

            LOGGER.debug("Found initial stats {} and current stats {} for {}", initialStats.get(), currentStats.get(), foldingUser);
            final long tcWusForUser = currentStats.get().getWus() - initialStats.get().getWus();
            final long tcPointsForUser = currentStats.get().getPoints() - initialStats.get().getPoints();
            final long tcPointsForUserMultiplier = (long) (tcPointsForUser * hardware.getMultiplier());

            return new TcUser(foldingUser.getDisplayName(), hardware.getDisplayName(), tcPointsForUserMultiplier, tcPointsForUser, tcWusForUser);
        } catch (final NotFoundException e) {
            LOGGER.warn("No hardware found for ID: {}", foldingUser.getHardwareId(), e);
            return null;
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting hardware for user: {}", foldingUser, e.getCause());
            return null;
        }
    }
}
