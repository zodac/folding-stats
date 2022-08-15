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

import static me.zodac.folding.rest.response.Responses.ok;

import java.time.Month;
import java.time.Year;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.tc.user.UserStatsStorer;
import me.zodac.folding.rest.util.DateDetails;
import me.zodac.folding.rest.util.ReadRequired;
import me.zodac.folding.rest.util.WriteRequired;
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
 * REST endpoints for {@code Team Competition} {@link MonthlyResult}s.
 */
@RestController
@RequestMapping("/results")
public class MonthlyResultEndpoint {

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
    public MonthlyResultEndpoint(final StatsRepository statsRepository, final UserStatsStorer userStatsStorer) {
        this.statsRepository = statsRepository;
        this.userStatsStorer = userStatsStorer;
    }

    /**
     * {@link GetMapping} request that retrieves a {@link MonthlyResult} for the given {@link Month}/{@link Year}.
     *
     * @param year    the {@link Year} of the {@link MonthlyResult}
     * @param month   the {@link Month} of the {@link MonthlyResult}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} with the {@link MonthlyResult}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/result/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMonthlyResult(@PathVariable("year") final String year,
                                              @PathVariable("month") final String month,
                                              final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to retrieve monthly TC result at '{}'", request::getRequestURI);

        final DateDetails date = DateDetails.of(year, month);
        final Optional<MonthlyResult> monthlyResult = statsRepository.getMonthlyResult(date.month(), date.year());
        return ok(monthlyResult.orElse(MonthlyResult.empty()));
    }

    /**
     * {@link PostMapping} request that performs a manual save of the current {@link MonthlyResult}.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(path = "/manual/save")
    public ResponseEntity<?> saveMonthlyResult() {
        AUDIT_LOGGER.info("GET request received to manually store monthly TC result");
        userStatsStorer.storeMonthlyResult();
        return ok();
    }
}