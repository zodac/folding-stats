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

package me.zodac.folding.rest.endpoint;

import static me.zodac.folding.api.util.DateTimeUtils.untilNextMonthUtc;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.nullRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.endpoint.util.IdResult;
import me.zodac.folding.rest.endpoint.util.IntegerParser;
import me.zodac.folding.rest.response.BatchCreateResponse;
import me.zodac.folding.rest.validator.HardwareValidator;
import me.zodac.folding.rest.validator.ValidationFailure;
import me.zodac.folding.rest.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * REST endpoints for <code>Team Competition</code> {@link Hardware}s.
 *
 * @see me.zodac.folding.client.java.request.HardwareRequestSender
 * @see me.zodac.folding.client.java.response.HardwareResponseParser
 */
@Path("/hardware/")
@RequestScoped
public class HardwareEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private UriInfo uriContext;

    @EJB
    private FoldingStatsCore foldingStatsCore;

    /**
     * {@link POST} request to create a {@link Hardware} based on the input request.
     *
     * @param hardwareRequest the {@link HardwareRequest} to create a {@link Hardware}
     * @return {@link Response.Status#CREATED} containing the created {@link Hardware}
     */
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(final HardwareRequest hardwareRequest) {
        LOGGER.debug("POST request received to create hardware at '{}' with request: {}", uriContext::getAbsolutePath, () -> hardwareRequest);

        final ValidationResult<Hardware> validationResult = validateCreate(hardwareRequest);
        if (validationResult.isFailure()) {
            return validationResult.getFailureResponse();
        }
        final Hardware validatedHardware = validationResult.getOutput();

        try {
            final Hardware elementWithId = foldingStatsCore.createHardware(validatedHardware);

            final UriBuilder elementLocationBuilder = uriContext
                .getRequestUriBuilder()
                .path(String.valueOf(elementWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Created hardware with ID {}", elementWithId.getId());
            return created(elementWithId, elementLocationBuilder);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating hardware: {}", hardwareRequest, e);
            return serverError();
        }
    }

    /**
     * {@link POST} request to create a {@link Collection} of {@link Hardware}s based on the input requests.
     *
     * <p>
     * Will perform a best-effort attempt to create all {@link Hardware}s and will return a response with successful and unsuccessful results.
     *
     * @param hardwareRequests the {@link HardwareRequest}s to create {@link Hardware}s
     * @return {@link Response.Status#OK} containing the created/failed {@link Hardware}s
     */
    @POST
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatchOf(final Collection<HardwareRequest> hardwareRequests) {
        LOGGER.debug("POST request received to create {} hardwares at '{}' with request: {}", hardwareRequests::size, uriContext::getAbsolutePath,
            () -> hardwareRequests);

        final Collection<Hardware> validHardwares = new ArrayList<>(hardwareRequests.size() / 2);
        final Collection<ValidationFailure> failedValidationResponses = new ArrayList<>(hardwareRequests.size() / 2);

        for (final HardwareRequest hardwareRequest : hardwareRequests) {
            final ValidationResult<Hardware> validationResult = validateCreate(hardwareRequest);

            if (validationResult.isFailure()) {
                LOGGER.error("Found validation error for {}: {}", hardwareRequest, validationResult);
                failedValidationResponses.add(validationResult.getValidationFailure());
            } else {
                validHardwares.add(validationResult.getOutput());
            }
        }

        if (validHardwares.isEmpty()) {
            LOGGER.error("All hardwares contain validation errors: {}", failedValidationResponses);
            return badRequest(failedValidationResponses);
        }

        final List<Object> successful = new ArrayList<>();
        final List<Object> unsuccessful = new ArrayList<>(failedValidationResponses);

        for (final Hardware validHardware : validHardwares) {
            try {
                final Hardware hardwareWithId = foldingStatsCore.createHardware(validHardware);
                successful.add(hardwareWithId);
            } catch (final Exception e) {
                LOGGER.error("Unexpected error creating hardware: {}", validHardware, e);
                unsuccessful.add(validHardware);
            }
        }

        final BatchCreateResponse batchCreateResponse = BatchCreateResponse.create(successful, unsuccessful);

        if (successful.isEmpty()) {
            return badRequest(batchCreateResponse);
        }

        if (!unsuccessful.isEmpty()) {
            LOGGER.error("{} hardwares successfully created, {} hardwares unsuccessful", successful.size(), unsuccessful.size());
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok(batchCreateResponse);
        }

        LOGGER.info("{} hardwares successfully created", successful.size());
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        return ok(batchCreateResponse.getSuccessful());
    }

    /**
     * {@link GET} request to retrieve all {@link Hardware}s.
     *
     * @param request the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Hardware}s
     */
    @GET
    @ReadRequired
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@Context final Request request) {
        LOGGER.debug("GET request received for all hardwares at '{}'", uriContext::getAbsolutePath);

        try {
            final Collection<Hardware> elements = foldingStatsCore.getAllHardware();
            return cachedOk(elements, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all hardwares", e);
            return serverError();
        }
    }

    /**
     * {@link GET} request to retrieve a {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @param request    the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Hardware}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/{hardwareId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("hardwareId") final String hardwareId, @Context final Request request) {
        LOGGER.debug("GET request for hardware received at '{}'", uriContext::getAbsolutePath);

        try {
            final IdResult idResult = IntegerParser.parsePositive(hardwareId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<Hardware> optionalElement = foldingStatsCore.getHardware(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No hardware found with ID {}", hardwareId);
                return notFound();
            }

            final Hardware element = optionalElement.get();
            return cachedOk(element, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting hardware with ID: {}", hardwareId, e);
            return serverError();
        }
    }

    /**
     * {@link GET} request to retrieve a {@link Hardware}.
     *
     * @param hardwareName the {@code hardwareName} of the {@link Hardware} to retrieve
     * @param request      the {@link Request}, to be used for {@link javax.ws.rs.core.CacheControl}
     * @return {@link Response.Status#OK} containing the {@link Hardware}
     */
    @GET
    @ReadRequired
    @PermitAll
    @Path("/fields")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByHardwareName(@QueryParam("hardwareName") final String hardwareName, @Context final Request request) {
        LOGGER.debug("GET request for hardware received at '{}'", uriContext::getAbsolutePath);

        try {
            if (StringUtils.isBlank(hardwareName)) {
                final String errorMessage = String.format("Input 'hardwareName' must not be blank: '%s'", hardwareName);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }

            final Optional<Hardware> optionalHardware = foldingStatsCore.getAllHardware()
                .stream()
                .filter(hardware -> hardware.getHardwareName().equalsIgnoreCase(hardwareName))
                .findAny();

            if (optionalHardware.isEmpty()) {
                LOGGER.error("No hardware found with 'hardwareName' '{}'", hardwareName);
                return notFound();
            }

            final Hardware element = optionalHardware.get();
            return cachedOk(element, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting hardware with 'hardwareName': '{}'", hardwareName, e);
            return serverError();
        }
    }

    /**
     * {@link PUT} request to update an existing {@link Hardware} based on the input request.
     *
     * @param hardwareId      the ID of the {@link Hardware} to be updated
     * @param hardwareRequest the {@link HardwareRequest} to update a {@link Hardware}
     * @return {@link Response.Status#OK} containing the updated {@link Hardware}
     */
    @PUT
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{hardwareId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateById(@PathParam("hardwareId") final String hardwareId, final HardwareRequest hardwareRequest) {
        LOGGER.debug("PUT request for hardware received at '{}'", uriContext::getAbsolutePath);

        if (hardwareRequest == null) {
            LOGGER.error("No payload provided");
            return nullRequest();
        }

        try {
            final IdResult idResult = IntegerParser.parsePositive(hardwareId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<Hardware> optionalElement = foldingStatsCore.getHardware(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No hardware found with ID {}", hardwareId);
                return notFound();
            }
            final Hardware existingHardware = optionalElement.get();

            if (existingHardware.isEqualRequest(hardwareRequest)) {
                LOGGER.debug("No change necessary");
                return ok(existingHardware);
            }

            final ValidationResult<Hardware> validationResult = validateUpdate(hardwareRequest, existingHardware);
            if (validationResult.isFailure()) {
                return validationResult.getFailureResponse();
            }
            final Hardware validatedHardware = validationResult.getOutput();

            // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
            final Hardware hardwareWithId = Hardware.updateWithId(existingHardware.getId(), validatedHardware);
            final Hardware updatedHardwareWithId = foldingStatsCore.updateHardware(hardwareWithId, existingHardware);

            final UriBuilder elementLocationBuilder = uriContext
                .getRequestUriBuilder()
                .path(String.valueOf(updatedHardwareWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Updated hardware with ID {}", updatedHardwareWithId.getId());
            return ok(updatedHardwareWithId, elementLocationBuilder);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating hardware with ID: {}", hardwareId, e);
            return serverError();
        }
    }

    /**
     * {@link DELETE} request to delete an existing {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to be deleted
     * @return {@link Response.Status#OK}
     */
    @DELETE
    @WriteRequired
    @RolesAllowed("admin")
    @Path("/{hardwareId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteById(@PathParam("hardwareId") final String hardwareId) {
        LOGGER.debug("DELETE request for hardware received at '{}'", uriContext::getAbsolutePath);

        try {
            final IdResult idResult = IntegerParser.parsePositive(hardwareId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<Hardware> optionalElement = foldingStatsCore.getHardware(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No hardware found with ID {}", hardwareId);
                return notFound();
            }
            final Hardware hardware = optionalElement.get();

            final ValidationResult<Hardware> validationResult = validateDelete(hardware);
            if (validationResult.isFailure()) {
                return validationResult.getFailureResponse();
            }
            final Hardware validatedHardware = validationResult.getOutput();

            foldingStatsCore.deleteHardware(validatedHardware);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Deleted hardware with ID {}", hardwareId);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error deleting hardware with ID: {}", hardwareId, e);
            return serverError();
        }
    }

    private ValidationResult<Hardware> validateCreate(final HardwareRequest hardwareRequest) {
        return HardwareValidator.validateCreate(hardwareRequest, foldingStatsCore.getAllHardware());
    }

    private ValidationResult<Hardware> validateUpdate(final HardwareRequest hardwareRequest, final Hardware existingHardware) {
        return HardwareValidator.validateUpdate(hardwareRequest, existingHardware, foldingStatsCore.getAllHardware());
    }

    private ValidationResult<Hardware> validateDelete(final Hardware hardware) {
        return HardwareValidator.validateDelete(hardware, foldingStatsCore.getAllUsersWithoutPasskeys());
    }
}
