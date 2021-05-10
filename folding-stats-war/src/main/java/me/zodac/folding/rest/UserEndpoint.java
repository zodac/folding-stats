package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.exception.FoldingIdInvalidException;
import me.zodac.folding.api.exception.FoldingIdOutOfRangeException;
import me.zodac.folding.api.exception.HardwareNotFoundException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.UserStatsOffset;
import me.zodac.folding.bean.UserTeamCompetitionStatsParser;
import me.zodac.folding.validator.UserValidator;
import me.zodac.folding.validator.ValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

/**
 * REST endpoints for users for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a GET endpoint with query, so we can see all instances of a user
@Path("/users/")
@RequestScoped
public class UserEndpoint extends AbstractIdentifiableCrudEndpoint<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEndpoint.class);

    @EJB
    private StorageFacade storageFacade;

    @EJB
    private UserTeamCompetitionStatsParser userTeamCompetitionStatsParser;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(final User user) {
        return super.create(user);
    }

    @POST
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOfUsers(final List<User> users) {
        return super.createBatchOf(users);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        return super.getAll();
    }

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(@PathParam("userId") final String userId) {
        return super.getById(userId);
    }

    @PUT
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserById(@PathParam("userId") final String userId, final User user) {
        return super.updateById(userId, user);
    }

    @DELETE
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUserById(@PathParam("userId") final String userId) {
        return super.deleteById(userId);
    }

    @PATCH
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserWithOffset(@PathParam("userId") final String userId, final UserStatsOffset userStatsOffset) {
        getLogger().debug("PATCH request to update offset for user received at '{}': {}", uriContext.getAbsolutePath(), userStatsOffset);

        try {
            final int parsedId = super.parseId(userId);
            final UserStatsOffset statsOffsetToUse = getValidUserStatsOffset(userStatsOffset, parsedId);
            storageFacade.addOrUpdateOffsetStats(parsedId, statsOffsetToUse);
            userTeamCompetitionStatsParser.parseTcStatsForUserAndWait(storageFacade.getUser(parsedId));
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

    private UserStatsOffset getValidUserStatsOffset(final UserStatsOffset userStatsOffset, final int parsedId) throws FoldingException, UserNotFoundException, HardwareNotFoundException {
        if (!userStatsOffset.isMissingPointsOrMultipliedPoints()) {
            return userStatsOffset;
        }

        final User user = storageFacade.getUser(parsedId);
        final Hardware hardware = storageFacade.getHardware(user.getHardwareId());
        return UserStatsOffset.updateWithHardwareMultiplier(userStatsOffset, hardware.getMultiplier());
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
        return UserValidator.isValid(element);
    }

    @Override
    protected User createElement(final User user) throws FoldingException, FoldingConflictException, FoldingExternalServiceException {
        return storageFacade.createUser(user);
    }

    @Override
    protected List<User> getAllElements() throws FoldingException {
        return storageFacade.getAllUsersWithPasskeys(false);
    }

    @Override
    protected User getElementById(final int userId) throws FoldingException, NotFoundException {
        return storageFacade.getUserWithPasskey(userId, false);
    }

    @Override
    protected User updateElementById(final int userId, final User user) throws FoldingException, NotFoundException, FoldingConflictException, FoldingExternalServiceException {
        if (user.getId() == 0) {
            // The payload 'should' have the ID, but it's not necessary if the correct URL is used
            final User userWithId = User.updateWithId(userId, user);
            storageFacade.updateUser(userWithId);
            return userWithId;
        }

        storageFacade.updateUser(user);
        return user;
    }

    @Override
    protected void deleteElementById(final int user) throws FoldingConflictException, FoldingException {
        storageFacade.deleteUser(user);
    }
}
