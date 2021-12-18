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

import static me.zodac.folding.api.util.DateTimeUtils.untilNextMonthUtc;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.nullRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.validation.HardwareValidator;
import me.zodac.folding.api.tc.validation.ValidationResult;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.response.Responses;
import me.zodac.folding.rest.util.IdResult;
import me.zodac.folding.rest.util.IntegerParser;
import me.zodac.folding.rest.util.ValidationFailureResponseMapper;
import me.zodac.folding.service.FoldingStatsService;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for <code>Team Competition</code> {@link Hardware}s.
 */
@RestController
@RequestMapping("/hardware")
public class HardwareEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private FoldingStatsService foldingStatsService;

    /**
     * {@link PostMapping} request to create a {@link Hardware} based on the input request.
     *
     * @param hardwareRequest the {@link HardwareRequest} to create a {@link Hardware}
     * @return {@link Responses#created(Object, int)} containing the created {@link Hardware}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody final HardwareRequest hardwareRequest, final HttpServletRequest request) {
        LOGGER.info("POST request received to create hardware at '{}' with request: {}", request::getRequestURI, () -> hardwareRequest);

        final ValidationResult<Hardware> validationResult = validateCreate(hardwareRequest);
        if (validationResult.isFailure()) {
            return ValidationFailureResponseMapper.map(validationResult);
        }
        final Hardware validatedHardware = validationResult.getOutput();

        try {
            final Hardware elementWithId = foldingStatsService.createHardware(validatedHardware);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Created hardware with ID {}", elementWithId.getId());

            return created(elementWithId, elementWithId.getId());
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating hardware: {}", hardwareRequest, e);
            return serverError();
        }
    }

    /**
     * {@link GetMapping} request to retrieve all {@link Hardware}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link Responses#cachedOk(Collection, long)} containing the {@link Hardware}s
     */
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(final HttpServletRequest request) {
        LOGGER.debug("GET request received for all hardwares at '{}'", request::getRequestURI);

        try {
            final Collection<Hardware> elements = foldingStatsService.getAllHardware();
            return cachedOk(elements, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all hardwares", e);
            return serverError();
        }
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Hardware} by {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @param request    the {@link HttpServletRequest}
     * @return {@link Responses#cachedOk(Object, long)} containing the {@link Hardware}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{hardwareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable("hardwareId") final String hardwareId, final HttpServletRequest request) {
        LOGGER.debug("GET request for hardware received at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(hardwareId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<Hardware> optionalElement = foldingStatsService.getHardware(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No hardware found with ID {}", hardwareId);
                return notFound();
            }

            final Hardware element = optionalElement.get();
            return cachedOk(element, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting hardware with ID: {}", hardwareId, e);
            return serverError();
        }
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Hardware} by {@code hardwareName}.
     *
     * @param hardwareName the {@code hardwareName} of the {@link Hardware} to retrieve
     * @param request      the {@link HttpServletRequest}
     * @return {@link Responses#cachedOk(Object, long)} containing the {@link Hardware}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByHardwareName(@RequestParam("hardwareName") final String hardwareName, final HttpServletRequest request) {
        LOGGER.debug("GET request for hardware received at '{}'", request::getRequestURI);

        try {
            if (StringUtils.isBlank(hardwareName)) {
                final String errorMessage = String.format("Input 'hardwareName' must not be blank: '%s'", hardwareName);
                LOGGER.error(errorMessage);
                return badRequest(errorMessage);
            }

            final Optional<Hardware> optionalHardware = foldingStatsService.getAllHardware()
                .stream()
                .filter(hardware -> hardware.getHardwareName().equalsIgnoreCase(hardwareName))
                .findAny();

            if (optionalHardware.isEmpty()) {
                LOGGER.error("No hardware found with 'hardwareName' '{}'", hardwareName);
                return notFound();
            }

            final Hardware element = optionalHardware.get();
            return cachedOk(element, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting hardware with 'hardwareName': '{}'", hardwareName, e);
            return serverError();
        }
    }

    /**
     * {@link PutMapping} request to update an existing {@link Hardware} based on the input request.
     *
     * @param hardwareId      the ID of the {@link Hardware} to be updated
     * @param hardwareRequest the {@link HardwareRequest} to update a {@link Hardware}
     * @return {@link Responses#ok(Object, int)} containing the updated {@link Hardware}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PutMapping(path = "/{hardwareId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateById(@PathVariable("hardwareId") final String hardwareId,
                                        @RequestBody final HardwareRequest hardwareRequest,
                                        final HttpServletRequest request) {
        LOGGER.debug("PUT request for hardware received at '{}'", request::getRequestURI);

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

            final Optional<Hardware> optionalElement = foldingStatsService.getHardware(parsedId);
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
                return ValidationFailureResponseMapper.map(validationResult);
            }
            final Hardware validatedHardware = validationResult.getOutput();

            // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
            final Hardware hardwareWithId = Hardware.updateWithId(existingHardware.getId(), validatedHardware);
            final Hardware updatedHardwareWithId = foldingStatsService.updateHardware(hardwareWithId, existingHardware);

            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Updated hardware with ID {}", updatedHardwareWithId.getId());
            return ok(updatedHardwareWithId, updatedHardwareWithId.getId());
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating hardware with ID: {}", hardwareId, e);
            return serverError();
        }
    }

    /**
     * {@link DeleteMapping} request to delete an existing {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to be deleted
     * @return {@link Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @DeleteMapping(path = "/{hardwareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteById(@PathVariable("hardwareId") final String hardwareId, final HttpServletRequest request) {
        LOGGER.debug("DELETE request for hardware received at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(hardwareId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<Hardware> optionalElement = foldingStatsService.getHardware(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No hardware found with ID {}", hardwareId);
                return notFound();
            }
            final Hardware hardware = optionalElement.get();

            final ValidationResult<Hardware> validationResult = validateDelete(hardware);
            if (validationResult.isFailure()) {
                return ValidationFailureResponseMapper.map(validationResult);
            }
            final Hardware validatedHardware = validationResult.getOutput();

            foldingStatsService.deleteHardware(validatedHardware);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Deleted hardware with ID {}", hardwareId);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error deleting hardware with ID: {}", hardwareId, e);
            return serverError();
        }
    }

    private ValidationResult<Hardware> validateCreate(final HardwareRequest hardwareRequest) {
        return HardwareValidator.validateCreate(hardwareRequest, foldingStatsService.getAllHardware());
    }

    private ValidationResult<Hardware> validateUpdate(final HardwareRequest hardwareRequest, final Hardware existingHardware) {
        return HardwareValidator.validateUpdate(hardwareRequest, existingHardware, foldingStatsService.getAllHardware());
    }

    private ValidationResult<Hardware> validateDelete(final Hardware hardware) {
        return ValidationResult.successful(hardware);
//        return HardwareValidator.validateDelete(hardware, foldingStatsService.getAllUsersWithoutPasskeys());
    }
}