package me.zodac.folding.rest.endpoint;

import java.util.Collection;
import java.util.Optional;
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
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.rest.validator.UserValidator;
import me.zodac.folding.rest.validator.ValidationResult;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST endpoints for <code>Team Competition</code> {@link User}s.
 *
 * @see me.zodac.folding.client.java.request.UserRequestSender
 * @see me.zodac.folding.client.java.response.UserResponseParser
 */
@Path("/users/")
@RequestScoped
public class UserEndpoint extends AbstractCrudEndpoint<UserRequest, User> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * {@link POST} request to create a {@link User} based on the input request.
     *
     * @param userRequest the {@link UserRequest} to create a {@link User}
     * @return {@link Response.Status#CREATED} containing the created {@link User}
     */
    @Override
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final UserRequest userRequest) {
        return super.create(userRequest);
    }

    /**
     * {@link POST} request to create a {@link Collection} of {@link User}s based on the input requests.
     *
     * <p>
     * Will perform a best-effort attempt to create all {@link User}s and will return a response with successful and unsuccessful results.
     *
     * @param userRequests the {@link UserRequest}s to create {@link User}s
     * @return {@link Response.Status#OK} containing the created/failed {@link User}s
     */
    @Override
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOf(final Collection<UserRequest> userRequests) {
        return super.createBatchOf(userRequests);
    }

    /**
     * {@link GET} request to retrieve all {@link User}s.
     *
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link User}s
     */
    @Override
    @GET
    @ReadRequired
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@Context final Request request) {
        return super.getAll(request);
    }

    /**
     * {@link GET} request to retrieve a {@link User}.
     *
     * @param userId  the ID of the {@link User} to retrieve
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link User}
     */
    @Override
    @GET
    @ReadRequired
    @PermitAll
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("userId") final String userId, @Context final Request request) {
        return super.getById(userId, request);
    }

    /**
     * {@link PUT} request to update an existing {@link User} based on the input request.
     *
     * @param userId      the ID of the {@link User} to be updated
     * @param userRequest the {@link UserRequest} to update a {@link User}
     * @return {@link Response.Status#OK} containing the updated {@link User}
     */
    @Override
    @PUT
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateById(@PathParam("userId") final String userId, final UserRequest userRequest) {
        return super.updateById(userId, userRequest);
    }

    /**
     * {@link DELETE} request to delete an existing {@link User}.
     *
     * @param userId the ID of the {@link User} to be deleted
     * @return {@link Response.Status#OK}
     */
    @Override
    @DELETE
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("userId") final String userId) {
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
    protected User createElement(final User user) {
        return businessLogic.createUser(user);
    }

    @Override
    protected Collection<User> getAllElements() {
        return businessLogic.getAllUsersWithoutPasskeys();
    }

    @Override
    protected ValidationResult<User> validateCreateAndConvert(final UserRequest userRequest) {
        final UserValidator userValidator = UserValidator.createValidator(businessLogic, HttpFoldingStatsRetriever.create());
        return userValidator.validateCreate(userRequest);
    }

    @Override
    protected ValidationResult<User> validateUpdateAndConvert(final UserRequest userRequest, final User existingUser) {
        final UserValidator userValidator = UserValidator.createValidator(businessLogic, HttpFoldingStatsRetriever.create());
        return userValidator.validateUpdate(userRequest, existingUser);
    }

    @Override
    protected ValidationResult<User> validateDeleteAndConvert(final User user) {
        return ValidationResult.success(user);
    }

    @Override
    protected Optional<User> getElementById(final int userId) {
        return businessLogic.getUserWithoutPasskey(userId);
    }

    @Override
    protected User updateElementById(final User user, final User existingUser) {
        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final User userWithId = User.updateWithId(existingUser.getId(), user);
        return businessLogic.updateUser(userWithId, existingUser);
    }

    @Override
    protected void deleteElement(final User user) {
        businessLogic.deleteUser(user);
    }
}
