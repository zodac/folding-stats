package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.validator.TeamValidator;
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
 * REST endpoints for teams for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a PUT/PATCH endpoint
@Path("/teams/")
@RequestScoped
public class TeamEndpoint extends AbstractIdentifiableCrudEndpoint<Team> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HardwareEndpoint.class);

    @EJB
    private StorageFacade storageFacade;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTeam(final Team team) {
        return super.create(team);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTeams() {
        return super.getAll();
    }

    @GET
    @Path("/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamById(@PathParam("teamId") final String teamId) {
        return super.getById(teamId);
    }

    @PUT
    @Path("/{teamId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTeamById(@PathParam("teamId") final String teamId, final Team team) {
        return super.updateById(teamId, team);
    }

    @DELETE
    @Path("/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTeamById(@PathParam("teamId") final String teamId) {
        return super.deleteById(teamId);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected String elementType() {
        return "team";
    }

    @Override
    protected ValidationResponse validate(final Team element) {
        return TeamValidator.isValid(element);
    }

    @Override
    protected Team createElement(final Team team) throws FoldingException {
        return storageFacade.createTeam(team);
    }

    @Override
    protected List<Team> getAllElements() throws FoldingException {
        return storageFacade.getAllTeams();
    }

    @Override
    protected Team getElementById(final int teamId) throws FoldingException, NotFoundException {
        return storageFacade.getTeam(teamId);
    }

    @Override
    protected void updateElementById(final Team team) throws FoldingException, NotFoundException {
        storageFacade.updateTeam(team);
    }

    @Override
    protected void deleteElementById(final int teamId) throws FoldingConflictException, FoldingException {
        storageFacade.deleteTeam(teamId);
    }
}
