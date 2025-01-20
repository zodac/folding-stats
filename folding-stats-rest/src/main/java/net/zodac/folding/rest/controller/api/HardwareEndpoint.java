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

package net.zodac.folding.rest.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.rest.api.tc.request.HardwareRequest;
import net.zodac.folding.rest.response.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * REST endpoints for {@code Team Competition} {@link Hardware}s.
 */
// TODO: Finish Swagger docs
@Tag(name = "Hardware Endpoints", description = "REST endpoints to create, read, update and delete hardware on the system")
public interface HardwareEndpoint {

    /**
     * {@link PostMapping} request to create a {@link Hardware} based on the input request.
     *
     * @param hardwareRequest the {@link HardwareRequest} to create a {@link Hardware}
     * @param request         the {@link HttpServletRequest}
     * @return {@link Responses#created(Object, int)} containing the created {@link Hardware}
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
        @ApiResponse(responseCode = "401", description = "System user cannot be logged in with provided credentials", content = @Content),
        @ApiResponse(responseCode = "403", description = "System user does not have the correct role to perform this request", content = @Content),
        @ApiResponse(responseCode = "409", description = "A hardware with the same 'hardwareName' already exists", content = @Content),
        @ApiResponse(responseCode = "502", description = "An error occurred connecting to an external system", content = @Content),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute write requests", content = @Content),
    })
    @RequestBody(
        description = "The new hardware to be created",
        required = true,
        content = @Content(
            schema = @Schema(implementation = HardwareRequest.class),
            examples = @ExampleObject(
                name = "An example request to create a hardware, with all required fields",
                value = """
                    {
                      "hardwareName": "Hardware1",
                      "displayName": "Hardware1",
                      "hardwareMake": "NVIDIA",
                      "hardwareType": "GPU",
                      "multiplier": 21.33,
                      "averagePpd": 1
                    }"""
            ))
    )
    ResponseEntity<Hardware> create(HardwareRequest hardwareRequest, HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve all {@link Hardware}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link Responses#cachedOk(Collection, long)} containing the {@link Hardware}s
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
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute read requests", content = @Content),
    })
    ResponseEntity<Collection<Hardware>> getAll(HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link Hardware} by {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @param request    the {@link HttpServletRequest}
     * @return {@link Responses#cachedOk(Object)} containing the {@link Hardware}
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
                examples = @ExampleObject("""
                    {
                        "error": "The input is not a valid format: Failed to convert value of type 'java.lang.String' to required type 'int';
                                  nested exception is java.lang.NumberFormatException: For input string: \\"a\\""
                    }"""
                )
            )),
        @ApiResponse(responseCode = "404", description = "No hardware exists with the given ID", content = @Content),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute read requests", content = @Content),
    })
    @Parameter(name = "hardwareId", description = "The ID of the hardware to be retrieved")
    ResponseEntity<Hardware> getById(int hardwareId, HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link Hardware} by {@code hardwareName}.
     *
     * @param hardwareName the {@code hardwareName} of the {@link Hardware} to retrieve
     * @param request      the {@link HttpServletRequest}
     * @return {@link Responses#cachedOk(Object)} containing the {@link Hardware}
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
        @ApiResponse(responseCode = "404", description = "No hardware exists with the given 'hardwareName'", content = @Content),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute read requests", content = @Content),
    })
    @Parameter(name = "hardwareName", description = "The 'hardwareName' of the hardware to be retrieved")
    ResponseEntity<Hardware> getByHardwareName(String hardwareName, HttpServletRequest request);

    /**
     * {@link PutMapping} request to update an existing {@link Hardware} based on the input request.
     *
     * @param hardwareId      the ID of the {@link Hardware} to be updated
     * @param hardwareRequest the {@link HardwareRequest} to update a {@link Hardware}
     * @param request         the {@link HttpServletRequest}
     * @return {@link Responses#ok(Object, int)} containing the updated {@link Hardware}
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
                schema = @Schema(implementation = Hardware.class),
                examples = @ExampleObject("""
                    {
                        "id": 1,
                        "hardwareName": "Hardware1",
                        "displayName": "Hardware1 (Updated)",
                        "hardwareMake": "NVIDIA",
                        "hardwareType": "GPU",
                        "multiplier": 15.33,
                        "averagePpd": 1
                    }"""
                )
            )),
        @ApiResponse(
            responseCode = "400",
            description = "The given hardware payload is invalid",
            content = @Content(
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
        @ApiResponse(responseCode = "401", description = "System user cannot be logged in with provided credentials", content = @Content),
        @ApiResponse(responseCode = "403", description = "System user does not have the correct role to perform this request", content = @Content),
        @ApiResponse(responseCode = "404", description = "No hardware exists with the given ID", content = @Content),
        @ApiResponse(responseCode = "409", description = "A hardware with the same 'hardwareName' already exists", content = @Content),
        @ApiResponse(responseCode = "502", description = "An error occurred connecting to an external system", content = @Content),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute write requests", content = @Content),
    })
    @RequestBody(
        description = "The new hardware to be updated",
        required = true,
        content = @Content(
            schema = @Schema(implementation = HardwareRequest.class),
            examples = @ExampleObject(
                name = "An example request to update a hardware, with all required fields",
                value = """
                    {
                      "hardwareName": "Hardware1",
                      "displayName": "Hardware1 (Updated)",
                      "hardwareMake": "NVIDIA",
                      "hardwareType": "GPU",
                      "multiplier": 15.33,
                      "averagePpd": 1
                    }"""
            ))
    )
    @Parameter(name = "hardwareId", description = "The ID of the hardware to be updated")
    ResponseEntity<Hardware> updateById(int hardwareId, HardwareRequest hardwareRequest, HttpServletRequest request);

    /**
     * {@link DeleteMapping} request to delete an existing {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to be deleted
     * @param request    the {@link HttpServletRequest}
     * @return {@link Responses#ok()}
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
        @ApiResponse(responseCode = "502", description = "An error occurred connecting to an external system", content = @Content),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute write requests", content = @Content),
    })
    @Parameter(name = "hardwareId", description = "The ID of the hardware to be deleted")
    ResponseEntity<Void> deleteById(int hardwareId, HttpServletRequest request);
}
