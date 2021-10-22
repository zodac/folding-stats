package me.zodac.folding.rest.endpoint;

import java.util.Collection;
import java.util.Optional;
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
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.util.ProcessingType;
import me.zodac.folding.ejb.tc.scheduled.StatsScheduler;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.rest.validator.TeamValidator;
import me.zodac.folding.rest.validator.ValidationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * REST endpoints for <code>Team Competition</code> {@link Team}s.
 *
 * @see me.zodac.folding.client.java.request.TeamRequestSender
 * @see me.zodac.folding.client.java.response.TeamResponseParser
 */
@Path("/teams/")
@RequestScoped
public class TeamEndpoint extends AbstractCrudEndpoint<TeamRequest, Team> {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private StatsScheduler statsScheduler;

    /**
     * {@link POST} request to create a {@link Team} based on the input request.
     *
     * @param teamRequest the {@link TeamRequest} to create a {@link Team}
     * @return {@link Response.Status#CREATED} containing the created {@link Team}
     */
    @Override
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final TeamRequest teamRequest) {
        return super.create(teamRequest);
    }

    /**
     * {@link POST} request to create a {@link Collection} of {@link Team}s based on the input requests.
     *
     * <p>
     * Will perform a best-effort attempt to create all {@link Team}s and will return a response with successful and unsuccessful results.
     *
     * @param teamRequests the {@link TeamRequest}s to create {@link Team}s
     * @return {@link Response.Status#OK} containing the created/failed {@link Team}s
     */
    @Override
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOf(final Collection<TeamRequest> teamRequests) {
        return super.createBatchOf(teamRequests);
    }

    /**
     * {@link GET} request to retrieve all {@link Team}s.
     *
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Team}s
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
     * {@link GET} request to retrieve a {@link Team}.
     *
     * @param teamId  the ID of the {@link Team} to retrieve
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Team}
     */
    @Override
    @GET
    @ReadRequired
    @PermitAll
    @Path("/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("teamId") final String teamId, @Context final Request request) {
        return super.getById(teamId, request);
    }

    /**
     * {@link PUT} request to update an existing {@link Team} based on the input request.
     *
     * @param teamId      the ID of the {@link Team} to be updated
     * @param teamRequest the {@link TeamRequest} to update a {@link Team}
     * @return {@link Response.Status#OK} containing the updated {@link Team}
     */
    @Override
    @PUT
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{teamId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateById(@PathParam("teamId") final String teamId, final TeamRequest teamRequest) {
        return super.updateById(teamId, teamRequest);
    }

    /**
     * {@link DELETE} request to delete an existing {@link Team}.
     *
     * @param teamId the ID of the {@link Team} to be deleted
     * @return {@link Response.Status#OK}
     */
    @Override
    @DELETE
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("teamId") final String teamId) {
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
    protected Team createElement(final Team team) {
        final Team teamWithId = businessLogic.createTeam(team);
        statsScheduler.manualTeamCompetitionStatsParsing(ProcessingType.SYNCHRONOUS);
        return teamWithId;
    }

    @Override
    protected Collection<Team> getAllElements() {
        return businessLogic.getAllTeams();
    }

    @Override
    protected ValidationResult<Team> validateCreateAndConvert(final TeamRequest teamRequest) {
        final TeamValidator teamValidator = TeamValidator.create(businessLogic);
        return teamValidator.validateCreate(teamRequest);
    }

    @Override
    protected ValidationResult<Team> validateUpdateAndConvert(final TeamRequest teamRequest, final Team existingTeam) {
        final TeamValidator teamValidator = TeamValidator.create(businessLogic);
        return teamValidator.validateUpdate(teamRequest, existingTeam);
    }

    @Override
    protected ValidationResult<Team> validateDeleteAndConvert(final Team team) {
        final TeamValidator teamValidator = TeamValidator.create(businessLogic);
        return teamValidator.validateDelete(team);
    }

    @Override
    protected Optional<Team> getElementById(final int teamId) {
        return businessLogic.getTeam(teamId);
    }

    @Override
    protected Team updateElementById(final Team team, final Team existingTeam) {
        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final Team teamWithId = Team.updateWithId(existingTeam.getId(), team);
        return businessLogic.updateTeam(teamWithId);
    }

    @Override
    protected void deleteElement(final Team team) {
        businessLogic.deleteTeam(team);
    }
}
