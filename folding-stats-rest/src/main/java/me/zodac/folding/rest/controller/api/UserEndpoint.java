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

package me.zodac.folding.rest.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST endpoints for {@code Team Competition} {@link User}s.
 */
public interface UserEndpoint {

    /**
     * {@link PostMapping} request to create a {@link User} based on the input request.
     *
     * @param userRequest the {@link UserRequest} to create a {@link User}
     * @param request     the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#created(Object, int)} containing the created {@link User}
     */
    ResponseEntity<User> create(@RequestBody UserRequest userRequest, HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve all {@link User}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link User}s
     */
    ResponseEntity<Collection<User>> getAll(HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve all {@link User}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link User}s
     */
    ResponseEntity<Collection<User>> getAllWithPasskeys(HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link User} by {@code userId}.
     *
     * @param userId  the ID of the {@link User} to retrieve
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link User}
     */
    ResponseEntity<User> getById(@PathVariable("userId") int userId, HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link User} by {@code userId}, with the passkey exposed.
     *
     * @param userId  the ID of the {@link User} to retrieve
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link User}
     */
    ResponseEntity<User> getByIdWithPasskey(@PathVariable("userId") int userId, HttpServletRequest request);

    /**
     * {@link PutMapping} request to update an existing {@link User} based on the input request.
     *
     * @param userId      the ID of the {@link User} to be updated
     * @param userRequest the {@link UserRequest} to update a {@link User}
     * @param request     the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object, int)} containing the updated {@link User}
     */
    ResponseEntity<User> updateById(@PathVariable("userId") int userId, @RequestBody UserRequest userRequest, HttpServletRequest request);

    /**
     * {@link DeleteMapping} request to delete an existing {@link User}.
     *
     * @param userId  the ID of the {@link User} to be deleted
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    ResponseEntity<Void> deleteById(@PathVariable("userId") int userId, HttpServletRequest request);
}
