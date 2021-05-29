package me.zodac.folding.rest;

import me.zodac.folding.api.db.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.api.utils.ExecutionType;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.BusinessLogic;
import me.zodac.folding.ejb.TeamCompetitionStatsScheduler;
import me.zodac.folding.rest.validator.TeamValidator;
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
public class TeamEndpoint extends AbstractIdentifiableCrudEndpoint<Team> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamEndpoint.class);

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private TeamCompetitionStatsScheduler teamCompetitionStatsScheduler;

    @POST
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTeam(final Team team) {
        return super.create(team);
    }

    @POST
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOfTeams(final Collection<Team> teams) {
        return super.createBatchOf(teams);
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
    public Response updateTeamById(@PathParam("teamId") final String teamId, final Team team) {
        return super.updateById(teamId, team);
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
    protected ValidationResponse validate(final Team element) {
        final TeamValidator teamValidator = TeamValidator.create();
        return teamValidator.isValid(element);
    }

    @Override
    protected Team createElement(final Team team) throws FoldingException, FoldingConflictException {
        final Team teamWithId = businessLogic.createTeam(team);
        teamCompetitionStatsScheduler.parseTcStatsForTeam(team, ExecutionType.SYNCHRONOUS);
        return teamWithId;
    }

    @Override
    protected Collection<Team> getAllElements() throws FoldingException {
        return businessLogic.getAllTeams();
    }

    @Override
    protected Team getElementById(final int teamId) throws FoldingException, NotFoundException {
        return businessLogic.getTeam(teamId);
    }

    @Override
    protected Team updateElementById(final int teamId, final Team team) throws FoldingException, FoldingConflictException {
        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final Team teamWithId = Team.updateWithId(teamId, team);
        businessLogic.updateTeam(teamWithId);
        return teamWithId;
    }

    @Override
    protected void deleteElementById(final int teamId) throws FoldingConflictException, FoldingException {
        businessLogic.deleteTeam(teamId);
    }
}
