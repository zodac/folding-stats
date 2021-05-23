package me.zodac.folding.rest;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.db.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.FoldingIdInvalidException;
import me.zodac.folding.api.tc.exception.FoldingIdOutOfRangeException;
import me.zodac.folding.api.tc.exception.HardwareNotFoundException;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.BusinessLogic;
import me.zodac.folding.ejb.UserTeamCompetitionStatsParser;
import me.zodac.folding.rest.validator.UserValidator;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
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

import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

/**
 * REST endpoints for users for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a GET endpoint with query, so we can see all instances of a user
@Path("/users/")
@RequestScoped
public class UserEndpoint extends AbstractIdentifiableCrudEndpoint<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpoint.class);

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private UserTeamCompetitionStatsParser userTeamCompetitionStatsParser;

    @POST
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(final User user) {
        return super.create(user);
    }

    @POST
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOfUsers(final Collection<User> users) {
        return super.createBatchOf(users);
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
    public Response updateUserById(@PathParam("userId") final String userId, final User user) {
        return super.updateById(userId, user);
    }

    @DELETE
    @RolesAllowed("admin")
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUserById(@PathParam("userId") final String userId) {
        return super.deleteById(userId);
    }

    @PATCH
    @RolesAllowed("admin")
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserWithOffset(@PathParam("userId") final String userId, final OffsetStats offsetStats) {
        getLogger().debug("PATCH request to update offset for user received at '{}': {}", uriContext.getAbsolutePath(), offsetStats);

        if (SystemStateManager.current().isWriteBlocked()) {
            getLogger().warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final int parsedId = super.parseId(userId);
            getElementById(parsedId); // We call this so if the value does not exist, we can fail with a NOT_FOUND response

            final OffsetStats offsetStatsToUse = getValidUserStatsOffset(offsetStats, parsedId);
            businessLogic.addOrUpdateOffsetStats(parsedId, offsetStatsToUse);
            SystemStateManager.next(SystemState.UPDATING_STATS);
            userTeamCompetitionStatsParser.parseTcStatsForUserAndWait(businessLogic.getUser(parsedId));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok();
        } catch (final FoldingIdInvalidException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingIdOutOfRangeException e) {
            final String errorMessage = String.format("The %s ID '%s' is out of range", elementType(), e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final UserNotFoundException e) {
            getLogger().error("Error finding user with ID: {}", userId, e.getCause());
            return notFound();
        } catch (final FoldingException e) {
            getLogger().error("Error updating user with ID: {}", userId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error updating user with ID: {}", userId, e);
            return serverError();
        }
    }

    private OffsetStats getValidUserStatsOffset(final OffsetStats offsetStats, final int parsedId) throws FoldingException, UserNotFoundException, HardwareNotFoundException {
        if (!offsetStats.isMissingPointsOrMultipliedPoints()) {
            return offsetStats;
        }

        final User user = businessLogic.getUser(parsedId);
        final Hardware hardware = businessLogic.getHardware(user.getHardwareId());
        return OffsetStats.updateWithHardwareMultiplier(offsetStats, hardware.getMultiplier());
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
    protected ValidationResponse validate(final User element) {
        final UserValidator userValidator = UserValidator.create(businessLogic, HttpFoldingStatsRetriever.create());
        return userValidator.isValid(element);
    }

    @Override
    protected User createElement(final User user) throws FoldingException, FoldingConflictException, FoldingExternalServiceException {
        return businessLogic.createUser(user);
    }

    @Override
    protected Collection<User> getAllElements() throws FoldingException {
        return businessLogic.getAllUsersWithPasskeys(false);
    }

    @Override
    protected User getElementById(final int userId) throws FoldingException, NotFoundException {
        return businessLogic.getUserWithPasskey(userId, false);
    }

    @Override
    protected User updateElementById(final int userId, final User user) throws FoldingException, NotFoundException, FoldingConflictException, FoldingExternalServiceException {
        if (user.getId() == 0) {
            // The payload 'should' have the ID, but it's not necessary if the correct URL is used
            final User userWithId = User.updateWithId(userId, user);
            businessLogic.updateUser(userWithId);
            return userWithId;
        }

        businessLogic.updateUser(user);
        return user;
    }

    @Override
    protected void deleteElementById(final int user) throws FoldingConflictException, FoldingException {
        businessLogic.deleteUser(user);
    }
}
