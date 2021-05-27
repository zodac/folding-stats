package me.zodac.folding.rest;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.db.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.exception.FoldingIdInvalidException;
import me.zodac.folding.api.tc.exception.FoldingIdOutOfRangeException;
import me.zodac.folding.api.tc.exception.NoStatsAvailableException;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.api.tc.exception.TeamNotFoundException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
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

import static me.zodac.folding.rest.response.Responses.badGateway;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

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

    @PATCH
    @RolesAllowed("admin")
    @Path("/{teamId}/retire/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retireUserFromTeam(@PathParam("teamId") final String teamId, @PathParam("userId") final String userId) {
        getLogger().debug("PATCH request to retire user from team received at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isWriteBlocked()) {
            getLogger().warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final int parsedTeamId = ParsingUtils.parseId(teamId);
            final int parsedUnitId = ParsingUtils.parseId(userId);

            final Team team = businessLogic.getTeam(parsedTeamId);
            final TeamValidator teamValidator = TeamValidator.create(businessLogic);
            final ValidationResponse validationResponse = teamValidator.isValidRetirement(team, parsedUnitId);
            if (validationResponse.isInvalid()) {
                return badRequest(validationResponse);
            }

            final Team updatedTeam = businessLogic.retireUser(parsedTeamId, parsedUnitId);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok(updatedTeam);
        } catch (final FoldingIdInvalidException e) {
            final String errorMessage = String.format("The ID '%s' is not a valid format", e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingIdOutOfRangeException e) {
            final String errorMessage = String.format("The ID '%s' is out of range", e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final UserNotFoundException e) {
            getLogger().error("Error finding user with ID: {}", userId, e.getCause());
            return notFound();
        } catch (final TeamNotFoundException e) {
            getLogger().error("Error finding team with ID: {}", teamId, e.getCause());
            return notFound();
        } catch (final FoldingException e) {
            getLogger().error("Error updating team with ID: {}", teamId, e.getCause());
            return serverError();
        } catch (final FoldingConflictException e) {
            getLogger().error("Error updating team with ID: {}", teamId, e);
            return serverError();
        } catch (final NoStatsAvailableException e) {
            getLogger().error("No stats returned for user to retire with ID {}, possible invalid ID?", userId, e);
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error updating team with ID: {}", teamId, e);
            return serverError();
        }
    }

    @PATCH
    @RolesAllowed("admin")
    @Path("/{teamId}/unretire/{retiredUserId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response unretireUserFromTeam(@PathParam("teamId") final String teamId, @PathParam("retiredUserId") final String retiredUserId) {
        getLogger().debug("PATCH request to un-retire user from team received at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isWriteBlocked()) {
            getLogger().warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final int parsedTeamId = ParsingUtils.parseId(teamId);
            final int parsedRetiredUserId = ParsingUtils.parseId(retiredUserId);

            final Team team = businessLogic.getTeam(parsedTeamId);
            final TeamValidator teamValidator = TeamValidator.create(businessLogic);
            final ValidationResponse validationResponse = teamValidator.isValidUnretirement(team, parsedRetiredUserId);
            if (validationResponse.isInvalid()) {
                return badRequest(validationResponse);
            }

            final Team updatedTeam = businessLogic.unretireUser(parsedTeamId, parsedRetiredUserId);
            return ok(updatedTeam);
        } catch (final FoldingIdInvalidException e) {
            final String errorMessage = String.format("The ID '%s' is not a valid format", e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingIdOutOfRangeException e) {
            final String errorMessage = String.format("The ID '%s' is out of range", e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingExternalServiceException e) {
            final String errorMessage = String.format("Error connecting to external service at '%s': %s", e.getUrl(), e.getMessage());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badGateway();
        } catch (final UserNotFoundException e) {
            getLogger().error("Error finding retired user with ID: {}", retiredUserId, e.getCause());
            return notFound();
        } catch (final TeamNotFoundException e) {
            getLogger().error("Error finding team with ID: {}", teamId, e.getCause());
            return notFound();
        } catch (final FoldingException e) {
            getLogger().error("Error updating team with ID: {}", teamId, e.getCause());
            return serverError();
        } catch (final FoldingConflictException e) {
            getLogger().error("Error updating team with ID: {}", teamId, e);
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error updating team with ID: {}", teamId, e);
            return serverError();
        }
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
        final TeamValidator teamValidator = TeamValidator.create(businessLogic);
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
