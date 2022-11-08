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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.annotation.security.PermitAll;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.util.EncodingUtils;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.LoginCredentials;
import me.zodac.folding.rest.exception.ForbiddenException;
import me.zodac.folding.rest.exception.InvalidLoginCredentialsException;
import me.zodac.folding.rest.exception.UnauthorizedException;
import me.zodac.folding.rest.util.ReadRequired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints to log in as a system user.
 */
@Tag(name = "Login Endpoints", description = "REST endpoints to login as a system user")
@RestController
@RequestMapping("/login")
public class LoginEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

    private final FoldingRepository foldingRepository;

    // Prometheus counters
    private final Counter loginAttempts;
    private final Counter successfulLogins;
    private final Counter failedLogins;

    /**
     * {@link Autowired} constructor to inject {@link MeterRegistry} and configure Prometheus {@link Counter}s.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param registry          the Prometheus {@link MeterRegistry}
     */
    public LoginEndpoint(final FoldingRepository foldingRepository, final MeterRegistry registry) {
        this.foldingRepository = foldingRepository;

        loginAttempts = Counter.builder("login_attempts_counter")
            .description("Number of login attempts to the admin page")
            .register(registry);
        successfulLogins = Counter.builder("login_success_counter")
            .description("Number of successful login attempts to the admin page")
            .register(registry);
        failedLogins = Counter.builder("login_failure_counter")
            .description("Number of failed login attempts to the admin page")
            .register(registry);
    }

    /**
     * {@link PostMapping} request to log in as an admin system user.
     *
     * @param loginCredentials the {@link LoginCredentials}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
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
                mediaType = "application/json",
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
    @ReadRequired
    @PermitAll
    @PostMapping(path = "/admin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> loginAsAdmin(
        @RequestBody @Parameter(description = "The login credentials to be checked") final LoginCredentials loginCredentials
    ) {
        AUDIT_LOGGER.info("Login request received");
        loginAttempts.increment();

        if (EncodingUtils.isInvalidBasicAuthentication(loginCredentials.getEncodedUserNameAndPassword())) {
            AUDIT_LOGGER.error("Invalid payload: {}", loginCredentials);
            failedLogins.increment();
            throw new InvalidLoginCredentialsException(loginCredentials);
        }

        try {
            final Map<String, String> decodedUserNameAndPassword =
                EncodingUtils.decodeBasicAuthentication(loginCredentials.getEncodedUserNameAndPassword());
            final String userName = decodedUserNameAndPassword.get(EncodingUtils.DECODED_USERNAME_KEY);
            final String password = decodedUserNameAndPassword.get(EncodingUtils.DECODED_PASSWORD_KEY);
            AUDIT_LOGGER.debug("Login request received for user: '{}'", userName);

            final UserAuthenticationResult userAuthenticationResult = foldingRepository.authenticateSystemUser(userName, password);

            if (!userAuthenticationResult.userExists() || !userAuthenticationResult.passwordMatch()) {
                AUDIT_LOGGER.warn("Invalid user credentials supplied: {}", loginCredentials);
                failedLogins.increment();
                throw new UnauthorizedException();
            }

            if (!userAuthenticationResult.isAdmin()) {
                AUDIT_LOGGER.warn("User '{}' is not an admin", userName);
                failedLogins.increment();
                throw new ForbiddenException();
            }

            AUDIT_LOGGER.info("Admin user '{}' logged in", userName);
            successfulLogins.increment();
            return ok();
        } catch (final IllegalArgumentException e) {
            AUDIT_LOGGER.error("Encoded username and password was not a valid Base64 string", e);
            failedLogins.increment();
            throw new InvalidLoginCredentialsException(loginCredentials, e);
        }
    }
}
