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

package net.zodac.folding.rest.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import net.zodac.folding.api.tc.change.UserChange;
import net.zodac.folding.api.tc.change.UserChangeState;
import net.zodac.folding.bean.tc.user.UserChangeApplier;
import net.zodac.folding.rest.api.tc.request.UserChangeRequest;
import net.zodac.folding.rest.response.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST endpoints for {@code Team Competition} {@link UserChange}s.
 */
@Tag(name = "UserChange Endpoints", description = "REST endpoints to create, read, approve and reject user change requests on the system")
public interface UserChangeEndpoint {

    /**
     * {@link PostMapping} request to create a {@link UserChange} based on the input request.
     *
     * @param userChangeRequest the {@link UserChangeRequest} to create a {@link UserChange}
     * @param request           the {@link HttpServletRequest}
     * @return {@link Responses#created(Object, int)} containing the created {@link UserChange}
     */
    @Operation(summary = "Create a new user change with the given properties")
    ResponseEntity<UserChange> create(@RequestBody UserChangeRequest userChangeRequest, HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve all {@link UserChange}s with one of the provides {@link UserChangeState}s.
     *
     * @param state          a comma-separated {@link String} of the {@link UserChangeState}s to look for
     * @param numberOfMonths the number of months back from which to retrieve {@link UserChange}s (<b>0</b> means retrieve all)
     * @param request        the {@link HttpServletRequest}
     * @return {@link Responses#ok(Collection)} containing the {@link UserChange}s
     */
    ResponseEntity<Collection<UserChange>> getAll(
        @RequestParam(value = "state", required = false, defaultValue = UserChangeState.ALL_STATES) String state,
        @RequestParam(value = "numberOfMonths", required = false, defaultValue = "0") long numberOfMonths,
        HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve all {@link UserChange}s.
     *
     * @param state          a comma-separated {@link String} of the {@link UserChangeState}s to look for
     * @param numberOfMonths the number of months back from which to retrieve {@link UserChange}s (<b>0</b> means retrieve all)
     * @param request        the {@link HttpServletRequest}
     * @return {@link Responses#ok(Collection)} containing the {@link UserChange}s
     */
    ResponseEntity<Collection<UserChange>> getAllWithPasskeys(
        @RequestParam(value = "state", required = false, defaultValue = UserChangeState.ALL_STATES) String state,
        @RequestParam(value = "numberOfMonths", required = false, defaultValue = "0") long numberOfMonths,
        HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link UserChange} by {@code userChangeId}.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link Responses#ok(Object)} containing the {@link UserChange}
     */
    ResponseEntity<UserChange> getById(@PathVariable("userChangeId") int userChangeId, HttpServletRequest request);

    /**
     * {@link PutMapping} request to update a {@link UserChange} to {@link UserChangeState#APPROVED_NOW}.
     *
     * <p>
     * Also applies the {@link UserChange} right away.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link Responses#ok(Object, int)} containing the updated {@link UserChange}
     * @see UserChangeApplier#apply(UserChange)
     */
    ResponseEntity<UserChange> approveImmediately(@PathVariable("userChangeId") int userChangeId, HttpServletRequest request);

    /**
     * {@link PutMapping} request to update a {@link UserChange} to {@link UserChangeState#APPROVED_NEXT_MONTH}.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link Responses#ok(Object, int)} containing the updated {@link UserChange}
     */
    ResponseEntity<UserChange> approveNextMonth(@PathVariable("userChangeId") int userChangeId, HttpServletRequest request);

    /**
     * {@link PutMapping} request to update a {@link UserChange} to {@link UserChangeState#REJECTED}.
     *
     * @param userChangeId the ID of the {@link UserChange} to be updated
     * @param request      the {@link HttpServletRequest}
     * @return {@link Responses#ok(Object, int)} containing the updated {@link UserChange}
     */
    ResponseEntity<UserChange> reject(@PathVariable("userChangeId") int userChangeId, HttpServletRequest request);
}
