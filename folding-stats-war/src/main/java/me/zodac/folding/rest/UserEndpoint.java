package me.zodac.folding.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.validator.FoldingUserValidator;
import me.zodac.folding.validator.ValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

/**
 * REST endpoints for users for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a PUT endpoint
//   Also add a GET endpoint with query, so we can see all instances of a user
@Path("/users/")
@RequestScoped
public class UserEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpoint.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @EJB
    private StorageFacade storageFacade;

    @Context
    private UriInfo uriContext;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFoldingUser(final FoldingUser foldingUser) {
        LOGGER.info("POST request received to create Folding user at '{}' with request: {}", uriContext.getAbsolutePath(), foldingUser);

        final ValidationResponse validationResponse = FoldingUserValidator.isValid(foldingUser);
        if (!validationResponse.isValid()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(validationResponse))
                    .build();
        }

        try {
            final FoldingUser foldingUserWithId = storageFacade.createFoldingUser(foldingUser);

            final UriBuilder builder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(foldingUserWithId.getId()));
            return Response
                    .created(builder.build())
                    .entity(GSON.toJson(foldingUserWithId))
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error creating Folding user: {}", foldingUser, e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating Folding user: {}", foldingUser, e);
            return Response
                    .serverError()
                    .build();
        }
    }

    // TODO: [zodac] Add queryParam to show/hide passkeys in public API?
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFoldingUsers() {
        LOGGER.info("GET request received for all Folding users at '{}'", uriContext.getAbsolutePath());

        try {
            final List<FoldingUser> foldingUsers = storageFacade.getAllFoldingUsers();
            LOGGER.info("Found {} Folding users", foldingUsers.size());
            return Response
                    .ok()
                    .entity(GSON.toJson(foldingUsers))
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting all Folding users", e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all Folding users", e);
            return Response
                    .serverError()
                    .build();
        }
    }

    @GET
    @Path("/{foldingUserId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFoldingUserById(@PathParam("foldingUserId") final String foldingUserId) {
        LOGGER.info("GET request for Folding user received at '{}'", uriContext.getAbsolutePath());

        try {
            final FoldingUser foldingUser = storageFacade.getFoldingUser(Integer.parseInt(foldingUserId));
            return Response
                    .ok()
                    .entity(foldingUser)
                    .build();
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("Folding user ID '%s' is invalid format", foldingUserId);
            final ErrorObject errorObject = new ErrorObject(errorMessage);

            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(errorObject, ErrorObject.class))
                    .build();
        } catch (final NotFoundException e) {
            LOGGER.debug("No Folding user found with ID: {}", foldingUserId, e);
            LOGGER.error("No Folding user found with ID: {}", foldingUserId);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting Folding user with ID: {}", foldingUserId, e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting Folding user with ID: {}", foldingUserId, e);
            return Response
                    .serverError()
                    .build();
        }
    }

    @DELETE
    @Path("/{foldingUserId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFoldingUserById(@PathParam("foldingUserId") final String foldingUserId) {
        LOGGER.info("DELETE request for Folding user received at '{}'", uriContext.getAbsolutePath());

        try {
            storageFacade.deleteFoldingUser(Integer.parseInt(foldingUserId));
            return Response
                    .noContent()
                    .build();
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("Folding user ID '%s' is invalid format", foldingUserId);
            final ErrorObject errorObject = new ErrorObject(errorMessage);

            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(errorObject, ErrorObject.class))
                    .build();
        } catch (final FoldingConflictException e) {
            final String errorMessage = String.format("Folding user ID '%s' is in use, remove all usages before deleting", foldingUserId);
            final ErrorObject errorObject = new ErrorObject(errorMessage);

            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(GSON.toJson(errorObject, ErrorObject.class))
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error deleting Folding user with ID: {}", foldingUserId, e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error deleting Folding user with ID: {}", foldingUserId, e);
            return Response
                    .serverError()
                    .build();
        }
    }
}
