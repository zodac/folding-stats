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
import me.zodac.folding.ejb.api.BusinessLogic;
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
    private BusinessLogic businessLogic;

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
     * @see BusinessLogic#printCacheContents()
     */
    @POST
    @RolesAllowed("admin")
    @Path("/caches")
    @Produces(MediaType.APPLICATION_JSON)
    public Response printCaches() {
        LOGGER.info("Printing cache contents");
        businessLogic.printCacheContents();
        return ok();
    }
}
