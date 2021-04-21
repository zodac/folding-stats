package me.zodac.folding.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.rest.response.ErrorResponse;
import me.zodac.folding.rest.tc.historic.DailyStats;
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
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Path("/users/{userId}/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHourlyUserStats(@PathParam("userId") final String userId) {
        LOGGER.info("GET request received to show hourly TC user stats at '{}'", uriContext.getAbsolutePath());

        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @GET
    @Path("/users/{userId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDailyUserStats(@PathParam("userId") final String userId, @PathParam("year") final String year, @PathParam("month") final String month) {
        LOGGER.info("GET request received to show daily TC user stats at '{}'", uriContext.getAbsolutePath());

        try {
            final Map<LocalDate, Stats> dailyUserStats = storageFacade.getDailyUserStats(Integer.parseInt(userId), Month.of(Integer.parseInt(month)), Year.parse(year));

            final List<DailyStats> dailyStats = dailyUserStats.entrySet()
                    .stream()
                    .map(entry -> DailyStats.createFromStats(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            return Response
                    .ok()
                    .entity(GSON.toJson(dailyStats))
                    .build();
        } catch (final DateTimeParseException e) {
            final String errorMessage = String.format("The year '%s' is not a valid format", year);

            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
                    .build();
        } catch (final DateTimeException e) {
            final String errorMessage = String.format("The month '%s' is not a valid format", month);

            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
                    .build();
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The user ID '%s' or month '%s' is not a valid format", userId, month);

            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
                    .build();
        } catch (final NotFoundException e) {
            LOGGER.debug("No user found with ID: {}", userId, e);
            LOGGER.error("No user found with ID: {}", userId);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting user with ID: {}", userId, e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting user with ID: {}", userId, e);
            return Response
                    .serverError()
                    .build();
        }
    }

    @GET
    @Path("/users/{userId}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMonthlyUserStats(@PathParam("userId") final String userId) {
        LOGGER.info("GET request received to show monthly TC user stats at '{}'", uriContext.getAbsolutePath());

        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @GET
    @Path("/teams/{teamId}/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHourlyTeamStats(@PathParam("teamId") final String teamId) {
        LOGGER.info("GET request received to show hourly TC team stats at '{}'", uriContext.getAbsolutePath());

        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @GET
    @Path("/teams/{teamId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDailyTeamStats(@PathParam("teamId") final String teamId) {
        LOGGER.info("GET request received to show daily TC team stats at '{}'", uriContext.getAbsolutePath());

        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }

    @GET
    @Path("/teams/{teamId}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMonthlyTeamStats(@PathParam("teamId") final String teamId) {
        LOGGER.info("GET request received to show monthly TC team stats at '{}'", uriContext.getAbsolutePath());

        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .build();
    }
}
