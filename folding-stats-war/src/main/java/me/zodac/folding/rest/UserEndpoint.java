package me.zodac.folding.rest;

import com.google.gson.Gson;
import me.zodac.folding.api.FoldingUser;
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
 * REST endpoints for users for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a DELETE and PUT endpoint
//   Also add a GET endpoint with query, so we can see all instances of a user
//   Update caches on user change
@Path("/user/")
@RequestScoped
public class UserEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpoint.class);
    private static final Gson GSON = new Gson();

    @Context
    private UriInfo uriContext;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFoldingUser(final FoldingUser foldingUser) {
        LOGGER.info("POST request received to create Folding user at '{}' with request: {}", this.uriContext.getAbsolutePath(), foldingUser);

        // TODO: [zodac] Check that hardware category ID is valid, else will fail at persist and return a 500. Should clean and return a 400 instead.
        if (!foldingUser.isValid()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(foldingUser))
                    .build();
        }

        try {
            final FoldingUser foldingUserWithId = PostgresDbManager.createFoldingUser(foldingUser);

            final UriBuilder builder = uriContext.getBaseUriBuilder()
                    .path(String.valueOf(foldingUserWithId.getId()));
            return Response.created(builder.build()).build();
        } catch (final FoldingException e) {
            LOGGER.error("Error creating Folding user: {}", foldingUser, e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating Folding user: {}", foldingUser, e);
            return Response.serverError().build();
        }
    }

    // TODO: [zodac] Add queryParam to show/hide passkeys in public API
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFoldingUsers() {
        LOGGER.info("GET request received for all Folding users at '{}'", this.uriContext.getAbsolutePath());

        try {
            final List<FoldingUser> foldingUsers = PostgresDbManager.getAllFoldingUsers();
            LOGGER.info("Found {} hardware categories", foldingUsers.size());
            return Response
                    .ok()
                    .entity(GSON.toJson(foldingUsers))
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting all Folding users", e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all Folding users", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{foldingUserId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFoldingUserById(@PathParam("foldingUserId") final String foldingUserId) {
        LOGGER.info("GET request for Folding user received at '{}'", this.uriContext.getAbsolutePath());

        try {
            final FoldingUser foldingUser = PostgresDbManager.getFoldingUser(foldingUserId);
            return Response
                    .ok()
                    .entity(foldingUser)
                    .build();
        } catch (final NoSuchElementException e) {
            LOGGER.debug("No Folding user found with ID: {}", foldingUserId, e);
            LOGGER.error("No Folding user found with ID: {}", foldingUserId);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting Folding user with ID: {}", foldingUserId, e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting Folding user with ID: {}", foldingUserId, e);
            return Response.serverError().build();
        }
    }
}
