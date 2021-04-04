package me.zodac.folding.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/tc_stats/")
@RequestScoped
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsEndpoint.class);

    @Context
    private UriInfo uriContext;

    @GET
    public Response getTeamCompetitionStats() {
        LOGGER.info("GET request received to load TC stats at '{}'", this.uriContext.getAbsolutePath());

        return Response
                .ok()
                .build();
    }
}
