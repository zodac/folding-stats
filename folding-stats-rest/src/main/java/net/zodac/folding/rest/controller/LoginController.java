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

package net.zodac.folding.rest.controller;

import static net.zodac.folding.rest.response.Responses.ok;

import jakarta.annotation.security.PermitAll;
import net.zodac.folding.api.UserAuthenticationResult;
import net.zodac.folding.api.util.DecodedLoginCredentials;
import net.zodac.folding.api.util.EncodingUtils;
import net.zodac.folding.api.util.LoggerName;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.rest.api.LoginCredentials;
import net.zodac.folding.rest.controller.api.LoginEndpoint;
import net.zodac.folding.rest.exception.ForbiddenException;
import net.zodac.folding.rest.exception.InvalidLoginCredentialsException;
import net.zodac.folding.rest.exception.UnauthorizedException;
import net.zodac.folding.rest.util.ReadRequired;
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
 * Implementation of {@link LoginEndpoint} REST endpoints.
 */
@RestController
@RequestMapping("/login")
public class LoginController implements LoginEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

    private final FoldingRepository foldingRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     */
    @Autowired
    public LoginController(final FoldingRepository foldingRepository) {
        this.foldingRepository = foldingRepository;
    }

    @Override
    @ReadRequired
    @PermitAll
    @PostMapping(path = "/admin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> loginAsAdmin(@RequestBody final LoginCredentials loginCredentials) {
        AUDIT_LOGGER.info("Login request received");

        if (EncodingUtils.isInvalidBasicAuthentication(loginCredentials.encodedUserNameAndPassword())) {
            AUDIT_LOGGER.error("Invalid payload: {}", loginCredentials);
            throw new InvalidLoginCredentialsException(loginCredentials);
        }

        try {
            final DecodedLoginCredentials decodedLoginCredentials =
                EncodingUtils.decodeBasicAuthentication(loginCredentials.encodedUserNameAndPassword());
            AUDIT_LOGGER.debug("Login request received for user: '{}'", decodedLoginCredentials.username());

            final UserAuthenticationResult userAuthenticationResult = foldingRepository.authenticateSystemUser(decodedLoginCredentials);

            if (!userAuthenticationResult.userExists() || !userAuthenticationResult.passwordMatch()) {
                AUDIT_LOGGER.warn("Invalid user credentials supplied: {}", loginCredentials);
                throw new UnauthorizedException();
            }

            if (!userAuthenticationResult.isAdmin()) {
                AUDIT_LOGGER.warn("User '{}' is not an admin", decodedLoginCredentials.username());
                throw new ForbiddenException();
            }

            AUDIT_LOGGER.info("Admin user '{}' logged in", decodedLoginCredentials.username());
            return ok();
        } catch (final IllegalArgumentException e) {
            AUDIT_LOGGER.error("Encoded username and password was not a valid Base64 string", e);
            throw new InvalidLoginCredentialsException(loginCredentials, e);
        }
    }
}
