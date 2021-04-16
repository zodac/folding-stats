package me.zodac.folding.rest;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * REST endpoints for users for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a PUT endpoint
//   Also add a GET endpoint with query, so we can see all instances of a user
@Path("/users/")
@RequestScoped
public class UserEndpoint extends AbstractIdentifiableCrudEndpoint<FoldingUser> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HardwareEndpoint.class);

    @EJB
    private StorageFacade storageFacade;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFoldingUser(final FoldingUser foldingUser) {
        return super.create(foldingUser);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFoldingUsers() {
        return super.getAll();
    }

    @GET
    @Path("/{foldingUserId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFoldingUserById(@PathParam("foldingUserId") final String foldingUserId) {
        return super.getById(foldingUserId);
    }

    @DELETE
    @Path("/{foldingUserId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFoldingUserById(@PathParam("foldingUserId") final String foldingUserId) {
        return super.deleteById(foldingUserId);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected String elementType() {
        return "Folding user";
    }

    @Override
    protected ValidationResponse validate(final FoldingUser element) {
        return FoldingUserValidator.isValid(element);
    }

    @Override
    protected FoldingUser createElement(final FoldingUser element) throws FoldingException {
        return storageFacade.createFoldingUser(element);
    }

    @Override
    protected List<FoldingUser> getAllElements() throws FoldingException {
        return storageFacade.getAllFoldingUsers();
    }

    @Override
    protected FoldingUser getElementById(final int elementId) throws FoldingException, NotFoundException {
        return storageFacade.getFoldingUser(elementId);
    }

    @Override
    protected void deleteElementById(final int elementId) throws FoldingConflictException, FoldingException {
        storageFacade.deleteFoldingUser(elementId);
    }
}
