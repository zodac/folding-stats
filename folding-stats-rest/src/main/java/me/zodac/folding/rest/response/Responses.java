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

package me.zodac.folding.rest.response;

import static me.zodac.folding.rest.util.RestUtilConstants.GSON;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Utility class to simplify returning a {@link ResponseEntity} in the REST layer.
 */
// TODO: [zodac] Some of these can be replaced by ResponseEntity functions
public final class Responses {

    private Responses() {

    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to request something from the system, but no response is
     * required, such as a delete request, for example.
     *
     * @return the <b>200_OK</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<E> ok() {
        return ResponseEntity
            .ok()
            .build();
    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to request something from the system, but no response is
     * required, such as a delete request, for example.
     *
     * @param entity the entity being retrieved
     * @return the <b>200_OK</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> ok(final E entity) {
        return ResponseEntity
            .ok()
            .body(GSON.toJson(entity));
    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to request something from the system, but no response is
     * required, such as a delete request, for example.
     *
     * @param entity               the entity being retrieved
     * @param cachePeriodInSeconds the cache period for the entity in seconds
     * @return the <b>200_OK</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> cachedOk(final E entity, final long cachePeriodInSeconds) {
        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(cachePeriodInSeconds, TimeUnit.SECONDS))
            .body(GSON.toJson(entity));
    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to retrieve a {@link Collection} of {@link me.zodac.folding.api.ResponsePojo} resources.
     *
     * @param entities the {@link Collection} of entities being retrieved
     * @return the <b>200_OK</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> ok(final Collection<E> entities) {
        return ResponseEntity
            .ok()
            .header("X-Total-Count", String.valueOf(entities.size()))
            .body(GSON.toJson(entities));
    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to retrieve a {@link Collection} of {@link me.zodac.folding.api.ResponsePojo} resources.
     *
     * @param entities             the {@link Collection} of entities being retrieved
     * @param cachePeriodInSeconds the cache period for the entity in seconds
     * @return the <b>200_OK</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> cachedOk(final Collection<E> entities, final long cachePeriodInSeconds) {
        return ResponseEntity
            .ok()
            .header("X-Total-Count", String.valueOf(entities.size()))
            .cacheControl(CacheControl.maxAge(cachePeriodInSeconds, TimeUnit.SECONDS))
            .body(GSON.toJson(entities));
    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to update a single {@link me.zodac.folding.api.ResponsePojo} resource.
     *
     * @param entity   the updated resource
     * @param entityId the ID of the updated resource
     * @return the <b>200_OK</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> ok(final E entity, final int entityId) {
        return responseWithLocation(entity, entityId, HttpStatus.OK);
    }

    /**
     * A <b>201_CREATED</b> {@link ResponseEntity}.
     *
     * <p>
     * Used for cases where a <b>POST</b> request with a supplied payload has created a resource.
     *
     * @param entity   the created resource
     * @param entityId the ID of the created resource
     * @return the <b>201_CREATED</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> created(final E entity, final int entityId) {
        return responseWithLocation(entity, entityId, HttpStatus.CREATED);
    }

    private static <E> ResponseEntity<String> responseWithLocation(final E entity, final int entityId, final HttpStatus httpStatus) {
        final String location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(entityId)
            .toUriString();

        return ResponseEntity
            .status(httpStatus)
            .header(HttpHeaders.LOCATION, location)
            .body(GSON.toJson(entity));
    }

    /**
     * A <b>400_BAD_REQUEST</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where the REST request has some invalid data. This can be malformed data, or an
     * invalid payload, or any other similar error.
     *
     * @param errorMessage an error message defining what part of the input payload caused the error
     * @return the <b>400_BAD_REQUEST</b> {@link ResponseEntity}
     */
    public static ResponseEntity<String> badRequest(final String errorMessage) {
        return ResponseEntity
            .badRequest()
            .body(GSON.toJson(ErrorResponse.create(errorMessage)));
    }

    /**
     * A <b>400_BAD_REQUEST</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where the REST request has some invalid data. This can be malformed data, or an
     * invalid payload, or any other similar error.
     *
     * @param entity the entity in the payload that caused the error
     * @return the <b>400_BAD_REQUEST</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> badRequest(final E entity) {
        return ResponseEntity
            .badRequest()
            .body(GSON.toJson(entity));
    }

    /**
     * A <b>400_BAD_REQUEST</b> {@link ResponseEntity}.
     *
     * <p>
     * Used for cases where the REST request has an empty or null payload.
     *
     * @return the <b>400_BAD_REQUEST</b> {@link ResponseEntity}
     */
    public static ResponseEntity<String> nullRequest() {
        return ResponseEntity
            .badRequest()
            .body(GSON.toJson(ErrorResponse.create("Payload is null")));
    }

    /**
     * A <b>401_UNAUTHORIZED</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where the user has not been successfully authenticated, and cannot be authorized to
     * execute the REST endpoint.
     *
     * @return the <b>401_UNAUTHORIZED</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<E> unauthorized() {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .build();

    }

    /**
     * A <b>403_FORBIDDEN</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where a user has successfully authenticated, but does not have the required
     * authorization to execute the REST endpoint.
     *
     * @return the <b>403_FORBIDDEN</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<E> forbidden() {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .build();
    }

    /**
     * A <b>404_NOT_FOUND</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases when an ID is supplied in a REST request, but no resource exists matching that ID..
     *
     * @return the <b>404_NOT_FOUND</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<E> notFound() {
        return ResponseEntity
            .notFound()
            .build();
    }

    /**
     * A <b>409_CONFLICT</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where the REST request is trying to do one of the following:
     * <ul>
     *     <li>Create a resource that already exists</li>
     *     <li>Update a resource with a value that conflicts with another resource</li>
     *     <li>Delete a resource that is being used by another resource</li>
     * </ul>
     *
     * @param entity the entity in the payload that caused the error
     * @return the <b>409_CONFLICT</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> conflict(final E entity) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(GSON.toJson(entity));
    }

    /**
     * A <b>500_INTERNAL_SERVER_ERROR</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an unexpected error has occurred.
     *
     * @return the <b>500_INTERNAL_SERVER_ERROR</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<E> serverError() {
        return ResponseEntity
            .internalServerError()
            .build();
    }
}
