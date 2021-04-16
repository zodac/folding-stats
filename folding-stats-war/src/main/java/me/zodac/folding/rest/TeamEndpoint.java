package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.validator.FoldingTeamValidator;
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
 * REST endpoints for teams for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a PUT/PATCH endpoint
@Path("/teams/")
@RequestScoped
public class TeamEndpoint extends AbstractIdentifiableCrudEndpoint<FoldingTeam> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HardwareEndpoint.class);

    @EJB
    private StorageFacade storageFacade;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFoldingTeam(final FoldingTeam foldingTeam) {
        return super.create(foldingTeam);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFoldingTeams() {
        return super.getAll();
    }

    @GET
    @Path("/{foldingTeamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFoldingTeamById(@PathParam("foldingTeamId") final String foldingTeamId) {
        return super.getById(foldingTeamId);
    }

    @DELETE
    @Path("/{foldingTeamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFoldingTeamById(@PathParam("foldingTeamId") final String foldingTeamId) {
        return super.deleteById(foldingTeamId);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected String elementType() {
        return "Folding team";
    }

    @Override
    protected ValidationResponse validate(final FoldingTeam element) {
        return FoldingTeamValidator.isValid(element);
    }

    @Override
    protected FoldingTeam createElement(final FoldingTeam element) throws FoldingException {
        return storageFacade.createFoldingTeam(element);
    }

    @Override
    protected List<FoldingTeam> getAllElements() throws FoldingException {
        return storageFacade.getAllFoldingTeams();
    }

    @Override
    protected FoldingTeam getElementById(final int elementId) throws FoldingException, NotFoundException {
        return storageFacade.getFoldingTeam(elementId);
    }

    @Override
    protected void deleteElementById(final int elementId) throws FoldingConflictException, FoldingException {
        storageFacade.deleteFoldingTeam(elementId);
    }
}
