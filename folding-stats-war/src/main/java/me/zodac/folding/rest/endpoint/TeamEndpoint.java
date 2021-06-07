package me.zodac.folding.rest.endpoint;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.TeamCompetitionStatsScheduler;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.rest.util.validator.TeamValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * REST endpoints for teams for <code>folding-stats</code>.
 */
@Path("/teams/")
@RequestScoped
public class TeamEndpoint extends AbstractCrudEndpoint<TeamRequest, Team> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamEndpoint.class);

    @EJB
    private transient TeamCompetitionStatsScheduler teamCompetitionStatsScheduler;

    @POST
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTeam(final TeamRequest teamRequest) {
        return super.create(teamRequest);
    }

    @POST
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOfTeams(final Collection<TeamRequest> teamRequests) {
        return super.createBatchOf(teamRequests);
    }

    @GET
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTeams(@Context final Request request) {
        return super.getAll(request);
    }

    @GET
    @PermitAll
    @Path("/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamById(@PathParam("teamId") final String teamId, @Context final Request request) {
        return super.getById(teamId, request);
    }

    @PUT
    @RolesAllowed("admin")
    @Path("/{teamId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTeamById(@PathParam("teamId") final String teamId, final TeamRequest teamRequest) {
        return super.updateById(teamId, teamRequest);
    }

    @DELETE
    @RolesAllowed("admin")
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
    protected Team createElement(final Team team) throws FoldingException {
        final Team teamWithId = oldFacade.createTeam(team);
        teamCompetitionStatsScheduler.parseTcStatsForTeam(team, ExecutionType.SYNCHRONOUS);
        return teamWithId;
    }

    @Override
    protected Collection<Team> getAllElements() throws FoldingException {
        return oldFacade.getAllTeams();
    }

    @Override
    protected ValidationResponse<Team> validateCreateAndConvert(final TeamRequest teamRequest) {
        final TeamValidator teamValidator = TeamValidator.create(oldFacade);
        return teamValidator.validateCreate(teamRequest);
    }

    @Override
    protected ValidationResponse<Team> validateUpdateAndConvert(final TeamRequest teamRequest) {
        final TeamValidator teamValidator = TeamValidator.create(oldFacade);
        return teamValidator.validateUpdate(teamRequest);
    }

    @Override
    protected ValidationResponse<Team> validateDeleteAndConvert(final Team team) {
        final TeamValidator teamValidator = TeamValidator.create(oldFacade);
        return teamValidator.validateDelete(team);
    }

    @Override
    protected Team getElementById(final int teamId) throws FoldingException, NotFoundException {
        return oldFacade.getTeam(teamId);
    }

    @Override
    protected Team updateElementById(final int teamId, final Team team) throws FoldingException {
        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final Team teamWithId = Team.updateWithId(teamId, team);
        oldFacade.updateTeam(teamWithId);
        return teamWithId;
    }

    @Override
    protected void deleteElementById(final int teamId) throws FoldingException {
        oldFacade.deleteTeam(teamId);
    }
}
