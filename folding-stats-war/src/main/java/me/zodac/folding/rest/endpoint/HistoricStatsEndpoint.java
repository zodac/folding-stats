package me.zodac.folding.rest.endpoint;

import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.okBuilder;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

import java.time.DateTimeException;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.security.PermitAll;
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
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.ejb.OldFacade;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.rest.parse.IntegerParser;
import me.zodac.folding.rest.parse.ParseResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: [zodac] Verify that all places that return a HTTP response also log something
@Path("/historic/")
@RequestScoped
public class HistoricStatsEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    // Stats updates occur every hour, so we must invalidate responses every hour
    private static final int CACHE_EXPIRATION_TIME = (int) TimeUnit.HOURS.toSeconds(1);

    @Context
    private transient UriInfo uriContext;

    @EJB
    private transient BusinessLogic businessLogic;

    @EJB
    private transient OldFacade oldFacade;

    @GET
    @PermitAll
    @Path("/users/{userId}/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserHistoricStatsHourly(@PathParam("userId") final String userId, @PathParam("year") final String year,
                                               @PathParam("month") final String month, @PathParam("day") final String day,
                                               @Context final Request request) {
        LOGGER.debug("GET request received to show hourly TC user stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final int dayAsInt = Integer.parseInt(day);
            final int monthAsInt = Integer.parseInt(month);
            final int yearAsInt = Year.parse(year).getValue();

            final YearMonth date = YearMonth.of(yearAsInt, monthAsInt);
            if (!date.isValidDay(dayAsInt)) {
                final String errorMessage = String.format("The day '%s' is not a valid day for %s/%s", day, year, month);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }

            final ParseResult parseResult = IntegerParser.parsePositive(userId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The user ID '%s' is not a valid format", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The user ID '%s' is out of range", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();

            final Optional<User> user = businessLogic.getUserWithPasskey(parsedId);
            if (user.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final Collection<HistoricStats> hourlyStats =
                oldFacade.getHistoricStatsHourly(parsedId, Integer.parseInt(day), Month.of(Integer.parseInt(month)), Year.parse(year));

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(CACHE_EXPIRATION_TIME);

            final EntityTag entityTag = new EntityTag(String.valueOf(hourlyStats.stream().mapToInt(HistoricStats::hashCode).sum()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);

            if (builder == null) {
                LOGGER.debug("Cached resources have changed");
                builder = okBuilder(hourlyStats);
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
            final String errorMessage = String.format("The month '%s' or day '%s' is not a valid format", month, day);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting user with ID: {}", userId, e);
            return serverError();
        }
    }

    @GET
    @PermitAll
    @Path("/users/{userId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserHistoricStatsDaily(@PathParam("userId") final String userId, @PathParam("year") final String year,
                                              @PathParam("month") final String month, @Context final Request request) {
        LOGGER.debug("GET request received to show daily TC user stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final ParseResult parseResult = IntegerParser.parsePositive(userId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The user ID '%s' is not a valid format", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The user ID '%s' is out of range", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();

            final Optional<User> user = businessLogic.getUserWithPasskey(parsedId);
            if (user.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final Collection<HistoricStats> dailyStats =
                oldFacade.getHistoricStatsDaily(parsedId, Month.of(Integer.parseInt(month)), Year.parse(year));

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
        } catch (final DateTimeException | NumberFormatException e) {
            final String errorMessage = String.format("The month '%s' is not a valid format", month);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting user with ID: {}", userId, e);
            return serverError();
        }
    }

    @GET
    @PermitAll
    @Path("/users/{userId}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserHistoricStatsMonthly(@PathParam("userId") final String userId, @PathParam("year") final String year,
                                                @Context final Request request) {
        LOGGER.debug("GET request received to show monthly TC user stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final ParseResult parseResult = IntegerParser.parsePositive(userId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The user ID '%s' is not a valid format", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The user ID '%s' is out of range", userId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();

            final Optional<User> user = businessLogic.getUserWithPasskey(parsedId);
            if (user.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final Collection<HistoricStats> monthlyStats = oldFacade.getHistoricStatsMonthly(parsedId, Year.parse(year));

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
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting user with ID: {}", userId, e);
            return serverError();
        }
    }

    @GET
    @PermitAll
    @Path("/teams/{teamId}/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("PMD.NcssCount") // TODO: [zodac] Revisit when TeamNotFoundException is removed
    public Response getTeamHistoricStatsHourly(@PathParam("teamId") final String teamId, @PathParam("year") final String year,
                                               @PathParam("month") final String month, @PathParam("day") final String day,
                                               @Context final Request request) {
        LOGGER.debug("GET request received to show hourly TC user stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final int dayAsInt = Integer.parseInt(day);
            final int monthAsInt = Integer.parseInt(month);
            final int yearAsInt = Year.parse(year).getValue();

            final YearMonth date = YearMonth.of(yearAsInt, monthAsInt);
            if (!date.isValidDay(dayAsInt)) {
                final String errorMessage = String.format("The day '%s' is not a valid day for %s/%s", day, year, month);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }

            final ParseResult parseResult = IntegerParser.parsePositive(teamId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The team ID '%s' is not a valid format", teamId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The team ID '%s' is out of range", teamId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();

            final Optional<Team> teamOptional = businessLogic.getTeam(parsedId);
            if (teamOptional.isEmpty()) {
                LOGGER.error("No team found with ID: {}", parsedId);
                return notFound();
            }
            final Team team = teamOptional.get();

            final Collection<User> teamUsers = businessLogic.getUsersOnTeam(team);
            final List<HistoricStats> teamHourlyStats = new ArrayList<>(teamUsers.size());

            for (final User user : teamUsers) {
                LOGGER.debug("Getting historic stats for user with ID: {}", user.getId());
                final Collection<HistoricStats> dailyStats =
                    oldFacade.getHistoricStatsHourly(user.getId(), Integer.parseInt(day), Month.of(Integer.parseInt(month)), Year.parse(year));
                teamHourlyStats.addAll(dailyStats);
            }

            final Collection<HistoricStats> combinedTeamHourlyStats = HistoricStats.combine(teamHourlyStats);

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(CACHE_EXPIRATION_TIME);

            final EntityTag entityTag = new EntityTag(String.valueOf(combinedTeamHourlyStats.stream().mapToInt(HistoricStats::hashCode).sum()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);

            if (builder == null) {
                LOGGER.debug("Cached resources have changed");
                builder = okBuilder(combinedTeamHourlyStats);
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
            final String errorMessage = String.format("The month '%s' or day '%s' is not a valid format", month, day);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting team with ID: {}", teamId, e);
            return serverError();
        }
    }

    @GET
    @PermitAll
    @Path("/teams/{teamId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamHistoricStatsDaily(@PathParam("teamId") final String teamId, @PathParam("year") final String year,
                                              @PathParam("month") final String month, @Context final Request request) {
        LOGGER.debug("GET request received to show daily TC user stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final ParseResult parseResult = IntegerParser.parsePositive(teamId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The team ID '%s' is not a valid format", teamId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The team ID '%s' is out of range", teamId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();

            final Optional<Team> teamOptional = businessLogic.getTeam(parsedId);
            if (teamOptional.isEmpty()) {
                LOGGER.error("No team found with ID: {}", parsedId);
                return notFound();
            }
            final Team team = teamOptional.get();

            final Collection<User> teamUsers = businessLogic.getUsersOnTeam(team);
            final List<HistoricStats> teamDailyStats = new ArrayList<>(teamUsers.size());

            for (final User user : teamUsers) {
                LOGGER.debug("Getting historic stats for user with ID: {}", user.getId());
                final Collection<HistoricStats> dailyStats =
                    oldFacade.getHistoricStatsDaily(user.getId(), Month.of(Integer.parseInt(month)), Year.parse(year));
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
        } catch (final DateTimeException | NumberFormatException e) {
            final String errorMessage = String.format("The month '%s' is not a valid format", month);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting team with ID: {}", teamId, e);
            return serverError();
        }
    }

    @GET
    @PermitAll
    @Path("/teams/{teamId}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamHistoricStatsMonthly(@PathParam("teamId") final String teamId, @PathParam("year") final String year,
                                                @Context final Request request) {
        LOGGER.info("GET request received to show monthly TC team stats at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final ParseResult parseResult = IntegerParser.parsePositive(teamId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The team ID '%s' is not a valid format", teamId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The team ID '%s' is out of range", teamId);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();

            final Optional<Team> teamOptional = businessLogic.getTeam(parsedId);
            if (teamOptional.isEmpty()) {
                LOGGER.error("No team found with ID: {}", parsedId);
                return notFound();
            }
            final Team team = teamOptional.get();

            final Collection<User> teamUsers = businessLogic.getUsersOnTeam(team);
            final List<HistoricStats> teamMonthlyStats = new ArrayList<>(teamUsers.size());

            for (final User user : teamUsers) {
                LOGGER.debug("Getting historic stats for user with ID: {}", user.getId());
                final Collection<HistoricStats> monthlyStats = oldFacade.getHistoricStatsMonthly(user.getId(), Year.parse(year));
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
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting team with ID: {}", teamId, e);
            return serverError();
        }
    }
}
