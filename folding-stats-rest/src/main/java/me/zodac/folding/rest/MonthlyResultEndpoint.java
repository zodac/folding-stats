/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

import java.time.DateTimeException;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.tc.user.UserStatsStorer;
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
 * REST endpoints for <code>Team Competition</code> {@link MonthlyResult}s.
 */
@RestController
@RequestMapping("/results")
public class MonthlyResultEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private UserStatsStorer userStatsStorer;

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
        LOGGER.debug("GET request received to retrieve monthly TC result at '{}'", request::getRequestURI);

        try {
            final Optional<MonthlyResult> monthlyResult = statsRepository.getMonthlyResult(Month.of(Integer.parseInt(month)), Year.parse(year));
            return ok(monthlyResult.orElse(MonthlyResult.empty()));
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
            final String errorMessage = String.format("The year '%s' or month '%s' is not a valid format", year, month);
            LOGGER.debug(errorMessage, e);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC result", e);
            return serverError();
        }
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
        LOGGER.info("GET request received to manually store monthly TC result");

        try {
            userStatsStorer.storeMonthlyResult();
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error manually storing TC result", e);
            return serverError();
        }
    }
}