package me.zodac.folding.rest;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.DateTimeException;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.notImplemented;
import static me.zodac.folding.rest.response.Responses.okBuilder;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

@Path("/historic/")
@RequestScoped
public class HistoricStatsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricStatsEndpoint.class);

    // Stats updates occur every hour, so we must invalidate responses every hour
    private static final int CACHE_EXPIRATION_TIME = (int) TimeUnit.HOURS.toSeconds(1);

    @EJB
    private StorageFacade storageFacade;

    @Context
    private UriInfo uriContext;

    @GET
    @Path("/users/{userId}/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserHistoricStatsHourly(@PathParam("userId") final String userId) {
        LOGGER.info("GET request received to show hourly TC user stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        return notImplemented();
    }

    @GET
    @Path("/users/{userId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserHistoricStatsDaily(@PathParam("userId") final String userId, @PathParam("year") final String year, @PathParam("month") final String month, @Context final Request request) {
        LOGGER.debug("GET request received to show daily TC user stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final Collection<HistoricStats> dailyStats = storageFacade.getHistoricStatsDaily(Integer.parseInt(userId), Month.of(Integer.parseInt(month)), Year.parse(year));

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(CACHE_EXPIRATION_TIME);

            final EntityTag entityTag = new EntityTag(String.valueOf(dailyStats.stream().mapToInt(HistoricStats::hashCode).sum()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);

            if (builder == null) {
                LOGGER.debug("Cached resources have changed");
                builder = okBuilder(dailyStats);
                builder.tag(entityTag);
            }

            builder.cacheControl(cacheControl);
            return builder.build();
        } catch (final DateTimeParseException e) {
            final String errorMessage = String.format("The year '%s' is not a valid format", year);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final DateTimeException e) {
            final String errorMessage = String.format("The month '%s' is not a valid format", month);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The user ID '%s' or month '%s' is not a valid format", userId, month);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NotFoundException e) {
            LOGGER.debug("No user found with ID: {}", userId, e);
            LOGGER.error("No user found with ID: {}", userId);
            return notFound();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting user with ID: {}", userId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting user with ID: {}", userId, e);
            return serverError();
        }
    }

    @GET
    @Path("/users/{userId}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserHistoricStatsMonthly(@PathParam("userId") final String userId, @PathParam("year") final String year, @Context final Request request) {
        LOGGER.debug("GET request received to show monthly TC user stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final Collection<HistoricStats> monthlyStats = storageFacade.getHistoricStatsMonthly(Integer.parseInt(userId), Year.parse(year));

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(CACHE_EXPIRATION_TIME);

            final EntityTag entityTag = new EntityTag(String.valueOf(monthlyStats.stream().mapToInt(HistoricStats::hashCode).sum()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);

            if (builder == null) {
                LOGGER.debug("Cached resources have changed");
                builder = okBuilder(monthlyStats);
                builder.tag(entityTag);
            }

            builder.cacheControl(cacheControl);
            return builder.build();
        } catch (final DateTimeParseException e) {
            final String errorMessage = String.format("The year '%s' is not a valid format", year);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The user ID '%s' is not a valid format", userId);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NotFoundException e) {
            LOGGER.debug("No {} found with ID: {}", e.getType(), e.getId(), e);
            LOGGER.error("No {} found with ID: {}", e.getType(), e.getId());
            return notFound();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting user with ID: {}", userId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting user with ID: {}", userId, e);
            return serverError();
        }
    }

    @GET
    @Path("/teams/{teamId}/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamHistoricStatsHourly(@PathParam("teamId") final String teamId) {
        LOGGER.debug("GET request received to show hourly TC team stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        return notImplemented();
    }

    @GET
    @Path("/teams/{teamId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamHistoricStatsDaily(@PathParam("teamId") final String teamId, @PathParam("year") final String year, @PathParam("month") final String month, @Context final Request request) {
        LOGGER.debug("GET request received to show daily TC user stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final Team team = storageFacade.getTeam(Integer.parseInt(teamId));
            final List<HistoricStats> teamDailyStats = new ArrayList<>();

            for (final Integer userId : team.getUserIds()) {
                final Collection<HistoricStats> dailyStats = storageFacade.getHistoricStatsDaily(userId, Month.of(Integer.parseInt(month)), Year.parse(year));
                teamDailyStats.addAll(dailyStats);
            }

            final Collection<HistoricStats> combinedTeamDailyStats = HistoricStats.combine(teamDailyStats);

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(CACHE_EXPIRATION_TIME);

            final EntityTag entityTag = new EntityTag(String.valueOf(combinedTeamDailyStats.stream().mapToInt(HistoricStats::hashCode).sum()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);

            if (builder == null) {
                LOGGER.debug("Cached resources have changed");
                builder = okBuilder(combinedTeamDailyStats);
                builder.tag(entityTag);
            }

            builder.cacheControl(cacheControl);
            return builder.build();
        } catch (final DateTimeParseException e) {
            final String errorMessage = String.format("The year '%s' is not a valid format", year);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final DateTimeException e) {
            final String errorMessage = String.format("The month '%s' is not a valid format", month);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The team ID '%s' or month '%s' is not a valid format", teamId, month);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NotFoundException e) {
            LOGGER.debug("No {} found with ID: {}", e.getType(), e.getId(), e);
            LOGGER.error("No {} found with ID: {}", e.getType(), e.getId());
            return notFound();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting team with ID: {}", teamId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting team with ID: {}", teamId, e);
            return serverError();
        }
    }

    @GET
    @Path("/teams/{teamId}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamHistoricStatsMonthly(@PathParam("teamId") final String teamId, @PathParam("year") final String year, @Context final Request request) {
        LOGGER.debug("GET request received to show monthly TC team stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final Team team = storageFacade.getTeam(Integer.parseInt(teamId));
            final List<HistoricStats> teamMonthlyStats = new ArrayList<>();

            for (final Integer userId : team.getUserIds()) {
                final Collection<HistoricStats> monthlyStats = storageFacade.getHistoricStatsMonthly(userId, Year.parse(year));
                teamMonthlyStats.addAll(monthlyStats);
            }

            final Collection<HistoricStats> combinedTeamMonthlyStats = HistoricStats.combine(teamMonthlyStats);

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(CACHE_EXPIRATION_TIME);
            final EntityTag entityTag = new EntityTag(String.valueOf(combinedTeamMonthlyStats.stream().mapToInt(HistoricStats::hashCode).sum()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);

            if (builder == null) {
                LOGGER.debug("Cached resources have changed");
                builder = okBuilder(combinedTeamMonthlyStats);
                builder.tag(entityTag);
            }

            builder.cacheControl(cacheControl);
            return builder.build();
        } catch (final DateTimeParseException e) {
            final String errorMessage = String.format("The year '%s' is not a valid format", year);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The team ID '%s' is not a valid format", teamId);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NotFoundException e) {
            LOGGER.debug("No {} found with ID: {}", e.getType(), e.getId(), e);
            LOGGER.error("No {} found with ID: {}", e.getType(), e.getId());
            return notFound();
        } catch (final FoldingException e) {
            LOGGER.error("Error getting team with ID: {}", teamId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting team with ID: {}", teamId, e);
            return serverError();
        }
    }
}
