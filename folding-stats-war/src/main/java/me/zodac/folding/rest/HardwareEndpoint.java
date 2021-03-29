package me.zodac.folding.rest;

import com.google.gson.Gson;
import me.zodac.folding.api.HardwareCategory;
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
 * REST endpoints for hardware categories for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a DELETE and PUT endpoint
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
    public Response createHardware(final HardwareCategory hardwareCategory) {
        LOGGER.info("POST request received to create hardware category at '{}' with request: {}", this.uriContext.getAbsolutePath(), hardwareCategory);

        if (!hardwareCategory.isValid()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(hardwareCategory))
                    .build();
        }

        try {
            final HardwareCategory hardwareCategoryWithId = PostgresDbManager.createHardwareCategory(hardwareCategory);

            final UriBuilder builder = uriContext.getBaseUriBuilder()
                    .path(String.valueOf(hardwareCategoryWithId.getId()));
            return Response.created(builder.build()).build();
        } catch (final FoldingException e) {
            LOGGER.error("Error creating hardware category: {}", hardwareCategory, e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating hardware category: {}", hardwareCategory, e);
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllHardwareCategories() {
        LOGGER.info("GET request received for all hardware categories at '{}'", this.uriContext.getAbsolutePath());

        try {
            final List<HardwareCategory> hardwareCategories = PostgresDbManager.getAllHardwareCategories();
            LOGGER.info("Found {} hardware categories", hardwareCategories.size());
            return Response
                    .ok()
                    .entity(GSON.toJson(hardwareCategories))
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting all hardware categories", e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all hardware categories", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{hardwareCategoryId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHardwareCategoryById(@PathParam("hardwareCategoryId") final String hardwareCategoryId) {
        LOGGER.info("GET request for hardware category received at '{}'", this.uriContext.getAbsolutePath());

        try {
            final HardwareCategory hardwareCategory = PostgresDbManager.getHardwareCategory(hardwareCategoryId);
            return Response
                    .ok()
                    .entity(hardwareCategory)
                    .build();
        } catch (final NoSuchElementException e) {
            LOGGER.debug("No hardware category found with ID: {}", hardwareCategoryId, e);
            LOGGER.error("No hardware category found with ID: {}", hardwareCategoryId);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting hardware category with ID: {}", hardwareCategoryId, e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting hardware category with ID: {}", hardwareCategoryId, e);
            return Response.serverError().build();
        }
    }
}
