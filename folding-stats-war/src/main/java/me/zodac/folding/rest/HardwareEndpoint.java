package me.zodac.folding.rest;

import com.google.gson.Gson;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.db.postgres.PostgresDbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * REST endpoints for hardware for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a DELETE and PUT endpoint
//   Add GET query endpoint to retrieve all hardware instances with same name
@Path("/hardware/")
@RequestScoped
public class HardwareEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(HardwareEndpoint.class);
    private static final Gson GSON = new Gson();

    @Context
    private UriInfo uriContext;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createHardware(final Hardware hardware) {
        LOGGER.info("POST request received to create hardware at '{}' with request: {}", this.uriContext.getAbsolutePath(), hardware);

        if (!hardware.isValid()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(hardware))
                    .build();
        }

        try {
            final Hardware hardwareWithId = PostgresDbManager.createHardware(hardware);

            final UriBuilder builder = uriContext.getBaseUriBuilder()
                    .path(String.valueOf(hardwareWithId.getId()));
            return Response.created(builder.build()).build();
        } catch (final FoldingException e) {
            LOGGER.error("Error creating hardware: {}", hardware, e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating hardware: {}", hardware, e);
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllHardware() {
        LOGGER.info("GET request received for all hardware at '{}'", this.uriContext.getAbsolutePath());

        try {
            final List<Hardware> hardware = PostgresDbManager.getAllHardware();
            LOGGER.info("Found {} hardware", hardware.size());
            return Response
                    .ok()
                    .entity(GSON.toJson(hardware))
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting all hardware", e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all hardware", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{hardwareId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHardwareById(@PathParam("hardwareId") final String hardwareId) {
        LOGGER.info("GET request for hardware received at '{}'", this.uriContext.getAbsolutePath());

        try {
            final Hardware hardware = PostgresDbManager.getHardware(hardwareId);
            return Response
                    .ok()
                    .entity(hardware)
                    .build();
        } catch (final NoSuchElementException e) {
            LOGGER.debug("No hardware found with ID: {}", hardwareId, e);
            LOGGER.error("No hardware found with ID: {}", hardwareId);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting hardware with ID: {}", hardwareId, e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting hardware with ID: {}", hardwareId, e);
            return Response.serverError().build();
        }
    }
}
