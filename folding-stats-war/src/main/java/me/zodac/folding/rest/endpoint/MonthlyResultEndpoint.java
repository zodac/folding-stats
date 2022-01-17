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

package me.zodac.folding.rest.endpoint;

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
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import me.zodac.folding.ejb.tc.user.UserStatsStorer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST endpoints for <code>Team Competition</code> {@link MonthlyResult}s.
 *
 * @see me.zodac.folding.client.java.request.MonthlyResultRequestSender
 * @see me.zodac.folding.client.java.response.MonthlyResultResponseParser
 */
@Path("/results/")
@RequestScoped
public class MonthlyResultEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private UriInfo uriContext;

    @EJB
    private FoldingStatsCore foldingStatsCore;

    @EJB
    private UserStatsStorer userStatsStorer;

    /**
     * {@link GET} request that retrieves a {@link MonthlyResult} for the given {@link Month}/{@link Year}.
     *
     * @param year  the {@link Year} of the {@link MonthlyResult}
     * @param month the {@link Month} of the {@link MonthlyResult}
     * @return {@link Response.Status#OK} with the {@link MonthlyResult}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/result/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMonthlyResult(@PathParam("year") final String year,
                                     @PathParam("month") final String month) {
        LOGGER.debug("GET request received to retrieve monthly TC result at '{}'", uriContext.getAbsolutePath());

        try {
            final Optional<MonthlyResult> monthlyResult = foldingStatsCore.getMonthlyResult(Month.of(Integer.parseInt(month)), Year.parse(year));
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
     * {@link POST} request that performs a manual save of the current {@link MonthlyResult}.
     *
     * @return {@link Response.Status#OK}
     */
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/manual/save/")
    public Response saveMonthlyResult() {
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
