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

import static net.zodac.folding.rest.response.Responses.created;
import static net.zodac.folding.rest.response.Responses.ok;
import static net.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.zodac.folding.api.tc.change.UserChange;
import net.zodac.folding.api.tc.change.UserChangeState;
import net.zodac.folding.api.util.LoggerName;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.bean.tc.user.UserChangeApplier;
import net.zodac.folding.bean.tc.validation.UserChangeValidator;
import net.zodac.folding.rest.api.tc.request.UserChangeRequest;
import net.zodac.folding.rest.controller.api.UserChangeEndpoint;
import net.zodac.folding.rest.exception.InvalidStateException;
import net.zodac.folding.rest.util.ReadRequired;
import net.zodac.folding.rest.util.WriteRequired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implementation of {@link UserChangeEndpoint} REST endpoints.
 */
@RestController
@RequestMapping("/changes")
public class UserChangeController implements UserChangeEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());
    private static final Pattern SINGLE_COMMA_PATTERN = Pattern.compile(",");

    private final FoldingRepository foldingRepository;
    private final UserChangeApplier userChangeApplier;
    private final UserChangeValidator userChangeValidator;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository   the {@link FoldingRepository}
     * @param userChangeApplier   the {@link UserChangeApplier}
     * @param userChangeValidator the {@link UserChangeValidator}
     */
    @Autowired
    public UserChangeController(final FoldingRepository foldingRepository,
                                final UserChangeApplier userChangeApplier,
                                final UserChangeValidator userChangeValidator) {
        this.foldingRepository = foldingRepository;
        this.userChangeApplier = userChangeApplier;
        this.userChangeValidator = userChangeValidator;
    }

    @Override
    @PermitAll
    @WriteRequired
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserChange> create(@RequestBody final UserChangeRequest userChangeRequest,
                                             final HttpServletRequest request) {
        AUDIT_LOGGER.info("POST request for user change received at '{}' with request: {}", request.getRequestURI(), userChangeRequest);

        final UserChange validatedUserChange = userChangeValidator.validate(userChangeRequest);
        final UserChange createdUserChange = foldingRepository.createUserChange(validatedUserChange);

        // TODO: When creating this change, does the response include user passkeys?
        return created(createdUserChange, createdUserChange.id());
    }

    @Override
    @ReadRequired
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<UserChange>> getAll(
        @RequestParam(value = "state", required = false, defaultValue = UserChangeState.ALL_STATES) final String state,
        @RequestParam(value = "numberOfMonths", required = false, defaultValue = "0") final long numberOfMonths,
        final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for all user changes received at '{}?{}'", request.getRequestURI(), extractParameters(request));

        final Collection<UserChangeState> states = getStatesBasedOnInput(state);
        if (states.isEmpty()) {
            return ok(List.of());
        }

        final Collection<UserChange> userChanges = foldingRepository.getAllUserChangesWithoutPasskeys(states, numberOfMonths);
        return ok(userChanges);
    }

    private static Collection<UserChangeState> getStatesBasedOnInput(final CharSequence state) {
        if (UserChangeState.ALL_STATES.contentEquals(state)) {
            return UserChangeState.getAllValues();
        }

        return Arrays.stream(SINGLE_COMMA_PATTERN.split(state))
            .map(UserChangeState::get)
            .filter(userChangeState -> userChangeState != UserChangeState.INVALID)
            .collect(Collectors.toSet());
    }

    @Override
    @RolesAllowed("admin")
    @ReadRequired
    @GetMapping(path = "/passkey", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<UserChange>> getAllWithPasskeys(
        @RequestParam(value = "state", required = false, defaultValue = UserChangeState.ALL_STATES) final String state,
        @RequestParam(value = "numberOfMonths", required = false, defaultValue = "0") final long numberOfMonths,
        final HttpServletRequest request) {
        AUDIT_LOGGER.info("GET request for all user changes with passkey received at '{}?{}'", request.getRequestURI(), extractParameters(request));

        final Collection<UserChangeState> states = getStatesBasedOnInput(state);
        if (states.isEmpty()) {
            return ok(List.of());
        }

        final Collection<UserChange> userChanges = foldingRepository.getAllUserChangesWithPasskeys(states, numberOfMonths);
        return ok(userChanges);
    }

    @Override
    @RolesAllowed("admin")
    @ReadRequired
    @GetMapping(path = "/{userChangeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserChange> getById(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("GET request for user change received at '{}'", request.getRequestURI());

        final UserChange userChange = foldingRepository.getUserChange(userChangeId);
        return ok(userChange);
    }

    @Override
    @RolesAllowed("admin")
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/approve/immediate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserChange> approveImmediately(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("PUT request to approve user change immediately received at '{}'", request.getRequestURI());
        return update(userChangeId, UserChangeState.APPROVED_NOW);
    }

    @Override
    @RolesAllowed("admin")
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/approve/next", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserChange> approveNextMonth(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("PUT request to approve user change next month received at '{}'", request.getRequestURI());
        return update(userChangeId, UserChangeState.APPROVED_NEXT_MONTH);
    }

    @Override
    @RolesAllowed("admin")
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserChange> reject(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("PUT request to reject user change received at '{}'", request.getRequestURI());
        return update(userChangeId, UserChangeState.REJECTED);
    }

    private ResponseEntity<UserChange> update(final int userChangeId, final UserChangeState newState) {
        AUDIT_LOGGER.debug("Updating UserChange ID {} to state {}", userChangeId, newState);

        final UserChange existingUserChange = foldingRepository.getUserChange(userChangeId);
        if (existingUserChange.state().isFinalState()) {
            throw new InvalidStateException(existingUserChange.state(), newState);
        }

        final UserChange userChangeToUpdate = UserChange.updateWithState(newState, existingUserChange);
        final UserChange updatedUserChange = foldingRepository.updateUserChange(userChangeToUpdate);

        if (newState == UserChangeState.APPROVED_NOW) {
            AUDIT_LOGGER.info("User change has been set to {}, applying change immediately", UserChangeState.APPROVED_NOW);
            final UserChange appliedUserChange = userChangeApplier.apply(updatedUserChange);
            final UserChange maskedUserChange = UserChange.hidePasskey(appliedUserChange);
            return ok(maskedUserChange, maskedUserChange.id());
        }

        final UserChange maskedUserChange = UserChange.hidePasskey(updatedUserChange);
        return ok(maskedUserChange, maskedUserChange.id());
    }
}
