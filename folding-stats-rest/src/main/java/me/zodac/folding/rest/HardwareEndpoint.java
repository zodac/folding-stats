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
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.validation.HardwareValidator;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.exception.NotFoundException;
import me.zodac.folding.rest.util.ReadRequired;
import me.zodac.folding.rest.util.WriteRequired;
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
 * REST endpoints for {@code Team Competition} {@link Hardware}s.
 */
@Tag(name = "Hardware Endpoints", description = "REST endpoints to create, read, update and delete hardware on the system")
@RestController
@RequestMapping("/hardware")
// TODO: Extract all entry logging to decorator?
public class HardwareEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

    private final HardwareValidator hardwareValidator;
    private final FoldingRepository foldingRepository;

    // Prometheus counters
    private final Counter hardwareCreates;
    private final Counter hardwareUpdates;
    private final Counter hardwareDeletes;

    /**
     * {@link Autowired} constructor to inject {@link MeterRegistry} and configure Prometheus {@link Counter}s.
     *
     * @param hardwareValidator the {@link HardwareValidator}
     * @param foldingRepository the {@link FoldingRepository}
     * @param registry          the Prometheus {@link MeterRegistry}
     */
    @Autowired
    public HardwareEndpoint(final HardwareValidator hardwareValidator, final FoldingRepository foldingRepository, final MeterRegistry registry) {
        this.hardwareValidator = hardwareValidator;
        this.foldingRepository = foldingRepository;

        hardwareCreates = Counter.builder("hardware_create_counter")
            .description("Number of Hardware creations through the REST endpoint")
            .register(registry);
        hardwareUpdates = Counter.builder("hardware_update_counter")
            .description("Number of Hardware updates through the REST endpoint")
            .register(registry);
        hardwareDeletes = Counter.builder("hardware_delete_counter")
            .description("Number of Hardware deletions through the REST endpoint")
            .register(registry);
    }

    /**
     * {@link PostMapping} request to create a {@link Hardware} based on the input request.
     *
     * @param hardwareRequest the {@link HardwareRequest} to create a {@link Hardware}
     * @param request         the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#created(Object, int)} containing the created {@link Hardware}
     */
    @Operation(summary = "Create a hardware", security = @SecurityRequirement(name = "basicAuthentication"))
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Hardware has been created",
            headers = @Header(
                name = "location",
                description = "The URL for the created hardware"
            ),
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Hardware.class),
                examples = @ExampleObject("""
                    {
                        "id": 1,
                        "hardwareName": "Hardware1",
                        "displayName": "Hardware1",
                        "hardwareMake": "NVIDIA",
                        "hardwareType": "GPU",
                        "multiplier": 21.33,
                        "averagePpd": 1
                    }""")
            )),
        @ApiResponse(
            responseCode = "400",
            description = "The given hardware payload is invalid",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "invalidObject": {
                            "id": 1,
                            "hardwareName": " ",
                            "hardwareMake": "invalid",
                            "hardwareType": "invalid",
                            "multiplier": -1.00,
                            "averagePpd": -1
                        },
                        "errors": [
                            "Field 'hardwareName' must not be empty",
                            "Field 'displayName' must not be empty",
                            "Field 'hardwareMake' must be one of: [AMD, INTEL, NVIDIA]",
                            "Field 'hardwareType' must be one of: [CPU, GPU]",
                            "Field 'multiplier' must be 1.00 or higher",
                            "Field 'averagePpd' must be 1 or higher"
                        ]
                    }"""
                )
            )),
        @ApiResponse(responseCode = "401", description = "System user cannot be logged in with provided credentials"),
        @ApiResponse(responseCode = "403", description = "System user does not have the correct role to perform this request"),
        @ApiResponse(responseCode = "409", description = "A hardware with the same 'hardwareName' already exists"),
        @ApiResponse(responseCode = "502", description = "An error occurred connecting to an external system"),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute write requests"),
    })
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hardware> create(
        @RequestBody @Parameter(description = "The new hardware to be created") final HardwareRequest hardwareRequest,
        final HttpServletRequest request
    ) {
        AUDIT_LOGGER.info("POST request received to create hardware at '{}' with request: {}", request::getRequestURI, () -> hardwareRequest);

        final Hardware validatedHardware = hardwareValidator.create(hardwareRequest);
        final Hardware elementWithId = foldingRepository.createHardware(validatedHardware);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Created hardware with ID {}", elementWithId.id());
        hardwareCreates.increment();
        return created(elementWithId, elementWithId.id());
    }

    /**
     * {@link GetMapping} request to retrieve all {@link Hardware}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Hardware}s
     */
    @Operation(summary = "Retrieves all hardwares")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "All hardwares",
            headers = @Header(
                name = "eTag",
                description = "An EntityTag which can be used to retrieve a cached version of the response in future requests"
            ),
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Hardware.class)),
                examples = @ExampleObject("""
                    [
                        {
                            "id": 1,
                            "hardwareName": "Hardware1",
                            "displayName": "Hardware1",
                            "hardwareMake": "NVIDIA",
                            "hardwareType": "GPU",
                            "multiplier": 21.33,
                            "averagePpd": 1
                        },
                        {
                            "id": 2,
                            "hardwareName": "Hardware2",
                            "displayName": "Hardware2",
                            "hardwareMake": "AMD",
                            "hardwareType": "GPU",
                            "multiplier": 3.62,
                            "averagePpd": 25
                        }
                    ]""")
            )),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute read requests"),
    })
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Hardware>> getAll(final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received for all hardwares at '{}'", request::getRequestURI);
        final Collection<Hardware> elements = foldingRepository.getAllHardware();
        return cachedOk(elements);
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Hardware} by {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @param request    the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link Hardware}
     */
    @Operation(summary = "Retrieves a single hardware by its ID")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "The requested hardware",
            headers = @Header(
                name = "eTag",
                description = "An EntityTag which can be used to retrieve a cached version of the response in future requests"
            ),
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Hardware.class),
                examples = @ExampleObject("""
                    {
                        "id": 1,
                        "hardwareName": "Hardware1",
                        "displayName": "Hardware1",
                        "hardwareMake": "NVIDIA",
                        "hardwareType": "GPU",
                        "multiplier": 21.33,
                        "averagePpd": 1
                    }"""
                )
            )),
        @ApiResponse(
            responseCode = "400",
            description = "The provided ID is an invalid integer",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject("""
                    {
                        "error": "The input is not a valid format: Failed to convert value of type 'java.lang.String' to required type 'int';
                                  nested exception is java.lang.NumberFormatException: For input string: \\"a\\""
                    }"""
                )
            )),
        @ApiResponse(responseCode = "404", description = "No hardware exists with the given ID"),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute read requests"),
    })
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{hardwareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hardware> getById(
        @PathVariable("hardwareId") @Parameter(description = "The ID of the hardware to be retrieved") final int hardwareId,
        final HttpServletRequest request
    ) {
        AUDIT_LOGGER.debug("GET request for hardware received at '{}'", request::getRequestURI);

        final Hardware element = foldingRepository.getHardware(hardwareId);
        return cachedOk(element);
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Hardware} by {@code hardwareName}.
     *
     * @param hardwareName the {@code hardwareName} of the {@link Hardware} to retrieve
     * @param request      the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link Hardware}
     */
    @Operation(summary = "Retrieves a single hardware by its 'hardwareName'")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "The requested hardware",
            headers = @Header(
                name = "eTag",
                description = "An EntityTag which can be used to retrieve a cached version of the response in future requests"
            ),
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Hardware.class),
                examples = @ExampleObject("""
                    {
                        "id": 1,
                        "hardwareName": "Hardware1",
                        "displayName": "Hardware1",
                        "hardwareMake": "NVIDIA",
                        "hardwareType": "GPU",
                        "multiplier": 21.33,
                        "averagePpd": 1
                    }"""
                )
            )),
        @ApiResponse(responseCode = "404", description = "No hardware exists with the given 'hardwareName'"),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute read requests"),
    })
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hardware> getByHardwareName(
        @RequestParam("hardwareName") @Parameter(description = "The 'hardwareName' of the hardware to be retrieved") final String hardwareName,
        final HttpServletRequest request
    ) {
        AUDIT_LOGGER.debug("GET request for hardware received at '{}?{}'", request::getRequestURI, () -> extractParameters(request));

        final Hardware retrievedHardware = foldingRepository.getAllHardware()
            .stream()
            .filter(hardware -> hardware.hardwareName().equalsIgnoreCase(hardwareName))
            .findAny()
            .orElseThrow(() -> new NotFoundException(Hardware.class, hardwareName));

        return cachedOk(retrievedHardware);
    }

    /**
     * {@link PutMapping} request to update an existing {@link Hardware} based on the input request.
     *
     * @param hardwareId      the ID of the {@link Hardware} to be updated
     * @param hardwareRequest the {@link HardwareRequest} to update a {@link Hardware}
     * @param request         the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object, int)} containing the updated {@link Hardware}
     */
    @Operation(summary = "Updates a single hardware", security = @SecurityRequirement(name = "basicAuthentication"))
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "The hardware has been updated",
            headers = @Header(
                name = "location",
                description = "The URL for the updated hardware"
            ),
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Hardware.class),
                examples = @ExampleObject("""
                    {
                        "id": 1,
                        "hardwareName": "Hardware1_Updated",
                        "displayName": "Hardware1_Updated",
                        "hardwareMake": "NVIDIA",
                        "hardwareType": "GPU",
                        "multiplier": 21.33,
                        "averagePpd": 1
                    }"""
                )
            )),
        @ApiResponse(
            responseCode = "400",
            description = "The given hardware payload is invalid",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    example = """
                    {
                        "invalidObject": {
                            "id": 1,
                            "hardwareName": " ",
                            "hardwareMake": "invalid",
                            "hardwareType": "invalid",
                            "multiplier": -1.00,
                            "averagePpd": -1
                        },
                        "errors": [
                            "Field 'hardwareName' must not be empty",
                            "Field 'displayName' must not be empty",
                            "Field 'hardwareMake' must be one of: [AMD, INTEL, NVIDIA]",
                            "Field 'hardwareType' must be one of: [CPU, GPU]",
                            "Field 'multiplier' must be 1.00 or higher",
                            "Field 'averagePpd' must be 1 or higher"
                        ]
                    }"""
                )
            )),
        @ApiResponse(responseCode = "401", description = "System user cannot be logged in with provided credentials"),
        @ApiResponse(responseCode = "403", description = "System user does not have the correct role to perform this request"),
        @ApiResponse(responseCode = "404", description = "No hardware exists with the given ID"),
        @ApiResponse(responseCode = "409", description = "A hardware with the same 'hardwareName' already exists"),
        @ApiResponse(responseCode = "502", description = "An error occurred connecting to an external system"),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute write requests"),
    })
    @WriteRequired
    @RolesAllowed("admin")
    @PutMapping(path = "/{hardwareId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hardware> updateById(
        @PathVariable("hardwareId") @Parameter(description = "The ID of the hardware to be updated") final int hardwareId,
        @RequestBody @Parameter(description = "The new hardware to be updated") final HardwareRequest hardwareRequest,
        final HttpServletRequest request
    ) {
        AUDIT_LOGGER.info("PUT request for hardware received at '{}' with request {}", request::getRequestURI, () -> hardwareRequest);

        final Hardware existingHardware = foldingRepository.getHardware(hardwareId);

        if (existingHardware.isEqualRequest(hardwareRequest)) {
            AUDIT_LOGGER.info("Request is same as existing hardware");
            return ok(existingHardware);
        }

        final Hardware validatedHardware = hardwareValidator.update(hardwareRequest, existingHardware);

        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final Hardware hardwareWithId = Hardware.updateWithId(existingHardware.id(), validatedHardware);
        final Hardware updatedHardwareWithId = foldingRepository.updateHardware(hardwareWithId, existingHardware);

        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        AUDIT_LOGGER.info("Updated hardware with ID {}", updatedHardwareWithId.id());
        hardwareUpdates.increment();
        return ok(updatedHardwareWithId, updatedHardwareWithId.id());
    }

    /**
     * {@link DeleteMapping} request to delete an existing {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to be deleted
     * @param request    the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @Operation(summary = "Deletes a single hardware", security = @SecurityRequirement(name = "basicAuthentication"))
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "The hardware has been deleted"),
        @ApiResponse(responseCode = "401", description = "System user cannot be logged in with provided credentials"),
        @ApiResponse(responseCode = "403", description = "System user does not have the correct role to perform this request"),
        @ApiResponse(responseCode = "404", description = "No hardware exists with the given ID"),
        @ApiResponse(
            responseCode = "409",
            description = "The hardware is being referenced by a user and cannot be deleted",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = """
                    {
                        "invalidObject": {
                            "id": 1,
                            "hardwareName": "Hardware1",
                            "displayName": "Hardware1",
                            "hardwareMake": "NVIDIA",
                            "hardwareType": "GPU",
                            "multiplier": 16.38,
                            "averagePpd": 1
                        },
                        "conflictingObject": {
                            "id": 1,
                            "foldingUserName": "User1",
                            "displayName": "User1",
                            "passkey": "fc7d6837************************",
                            "category": "WILDCARD",
                            "hardware": {
                                "id": 1,
                                "hardwareName": "Hardware1",
                                "displayName": "Hardware1",
                                "hardwareMake": "NVIDIA",
                                "hardwareType": "GPU",
                                "multiplier": 16.38,
                                "averagePpd": 1
                            },
                            "team": {
                                "id": 1,
                                "teamName": "Team1",
                                "teamDescription": "Desc"
                            },
                            "role": "CAPTAIN"
                        },
                        "validationResult": "FAILURE_DUE_TO_CONFLICT",
                        "errors": [
                            "Payload conflicts with an existing object"
                        ]
                    }"""
                )
            )),
        @ApiResponse(responseCode = "502", description = "An error occurred connecting to an external system"),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute write requests"),
    })
    @WriteRequired
    @RolesAllowed("admin")
    @DeleteMapping(path = "/{hardwareId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(
        @PathVariable("hardwareId") @Parameter(description = "The ID of the hardware to be deleted") final int hardwareId,
        final HttpServletRequest request
    ) {
        AUDIT_LOGGER.info("DELETE request for hardware received at '{}'", request::getRequestURI);

        final Hardware hardware = foldingRepository.getHardware(hardwareId);
        final Hardware validatedHardware = hardwareValidator.delete(hardware);

        foldingRepository.deleteHardware(validatedHardware);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        AUDIT_LOGGER.info("Deleted hardware with ID {}", hardwareId);
        hardwareDeletes.increment();
        return ok();
    }
}