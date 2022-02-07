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

import static me.zodac.folding.rest.response.Responses.ok;

import java.util.Map;
import javax.annotation.security.PermitAll;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.util.EncodingUtils;
import me.zodac.folding.bean.FoldingRepository;
import me.zodac.folding.rest.api.LoginCredentials;
import me.zodac.folding.rest.exception.ForbiddenException;
import me.zodac.folding.rest.exception.InvalidLoginCredentialsException;
import me.zodac.folding.rest.exception.UnauthorizedException;
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
@RestController
@RequestMapping("/login")
public class LoginEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private FoldingRepository foldingRepository;

    /**
     * {@link PostMapping} request to log in as an admin system user.
     *
     * @param loginCredentials the {@link LoginCredentials}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object)}
     * @throws InvalidLoginCredentialsException thrown if the input {@link LoginCredentials} are in an incorrect format
     * @throws UnauthorizedException            thrown if the user does not exist, or the password is incorrect
     * @throws ForbiddenException               thown if the user and password is accepted, but it does not have the correct role
     */
    @ReadRequired
    @PermitAll
    @PostMapping(path = "/admin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginAsAdmin(@RequestBody final LoginCredentials loginCredentials) {
        LOGGER.debug("Login request received");

        if (EncodingUtils.isInvalidBasicAuthentication(loginCredentials.getEncodedUserNameAndPassword())) {
            LOGGER.error("Invalid payload: {}", loginCredentials);
            throw new InvalidLoginCredentialsException(loginCredentials);
        }

        try {
            final Map<String, String> decodedUserNameAndPassword =
                EncodingUtils.decodeBasicAuthentication(loginCredentials.getEncodedUserNameAndPassword());
            final String userName = decodedUserNameAndPassword.get(EncodingUtils.DECODED_USERNAME_KEY);
            final String password = decodedUserNameAndPassword.get(EncodingUtils.DECODED_PASSWORD_KEY);
            LOGGER.debug("Login request received for user: '{}'", userName);

            final UserAuthenticationResult userAuthenticationResult = foldingRepository.authenticateSystemUser(userName, password);

            if (!userAuthenticationResult.userExists() || !userAuthenticationResult.passwordMatch()) {
                LOGGER.warn("Invalid user credentials supplied: {}", loginCredentials);
                throw new UnauthorizedException();
            }

            if (!userAuthenticationResult.isAdmin()) {
                LOGGER.warn("User '{}' is not an admin", userName);
                throw new ForbiddenException();
            }

            LOGGER.info("Admin user '{}' logged in", userName);
            return ok();
        } catch (final IllegalArgumentException e) {
            LOGGER.error("Encoded username and password was not a valid Base64 string", e);
            throw new InvalidLoginCredentialsException(loginCredentials, e);
        }
    }
}
