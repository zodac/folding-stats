package me.zodac.folding.rest.endpoint;

import static me.zodac.folding.api.util.DateTimeUtils.untilNextMonthUtc;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.nullRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.ejb.api.BusinessLogic;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.rest.endpoint.util.IdResult;
import me.zodac.folding.rest.endpoint.util.IntegerParser;
import me.zodac.folding.rest.response.BatchCreateResponse;
import me.zodac.folding.rest.validator.TeamValidator;
import me.zodac.folding.rest.validator.ValidationFailure;
import me.zodac.folding.rest.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;
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
public class TeamEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private UriInfo uriContext;

    @EJB
    private BusinessLogic businessLogic;

    /**
     * {@link POST} request to create a {@link Team} based on the input request.
     *
     * @param teamRequest the {@link TeamRequest} to create a {@link Team}
     * @return {@link Response.Status#CREATED} containing the created {@link Team}
     */
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final TeamRequest teamRequest) {
        LOGGER.debug("POST request received to create team at '{}' with request: {}", uriContext::getAbsolutePath, () -> teamRequest);

        final ValidationResult<Team> validationResult = validateCreate(teamRequest);
        if (validationResult.isFailure()) {
            return validationResult.getFailureResponse();
        }
        final Team validatedTeam = validationResult.getOutput();

        try {
            final Team elementWithId = businessLogic.createTeam(validatedTeam);

            final UriBuilder elementLocationBuilder = uriContext
                .getRequestUriBuilder()
                .path(String.valueOf(elementWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Created team with ID {}", elementWithId.getId());
            return created(elementWithId, elementLocationBuilder);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating team: {}", teamRequest, e);
            return serverError();
        }
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
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOf(final Collection<TeamRequest> teamRequests) {
        LOGGER.debug("POST request received to create {} teams at '{}' with request: {}", teamRequests::size, uriContext::getAbsolutePath,
            () -> teamRequests);

        final Collection<Team> validTeams = new ArrayList<>(teamRequests.size() / 2);
        final Collection<ValidationFailure> failedValidationResponses = new ArrayList<>(teamRequests.size() / 2);

        for (final TeamRequest teamRequest : teamRequests) {
            final ValidationResult<Team> validationResult = validateCreate(teamRequest);

            if (validationResult.isFailure()) {
                LOGGER.error("Found validation error for {}: {}", teamRequest, validationResult);
                failedValidationResponses.add(validationResult.getValidationFailure());
            } else {
                validTeams.add(validationResult.getOutput());
            }
        }

        if (validTeams.isEmpty()) {
            LOGGER.error("All teams contain validation errors: {}", failedValidationResponses);
            return badRequest(failedValidationResponses);
        }

        final List<Object> successful = new ArrayList<>();
        final List<Object> unsuccessful = new ArrayList<>(failedValidationResponses);

        for (final Team validTeam : validTeams) {
            try {
                final Team teamWithId = businessLogic.createTeam(validTeam);
                successful.add(teamWithId);
            } catch (final Exception e) {
                LOGGER.error("Unexpected error creating team: {}", validTeam, e);
                unsuccessful.add(validTeam);
            }
        }

        final BatchCreateResponse batchCreateResponse = BatchCreateResponse.create(successful, unsuccessful);

        if (successful.isEmpty()) {
            return badRequest(batchCreateResponse);
        }

        if (!unsuccessful.isEmpty()) {
            LOGGER.error("{} teams successfully created, {} teams unsuccessful", successful.size(), unsuccessful.size());
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok(batchCreateResponse);
        }

        LOGGER.info("{} teams successfully created", successful.size());
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        return ok(batchCreateResponse.getSuccessful());
    }

    /**
     * {@link GET} request to retrieve all {@link Team}s.
     *
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Team}s
     */
    @GET
    @ReadRequired
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@Context final Request request) {
        LOGGER.debug("GET request received for all teams at '{}'", uriContext::getAbsolutePath);

        try {
            final Collection<Team> elements = businessLogic.getAllTeams();
            return cachedOk(elements, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all teams", e);
            return serverError();
        }
    }

    /**
     * {@link GET} request to retrieve a {@link Team}.
     *
     * @param teamId  the ID of the {@link Team} to retrieve
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Team}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("teamId") final String teamId, @Context final Request request) {
        LOGGER.debug("GET request for team received at '{}'", uriContext::getAbsolutePath);

        try {
            final IdResult idResult = IntegerParser.parsePositive(teamId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<Team> optionalElement = businessLogic.getTeam(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No team found with ID {}", teamId);
                return notFound();
            }

            final Team element = optionalElement.get();
            return cachedOk(element, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting team with ID: {}", teamId, e);
            return serverError();
        }
    }

    /**
     * {@link GET} request to retrieve a {@link Team}.
     *
     * @param teamName the {@code teamName} of the {@link Team} to retrieve
     * @param request  the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Team}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/fields")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByHardwareName(@QueryParam("teamName") final String teamName, @Context final Request request) {
        LOGGER.debug("GET request for team received at '{}'", uriContext::getAbsolutePath);

        try {
            if (StringUtils.isBlank(teamName)) {
                final String errorMessage = String.format("Input 'teamName' must not be blank: '%s'", teamName);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }

            final Optional<Team> optionalTeam = businessLogic.getAllTeams()
                .stream()
                .filter(team -> team.getTeamName().equalsIgnoreCase(teamName))
                .findAny();

            if (optionalTeam.isEmpty()) {
                LOGGER.error("No hardware found with 'hardwareName' '{}'", teamName);
                return notFound();
            }

            final Team element = optionalTeam.get();
            return cachedOk(element, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting team with 'hardwareName': '{}'", teamName, e);
            return serverError();
        }
    }

    /**
     * {@link PUT} request to update an existing {@link Team} based on the input request.
     *
     * @param teamId      the ID of the {@link Team} to be updated
     * @param teamRequest the {@link TeamRequest} to update a {@link Team}
     * @return {@link Response.Status#OK} containing the updated {@link Team}
     */
    @PUT
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{teamId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateById(@PathParam("teamId") final String teamId, final TeamRequest teamRequest) {
        LOGGER.debug("PUT request for team received at '{}'", uriContext::getAbsolutePath);

        if (teamRequest == null) {
            LOGGER.error("No payload provided");
            return nullRequest();
        }

        try {
            final IdResult idResult = IntegerParser.parsePositive(teamId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<Team> optionalElement = businessLogic.getTeam(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No team found with ID {}", teamId);
                return notFound();
            }
            final Team existingTeam = optionalElement.get();

            if (existingTeam.isEqualRequest(teamRequest)) {
                LOGGER.debug("No change necessary");
                return ok(existingTeam);
            }

            final ValidationResult<Team> validationResult = validateUpdate(teamRequest, existingTeam);
            if (validationResult.isFailure()) {
                return validationResult.getFailureResponse();
            }
            final Team validatedHardware = validationResult.getOutput();

            // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
            final Team teamWithId = Team.updateWithId(existingTeam.getId(), validatedHardware);
            final Team updatedTeamWithId = businessLogic.updateTeam(teamWithId);

            final UriBuilder elementLocationBuilder = uriContext
                .getRequestUriBuilder()
                .path(String.valueOf(updatedTeamWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Updated team with ID {}", updatedTeamWithId.getId());
            return ok(updatedTeamWithId, elementLocationBuilder);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating team with ID: {}", teamId, e);
            return serverError();
        }
    }

    /**
     * {@link DELETE} request to delete an existing {@link Team}.
     *
     * @param teamId the ID of the {@link Team} to be deleted
     * @return {@link Response.Status#OK}
     */
    @DELETE
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("teamId") final String teamId) {
        LOGGER.debug("DELETE request for team received at '{}'", uriContext::getAbsolutePath);

        try {
            final IdResult idResult = IntegerParser.parsePositive(teamId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<Team> optionalElement = businessLogic.getTeam(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No team found with ID {}", teamId);
                return notFound();
            }
            final Team team = optionalElement.get();

            final ValidationResult<Team> validationResult = validateDelete(team);
            if (validationResult.isFailure()) {
                return validationResult.getFailureResponse();
            }
            final Team validatedTeam = validationResult.getOutput();

            businessLogic.deleteTeam(validatedTeam);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Deleted team with ID {}", teamId);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error deleting team with ID: {}", teamId, e);
            return serverError();
        }
    }

    private ValidationResult<Team> validateCreate(final TeamRequest teamRequest) {
        return TeamValidator.validateCreate(teamRequest, businessLogic.getAllTeams());
    }

    private ValidationResult<Team> validateUpdate(final TeamRequest teamRequest, final Team existingTeam) {
        return TeamValidator.validateUpdate(teamRequest, existingTeam, businessLogic.getAllTeams());
    }

    private ValidationResult<Team> validateDelete(final Team team) {
        return TeamValidator.validateDelete(team, businessLogic.getAllUsersWithoutPasskeys());
    }
}
