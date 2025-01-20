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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.bean.tc.lars.LarsHardwareUpdater;
import net.zodac.folding.rest.response.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * REST endpoints for debugging.
 */
@Tag(name = "Debugging Endpoints", description = "REST endpoints for debugging functions for admins")
public interface DebugEndpoint {

    /**
     * {@link PostMapping} request to print the contents of all caches to the system log.
     *
     * @return {@link Responses#ok()}
     * @see LarsHardwareUpdater
     */
    @Operation(summary = "Manually trigger an update of hardware from LARS", security = @SecurityRequirement(name = "basicAuthentication"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "LARS update was successfully executed"),
        @ApiResponse(responseCode = "401", description = "System user cannot be logged in with provided credentials"),
        @ApiResponse(responseCode = "403", description = "System user does not have the correct role to perform this request"),
    })
    ResponseEntity<Void> startLarsUpdate();

    /**
     * {@link PostMapping} request to print the contents of all caches to the system log.
     *
     * @return {@link Responses#ok()}
     * @see FoldingRepository#printCacheContents()
     */
    @Operation(summary = "Print contents of caches to system log", security = @SecurityRequirement(name = "basicAuthentication"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cache contents were successfully printed"),
        @ApiResponse(responseCode = "401", description = "System user cannot be logged in with provided credentials"),
        @ApiResponse(responseCode = "403", description = "System user does not have the correct role to perform this request"),
    })
    ResponseEntity<Void> printCaches();
}
