package me.zodac.folding.rest;

import com.google.gson.Gson;
import me.zodac.folding.StorageFacade;
import me.zodac.folding.TcTeam;
import me.zodac.folding.TcUser;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
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

@Path("/tc_stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsEndpoint.class);
    private static final Gson GSON = new Gson();

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

            return Response
                    .ok()
                    .entity(GSON.toJson(tcTeams))
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

    private FoldingUser getUserOrNull(final int userId) {
        if (userId == FoldingUser.EMPTY_USER.getId()) {
            return null;
        }

        try {
            return storageFacade.getFoldingUser(userId);
        } catch (final NotFoundException e) {
            LOGGER.warn("Unable to find user ID: {}", userId, e);
            return null;
        } catch (final FoldingException e) {
            LOGGER.warn("Error finding user ID: {}", userId, e.getCause());
            return null;
        }
    }

    private TcTeam convertFoldingTeamToTcTeam(final FoldingTeam foldingTeam) throws FoldingException {
        LOGGER.info("Converting team for TC stats: {}", foldingTeam);

        final FoldingUser nvidiaGpuFoldingUser = getUserOrNull(foldingTeam.getNvidiaGpuUserId());
        final FoldingUser amdGpuFoldingUser = getUserOrNull(foldingTeam.getAmdGpuUserId());
        final FoldingUser wildcardFoldingUser = getUserOrNull(foldingTeam.getWildcardUserId());

        final TcUser nvidiaGpuTcUser = nvidiaGpuFoldingUser == null ? null : convertFoldingUserToTcUser(nvidiaGpuFoldingUser);
        final TcUser amdGpuTcUser = amdGpuFoldingUser == null ? null : convertFoldingUserToTcUser(amdGpuFoldingUser);
        final TcUser wildcardTcUser = wildcardFoldingUser == null ? null : convertFoldingUserToTcUser(wildcardFoldingUser);

        long teamPoints = 0L;
        long teamPointsWithoutMultiplier = 0L;


        if (nvidiaGpuTcUser != null) {
            teamPoints += nvidiaGpuTcUser.getPoints();
            teamPointsWithoutMultiplier += nvidiaGpuTcUser.getPointsWithoutMultiplier();
        }

        if (amdGpuTcUser != null) {
            teamPoints += amdGpuTcUser.getPoints();
            teamPointsWithoutMultiplier += amdGpuTcUser.getPointsWithoutMultiplier();
        }

        if (wildcardTcUser != null) {
            teamPoints += wildcardTcUser.getPoints();
            teamPointsWithoutMultiplier += wildcardTcUser.getPointsWithoutMultiplier();
        }

        try {
            final FoldingUser captain = storageFacade.getFoldingUser(foldingTeam.getCaptainUserId());
            return new TcTeam(foldingTeam.getTeamName(), captain.getDisplayName(), teamPoints, teamPointsWithoutMultiplier, nvidiaGpuTcUser, amdGpuTcUser, wildcardTcUser);
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

            // TODO: [zodac] Get actual points! :)statsP
            return new TcUser(foldingUser.getDisplayName(), hardware.getDisplayName(), 0L, 0L);
        } catch (final NotFoundException e) {
            LOGGER.warn("No hardware found for ID: {}", foldingUser.getHardwareId(), e);
            return null;
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting hardware for user: {}", foldingUser, e.getCause());
            return null;
        }
    }
}
