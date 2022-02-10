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

import static me.zodac.folding.api.util.DateTimeUtils.untilNextMonthUtc;
import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.ok;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.validation.UserValidator;
import me.zodac.folding.api.tc.validation.ValidationResult;
import me.zodac.folding.bean.FoldingRepository;
import me.zodac.folding.rest.api.tc.request.UserRequest;
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
    private FoldingRepository foldingRepository;

    // Prometheus counters
    private final Counter userCreates;
    private final Counter userUpdates;
    private final Counter userDeletes;

    /**
     * Constructor to inject {@link MeterRegistry} and configure Prometheus {@link Counter}s.
     *
     * @param registry the Prometheus {@link MeterRegistry}
     */
    public UserEndpoint(final MeterRegistry registry) {
        userCreates = Counter.builder("user_create_counter")
            .description("Number of User creations through the REST endpoint")
            .register(registry);
        userUpdates = Counter.builder("user_update_counter")
            .description("Number of User updates through the REST endpoint")
            .register(registry);
        userDeletes = Counter.builder("user_delete_counter")
            .description("Number of User deletions through the REST endpoint")
            .register(registry);
    }

    /**
     * {@link PostMapping} request to create a {@link User} based on the input request.
     *
     * @param userRequest the {@link UserRequest} to create a {@link User}
     * @param request     the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#created(Object, int)} containing the created {@link User}
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
        final User validatedUser = validationResult.output();

        final User elementWithId = foldingRepository.createUser(validatedUser);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        LOGGER.info("Created user with ID {}", elementWithId.getId());
        userCreates.increment();
        return created(elementWithId, elementWithId.getId());
    }

    /**
     * {@link GetMapping} request to retrieve all {@link User}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link User}s
     */
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(final HttpServletRequest request) {
        LOGGER.debug("GET request received for all users at '{}'", request::getRequestURI);
        final Collection<User> elements = foldingRepository.getAllUsersWithoutPasskeys();
        return cachedOk(elements, untilNextMonthUtc(ChronoUnit.SECONDS));
    }

    /**
     * {@link GetMapping} request to retrieve a {@link User} by {@code userId}.
     *
     * @param userId  the ID of the {@link User} to retrieve
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object, long)} containing the {@link User}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable("userId") final String userId, final HttpServletRequest request) {
        LOGGER.debug("GET request for user received at '{}'", request::getRequestURI);

        final int parsedId = IntegerParser.parsePositive(userId);
        final User element = foldingRepository.getUserWithoutPasskey(parsedId);
        return cachedOk(element, untilNextMonthUtc(ChronoUnit.SECONDS));
    }

    /**
     * {@link GetMapping} request to retrieve a {@link User} by {@code userId}, with the passkey exposed.
     *
     * @param userId  the ID of the {@link User} to retrieve
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object, long)} containing the {@link User}
     */
    @ReadRequired
    @RolesAllowed("admin")
    @GetMapping(path = "/{userId}/passkey", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByIdWithPasskey(@PathVariable("userId") final String userId, final HttpServletRequest request) {
        LOGGER.debug("GET request for user with passkey received at '{}'", request::getRequestURI);

        final int parsedId = IntegerParser.parsePositive(userId);
        final User element = foldingRepository.getUserWithPasskey(parsedId);
        return cachedOk(element, untilNextMonthUtc(ChronoUnit.SECONDS));
    }


    /**
     * {@link PutMapping} request to update an existing {@link User} based on the input request.
     *
     * @param userId      the ID of the {@link User} to be updated
     * @param userRequest the {@link UserRequest} to update a {@link User}
     * @param request     the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object, int)} containing the updated {@link User}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PutMapping(path = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateById(@PathVariable("userId") final String userId,
                                        @RequestBody final UserRequest userRequest,
                                        final HttpServletRequest request) {
        LOGGER.debug("PUT request for user received at '{}'", request::getRequestURI);

        final int parsedId = IntegerParser.parsePositive(userId);
        final User existingUser = foldingRepository.getUserWithPasskey(parsedId);

        if (existingUser.isEqualRequest(userRequest)) {
            LOGGER.debug("No change necessary");
            final User userWithHiddenPasskey = User.hidePasskey(existingUser);
            return ok(userWithHiddenPasskey);
        }

        final ValidationResult<User> validationResult = validateUpdate(userRequest, existingUser);
        if (validationResult.isFailure()) {
            return ValidationFailureResponseMapper.map(validationResult);
        }
        final User validatedUser = validationResult.output();

        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final User userWithId = User.updateWithId(existingUser.getId(), validatedUser);
        final User updatedUserWithId = foldingRepository.updateUser(userWithId, existingUser);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        LOGGER.info("Updated user with ID {}", updatedUserWithId.getId());
        userUpdates.increment();
        return ok(updatedUserWithId, updatedUserWithId.getId());
    }

    /**
     * {@link DeleteMapping} request to delete an existing {@link User}.
     *
     * @param userId  the ID of the {@link User} to be deleted
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @DeleteMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteById(@PathVariable("userId") final String userId, final HttpServletRequest request) {
        LOGGER.debug("DELETE request for user received at '{}'", request::getRequestURI);

        final int parsedId = IntegerParser.parsePositive(userId);
        final User user = foldingRepository.getUserWithoutPasskey(parsedId);

        final ValidationResult<User> validationResult = validateDelete(user);
        if (validationResult.isFailure()) {
            return ValidationFailureResponseMapper.map(validationResult);
        }
        final User validatedUser = validationResult.output();

        foldingRepository.deleteUser(validatedUser);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        LOGGER.info("Deleted user with ID {}", userId);
        userDeletes.increment();
        return ok();
    }

    private ValidationResult<User> validateCreate(final UserRequest userRequest) {
        final UserValidator userValidator = UserValidator.create(HttpFoldingStatsRetriever.create());
        return userValidator.validateCreate(
            userRequest,
            foldingRepository.getAllUsersWithPasskeys(),
            foldingRepository.getAllHardware(),
            foldingRepository.getAllTeams()
        );
    }

    private ValidationResult<User> validateUpdate(final UserRequest userRequest, final User existingUser) {
        final UserValidator userValidator = UserValidator.create(HttpFoldingStatsRetriever.create());
        return userValidator.validateUpdate(
            userRequest,
            existingUser,
            foldingRepository.getAllUsersWithPasskeys(),
            foldingRepository.getAllHardware(),
            foldingRepository.getAllTeams()
        );
    }

    private ValidationResult<User> validateDelete(final User existingUser) {
        final UserValidator userValidator = UserValidator.create(HttpFoldingStatsRetriever.create());
        return userValidator.validateDelete(existingUser);
    }
}