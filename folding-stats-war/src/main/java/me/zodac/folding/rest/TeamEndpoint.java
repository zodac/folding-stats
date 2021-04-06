package me.zodac.folding.rest;

import com.google.gson.Gson;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.db.postgres.PostgresDbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Context
    private UriInfo uriContext;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTeam(final FoldingTeam foldingTeam) {
        LOGGER.info("POST request received to create Folding team at '{}' with request: {}", this.uriContext.getAbsolutePath(), foldingTeam);

        // TODO: [zodac] Check that user IDs are valid, else will fail at persist and return a 500. Should clean and return a 400 instead.
        if (!foldingTeam.isValid()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(foldingTeam))
                    .build();
        }

        try {
            final FoldingTeam foldingTeamWithId = PostgresDbManager.createFoldingTeam(foldingTeam);

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
            final List<FoldingTeam> foldingTeams = PostgresDbManager.getAllFoldingTeams();
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
            final FoldingTeam foldingTeam = PostgresDbManager.getFoldingTeam(foldingTeamId);
            return Response
                    .ok()
                    .entity(foldingTeam)
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
