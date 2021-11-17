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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class to simplify returning a {@link ResponseEntity} in the REST layer.
 */
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
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    /**
//     * A <b>200_OK</b> {@link Response}.
//     *
//     * <p>
//     * Generally used for cases where an HTTP request is sent to retrieve a {@link Collection} of {@link me.zodac.folding.api.ResponsePojo} resources.
//     *
//     * @param entities the {@link Collection} of entities being retrieved
//     * @return the <b>200_OK</b> {@link Response}
//     * @see #okBuilder(Collection)
//     */
//    public static Response ok(final Collection<?> entities) {
//        return okBuilder(entities)
//            .build();
//    }
//
//    /**
//     * A <b>200_OK</b> {@link Response}.
//     *
//     * <p>
//     * Generally used for cases where an HTTP request is sent to retrieve a single {@link me.zodac.folding.api.ResponsePojo} resource, or also
//     * if a batch of resources is being created (since there are partial failure scenarios, we cannot return a <b>201_CREATED</b>).
//     *
//     * @param entity the {@link Object} being retrieved
//     * @return the <b>200_OK</b> {@link Response}
//     * @see #okBuilder(Object)
//     */
//    public static Response ok(final Object entity) {
//        return okBuilder(entity)
//            .build();
//    }
//
//    /**
//     * A <b>200_OK</b> {@link Response}.
//     *
//     * <p>
//     * Generally used for cases where an HTTP request is sent to update a single {@link me.zodac.folding.api.ResponsePojo} resource.
//     *
//     * @param entity                the updated resource
//     * @param entityLocationBuilder the {@link UriBuilder} defining the {@link java.net.URI} of the updated resource, to
//     *                              be populated in the <b>location</b> header in the {@link Response}
//     * @return the <b>200_OK</b> {@link Response}
//     */
//    public static Response ok(final Object entity, final UriBuilder entityLocationBuilder) {
//        return Response
//            .ok(entityLocationBuilder.build())
//            .entity(GSON.toJson(entity))
//            .build();
//    }
//
//    /**
//     * A cached <b>200_OK</b> {@link Response}.
//     *
//     * <p>
//     * Similar to {@link #ok(Object)}, but we check for an entity tag by hashing the entity and performing
//     * {@link Request#evaluatePreconditions(EntityTag)} on the value. If the new entity tag matches the old one, we simply sent a
//     * <b>304_NOT_MODIFIED</b> {@link Response}. Otherwise, we send the <b>200_OK</b> {@link Response} with the new content.
//     *
//     * @param entity                  the {@link Object} being retrieved
//     * @param request                 the {@link Request} to validate the entity tag against
//     * @param expirationTimeInSeconds the cache expiration time for the {@link Response}
//     * @return the <b>200_OK</b> or <b>304_NOT_MODIFIED</b> {@link Response}
//     */
//    public static Response cachedOk(final Object entity, final Request request, final int expirationTimeInSeconds) {
//        final CacheControl cacheControl = new CacheControl();
//        cacheControl.setMaxAge(expirationTimeInSeconds);
//
//        final EntityTag entityTag = new EntityTag(String.valueOf(entity.hashCode()));
//        Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);
//
//        if (builder == null) {
//            builder = okBuilder(entity);
//            builder.tag(entityTag);
//        }
//
//        builder.cacheControl(cacheControl);
//        return builder.build();
//    }
//
//    /**
//     * A cached <b>200_OK</b> {@link Response}.
//     *
//     * <p>
//     * Similar to {@link #ok(Collection)}, but we check for an entity tag by hashing the entities and performing
//     * {@link Request#evaluatePreconditions(EntityTag)} on the value. If the new entity tag matches the old one, we simply sent a
//     * <b>304_NOT_MODIFIED</b> {@link Response}. Otherwise, we send the <b>200_OK</b> {@link Response} with the new content.
//     *
//     * @param entities                the {@link Collection} of entities being retrieved
//     * @param request                 the {@link Request} to validate the entity tag against
//     * @param expirationTimeInSeconds the cache expiration time for the {@link Response}
//     * @return the <b>200_OK</b> or <b>304_NOT_MODIFIED</b> {@link Response}
//     */
//    public static Response cachedOk(final Collection<?> entities, final Request request, final int expirationTimeInSeconds) {
//        final CacheControl cacheControl = new CacheControl();
//        cacheControl.setMaxAge(expirationTimeInSeconds);
//
//        final EntityTag entityTag = new EntityTag(String.valueOf(entities.stream().mapToInt(Object::hashCode).sum()));
//        Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);
//
//        if (builder == null) {
//            builder = okBuilder(entities);
//            builder.tag(entityTag);
//        }
//
//        builder.cacheControl(cacheControl);
//        return builder.build();
//    }
//
//    private static Response.ResponseBuilder okBuilder(final Collection<?> entities) {
//        return Response
//            .ok()
//            .header("X-Total-Count", entities.size())
//            .entity(GSON.toJson(entities));
//    }
//
//    private static Response.ResponseBuilder okBuilder(final Object entity) {
//        return Response
//            .ok()
//            .entity(GSON.toJson(entity));
//    }
//
//    /**
//     * A <b>201_CREATED</b> {@link Response}.
//     *
//     * <p>
//     * Used for cases where a <b>POST</b> request with a supplied payload has created a resource.
//     *
//     * @param entity                the created resource
//     * @param entityLocationBuilder the {@link UriBuilder} defining the {@link java.net.URI} of the created resource, to
//     *                              be populated in the <b>location</b> header in the {@link Response}
//     * @return the <b>201_CREATED</b> {@link Response}
//     */
//    public static Response created(final Object entity, final UriBuilder entityLocationBuilder) {
//        return Response
//            .created(entityLocationBuilder.build())
//            .entity(GSON.toJson(entity))
//            .build();
//    }
//
//    /**
//     * A <b>400_BAD_REQUEST</b> {@link Response}.
//     *
//     * <p>
//     * Generally used for cases where the REST request has some invalid data. This can be malformed data, or an
//     * invalid payload, or any other similar error.
//     *
//     * @param errorMessage an error message defining what part of the input payload caused the error
//     * @return the <b>400_BAD_REQUEST</b> {@link Response}
//     */
//    public static Response badRequest(final String errorMessage) {
//        return Response
//            .status(Response.Status.BAD_REQUEST)
//            .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
//            .build();
//    }
//

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
    public static <E> ResponseEntity<E> badRequest(final E entity) {
        return new ResponseEntity<>(entity, HttpStatus.BAD_REQUEST);
    }
