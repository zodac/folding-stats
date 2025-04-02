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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.zodac.folding.rest.api.LoginCredentials;
import net.zodac.folding.rest.exception.ForbiddenException;
import net.zodac.folding.rest.exception.InvalidLoginCredentialsException;
import net.zodac.folding.rest.exception.UnauthorizedException;
import net.zodac.folding.rest.response.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST endpoints to log in as a system user.
 */
@Tag(name = "Login Endpoints", description = "REST endpoints to login as a system user")
public interface LoginEndpoint { // NOPMD: ImplicitFunctionalInterface - Not intended as a functional endpoint

    /**
     * {@link PostMapping} request to log in as an admin system user.
     *
     * @param loginCredentials the {@link LoginCredentials}
     * @return {@link Responses#ok()}
     * @throws InvalidLoginCredentialsException thrown if the input {@link LoginCredentials} are in an incorrect format
     * @throws UnauthorizedException            thrown if the user does not exist, or the password is incorrect
     * @throws ForbiddenException               thrown if the user and password is accepted, but it does not have the correct role
     */
    @Operation(summary = "Accepts user credentials and verifies whether they have admin access")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Credentials are valid and for an admin user"),
        @ApiResponse(
            responseCode = "400",
            description = "The given login payload is invalid",
            content = @Content(
                schema = @Schema(example = """
                    {
                        "encodedUserNameAndPassword": "dXNlcm5hbWU6cGFzc3dvcmQ="
                    }"""
                )
            )),
        @ApiResponse(responseCode = "401", description = "Provided credentials cannot be logged in"),
        @ApiResponse(responseCode = "403", description = "Provided credentials is for a valid user, but not an admin"),
        @ApiResponse(responseCode = "503", description = "The system is not in a valid state to execute write requests"),
    })
    ResponseEntity<Void> loginAsAdmin(@RequestBody @Parameter(description = "The login credentials to be checked") LoginCredentials loginCredentials);
}
