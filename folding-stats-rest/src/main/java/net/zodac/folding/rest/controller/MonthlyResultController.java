/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.rest.controller;

import static net.zodac.folding.rest.response.Responses.ok;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import net.zodac.folding.api.tc.result.MonthlyResult;
import net.zodac.folding.api.util.LoggerName;
import net.zodac.folding.bean.StatsRepository;
import net.zodac.folding.bean.tc.user.UserStatsStorer;
import net.zodac.folding.rest.controller.api.MonthlyResultEndpoint;
import net.zodac.folding.rest.util.ReadRequired;
import net.zodac.folding.rest.util.WriteRequired;
import net.zodac.folding.rest.util.date.DateParser;
import net.zodac.folding.rest.util.date.MonthDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implementation of {@link MonthlyResultEndpoint} REST endpoints.
 */
@RestController
@RequestMapping("/results")
public class MonthlyResultController implements MonthlyResultEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

    private final StatsRepository statsRepository;
    private final UserStatsStorer userStatsStorer;

    /**
     * {@link Autowired} constructor.
     *
     * @param statsRepository the {@link StatsRepository}
     * @param userStatsStorer the {@link UserStatsStorer}
     */
    @Autowired
    public MonthlyResultController(final StatsRepository statsRepository, final UserStatsStorer userStatsStorer) {
        this.statsRepository = statsRepository;
        this.userStatsStorer = userStatsStorer;
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/result/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MonthlyResult> getMonthlyResult(@PathVariable("year") final String year,
                                                          @PathVariable("month") final String month,
                                                          final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to retrieve monthly TC result at '{}'", request.getRequestURI());

        final MonthDetails date = DateParser.of(year, month);
        final MonthlyResult monthlyResult = statsRepository.getMonthlyResult(date.month(), date.year());
        return ok(monthlyResult);
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(path = "/manual/save")
    public ResponseEntity<Void> saveMonthlyResult() {
        AUDIT_LOGGER.info("GET request received to manually store monthly TC result");
        userStatsStorer.storeMonthlyResult();
        return ok();
    }
}
