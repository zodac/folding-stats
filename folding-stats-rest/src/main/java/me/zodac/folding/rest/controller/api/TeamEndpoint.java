/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

package me.zodac.folding.rest.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST endpoints for {@code Team Competition} {@link Team}s.
 */
public interface TeamEndpoint {

    /**
     * {@link PostMapping} request to create a {@link Team} based on the input request.
     *
     * @param teamRequest the {@link TeamRequest} to create a {@link Team}
     * @param request     the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#created(Object, int)} containing the created {@link Team}
     */
    ResponseEntity<Team> create(@RequestBody TeamRequest teamRequest, HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve all {@link Team}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Team}s
     */
    ResponseEntity<Collection<Team>> getAll(HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link Team} by {@code teamId}.
     *
     * @param teamId  the ID of the {@link Team} to retrieve
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link Team}
     */
    ResponseEntity<Team> getById(@PathVariable("teamId") int teamId, HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link Team} by {@code teamName}.
     *
     * @param teamName the {@code teamName} of the {@link Team} to retrieve
     * @param request  the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link Team}
     */
    ResponseEntity<Team> getByTeamName(@RequestParam("teamName") String teamName, HttpServletRequest request);

    /**
     * {@link PutMapping} request to update an existing {@link Team} based on the input request.
     *
     * @param teamId      the ID of the {@link Team} to be updated
     * @param teamRequest the {@link TeamRequest} to update a {@link Team}
     * @param request     the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object, int)} containing the updated {@link Team}
     */
    ResponseEntity<Team> updateById(@PathVariable("teamId") int teamId, @RequestBody TeamRequest teamRequest, HttpServletRequest request);

    /**
     * {@link DeleteMapping} request to delete an existing {@link Team}.
     *
     * @param teamId  the ID of the {@link Team} to be deleted
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    ResponseEntity<Void> deleteById(@PathVariable("teamId") int teamId, HttpServletRequest request);
}
