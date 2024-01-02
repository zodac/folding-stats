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

package me.zodac.folding.rest.response;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import me.zodac.folding.api.util.DateTimeUtils;
import me.zodac.folding.rest.api.header.RestHeader;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
     * @return the <b>200_OK</b> {@link ResponseEntity}
     */
    public static ResponseEntity<Void> ok() {
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
    public static <E> ResponseEntity<E> ok(final E entity) {
        return ResponseEntity
            .ok()
            .body(entity);
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
    public static <E> ResponseEntity<Collection<E>> ok(final Collection<E> entities) {
        return ResponseEntity
            .ok()
            .header(RestHeader.TOTAL_COUNT.headerName(), String.valueOf(entities.size()))
            .body(entities);
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
    public static <E> ResponseEntity<E> ok(final E entity, final int entityId) {
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
    public static <E> ResponseEntity<E> cachedOk(final E entity) {
        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(DATE_TIME_UTILS.untilNextMonthUtc(ChronoUnit.SECONDS), TimeUnit.SECONDS))
            .eTag(String.valueOf(entity.hashCode()))
            .body(entity);
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
    public static <E> ResponseEntity<Collection<E>> cachedOk(final Collection<E> entities) {
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
    public static <E> ResponseEntity<Collection<E>> cachedOk(final Collection<E> entities, final long cachePeriodInSeconds) {
        return ResponseEntity
            .ok()
            .header(RestHeader.TOTAL_COUNT.headerName(), String.valueOf(entities.size()))
            .cacheControl(CacheControl.maxAge(cachePeriodInSeconds, TimeUnit.SECONDS))
            .eTag(String.valueOf(entities.stream().mapToInt(Object::hashCode).sum()))
            .body(entities);
    }

    /**
     * A <b>201_CREATED</b> {@link ResponseEntity}.
     *
     * <p>
     * Used for cases where a <b>POST</b> request with a supplied payload has created a resource.
     *
     * @param entity   the created resource
     * @param <E>      the response body type
     * @return the <b>201_CREATED</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<E> created(final E entity) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(entity);
    }

    /**
     * A <b>201_CREATED</b> {@link ResponseEntity}.
     *
     * <p>
     * Used for cases where a <b>POST</b> request with a supplied payload has created a resource with an ID.
     *
     * @param entity   the created resource
     * @param entityId the ID of the created resource
     * @param <E>      the response body type
     * @return the <b>201_CREATED</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<E> created(final E entity, final int entityId) {
        return responseWithLocation(entity, entityId, HttpStatus.CREATED);
    }

    private static <E> ResponseEntity<E> responseWithLocation(final E entity, final int entityId, final HttpStatusCode httpStatus) {
        final URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(entityId)
            .toUri();

        return ResponseEntity
            .status(httpStatus)
            .location(location)
            .body(entity);
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
    public static ResponseEntity<Void> redirect(final String redirectUrl) {
        return ResponseEntity
            .status(HttpStatus.SEE_OTHER)
            .location(URI.create(redirectUrl))
            .build();
    }
}
