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
import me.zodac.folding.ejb.api.FoldingStatsCore;
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
    private FoldingStatsCore foldingStatsCore;

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
     * @see FoldingStatsCore#printCacheContents()
     */
    @POST
    @RolesAllowed("admin")
    @Path("/caches")
    @Produces(MediaType.APPLICATION_JSON)
    public Response printCaches() {
        LOGGER.info("Printing cache contents");
        foldingStatsCore.printCacheContents();
        return ok();
    }
}
