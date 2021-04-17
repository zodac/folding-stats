package me.zodac.folding.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.db.postgres.PostgresDbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.TreeMap;

@Path("/historic/")
@RequestScoped
public class HistoricStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricStatsEndpoint.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @EJB
    private StorageFacade storageFacade;

    @Context
    private UriInfo uriContext;

    @GET
    @Path("/users/{foldingUserId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDailyUserStats(@PathParam("foldingUserId") final String foldingUserId, @PathParam("year") final String year, @PathParam("month") final String month) {
        LOGGER.info("GET request received to show daily TC user stats at '{}'", uriContext.getAbsolutePath());


        try {
            // TODO: [zodac] StorageFacade
            final Map<LocalDate, UserStats> dailyUserStats = new PostgresDbManager().getDailyUserStats(Integer.parseInt(foldingUserId), Month.of(Integer.parseInt(month)), Year.parse(year));
            final Map<LocalDate, UserStats> diffDailyUserStats = new TreeMap<>();
            

            return Response
                    .ok()
                    .entity(GSON.toJson(dailyUserStats))
                    .build();
        } catch (final DateTimeParseException e) {
            final String errorMessage = String.format("The year '%s' is not a valid format", year);
            final ErrorObject errorObject = new ErrorObject(errorMessage);

            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(errorObject, ErrorObject.class))
                    .build();
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The Folding user ID '%s' or month '%s' is not a valid format", foldingUserId, month);
            final ErrorObject errorObject = new ErrorObject(errorMessage);

            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(errorObject, ErrorObject.class))
                    .build();
        } catch (final NotFoundException e) {
            LOGGER.debug("No Folding user found with ID: {}", foldingUserId, e);
            LOGGER.error("No Folding user found with ID: {}", foldingUserId);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting Folding user with ID: {}", foldingUserId, e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting Folding user with ID: {}", foldingUserId, e);
            return Response
                    .serverError()
                    .build();
        }
    }

    @GET
    @Path("/teams/{foldingTeamId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDailyTeamStats(@PathParam("foldingTeamId") final String foldingTeamId) {
        LOGGER.info("GET request received to show daily TC team stats at '{}'", uriContext.getAbsolutePath());

        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @GET
    @Path("/teams/{foldingTeamId}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMonthlyTeamStats(@PathParam("foldingTeamId") final String foldingTeamId) {
        LOGGER.info("GET request received to show monthly TC team stats at '{}'", uriContext.getAbsolutePath());

        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }
}
