package me.zodac.folding.rest;

import com.google.gson.Gson;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.rest.HardwareCategory;
import me.zodac.folding.api.service.HardwareCategoryWithId;
import me.zodac.folding.db.postgres.PostgresDbManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST endpoints for hardware categories for <code>folding-stats</code>.
 */
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

        // TODO: [zodac] Assuming multiplier cannot be less than 0, assuming we might want a 0.1/0.5 at some point with future hardware?
        if (StringUtils.isBlank(hardwareCategory.getCategoryName()) || hardwareCategory.getMultiplier() <= 0.0D){
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(hardwareCategory))
                    .build();
        }

        try {
            final HardwareCategoryWithId hardwareCategoryWithId = PostgresDbManager.createHardwareCategory(hardwareCategory);

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
            final List<HardwareCategoryWithId> hardwareCategoryWithIds = PostgresDbManager.getAllHardwareCategories();
			LOGGER.info("Found {} hardware categories", hardwareCategoryWithIds.size());
            return Response
                    .ok()
                    .entity(GSON.toJson(hardwareCategoryWithIds))
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
            final HardwareCategoryWithId hardwareCategoryWithId = PostgresDbManager.getHardwareCategory(hardwareCategoryId);
            return Response
                    .ok()
                    .entity(hardwareCategoryWithId)
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
