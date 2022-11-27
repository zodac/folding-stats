/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.rest;

import static me.zodac.folding.rest.response.Responses.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.lars.LarsHardwareUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for debugging.
 *
 * <p>
 * <b>NOTE:</b> There are no client-libraries for these endpoints.
 */
@Tag(name = "Debugging Endpoints", description = "REST endpoints for debugging functions")
@RestController
@RequestMapping("/debug")
public class DebugEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

    private final FoldingRepository foldingRepository;
    private final LarsHardwareUpdater larsHardwareUpdater;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository   the {@link FoldingRepository}
     * @param larsHardwareUpdater the {@link LarsHardwareUpdater}
     */
    @Autowired
    public DebugEndpoint(final FoldingRepository foldingRepository, final LarsHardwareUpdater larsHardwareUpdater) {
        this.foldingRepository = foldingRepository;
        this.larsHardwareUpdater = larsHardwareUpdater;
    }

    /**
     * {@link PostMapping} request to print the contents of all caches to the system log.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     * @see LarsHardwareUpdater
     */
    @Operation(summary = "Manually trigger an update of hardware from LARS", security = @SecurityRequirement(name = "basicAuthentication"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "LARS update was successfully executed"),
        @ApiResponse(responseCode = "401", description = "System user cannot be logged in with provided credentials"),
        @ApiResponse(responseCode = "403", description = "System user does not have the correct role to perform this request"),
    })
    @RolesAllowed("admin")
    @PostMapping(path = "/lars", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> startLarsUpdate() {
        AUDIT_LOGGER.info("GET request received to manually update hardware from LARS DB");
        larsHardwareUpdater.retrieveHardwareAndPersist();
        return ok();
    }

    /**
     * {@link PostMapping} request to print the contents of all caches to the system log.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     * @see FoldingRepository#printCacheContents()
     */
    @Operation(summary = "Print contents of caches to system log", security = @SecurityRequirement(name = "basicAuthentication"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cache contents were successfully printed"),
        @ApiResponse(responseCode = "401", description = "System user cannot be logged in with provided credentials"),
        @ApiResponse(responseCode = "403", description = "System user does not have the correct role to perform this request"),
    })
    @RolesAllowed("admin")
    @PostMapping(path = "/caches", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> printCaches() {
        AUDIT_LOGGER.info("Printing cache contents");
        foldingRepository.printCacheContents();
        return ok();
    }
}