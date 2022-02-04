/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.rest;

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
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.FoldingRepository;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.rest.util.IdResult;
import me.zodac.folding.rest.util.IntegerParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for <code>Team Competition</code> {@link HistoricStats}.
 */
@RestController
@RequestMapping("/historic")
public class HistoricStatsEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    // Stat updates occur every hour, so we must invalidate responses every hour
    private static final int CACHE_EXPIRATION_TIME = (int) TimeUnit.HOURS.toSeconds(1);

    @Autowired
    private FoldingRepository foldingRepository;

    @Autowired
    private StatsRepository statsRepository;

    /**
     * {@link GetMapping} request to retrieve a {@link User}'s hourly {@link HistoricStats} for a single {@code day}.
     *
     * @param userId  the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param day     the {@code day} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link User}'s hourly {@link HistoricStats}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/users/{userId}/{year}/{month}/{day}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserHistoricStatsHourly(@PathVariable("userId") final String userId,
                                                        @PathVariable("year") final String year,
                                                        @PathVariable("month") final String month,
                                                        @PathVariable("day") final String day,
                                                        final HttpServletRequest request) {
        LOGGER.debug("GET request received to show hourly TC user stats at '{}'", request::getRequestURI);

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
                return idResult.failureResponse();
            }
            final int parsedId = idResult.id();

            final Optional<User> user = foldingRepository.getUserWithPasskey(parsedId);
            if (user.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final Collection<HistoricStats> historicStats = statsRepository.getHistoricStats(user.get(), Year.parse(year), Month.of(monthAsInt),
                dayAsInt);
            return cachedOk(historicStats, CACHE_EXPIRATION_TIME);
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
     * {@link GetMapping} request to retrieve a {@link User}'s daily {@link HistoricStats} for a single {@link Month}.
     *
     * @param userId  the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link User}'s daily {@link HistoricStats}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/users/{userId}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserHistoricStatsDaily(@PathVariable("userId") final String userId,
                                                       @PathVariable("year") final String year,
                                                       @PathVariable("month") final String month,
                                                       final HttpServletRequest request) {
        LOGGER.debug("GET request received to show daily TC user stats at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.failureResponse();
            }
            final int parsedId = idResult.id();

            final Optional<User> user = foldingRepository.getUserWithPasskey(parsedId);
            if (user.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final Collection<HistoricStats> historicStats =
                statsRepository.getHistoricStats(user.get(), Year.parse(year), Month.of(parseInt(month)));
            return cachedOk(historicStats, CACHE_EXPIRATION_TIME);
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
     * {@link GetMapping} request to retrieve a {@link User}'s monthly {@link HistoricStats} for a single {@link Year}.
     *
     * @param userId  the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link User}'s monthly {@link HistoricStats}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/users/{userId}/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserHistoricStatsMonthly(@PathVariable("userId") final String userId,
                                                         @PathVariable("year") final String year,
                                                         final HttpServletRequest request) {
        LOGGER.debug("GET request received to show monthly TC user stats at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.failureResponse();
            }
            final int parsedId = idResult.id();

            final Optional<User> user = foldingRepository.getUserWithPasskey(parsedId);
            if (user.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final Collection<HistoricStats> historicStats = statsRepository.getHistoricStats(user.get(), Year.parse(year));
            return cachedOk(historicStats, CACHE_EXPIRATION_TIME);
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
     * {@link GetMapping} request to retrieve a {@link Team}'s hourly {@link HistoricStats} for a single {@code day}.
     *
     * @param teamId  the ID of the {@link Team} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param day     the {@code day} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Team}'s hourly {@link HistoricStats}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/teams/{teamId}/{year}/{month}/{day}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeamHistoricStatsHourly(@PathVariable("teamId") final String teamId,
                                                        @PathVariable("year") final String year,
                                                        @PathVariable("month") final String month,
                                                        @PathVariable("day") final String day,
                                                        final HttpServletRequest request) {
        LOGGER.debug("GET request received to show hourly TC user stats at '{}'", request::getRequestURI);

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
                return idResult.failureResponse();
            }
            final int parsedId = idResult.id();

            final Optional<Team> teamOptional = foldingRepository.getTeam(parsedId);
            if (teamOptional.isEmpty()) {
                LOGGER.error("No team found with ID: {}", parsedId);
                return notFound();
            }
            final Team team = teamOptional.get();

            final Collection<User> teamUsers = foldingRepository.getUsersOnTeam(team);
            final List<HistoricStats> teamHourlyStats = new ArrayList<>(teamUsers.size());

            for (final User user : teamUsers) {
                LOGGER.debug("Getting historic stats for user with ID: {}", user.getId());
                final Collection<HistoricStats> dailyStats =
                    statsRepository.getHistoricStats(user, Year.parse(year), Month.of(monthAsInt), dayAsInt);
                teamHourlyStats.addAll(dailyStats);
            }

            final Collection<HistoricStats> combinedHistoricStats = HistoricStats.combine(teamHourlyStats);
            return cachedOk(combinedHistoricStats, CACHE_EXPIRATION_TIME);
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
     * {@link GetMapping} request to retrieve a {@link Team}'s daily {@link HistoricStats} for a single {@link Month}.
     *
     * @param teamId  the ID of the {@link Team} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Team}'s daily {@link HistoricStats}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/teams/{teamId}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeamHistoricStatsDaily(@PathVariable("teamId") final String teamId,
                                                       @PathVariable("year") final String year,
                                                       @PathVariable("month") final String month,
                                                       final HttpServletRequest request) {
        LOGGER.debug("GET request received to show daily TC user stats at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(teamId);
            if (idResult.isFailure()) {
                return idResult.failureResponse();
            }
            final int parsedId = idResult.id();

            final Optional<Team> teamOptional = foldingRepository.getTeam(parsedId);
            if (teamOptional.isEmpty()) {
                LOGGER.error("No team found with ID: {}", parsedId);
                return notFound();
            }
            final Team team = teamOptional.get();

            final Collection<User> teamUsers = foldingRepository.getUsersOnTeam(team);
            final List<HistoricStats> teamDailyStats = new ArrayList<>(teamUsers.size());

            for (final User user : teamUsers) {
                LOGGER.debug("Getting historic stats for user with ID: {}", user.getId());
                final Collection<HistoricStats> dailyStats = statsRepository.getHistoricStats(user, Year.parse(year), Month.of(parseInt(month)));
                teamDailyStats.addAll(dailyStats);
            }

            final Collection<HistoricStats> combinedHistoricStats = HistoricStats.combine(teamDailyStats);
            return cachedOk(combinedHistoricStats, CACHE_EXPIRATION_TIME);
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
     * {@link GetMapping} request to retrieve a {@link Team}'s monthly {@link HistoricStats} for a single {@link Year}.
     *
     * @param teamId  the ID of the {@link Team} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Team}'s monthly {@link HistoricStats}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/teams/{teamId}/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeamHistoricStatsMonthly(@PathVariable("teamId") final String teamId,
                                                         @PathVariable("year") final String year,
                                                         final HttpServletRequest request) {
        LOGGER.info("GET request received to show monthly TC team stats at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(teamId);
            if (idResult.isFailure()) {
                return idResult.failureResponse();
            }
            final int parsedId = idResult.id();

            final Optional<Team> teamOptional = foldingRepository.getTeam(parsedId);
            if (teamOptional.isEmpty()) {
                LOGGER.error("No team found with ID: {}", parsedId);
                return notFound();
            }
            final Team team = teamOptional.get();

            final Collection<User> teamUsers = foldingRepository.getUsersOnTeam(team);
            final List<HistoricStats> teamMonthlyStats = new ArrayList<>(teamUsers.size());

            for (final User user : teamUsers) {
                LOGGER.debug("Getting historic stats for user with ID: {}", user.getId());
                final Collection<HistoricStats> monthlyStats = statsRepository.getHistoricStats(user, Year.parse(year));
                teamMonthlyStats.addAll(monthlyStats);
            }

            final Collection<HistoricStats> combinedHistoricStats = HistoricStats.combine(teamMonthlyStats);
            return cachedOk(combinedHistoricStats, CACHE_EXPIRATION_TIME);
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