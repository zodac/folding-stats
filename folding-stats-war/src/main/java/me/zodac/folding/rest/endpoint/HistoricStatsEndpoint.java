package me.zodac.folding.rest.endpoint;

import static java.lang.Integer.parseInt;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.serverError;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.rest.endpoint.util.IdResult;
import me.zodac.folding.rest.endpoint.util.IntegerParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST endpoints for <code>Team Competition</code> {@link HistoricStats}.
 *
 * @see me.zodac.folding.client.java.request.HistoricStatsRequestSender
 * @see me.zodac.folding.client.java.response.HistoricStatsResponseParser
 */
// TODO: [zodac] Verify that all places that return a HTTP response also log something (after removing AbstractCrudEndpoint)
@Path("/historic/")
@RequestScoped
public class HistoricStatsEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    // Stat updates occur every hour, so we must invalidate responses every hour
    private static final int CACHE_EXPIRATION_TIME = (int) TimeUnit.HOURS.toSeconds(1);

    @Context
    private UriInfo uriContext;

    @EJB
    private BusinessLogic businessLogic;

    /**
     * {@link GET} request to retrieve a {@link User}'s hourly {@link HistoricStats} for a single {@code day}.
     *
     * @param userId  the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param day     the {@code day} of the {@link HistoricStats}
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link User}'s hourly {@link HistoricStats}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/users/{userId}/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserHistoricStatsHourly(@PathParam("userId") final String userId,
                                               @PathParam("year") final String year,
                                               @PathParam("month") final String month,
                                               @PathParam("day") final String day,
                                               @Context final Request request) {
        LOGGER.debug("GET request received to show hourly TC user stats at '{}'", uriContext.getAbsolutePath());

        try {
            final int dayAsInt = parseInt(day);
            final int monthAsInt = parseInt(month);
            final int yearAsInt = Year.parse(year).getValue();

            final YearMonth date = YearMonth.of(yearAsInt, monthAsInt);
            if (!date.isValidDay(dayAsInt)) {
                final String errorMessage = String.format("The day '%s' is not a valid day for %s/%s", day, year, month);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }

            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> user = businessLogic.getUserWithPasskey(parsedId);
            if (user.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final Collection<HistoricStats> historicStats = businessLogic.getHistoricStats(user.get(), Year.parse(year), Month.of(monthAsInt),
                dayAsInt);
            return cachedOk(historicStats, request, CACHE_EXPIRATION_TIME);
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

    /**
     * {@link GET} request to retrieve a {@link User}'s daily {@link HistoricStats} for a single {@link Month}.
     *
     * @param userId  the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link User}'s daily {@link HistoricStats}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/users/{userId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserHistoricStatsDaily(@PathParam("userId") final String userId,
                                              @PathParam("year") final String year,
                                              @PathParam("month") final String month,
                                              @Context final Request request) {
        LOGGER.debug("GET request received to show daily TC user stats at '{}'", uriContext.getAbsolutePath());

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> user = businessLogic.getUserWithPasskey(parsedId);
            if (user.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final Collection<HistoricStats> historicStats = businessLogic.getHistoricStats(user.get(), Year.parse(year), Month.of(parseInt(month)));
            return cachedOk(historicStats, request, CACHE_EXPIRATION_TIME);
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

    /**
     * {@link GET} request to retrieve a {@link User}'s monthly {@link HistoricStats} for a single {@link Year}.
     *
     * @param userId  the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link User}'s monthly {@link HistoricStats}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/users/{userId}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserHistoricStatsMonthly(@PathParam("userId") final String userId,
                                                @PathParam("year") final String year,
                                                @Context final Request request) {
        LOGGER.debug("GET request received to show monthly TC user stats at '{}'", uriContext.getAbsolutePath());

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> user = businessLogic.getUserWithPasskey(parsedId);
            if (user.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final Collection<HistoricStats> historicStats = businessLogic.getHistoricStats(user.get(), Year.parse(year));
            return cachedOk(historicStats, request, CACHE_EXPIRATION_TIME);
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

    /**
     * {@link GET} request to retrieve a {@link Team}'s hourly {@link HistoricStats} for a single {@code day}.
     *
     * @param teamId  the ID of the {@link Team} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param day     the {@code day} of the {@link HistoricStats}
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Team}'s hourly {@link HistoricStats}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/teams/{teamId}/{year}/{month}/{day}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamHistoricStatsHourly(@PathParam("teamId") final String teamId,
                                               @PathParam("year") final String year,
                                               @PathParam("month") final String month,
                                               @PathParam("day") final String day,
                                               @Context final Request request) {
        LOGGER.debug("GET request received to show hourly TC user stats at '{}'", uriContext.getAbsolutePath());

        try {
            final int dayAsInt = parseInt(day);
            final int monthAsInt = parseInt(month);
            final int yearAsInt = Year.parse(year).getValue();

            final YearMonth date = YearMonth.of(yearAsInt, monthAsInt);
            if (!date.isValidDay(dayAsInt)) {
                final String errorMessage = String.format("The day '%s' is not a valid day for %s/%s", day, year, month);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }

            final IdResult idResult = IntegerParser.parsePositive(teamId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

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
                final Collection<HistoricStats> dailyStats = businessLogic.getHistoricStats(user, Year.parse(year), Month.of(monthAsInt), dayAsInt);
                teamHourlyStats.addAll(dailyStats);
            }

            final Collection<HistoricStats> combinedHistoricStats = HistoricStats.combine(teamHourlyStats);
            return cachedOk(combinedHistoricStats, request, CACHE_EXPIRATION_TIME);
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

    /**
     * {@link GET} request to retrieve a {@link Team}'s daily {@link HistoricStats} for a single {@link Month}.
     *
     * @param teamId  the ID of the {@link Team} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Team}'s daily {@link HistoricStats}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/teams/{teamId}/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamHistoricStatsDaily(@PathParam("teamId") final String teamId,
                                              @PathParam("year") final String year,
                                              @PathParam("month") final String month,
                                              @Context final Request request) {
        LOGGER.debug("GET request received to show daily TC user stats at '{}'", uriContext.getAbsolutePath());

        try {
            final IdResult idResult = IntegerParser.parsePositive(teamId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

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
                final Collection<HistoricStats> dailyStats = businessLogic.getHistoricStats(user, Year.parse(year), Month.of(parseInt(month)));
                teamDailyStats.addAll(dailyStats);
            }

            final Collection<HistoricStats> combinedHistoricStats = HistoricStats.combine(teamDailyStats);
            return cachedOk(combinedHistoricStats, request, CACHE_EXPIRATION_TIME);
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

    /**
     * {@link GET} request to retrieve a {@link Team}'s monthly {@link HistoricStats} for a single {@link Year}.
     *
     * @param teamId  the ID of the {@link Team} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Team}'s monthly {@link HistoricStats}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/teams/{teamId}/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeamHistoricStatsMonthly(@PathParam("teamId") final String teamId,
                                                @PathParam("year") final String year,
                                                @Context final Request request) {
        LOGGER.info("GET request received to show monthly TC team stats at '{}'", uriContext.getAbsolutePath());

        try {
            final IdResult idResult = IntegerParser.parsePositive(teamId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

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
                final Collection<HistoricStats> monthlyStats = businessLogic.getHistoricStats(user, Year.parse(year));
                teamMonthlyStats.addAll(monthlyStats);
            }

            final Collection<HistoricStats> combinedHistoricStats = HistoricStats.combine(teamMonthlyStats);
            return cachedOk(combinedHistoricStats, request, CACHE_EXPIRATION_TIME);
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
