package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.validator.UserValidator;
import me.zodac.folding.validator.ValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(final User user) {
        return super.create(user);
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
    protected User createElement(final User user) throws FoldingException, NotFoundException {
        return storageFacade.createUser(user);
    }

    @Override
    protected List<User> getAllElements() throws FoldingException {
        return storageFacade.getAllUsers();
    }

    @Override
    protected User getElementById(final int userId) throws FoldingException, NotFoundException {
        return storageFacade.getUser(userId);
    }

    @Override
    protected void updateElementById(final int userId, final User user) throws FoldingException, NotFoundException {
        if (user.getId() == 0) {
            // The payload 'should' have the ID, but it's not necessary if the correct URL is used
            final User userWithId = User.updateWithId(userId, user);
            storageFacade.updateUser(userWithId);
        } else {
            storageFacade.updateUser(user);
        }
    }

    @Override
    protected void deleteElementById(final int user) throws FoldingConflictException, FoldingException {
        storageFacade.deleteUser(user);
    }
}
