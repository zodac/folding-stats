/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.user.UserChangeApplier;
import me.zodac.folding.bean.tc.validation.UserChangeValidator;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import me.zodac.folding.rest.controller.api.UserChangeEndpoint;
import me.zodac.folding.rest.exception.InvalidStateException;
import me.zodac.folding.rest.util.ReadRequired;
import me.zodac.folding.rest.util.WriteRequired;
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

    // Prometheus counters
    private final Counter userChangeCreates;
    private final Counter userChangeRejects;
    private final Counter userChangeImmediateApprovals;
    private final Counter userChangeNextMonthApprovals;

    /**
     * {@link Autowired} constructor to inject {@link MeterRegistry} and configure Prometheus {@link Counter}s.
     *
     * @param foldingRepository   the {@link FoldingRepository}
     * @param registry            the Prometheus {@link MeterRegistry}
     * @param userChangeApplier   the {@link UserChangeApplier}
     * @param userChangeValidator the {@link UserChangeValidator}
     */
    @Autowired
    public UserChangeController(final FoldingRepository foldingRepository,
                                final MeterRegistry registry,
                                final UserChangeApplier userChangeApplier,
                                final UserChangeValidator userChangeValidator) {
        this.foldingRepository = foldingRepository;
        this.userChangeApplier = userChangeApplier;
        this.userChangeValidator = userChangeValidator;

        userChangeCreates = Counter.builder("user_change_create_counter")
            .description("Number of UserChange creations through the REST endpoint")
            .register(registry);
        userChangeRejects = Counter.builder("user_change_reject_counter")
            .description("Number of UserChange rejections through the REST endpoint")
            .register(registry);
        userChangeImmediateApprovals = Counter.builder("user_change_approval_immediate_counter")
            .description("Number of UserChange approvals for immediate change through the REST endpoint")
            .register(registry);
        userChangeNextMonthApprovals = Counter.builder("user_change_approval_next_month_counter")
            .description("Number of UserChange approvals for next month through the REST endpoint")
            .register(registry);
    }

    @Override
    @PermitAll
    @WriteRequired
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserChange> create(@RequestBody final UserChangeRequest userChangeRequest,
                                             final HttpServletRequest request) {
        AUDIT_LOGGER.info("POST request for user change received at '{}' with request: {}", request::getRequestURI, () -> userChangeRequest);

        final UserChange validatedUserChange = userChangeValidator.validate(userChangeRequest);
        final UserChange createdUserChange = foldingRepository.createUserChange(validatedUserChange);

        userChangeCreates.increment();

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
        AUDIT_LOGGER.debug("GET request for all user changes received at '{}?{}'", request::getRequestURI, () -> extractParameters(request));

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
        AUDIT_LOGGER.info("GET request for all user changes with passkey received at '{}?{}'", request::getRequestURI,
            () -> extractParameters(request));

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
        AUDIT_LOGGER.info("GET request for user change received at '{}'", request::getRequestURI);

        final UserChange userChange = foldingRepository.getUserChange(userChangeId);
        return ok(userChange);
    }

    @Override
    @RolesAllowed("admin")
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/approve/immediate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserChange> approveImmediately(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("PUT request to approve user change immediately received at '{}'", request::getRequestURI);
        userChangeImmediateApprovals.increment();
        return update(userChangeId, UserChangeState.APPROVED_NOW);
    }

    @Override
    @RolesAllowed("admin")
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/approve/next", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserChange> approveNextMonth(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("PUT request to approve user change next month received at '{}'", request::getRequestURI);
        userChangeNextMonthApprovals.increment();
        return update(userChangeId, UserChangeState.APPROVED_NEXT_MONTH);
    }

    @Override
    @RolesAllowed("admin")
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserChange> reject(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("PUT request to reject user change received at '{}'", request::getRequestURI);
        userChangeRejects.increment();
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
