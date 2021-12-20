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
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.validation.UserValidator;
import me.zodac.folding.api.tc.validation.ValidationResult;
import me.zodac.folding.rest.api.FoldingService;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.rest.response.Responses;
import me.zodac.folding.rest.util.IdResult;
import me.zodac.folding.rest.util.IntegerParser;
import me.zodac.folding.rest.util.ValidationFailureResponseMapper;
import me.zodac.folding.state.SystemStateManager;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for <code>Team Competition</code> {@link User}s.
 */
@RestController
@RequestMapping("/users")
public class UserEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private FoldingService foldingService;

    /**
     * {@link PostMapping} request to create a {@link User} based on the input request.
     *
     * @param userRequest the {@link UserRequest} to create a {@link User}
     * @param request     the {@link HttpServletRequest}
     * @return {@link Responses#created(Object, int)} containing the created {@link User}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody final UserRequest userRequest, final HttpServletRequest request) {
        LOGGER.debug("POST request received to create user at '{}' with request: {}", request::getRequestURI, () -> userRequest);

        final ValidationResult<User> validationResult = validateCreate(userRequest);
        if (validationResult.isFailure()) {
            return ValidationFailureResponseMapper.map(validationResult);
        }
        final User validatedUser = validationResult.getOutput();

        try {
            final User elementWithId = foldingService.createUser(validatedUser);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);

            LOGGER.info("Created user with ID {}", elementWithId.getId());
            return created(elementWithId, elementWithId.getId());
        } catch (final Exception e) {
            LOGGER.error("Unexpected error creating user: {}", userRequest, e);
            return serverError();
        }
    }

    /**
     * {@link GetMapping} request to retrieve all {@link User}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link Responses#cachedOk(Collection, long)} containing the {@link User}s
     */
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(final HttpServletRequest request) {
        LOGGER.debug("GET request received for all users at '{}'", request::getRequestURI);

        try {
            final Collection<User> elements = foldingService.getAllUsersWithoutPasskeys();
            return cachedOk(elements, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting all users", e);
            return serverError();
        }
    }

    /**
     * {@link GetMapping} request to retrieve a {@link User} by {@code userId}.
     *
     * @param userId  the ID of the {@link User} to retrieve
     * @param request the {@link HttpServletRequest}
     * @return {@link Responses#cachedOk(Object, long)} containing the {@link User}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable("userId") final String userId, final HttpServletRequest request) {
        LOGGER.debug("GET request for user received at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalElement = foldingService.getUserWithoutPasskey(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No user found with ID {}", userId);
                return notFound();
            }

            final User element = optionalElement.get();
            return cachedOk(element, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting user with ID: {}", userId, e);
            return serverError();
        }
    }

    /**
     * {@link GetMapping} request to retrieve a {@link User} by {@code userId}, with the passkey exposed.
     *
     * @param userId  the ID of the {@link User} to retrieve
     * @param request the {@link HttpServletRequest}
     * @return {@link Responses#cachedOk(Object, long)} containing the {@link User}
     */
    @ReadRequired
    @RolesAllowed("admin")
    @GetMapping(path = "/{userId}/passkey", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByIdWithPasskey(@PathVariable("userId") final String userId, final HttpServletRequest request) {
        LOGGER.debug("GET request for user with passkey received at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalElement = foldingService.getUserWithPasskey(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No user with passkey found with ID {}", userId);
                return notFound();
            }

            final User element = optionalElement.get();
            return cachedOk(element, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            LOGGER.error("Unexpected error getting user with passkey ID: {}", userId, e);
            return serverError();
        }
    }


    /**
     * {@link PutMapping} request to update an existing {@link User} based on the input request.
     *
     * @param userId      the ID of the {@link User} to be updated
     * @param userRequest the {@link UserRequest} to update a {@link User}
     * @param request     the {@link HttpServletRequest}
     * @return {@link Responses#ok(Object, int)} containing the updated {@link User}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PutMapping(path = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateById(@PathVariable("userId") final String userId,
                                        @RequestBody final UserRequest userRequest,
                                        final HttpServletRequest request) {
        LOGGER.debug("PUT request for user received at '{}'", request::getRequestURI);

        if (userRequest == null) {
            LOGGER.error("No payload provided");
            return nullRequest();
        }

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalElement = foldingService.getUserWithPasskey(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No user found with ID {}", userId);
                return notFound();
            }
            final User existingUser = optionalElement.get();

            if (existingUser.isEqualRequest(userRequest)) {
                LOGGER.debug("No change necessary");
                final User userWithHiddenPasskey = User.hidePasskey(existingUser);
                return ok(userWithHiddenPasskey);
            }

            final ValidationResult<User> validationResult = validateUpdate(userRequest, existingUser);
            if (validationResult.isFailure()) {
                return ValidationFailureResponseMapper.map(validationResult);
            }
            final User validatedUser = validationResult.getOutput();

            // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
            final User userWithId = User.updateWithId(existingUser.getId(), validatedUser);
            final User updatedUserWithId = foldingService.updateUser(userWithId, existingUser);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);

            LOGGER.info("Updated user with ID {}", updatedUserWithId.getId());
            return ok(updatedUserWithId, updatedUserWithId.getId());
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating user with ID: {}", userId, e);
            return serverError();
        }
    }

    /**
     * {@link DeleteMapping} request to delete an existing {@link User}.
     *
     * @param userId  the ID of the {@link User} to be deleted
     * @param request the {@link HttpServletRequest}
     * @return {@link Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @DeleteMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteById(@PathVariable("userId") final String userId, final HttpServletRequest request) {
        LOGGER.debug("DELETE request for user received at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalElement = foldingService.getUserWithoutPasskey(parsedId);
            if (optionalElement.isEmpty()) {
                LOGGER.error("No user found with ID {}", userId);
                return notFound();
            }
            final User user = optionalElement.get();

            final ValidationResult<User> validationResult = validateDelete(user);
            if (validationResult.isFailure()) {
                return ValidationFailureResponseMapper.map(validationResult);
            }
            final User validatedUser = validationResult.getOutput();

            foldingService.deleteUser(validatedUser);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Deleted user with ID {}", userId);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error deleting user with ID: {}", userId, e);
            return serverError();
        }
    }

    private ValidationResult<User> validateCreate(final UserRequest userRequest) {
        final UserValidator userValidator = UserValidator.create(HttpFoldingStatsRetriever.create());
        return userValidator.validateCreate(
            userRequest,
            foldingService.getAllUsersWithPasskeys(),
            foldingService.getAllHardware(),
            foldingService.getAllTeams()
        );
    }

    private ValidationResult<User> validateUpdate(final UserRequest userRequest, final User existingUser) {
        final UserValidator userValidator = UserValidator.create(HttpFoldingStatsRetriever.create());
        return userValidator.validateUpdate(
            userRequest,
            existingUser,
            foldingService.getAllUsersWithPasskeys(),
            foldingService.getAllHardware(),
            foldingService.getAllTeams()
        );
    }

    private ValidationResult<User> validateDelete(final User existingUser) {
        final UserValidator userValidator = UserValidator.create(HttpFoldingStatsRetriever.create());
        return userValidator.validateDelete(existingUser);
    }
}