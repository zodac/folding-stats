/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.rest.controller;

import static me.zodac.folding.rest.response.Responses.cachedOk;

import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.rest.controller.api.HistoricStatsEndpoint;
import me.zodac.folding.rest.util.ReadRequired;
import me.zodac.folding.rest.util.date.DateDetails;
import me.zodac.folding.rest.util.date.DateParser;
import me.zodac.folding.rest.util.date.MonthDetails;
import me.zodac.folding.rest.util.date.YearDetails;
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
 * Implementation of {@link HistoricStatsEndpoint} REST endpoints.
 */
@RestController
@RequestMapping("/historic")
public class HistoricStatsController implements HistoricStatsEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

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
    public HistoricStatsController(final FoldingRepository foldingRepository, final StatsRepository statsRepository) {
        this.foldingRepository = foldingRepository;
        this.statsRepository = statsRepository;
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/users/{userId}/{year}/{month}/{day}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<HistoricStats>> getUserHistoricStatsHourly(@PathVariable("userId") final int userId,
                                                                                @PathVariable("year") final String year,
                                                                                @PathVariable("month") final String month,
                                                                                @PathVariable("day") final String day,
                                                                                final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show hourly TC user stats at '{}'", request.getRequestURI());

        final DateDetails date = DateParser.of(year, month, day);
        final User user = foldingRepository.getUserWithPasskey(userId);
        final Collection<HistoricStats> historicStats = statsRepository.getHistoricStats(user, date.year(), date.month(), date.day());
        return cachedOk(historicStats, CACHE_EXPIRATION_TIME);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/users/{userId}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<HistoricStats>> getUserHistoricStatsDaily(@PathVariable("userId") final int userId,
                                                                               @PathVariable("year") final String year,
                                                                               @PathVariable("month") final String month,
                                                                               final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show daily TC user stats at '{}'", request.getRequestURI());

        final MonthDetails date = DateParser.of(year, month);
        final User user = foldingRepository.getUserWithPasskey(userId);
        final Collection<HistoricStats> historicStats = statsRepository.getHistoricStats(user, date.year(), date.month());
        return cachedOk(historicStats, CACHE_EXPIRATION_TIME);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/users/{userId}/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<HistoricStats>> getUserHistoricStatsMonthly(@PathVariable("userId") final int userId,
                                                                                 @PathVariable("year") final String year,
                                                                                 final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show monthly TC user stats at '{}'", request.getRequestURI());

        final YearDetails date = DateParser.of(year);
        final User user = foldingRepository.getUserWithPasskey(userId);
        final Collection<HistoricStats> historicStats = statsRepository.getHistoricStats(user, date.year());
        return cachedOk(historicStats, CACHE_EXPIRATION_TIME);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/teams/{teamId}/{year}/{month}/{day}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<HistoricStats>> getTeamHistoricStatsHourly(@PathVariable("teamId") final int teamId,
                                                                                @PathVariable("year") final String year,
                                                                                @PathVariable("month") final String month,
                                                                                @PathVariable("day") final String day,
                                                                                final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show hourly TC user stats at '{}'", request.getRequestURI());

        final DateDetails date = DateParser.of(year, month, day);
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

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/teams/{teamId}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<HistoricStats>> getTeamHistoricStatsDaily(@PathVariable("teamId") final int teamId,
                                                                               @PathVariable("year") final String year,
                                                                               @PathVariable("month") final String month,
                                                                               final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show daily TC user stats at '{}'", request.getRequestURI());

        final MonthDetails date = DateParser.of(year, month);
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

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/teams/{teamId}/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<HistoricStats>> getTeamHistoricStatsMonthly(@PathVariable("teamId") final int teamId,
                                                                                 @PathVariable("year") final String year,
                                                                                 final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show monthly TC team stats at '{}'", request.getRequestURI());

        final YearDetails date = DateParser.of(year);
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
