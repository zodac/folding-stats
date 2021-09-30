package me.zodac.folding.rest.endpoint;

import static me.zodac.folding.rest.response.Responses.ok;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import me.zodac.folding.cache.CompetitionSummaryCache;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.InitialStatsCache;
import me.zodac.folding.cache.OffsetStatsCache;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TeamCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.cache.UserCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST endpoints for debugging.
 */
@Path("/debug/")
@RequestScoped
public class DebugEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @GET
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
        LOGGER.info("OffsetStatsCache: {}", OffsetStatsCache.getInstance().getCacheContents());
        LOGGER.info("RetiredTcStatsCache: {}", RetiredTcStatsCache.getInstance().getCacheContents());
        LOGGER.info("TcStatsCache: {}", TcStatsCache.getInstance().getCacheContents());
        LOGGER.info("TotalStatsCache: {}", TotalStatsCache.getInstance().getCacheContents());

        // TC overall
        LOGGER.info("CompetitionSummaryCache: {}", CompetitionSummaryCache.getInstance().get());

        return ok();
    }
}
