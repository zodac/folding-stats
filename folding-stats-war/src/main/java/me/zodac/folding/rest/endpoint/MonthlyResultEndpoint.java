package me.zodac.folding.rest.endpoint;

import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.ejb.tc.UserStatsStorer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/results/")
@RequestScoped
public class MonthlyResultEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private UriInfo uriContext;

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private UserStatsStorer userStatsStorer;

    @GET
    @PermitAll
    @Path("/result/{year}/{month}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMonthlyResult(@PathParam("year") final String year,
                                     @PathParam("month") final String month) {
        LOGGER.debug("GET request received to retrieve monthly TC result at '{}'", uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final Optional<MonthlyResult> monthlyResult = businessLogic.getMonthlyResult(Month.of(Integer.parseInt(month)), Year.parse(year));
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

    @GET
    @RolesAllowed("admin")
    @Path("/manual/save/")
    public Response saveMonthlyResult() {
        LOGGER.info("GET request received to manually store monthly TC result");

        if (SystemStateManager.current().isWriteBlocked()) {
            LOGGER.warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            userStatsStorer.storeMonthlyResult();
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error manually storing TC result", e);
            return serverError();
        }
    }
}
