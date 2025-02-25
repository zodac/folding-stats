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

import static net.zodac.folding.rest.response.Responses.cachedOk;
import static net.zodac.folding.rest.response.Responses.created;
import static net.zodac.folding.rest.response.Responses.ok;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import net.zodac.folding.api.state.SystemState;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.api.util.LoggerName;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.bean.tc.validation.UserValidator;
import net.zodac.folding.rest.api.tc.request.UserRequest;
import net.zodac.folding.rest.controller.api.UserEndpoint;
import net.zodac.folding.rest.util.ReadRequired;
import net.zodac.folding.rest.util.WriteRequired;
import net.zodac.folding.state.SystemStateManager;
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
 * Implementation of {@link UserEndpoint} REST endpoints.
 */
@RestController
@RequestMapping("/users")
public class UserController implements UserEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

    private final FoldingRepository foldingRepository;
    private final UserValidator userValidator;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param userValidator     the {@link UserValidator}
     */
    @Autowired
    public UserController(final FoldingRepository foldingRepository, final UserValidator userValidator) {
        this.foldingRepository = foldingRepository;
        this.userValidator = userValidator;
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> create(@RequestBody final UserRequest userRequest, final HttpServletRequest request) {
        AUDIT_LOGGER.info("POST request received to create user at '{}' with request: {}", request.getRequestURI(), userRequest);

        final User validatedUser = userValidator.create(userRequest);
        final User elementWithId = foldingRepository.createUser(validatedUser);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Created user with ID {}", elementWithId.id());
        return created(elementWithId, elementWithId.id());
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<User>> getAll(final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received for all users without passkeys at '{}'", request.getRequestURI());
        final Collection<User> elements = foldingRepository.getAllUsersWithoutPasskeys();
        return cachedOk(elements);
    }

    @Override
    @RolesAllowed("admin")
    @ReadRequired
    @GetMapping(path = "/all/passkey", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<User>> getAllWithPasskeys(final HttpServletRequest request) {
        AUDIT_LOGGER.info("GET request received for all users with passkeys at '{}'", request.getRequestURI());
        final Collection<User> elements = foldingRepository.getAllUsersWithPasskeys();
        return cachedOk(elements);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getById(@PathVariable("userId") final int userId, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for user received at '{}'", request.getRequestURI());

        final User element = foldingRepository.getUserWithoutPasskey(userId);
        return cachedOk(element);
    }

    @Override
    @ReadRequired
    @RolesAllowed("admin")
    @GetMapping(path = "/{userId}/passkey", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getByIdWithPasskey(@PathVariable("userId") final int userId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("GET request for user with passkey received at '{}'", request.getRequestURI());

        final User element = foldingRepository.getUserWithPasskey(userId);
        return cachedOk(element);
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PutMapping(path = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> updateById(@PathVariable("userId") final int userId,
                                           @RequestBody final UserRequest userRequest,
                                           final HttpServletRequest request) {
        AUDIT_LOGGER.info("PUT request for user received at '{}' with request {}", request.getRequestURI(), userRequest);

        final User existingUser = foldingRepository.getUserWithPasskey(userId);

        if (existingUser.isEqualRequest(userRequest)) {
            AUDIT_LOGGER.info("Request is same as existing user");
            final User userWithHiddenPasskey = User.hidePasskey(existingUser);
            return ok(userWithHiddenPasskey);
        }

        final User validatedUser = userValidator.update(userRequest, existingUser);

        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final User userWithId = User.updateWithId(existingUser.id(), validatedUser);
        final User updatedUserWithId = foldingRepository.updateUser(userWithId, existingUser);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Updated user with ID {}", updatedUserWithId.id());
        return ok(updatedUserWithId, updatedUserWithId.id());
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @DeleteMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable("userId") final int userId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("DELETE request for user received at '{}'", request.getRequestURI());

        final User user = foldingRepository.getUserWithoutPasskey(userId);
        final User validatedUser = userValidator.delete(user);

        foldingRepository.deleteUser(validatedUser);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        AUDIT_LOGGER.info("Deleted user with ID {}", userId);
        return ok();
    }
}
