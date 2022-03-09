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

import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.FoldingRepository;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.bean.tc.user.UserChangeApplier;
import me.zodac.folding.bean.tc.validation.UserChangeValidator;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import me.zodac.folding.rest.exception.InvalidStateException;
import me.zodac.folding.rest.response.Responses;
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
 * REST endpoints for <code>Team Competition</code> {@link UserChange}s.
 */
@RestController
@RequestMapping("/changes")
public class UserChangeEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ALL_STATES_OPTION = "*";

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
    public UserChangeEndpoint(final FoldingRepository foldingRepository,
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

    /**
     * {@link PostMapping} request to create a {@link UserChange} based on the input request.
     *
     * @param userChangeRequest the {@link UserChangeRequest} to create a {@link UserChange}
     * @param request           the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#created(Object, int)} containing the created {@link UserChange}
     */
    @PermitAll
    @WriteRequired
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody final UserChangeRequest userChangeRequest,
                                    final HttpServletRequest request) {
        LOGGER.info("POST request for user change received at '{}'", request::getRequestURI);

        final UserChange validatedUserChange = userChangeValidator.validate(userChangeRequest);
        final UserChange createdUserChange = foldingRepository.createUserChange(validatedUserChange);

        userChangeCreates.increment();
        return created(createdUserChange, createdUserChange.getId());
    }

    /**
     * {@link GetMapping} request to retrieve all {@link UserChange}s with one of the provides {@link UserChangeState}s.
     *
     * @param state          a comma-separated {@link String} of the {@link UserChangeState}s to look for
     * @param numberOfMonths the number of months back from which to retrieve {@link UserChange}s (<b>0</b> means retrieve all)
     * @param request        the {@link HttpServletRequest}
     * @return {@link Responses#ok(Collection)} containing the {@link UserChange}s
     */
    @ReadRequired
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(@RequestParam(value = "state", required = false, defaultValue = ALL_STATES_OPTION) final String state,
                                    @RequestParam(value = "numberOfMonths", required = false, defaultValue = "0") final int numberOfMonths,
                                    final HttpServletRequest request) {
        LOGGER.info("GET request for all user changes received at '{}?{}'", request::getRequestURI, () -> extractParameters(request));

        final Collection<UserChangeState> states = getStatesBasedOnInput(state);
        if (states.isEmpty()) {
            return ok();
        }

        final Collection<UserChange> userChanges = foldingRepository.getAllUserChangesWithoutPasskeys(states, numberOfMonths);
        return ok(userChanges);
    }

    private Collection<UserChangeState> getStatesBasedOnInput(final String state) {
        if (ALL_STATES_OPTION.equals(state)) {
            return UserChangeState.getAllValues();
        }

        return Arrays.stream(state.split(","))
            .map(UserChangeState::get)
            .filter(userChangeState -> userChangeState != UserChangeState.INVALID)
            .collect(Collectors.toSet());
    }

    /**
     * {@link GetMapping} request to retrieve all {@link UserChange}s.
     *
     * @param state          a comma-separated {@link String} of the {@link UserChangeState}s to look for
     * @param numberOfMonths the number of months back from which to retrieve {@link UserChange}s (<b>0</b> means retrieve all)
     * @param request        the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Collection)} containing the {@link UserChange}s
     */
    @RolesAllowed("admin")
    @ReadRequired
    @GetMapping(path = "/passkey", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllWithPasskeys(@RequestParam(value = "state", required = false, defaultValue = ALL_STATES_OPTION) final String state,
                                                @RequestParam(value = "numberOfMonths", required = false, defaultValue = "0")
                                                final int numberOfMonths,
                                                final HttpServletRequest request) {
        LOGGER.info("GET request for all user changes with passkey received at '{}?{}'", request::getRequestURI, () -> extractParameters(request));

        final Collection<UserChangeState> states = getStatesBasedOnInput(state);
        if (states.isEmpty()) {
            return ok();
        }

        final Collection<UserChange> userChanges = foldingRepository.getAllUserChangesWithPasskeys(states, numberOfMonths);
        return ok(userChanges);
    }

    /**
     * {@link GetMapping} request to retrieve a {@link UserChange} by {@code userChangeId}.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object)} containing the {@link UserChange}
     */
    @RolesAllowed("admin")
    @ReadRequired
    @GetMapping(path = "/{userChangeId}", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<?> getById(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        LOGGER.info("GET request for user change received at '{}'", request::getRequestURI);

        final UserChange userChange = foldingRepository.getUserChange(userChangeId);
        return ok(userChange);
    }

    /**
     * {@link PutMapping} request to update a {@link UserChange} to {@link UserChangeState#APPROVED_NOW}.
     *
     * <p>
     * Also applies the {@link UserChange} right away.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object, int)} containing the updated {@link UserChange}
     * @see UserChangeApplier#apply(UserChange)
     */
    @RolesAllowed("admin")
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/approve/immediate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> approveImmediately(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        LOGGER.info("PUT request to approve user change immediately received at '{}'", request::getRequestURI);
        userChangeImmediateApprovals.increment();
        return update(userChangeId, UserChangeState.APPROVED_NOW);
    }

    /**
     * {@link PutMapping} request to update a {@link UserChange} to {@link UserChangeState#APPROVED_NEXT_MONTH}.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object, int)} containing the updated {@link UserChange}
     */
    @RolesAllowed("admin")
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/approve/next", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> approveNextMonth(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        LOGGER.info("PUT request to approve user change next month received at '{}'", request::getRequestURI);
        userChangeNextMonthApprovals.increment();
        return update(userChangeId, UserChangeState.APPROVED_NEXT_MONTH);
    }

    /**
     * {@link PutMapping} request to update a {@link UserChange} to {@link UserChangeState#REJECTED}.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object, int)} containing the updated {@link UserChange}
     */
    @RolesAllowed("admin")
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reject(@PathVariable("userChangeId") final int userChangeId, final HttpServletRequest request) {
        LOGGER.info("PUT request to reject user change received at '{}'", request::getRequestURI);
        userChangeRejects.increment();
        return update(userChangeId, UserChangeState.REJECTED);
    }

    private ResponseEntity<?> update(final int userChangeId, final UserChangeState newState) {
        LOGGER.info("Updating UserChange ID {} to state {}", userChangeId, newState);

        final UserChange existingUserChange = foldingRepository.getUserChange(userChangeId);
        if (existingUserChange.getState().isFinalState()) {
            throw new InvalidStateException(existingUserChange.getState(), newState);
        }

        final UserChange userChangeToUpdate = UserChange.updateWithState(newState, existingUserChange);
        final UserChange updatedUserChange = foldingRepository.updateUserChange(userChangeToUpdate);

        if (newState == UserChangeState.APPROVED_NOW) {
            LOGGER.info("Requested for now, applying change");
            final UserChange appliedUserChange = userChangeApplier.apply(updatedUserChange);
            final UserChange maskedUserChange = UserChange.hidePasskey(appliedUserChange);
            return ok(maskedUserChange, maskedUserChange.getId());
        }

        final UserChange maskedUserChange = UserChange.hidePasskey(updatedUserChange);
        return ok(maskedUserChange, maskedUserChange.getId());
    }
}