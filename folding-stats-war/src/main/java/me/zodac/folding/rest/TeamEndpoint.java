package me.zodac.folding.rest;

import com.google.gson.Gson;
import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.validator.FoldingTeamValidator;
import me.zodac.folding.validator.ValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;


/**
 * REST endpoints for teams for <code>folding-stats</code>.
 */
// TODO: [zodac] Add a DELETE and PUT endpoint
//   Update caches on team change
//   Explicit endpoints to add/remove/replace a single user in a team
@Path("/team/")
@RequestScoped
public class TeamEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamEndpoint.class);
    private static final Gson GSON = new Gson();

    @EJB
    private StorageFacade storageFacade;

    @Context
    private UriInfo uriContext;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTeam(final FoldingTeam foldingTeam) {
        LOGGER.info("POST request received to create Folding team at '{}' with request: {}", this.uriContext.getAbsolutePath(), foldingTeam);

        final ValidationResponse validationResponse = FoldingTeamValidator.isValid(foldingTeam);
        if (!validationResponse.isValid()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(validationResponse))
                    .build();
        }

        try {
            final FoldingTeam foldingTeamWithId = storageFacade.createFoldingTeam(foldingTeam);

            final UriBuilder builder = uriContext.getBaseUriBuilder()
                    .path(String.valueOf(foldingTeamWithId.getId()));
            return Response.created(builder.build()).build();
        } catch (final FoldingException e) {
            LOGGER.error("Error creating Folding team: {}", foldingTeam, e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating Folding team: {}", foldingTeam, e);
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFoldingTeams() {
        LOGGER.info("GET request received for all Folding teams at '{}'", this.uriContext.getAbsolutePath());

        try {
            final List<FoldingTeam> foldingTeams = storageFacade.getAllFoldingTeams();
            LOGGER.info("Found {} Folding teams", foldingTeams.size());
            return Response
                    .ok()
                    .entity(GSON.toJson(foldingTeams))
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting all Folding teams", e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all Folding teams", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{foldingTeamId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFoldingTeamById(@PathParam("foldingTeamId") final String foldingTeamId) {
        LOGGER.info("GET request for Folding team received at '{}'", this.uriContext.getAbsolutePath());

        try {
            final FoldingTeam foldingTeam = storageFacade.getFoldingTeam(Integer.parseInt(foldingTeamId));
            return Response
                    .ok()
                    .entity(foldingTeam)
                    .build();
        } catch (final NumberFormatException e) {
            LOGGER.error("Folding team ID '{}' is not a valid number", foldingTeamId, e);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(String.format("Folding team ID '%s' is invalid format", foldingTeamId)))
                    .build();
        } catch (final NotFoundException e) {
            LOGGER.debug("No Folding team found with ID: {}", foldingTeamId, e);
            LOGGER.error("No Folding team found with ID: {}", foldingTeamId);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting Folding team with ID: {}", foldingTeamId, e.getCause());
            return Response.serverError().build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting Folding team with ID: {}", foldingTeamId, e);
            return Response.serverError().build();
        }
    }
}
