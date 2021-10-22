package me.zodac.folding.rest.endpoint;

import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import me.zodac.folding.cache.CompetitionSummaryCache;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.InitialStatsCache;
import me.zodac.folding.cache.OffsetTcStatsCache;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TeamCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.ejb.tc.lars.LarsHardwareUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST endpoints for debugging.
 *
 * <p>
 * <b>NOTE:</b> There are no client-libraries for these endpoints.
 */
@Path("/debug/")
@RequestScoped
public class DebugEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private LarsHardwareUpdater larsHardwareUpdater;

    /**
     * {@link POST} request to print the contents of all caches to the system log.
     *
     * @return {@link Response.Status#OK}
     * @see LarsHardwareUpdater
     */
    @POST
    @RolesAllowed("admin")
    @Path("/lars")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startLarsUpdate() {
        LOGGER.info("GET request received to manually update hardware from LARS DB");
        try {
            larsHardwareUpdater.retrieveHardwareAndPersist();
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating hardware from LARS", e);
            return serverError();
        }
    }

    /**
     * {@link POST} request to print the contents of all caches to the system log.
     *
     * @return {@link Response.Status#OK}
     */
    @POST
    @RolesAllowed("admin")
    @Path("/caches")
    @Produces(MediaType.APPLICATION_JSON)
    public Response printCaches() {
        LOGGER.info("Printing cache contents");

        // POJOs
        LOGGER.info("HardwareCache: {}", HardwareCache.getInstance().getCacheContents());
        LOGGER.info("TeamCache: {}", TeamCache.getInstance().getCacheContents());
        LOGGER.info("UserCache: {}", UserCache.getInstance().getCacheContents());

        // Stats
        LOGGER.info("InitialStatsCache: {}", InitialStatsCache.getInstance().getCacheContents());
        LOGGER.info("OffsetStatsCache: {}", OffsetTcStatsCache.getInstance().getCacheContents());
        LOGGER.info("RetiredTcStatsCache: {}", RetiredTcStatsCache.getInstance().getCacheContents());
        LOGGER.info("TcStatsCache: {}", TcStatsCache.getInstance().getCacheContents());
        LOGGER.info("TotalStatsCache: {}", TotalStatsCache.getInstance().getCacheContents());

        // TC overall
        LOGGER.info("CompetitionSummaryCache: {}", CompetitionSummaryCache.getInstance().get());

        return ok();
    }
}