//
//    /**
//     * A <b>400_BAD_REQUEST</b> {@link Response}.
//     *
//     * <p>
//     * Used for cases where the REST request has an empty or null payload.
//     *
//     * @return the <b>400_BAD_REQUEST</b> {@link Response}
//     */
//    public static Response nullRequest() {
//        return Response
//            .status(Response.Status.BAD_REQUEST)
//            .entity(GSON.toJson(ErrorResponse.create("Payload is null"), ErrorResponse.class))
//            .build();
//    }
//

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
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
//
//    /**
//     * A <b>404_NOT_FOUND</b> {@link Response}.
//     *
//     * <p>
//     * Generally used for cases when an ID is supplied in a REST request, but no resource exists matching that ID..
//     *
//     * @return the <b>404_NOT_FOUND</b> {@link Response}
//     */
//    public static Response notFound() {
//        return Response
//            .status(Response.Status.NOT_FOUND)
//            .build();
//    }
//
//    /**
//     * A <b>409_CONFLICT</b> {@link Response}.
//     *
//     * <p>
//     * Generally used for cases where the REST request is trying to do one of the following:
//     * <ul>
//     *     <li>Create a resource that already exists</li>
//     *     <li>Update a resource with a value that conflicts with another resource</li>
//     *     <li>Delete a resource that is being used by another resource</li>
//     * </ul>
//     *
//     * @param entity the entity in the payload that caused the error
//     * @return the <b>409_CONFLICT</b> {@link Response}
//     */
//    public static Response conflict(final Object entity) {
//        return Response
//            .status(Response.Status.CONFLICT)
//            .entity(GSON.toJson(entity))
//            .build();
//    }
//

    /**
     * A <b>500_INTERNAL_SERVER_ERROR</b> {@link ResponseEntity}.
     *
     * <p>
     * Generally used for cases where an unexpected error has occurred.
     *
     * @return the <b>500_INTERNAL_SERVER_ERROR</b> {@link ResponseEntity}
     */
    public static <E> ResponseEntity<E> serverError() {
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    /**
//     * A <b>503_SERVICE_UNAVAILABLE</b> {@link Response}.
//     *
//     * <p>
//     * Generally used for cases where either this service is unavailable due to the {@link me.zodac.folding.api.state.SystemState},
//     * or if an external service (such as the Folding@Home API) is unavailable.
//     *
//     * @return the <b>503_SERVICE_UNAVAILABLE</b> {@link Response}
//     */
//    public static Response serviceUnavailable() {
//        return Response
//            .status(Response.Status.SERVICE_UNAVAILABLE)
//            .build();
//    }
}
