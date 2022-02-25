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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.util.DateTimeUtils;
import me.zodac.folding.bean.FoldingRepository;
import me.zodac.folding.bean.tc.user.UserChangeApplier;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import me.zodac.folding.rest.util.IntegerParser;
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

    @Autowired
    private UserChangeApplier userChangeApplier;

    @Autowired
    private FoldingRepository foldingRepository;

    // Prometheus counters
    private final Counter userChangeCreates;
    private final Counter userChangeRejects;
    private final Counter userChangeImmediateApprovals;
    private final Counter userChangeNextMonthApprovals;

    /**
     * Constructor to inject {@link MeterRegistry} and configure Prometheus {@link Counter}s.
     *
     * @param registry the Prometheus {@link MeterRegistry}
     */
    public UserChangeEndpoint(final MeterRegistry registry) {
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

        final User existingUser = foldingRepository.getUserWithPasskey(userChangeRequest.getUserId());

        // TODO: Already existing check needed
        // TODO: Validation needed

        final User changedUser = User.create(
            existingUser.getId(),
            userChangeRequest.getFoldingUserName(),
            existingUser.getDisplayName(),
            userChangeRequest.getPasskey(),
            existingUser.getCategory(),
            existingUser.getProfileLink(),
            userChangeRequest.getLiveStatsLink(),
            foldingRepository.getHardware(userChangeRequest.getHardwareId()),
            existingUser.getTeam(),
            existingUser.isUserIsCaptain()
        );

        final UserChangeState state = userChangeRequest.isImmediate() ? UserChangeState.REQUESTED_NOW : UserChangeState.REQUESTED_NEXT_MONTH;

        LOGGER.info("User to update: {}", changedUser);
        LOGGER.info("State: {}", state);
        final LocalDateTime currentUtcTimestamp = DateTimeUtils.currentUtcLocalDateTime();
        final UserChange userChange = UserChange.createWithoutId(currentUtcTimestamp, currentUtcTimestamp, changedUser, state);
        final UserChange createdUserChange = foldingRepository.createUserChange(userChange);

        userChangeCreates.increment();
        return created(createdUserChange, createdUserChange.getId());
    }

    /**
     * {@link GetMapping} request to retrieve all {@link UserChange}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Collection)} containing the {@link UserChange}s
     */
    @ReadRequired
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(final HttpServletRequest request) {
        LOGGER.info("GET request for all user changes received at '{}'", request::getRequestURI);

        final Collection<UserChange> userChanges = foldingRepository.getAllUserChanges();
        return ok(userChanges);
    }

    /**
     * {@link GetMapping} request to retrieve all {@link UserChange}s with one of the provides {@link UserChangeState}s.
     *
     * @param state   a comma-separated {@link String} of the {@link UserChangeState}s to look for
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Collection)} containing the {@link UserChange}s
     */
    @ReadRequired
    @GetMapping(path = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllByState(@RequestParam("state") final String state, final HttpServletRequest request) {
        LOGGER.info("GET request for all user changes with state received at '{}'", request::getRequestURI);

        final Collection<UserChangeState> states = new HashSet<>();

        // TODO: Validate properly
        if (state.contains(",")) {
            final String[] rawStates = state.split(",");
            for (final String rawState : rawStates) {
                states.add(UserChangeState.get(rawState));
            }
            states.remove(UserChangeState.INVALID);
        } else {
            states.add(UserChangeState.get(state));
        }

        final Collection<UserChange> userChanges = foldingRepository.getAllUserChanges(states);

        // TODO: Hide passkeys
        return ok(userChanges);
    }

    /**
     * {@link GetMapping} request to retrieve a {@link UserChange} by {@code userChangeId}.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object)} containing the {@link UserChange}
     */
    @ReadRequired
    @GetMapping(path = "/{userChangeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable("userChangeId") final String userChangeId, final HttpServletRequest request) {
        LOGGER.info("GET request for user change received at '{}'", request::getRequestURI);

        final int parsedId = IntegerParser.parsePositive(userChangeId);
        final UserChange userChange = foldingRepository.getUserChange(parsedId);
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
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     * @see UserChangeApplier#apply(UserChange)
     */
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/approve/immediate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> approveImmediately(@PathVariable("userChangeId") final String userChangeId,
                                                final HttpServletRequest request) {
        LOGGER.info("PUT request to approve user change immediately received at '{}'", request::getRequestURI);
        userChangeImmediateApprovals.increment();
        return update(userChangeId, UserChangeState.APPROVED_NOW);
    }

    /**
     * {@link PutMapping} request to update a {@link UserChange} to {@link UserChangeState#APPROVED_NEXT_MONTH}.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/approve/next", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> approveNextMonth(@PathVariable("userChangeId") final String userChangeId,
                                              final HttpServletRequest request) {
        LOGGER.info("PUT request to approve user change next month received at '{}'", request::getRequestURI);
        userChangeNextMonthApprovals.increment();
        return update(userChangeId, UserChangeState.APPROVED_NEXT_MONTH);
    }

    /**
     * {@link PutMapping} request to update a {@link UserChange} to {@link UserChangeState#REJECTED}.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @PutMapping(path = "/{userChangeId}/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> reject(@PathVariable("userChangeId") final String userChangeId, final HttpServletRequest request) {
        LOGGER.info("PUT request to reject user change received at '{}'", request::getRequestURI);
        userChangeRejects.increment();
        return update(userChangeId, UserChangeState.REJECTED);
    }

    private ResponseEntity<?> update(final String userChangeId, final UserChangeState newState) {
        LOGGER.info("Updating UserChange ID {} to state {}", userChangeId, newState);
        final int id = IntegerParser.parsePositive(userChangeId);

        // TODO: Check if one exists with ID first, then verify it is in a changeable state
        foldingRepository.updateUserChange(id, newState); // TODO: Return from the update

        if (newState == UserChangeState.APPROVED_NOW) {
            LOGGER.info("Requested for now, applying change");
            final UserChange cr = foldingRepository.getUserChange(id);
            userChangeApplier.apply(cr);
        }
        return ok();
    }
}