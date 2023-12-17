/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.rest.controller;

import static me.zodac.folding.rest.response.Responses.ok;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.security.PermitAll;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.util.DecodedLoginCredentials;
import me.zodac.folding.api.util.EncodingUtils;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.LoginCredentials;
import me.zodac.folding.rest.controller.api.LoginEndpoint;
import me.zodac.folding.rest.exception.ForbiddenException;
import me.zodac.folding.rest.exception.InvalidLoginCredentialsException;
import me.zodac.folding.rest.exception.UnauthorizedException;
import me.zodac.folding.rest.util.ReadRequired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implementation of {@link LoginEndpoint} REST endpoints.
 */
@CrossOrigin
@RestController
@RequestMapping("/login")
public class LoginController implements LoginEndpoint {

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
    public LoginController(final FoldingRepository foldingRepository, final MeterRegistry registry) {
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

    @Override
    @ReadRequired
    @PermitAll
    @PostMapping(path = "/admin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> loginAsAdmin(@RequestBody final LoginCredentials loginCredentials) {
        AUDIT_LOGGER.info("Login request received");
        loginAttempts.increment();

        if (EncodingUtils.isInvalidBasicAuthentication(loginCredentials.encodedUserNameAndPassword())) {
            AUDIT_LOGGER.error("Invalid payload: {}", loginCredentials);
            failedLogins.increment();
            throw new InvalidLoginCredentialsException(loginCredentials);
        }

        try {
            final DecodedLoginCredentials decodedLoginCredentials =
                EncodingUtils.decodeBasicAuthentication(loginCredentials.encodedUserNameAndPassword());
            AUDIT_LOGGER.debug("Login request received for user: '{}'", decodedLoginCredentials.username());

            final UserAuthenticationResult userAuthenticationResult = foldingRepository.authenticateSystemUser(decodedLoginCredentials);

            if (!userAuthenticationResult.userExists() || !userAuthenticationResult.passwordMatch()) {
                AUDIT_LOGGER.warn("Invalid user credentials supplied: {}", loginCredentials);
                failedLogins.increment();
                throw new UnauthorizedException();
            }

            if (!userAuthenticationResult.isAdmin()) {
                AUDIT_LOGGER.warn("User '{}' is not an admin", decodedLoginCredentials.username());
                failedLogins.increment();
                throw new ForbiddenException();
            }

            AUDIT_LOGGER.info("Admin user '{}' logged in", decodedLoginCredentials.username());
            successfulLogins.increment();
            return ok();
        } catch (final IllegalArgumentException e) {
            AUDIT_LOGGER.error("Encoded username and password was not a valid Base64 string", e);
            failedLogins.increment();
            throw new InvalidLoginCredentialsException(loginCredentials, e);
        }
    }
}
