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

package me.zodac.folding.rest.response;

import static me.zodac.folding.rest.api.util.RestUtilConstants.GSON;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import me.zodac.folding.api.util.DateTimeUtils;
import me.zodac.folding.rest.api.header.RestHeader;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Utility class to simplify returning a {@link ResponseEntity} in the REST layer.
 */
public final class Responses {

    private static final DateTimeUtils DATE_TIME_UTILS = DateTimeUtils.create();

    private Responses() {

    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to request something from the system, but no response body is required, such as a
     * <b>DELETE</b> request, for example.
     *
     * @param <E> the response body type
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
     * Generally used for cases where an HTTP request is sent to request something from the system.
     *
     * @param entity the entity being retrieved
     * @param <E>    the response body type
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
     * Generally used for cases where an HTTP request is sent to retrieve a {@link Collection} of {@link me.zodac.folding.api.ResponsePojo} resources.
     *
     * @param entities the {@link Collection} of entities being retrieved
     * @param <E>      the response body type
     * @return the <b>200_OK</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> ok(final Collection<E> entities) {
        return ResponseEntity
            .ok()
            .header(RestHeader.TOTAL_COUNT.headerName(), String.valueOf(entities.size()))
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
     * @param <E>      the response body type
     * @return the <b>200_OK</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> ok(final E entity, final int entityId) {
        return responseWithLocation(entity, entityId, HttpStatus.OK);
    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to request something from the system, but no response is
     * required, such as a delete request, for example. Will cache the response until the start of the next UTC month.
     *
     * @param entity the entity being retrieved
     * @param <E>    the response body type
     * @return the <b>200_OK</b> {@link ResponseEntity}
     * @see DateTimeUtils#untilNextMonthUtc(TemporalUnit)
     */
    public static <E> ResponseEntity<String> cachedOk(final E entity) {
        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(DATE_TIME_UTILS.untilNextMonthUtc(ChronoUnit.SECONDS), TimeUnit.SECONDS))
            .eTag(String.valueOf(entity.hashCode()))
            .body(GSON.toJson(entity));
    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to retrieve a {@link Collection} of {@link me.zodac.folding.api.ResponsePojo} resources.
     * Will cache the response until the start of the next UTC month.
     *
     * @param entities the {@link Collection} of entities being retrieved
     * @param <E>      the response body type
     * @return the <b>200_OK</b> {@link ResponseEntity}
     * @see DateTimeUtils#untilNextMonthUtc(TemporalUnit)
     */
    public static <E> ResponseEntity<String> cachedOk(final Collection<E> entities) {
        return cachedOk(entities, DATE_TIME_UTILS.untilNextMonthUtc(ChronoUnit.SECONDS));
    }

    /**
     * A <b>200_OK</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an HTTP request is sent to retrieve a {@link Collection} of {@link me.zodac.folding.api.ResponsePojo} resources.
     *
     * @param entities             the {@link Collection} of entities being retrieved
     * @param cachePeriodInSeconds the cache period for the entity in seconds
     * @param <E>                  the response body type
     * @return the <b>200_OK</b> {@link ResponseEntity}
     * @see DateTimeUtils#untilNextMonthUtc(TemporalUnit)
     */
    public static <E> ResponseEntity<String> cachedOk(final Collection<E> entities, final long cachePeriodInSeconds) {
        return ResponseEntity
            .ok()
            .header(RestHeader.TOTAL_COUNT.headerName(), String.valueOf(entities.size()))
            .cacheControl(CacheControl.maxAge(cachePeriodInSeconds, TimeUnit.SECONDS))
            .eTag(String.valueOf(entities.stream().mapToInt(Object::hashCode).sum()))
            .body(GSON.toJson(entities));
    }

    /**
     * A <b>201_CREATED</b> {@link ResponseEntity}.
     *
     * <p>
     * Used for cases where a <b>POST</b> request with a supplied payload has created a resource.
     *
     * @param entity   the created resource
     * @param entityId the ID of the created resource
     * @param <E>      the response body type
     * @return the <b>201_CREATED</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<String> created(final E entity, final int entityId) {
        return responseWithLocation(entity, entityId, HttpStatus.CREATED);
    }

    private static <E> ResponseEntity<String> responseWithLocation(final E entity, final int entityId, final HttpStatus httpStatus) {
        final URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(entityId)
            .toUri();

        return ResponseEntity
            .status(httpStatus)
            .location(location)
            .body(GSON.toJson(entity));
    }

    /**
     * A <b>303_SEE_OTHER</b> {@link ResponseEntity}.
     *
     * <p>
     * Used to redirect to another URL in the 'location' header
     *
     * @param redirectUrl the URL to redirect to
     * @return the <b>303_SEE_OTHER</b> {@link ResponseEntity}
     */
    public static ResponseEntity<String> redirect(final String redirectUrl) {
        return ResponseEntity
            .status(HttpStatus.SEE_OTHER)
            .location(URI.create(redirectUrl))
            .build();
    }

    /**
     * A <b>404_NOT_FOUND</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases when an ID is supplied in a REST request, but no resource exists matching that ID.
     *
     * @param <E> the response body type
     * @return the <b>404_NOT_FOUND</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<E> notFound() {
        return ResponseEntity
            .notFound()
            .build();
    }
}
