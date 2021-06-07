package me.zodac.folding.rest.endpoint;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.rest.util.validator.UserValidator;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * REST endpoints for users for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a GET endpoint with query, so we can see all instances of a user
@Path("/users/")
@RequestScoped
public class UserEndpoint extends AbstractCrudEndpoint<UserRequest, User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpoint.class);

    @POST
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(final UserRequest userRequest) {
        return super.create(userRequest);
    }

    @POST
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOfUsers(final Collection<UserRequest> userRequests) {
        return super.createBatchOf(userRequests);
    }

    @GET
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers(@Context final Request request) {
        return super.getAll(request);
    }

    @GET
    @PermitAll
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("userId") final String userId, @Context final Request request) {
        return super.getById(userId, request);
    }

    @PUT
    @RolesAllowed("admin")
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserById(@PathParam("userId") final String userId, final UserRequest userRequest) {
        return super.updateById(userId, userRequest);
    }

    @DELETE
    @RolesAllowed("admin")
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUserById(@PathParam("userId") final String userId) {
        return super.deleteById(userId);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected String elementType() {
        return "user";
    }

    @Override
    protected User createElement(final User user) throws FoldingException, FoldingExternalServiceException {
        return oldFacade.createUser(user);
    }

    @Override
    protected Collection<User> getAllElements() throws FoldingException {
        return oldFacade.getAllUsersWithPasskeys(false);
    }

    @Override
    protected ValidationResponse<User> validateCreateAndConvert(final UserRequest userRequest) {
        final UserValidator userValidator = UserValidator.create(oldFacade, HttpFoldingStatsRetriever.create());
        return userValidator.validateCreate(userRequest);
    }

    @Override
    protected ValidationResponse<User> validateUpdateAndConvert(final UserRequest userRequest) {
        final UserValidator userValidator = UserValidator.create(oldFacade, HttpFoldingStatsRetriever.create());
        return userValidator.validateUpdate(userRequest);
    }

    @Override
    protected ValidationResponse<User> validateDeleteAndConvert(final User user) {
        return ValidationResponse.success(user);
    }

    @Override
    protected User getElementById(final int userId) throws FoldingException, NotFoundException {
        return oldFacade.getUserWithPasskey(userId, false);
    }

    @Override
    protected User updateElementById(final int userId, final User user) throws FoldingException, NotFoundException, FoldingExternalServiceException {
        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final User userWithId = User.updateWithId(userId, user);
        oldFacade.updateUser(userWithId);
        return userWithId;
    }

    @Override
    protected void deleteElementById(final int userId) throws FoldingException {
        oldFacade.deleteUser(userId);
    }
}
