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

import static me.zodac.folding.rest.response.Responses.cachedOk;

import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.rest.util.DateDetails;
import me.zodac.folding.rest.util.ReadRequired;
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
 * REST endpoints for {@code Team Competition} {@link HistoricStats}.
 */
@RestController
@RequestMapping("/historic")
public class HistoricStatsEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger("audit");

    // Stat updates occur every hour, so we must invalidate responses every hour
    private static final long CACHE_EXPIRATION_TIME = TimeUnit.HOURS.toSeconds(1L);

    private final FoldingRepository foldingRepository;
    private final StatsRepository statsRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param statsRepository   the {@link StatsRepository}
     */
    @Autowired
    public HistoricStatsEndpoint(final FoldingRepository foldingRepository, final StatsRepository statsRepository) {
        this.foldingRepository = foldingRepository;
        this.statsRepository = statsRepository;
    }

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
    public ResponseEntity<?> getUserHistoricStatsHourly(@PathVariable("userId") final int userId,
                                                        @PathVariable("year") final String year,
                                                        @PathVariable("month") final String month,
                                                        @PathVariable("day") final String day,
                                                        final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show hourly TC user stats at '{}'", request::getRequestURI);

        final DateDetails date = DateDetails.of(year, month, day);
        final User user = foldingRepository.getUserWithPasskey(userId);
        final Collection<HistoricStats> historicStats = statsRepository.getHistoricStats(user, date.year(), date.month(), date.day());
        return cachedOk(historicStats, CACHE_EXPIRATION_TIME);
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
    public ResponseEntity<?> getUserHistoricStatsDaily(@PathVariable("userId") final int userId,
                                                       @PathVariable("year") final String year,
                                                       @PathVariable("month") final String month,
                                                       final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show daily TC user stats at '{}'", request::getRequestURI);

        final DateDetails date = DateDetails.of(year, month);
        final User user = foldingRepository.getUserWithPasskey(userId);
        final Collection<HistoricStats> historicStats = statsRepository.getHistoricStats(user, date.year(), date.month());
        return cachedOk(historicStats, CACHE_EXPIRATION_TIME);
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
    public ResponseEntity<?> getUserHistoricStatsMonthly(@PathVariable("userId") final int userId,
                                                         @PathVariable("year") final String year,
                                                         final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show monthly TC user stats at '{}'", request::getRequestURI);

        final DateDetails date = DateDetails.of(year);
        final User user = foldingRepository.getUserWithPasskey(userId);
        final Collection<HistoricStats> historicStats = statsRepository.getHistoricStats(user, date.year());
        return cachedOk(historicStats, CACHE_EXPIRATION_TIME);
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
    public ResponseEntity<?> getTeamHistoricStatsHourly(@PathVariable("teamId") final int teamId,
                                                        @PathVariable("year") final String year,
                                                        @PathVariable("month") final String month,
                                                        @PathVariable("day") final String day,
                                                        final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show hourly TC user stats at '{}'", request::getRequestURI);

        final DateDetails date = DateDetails.of(year, month, day);
        final Team team = foldingRepository.getTeam(teamId);

        final Collection<User> teamUsers = foldingRepository.getUsersOnTeam(team);
        final Collection<HistoricStats> teamHourlyStats = new ArrayList<>(teamUsers.size());

        for (final User user : teamUsers) {
            AUDIT_LOGGER.debug("Getting historic stats for user with ID: {}", user.id());
            final Collection<HistoricStats> dailyStats = statsRepository.getHistoricStats(user, date.year(), date.month(), date.day());
            teamHourlyStats.addAll(dailyStats);
        }

        final Collection<HistoricStats> combinedHistoricStats = HistoricStats.combine(teamHourlyStats);
        return cachedOk(combinedHistoricStats, CACHE_EXPIRATION_TIME);
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
    public ResponseEntity<?> getTeamHistoricStatsDaily(@PathVariable("teamId") final int teamId,
                                                       @PathVariable("year") final String year,
                                                       @PathVariable("month") final String month,
                                                       final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show daily TC user stats at '{}'", request::getRequestURI);

        final DateDetails date = DateDetails.of(year, month);
        final Team team = foldingRepository.getTeam(teamId);

        final Collection<User> teamUsers = foldingRepository.getUsersOnTeam(team);
        final Collection<HistoricStats> teamDailyStats = new ArrayList<>(teamUsers.size());

        for (final User user : teamUsers) {
            AUDIT_LOGGER.debug("Getting historic stats for user with ID: {}", user.id());
            final Collection<HistoricStats> dailyStats = statsRepository.getHistoricStats(user, date.year(), date.month());
            teamDailyStats.addAll(dailyStats);
        }

        final Collection<HistoricStats> combinedHistoricStats = HistoricStats.combine(teamDailyStats);
        return cachedOk(combinedHistoricStats, CACHE_EXPIRATION_TIME);
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
    public ResponseEntity<?> getTeamHistoricStatsMonthly(@PathVariable("teamId") final int teamId,
                                                         @PathVariable("year") final String year,
                                                         final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show monthly TC team stats at '{}'", request::getRequestURI);

        final DateDetails date = DateDetails.of(year);
        final Team team = foldingRepository.getTeam(teamId);

        final Collection<User> teamUsers = foldingRepository.getUsersOnTeam(team);
        final Collection<HistoricStats> teamMonthlyStats = new ArrayList<>(teamUsers.size());

        for (final User user : teamUsers) {
            AUDIT_LOGGER.debug("Getting historic stats for user with ID: {}", user.id());
            final Collection<HistoricStats> monthlyStats = statsRepository.getHistoricStats(user, date.year());
            teamMonthlyStats.addAll(monthlyStats);
        }

        final Collection<HistoricStats> combinedHistoricStats = HistoricStats.combine(teamMonthlyStats);
        return cachedOk(combinedHistoricStats, CACHE_EXPIRATION_TIME);
    }
}