package me.zodac.folding.rest.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Collection;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import me.zodac.folding.api.ResponsePojo;

/**
 * Utility class to simplify returning a {@link Response} in the REST layer.
 *
 * <p>
 * For most use-cases we return a {@link Response}, but for more complex use-cases, we can return a {@link Response.ResponseBuilder}
 * to allow the REST function to decide to add additional logic (like a {@link javax.ws.rs.core.CacheControl} or similar).
 */
public final class Responses {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Responses() {

    }

    /**
     * A <b>200_OK</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where a HTTP request is sent to request something from the system, but no response is
     * required, such as a delete request, for example.
     *
     * @return the <b>200_OK</b> {@link Response}
     */
    public static Response ok() {
        return Response
            .ok()
            .build();
    }

    /**
     * A <b>200_OK</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where a HTTP request is sent to retrieve a {@link Collection} of {@link ResponsePojo} resources.
     *
     * @param entities the {@link Collection} of {@link ResponsePojo} resourced being retrieved
     * @return the <b>200_OK</b> {@link Response}
     * @see #okBuilder(Collection)
     */
    public static Response ok(final Collection<? extends ResponsePojo> entities) {
        return okBuilder(entities)
            .build();
    }

    /**
     * A <b>200_OK</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where a HTTP request is sent to retrieve a single {@link ResponsePojo} resource, or also
     * if a batch of resources is being created (since there are partial failure scenarios, we cannot return a <b>201_CREATED</b>).
     *
     * @param entity the {@link Object} being retrieved
     * @return the <b>200_OK</b> {@link Response}
     * @see #okBuilder(Object)
     */
    public static Response ok(final Object entity) {
        return okBuilder(entity)
            .build();
    }

    /**
     * A <b>200_OK</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where a HTTP request is sent to update a single {@link ResponsePojo} resource.
     *
     * @param entity                the updated resource
     * @param entityLocationBuilder the {@link UriBuilder} defining the {@link java.net.URI} of the updated resource, to
     *                              be populated in the <b>location</b> header in the {@link Response}
     * @return the <b>200_OK</b> {@link Response}
     */
    public static Response ok(final Object entity, final UriBuilder entityLocationBuilder) {
        return Response
            .ok(entityLocationBuilder.build())
            .entity(GSON.toJson(entity))
            .build();
    }

    /**
     * A <b>200_OK</b> {@link Response.ResponseBuilder}.
     *
     * <p>
     * Generally used for cases where a HTTP request is sent to create a batch of {@link ResponsePojo} resources, but
     * the REST layer might want to add additional logic like a {@link javax.ws.rs.core.CacheControl}.
     *
     * @param entities the {@link Collection} of {@link ResponsePojo} resourced being retrieved
     * @return the <b>200_OK</b> {@link Response}
     * @see #ok(Collection)
     */
    public static Response.ResponseBuilder okBuilder(final Collection<? extends ResponsePojo> entities) {
        return Response
            .ok()
            .header("X-Total-Count", entities.size())
            .entity(GSON.toJson(entities));
    }

    /**
     * A <b>200_OK</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where a HTTP request is sent to retrieve a single {@link ResponsePojo} resource, or also
     * if a batch of resources is being created (since there are partial failure scenarios, we cannot return a <b>201_CREATED</b>),
     * but the REST layer might want to add additional logic like a {@link javax.ws.rs.core.CacheControl}.
     *
     * @param entity the {@link Object} being retrieved
     * @return the <b>200_OK</b> {@link Response}
     * @see #ok(Object)
     */
    public static Response.ResponseBuilder okBuilder(final Object entity) {
        return Response
            .ok()
            .entity(GSON.toJson(entity));
    }

    /**
     * A <b>201_CREATED</b> {@link Response}.
     *
     * <p>
     * Used for cases where a <b>POST</b> request with a supplied payload has created a resource.
     *
     * @param entity                the created resource
     * @param entityLocationBuilder the {@link UriBuilder} defining the {@link java.net.URI} of the created resource, to
     *                              be populated in the <b>location</b> header in the {@link Response}
     * @return the <b>201_CREATED</b> {@link Response}
     */
    public static Response created(final Object entity, final UriBuilder entityLocationBuilder) {
        return Response
            .created(entityLocationBuilder.build())
            .entity(GSON.toJson(entity))
            .build();
    }

    /**
     * A <b>400_BAD_REQUEST</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where the REST request has some invalid data. This can be malformed data, or an
     * invalid payload, or any other similar error.
     *
     * @param errorMessage an error message defining what part of the input payload caused the error
     * @return the <b>400_BAD_REQUEST</b> {@link Response}
     */
    public static Response badRequest(final String errorMessage) {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
            .build();
    }

    /**
     * A <b>400_BAD_REQUEST</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where the REST request has some invalid data. This can be malformed data, or an
     * invalid payload, or any other similar error.
     *
     * @param entity the entity in the payload that caused the error
     * @return the <b>400_BAD_REQUEST</b> {@link Response}
     */
    public static Response badRequest(final Object entity) {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(GSON.toJson(entity))
            .build();
    }

    /**
     * A <b>400_BAD_REQUEST</b> {@link Response}.
     *
     * <p>
     * Used for cases where the REST request has an empty or null payload.
     *
     * @return the <b>400_BAD_REQUEST</b> {@link Response}
     */
    public static Response nullRequest() {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(GSON.toJson(ErrorResponse.create("Payload is null"), ErrorResponse.class))
            .build();
    }

    /**
     * A <b>401_UNAUTHORIZED</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where the user has not been successfully authenticated, and cannot be authorized to
     * execute the REST endpoint.
     *
     * @return the <b>401_UNAUTHORIZED</b> {@link Response}
     */
    public static Response unauthorized() {
        return Response
            .status(Response.Status.UNAUTHORIZED)
            .build();
    }

    /**
     * A <b>403_FORBIDDEN</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where a user has successfully authenticated, but does not have the required
     * authorization to execute the REST endpoint.
     *
     * @return the <b>403_FORBIDDEN</b> {@link Response}
     */
    public static Response forbidden() {
        return Response
            .status(Response.Status.FORBIDDEN)
            .build();
    }

    /**
     * A <b>404_NOT_FOUND</b> {@link Response}.
     *
     * <p>
     * Generally used for cases when an ID is supplied in a REST request, but no resource exists matching that ID..
     *
     * @return the <b>404_NOT_FOUND</b> {@link Response}
     */
    public static Response notFound() {
        return Response
            .status(Response.Status.NOT_FOUND)
            .build();
    }

    /**
     * A <b>409_CONFLICT</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where the REST request is trying to do one of the following:
     * <ul>
     *     <li>Create a resource that already exists</li>
     *     <li>Update a resource with a value that conflicts with another resource</li>
     *     <li>Delete a resource that is being used by another resource</li>
     * </ul>
     *
     * @param errorMessage an error message defining what part of the input payload caused the error
     * @return the <b>409_CONFLICT</b> {@link Response}
     */
    public static Response conflict(final String errorMessage) {
        return Response
            .status(Response.Status.CONFLICT)
            .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
            .build();
    }

    /**
     * A <b>409_CONFLICT</b> {@link Response}.
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
     * @return the <b>409_CONFLICT</b> {@link Response}
     */
    public static Response conflict(final Object entity) {
        return Response
            .status(Response.Status.CONFLICT)
            .entity(GSON.toJson(entity))
            .build();
    }

    /**
     * A <b>500_INTERNAL_SERVER_ERROR</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where an unexpected error has occurred.
     *
     * @return the <b>500_INTERNAL_SERVER_ERROR</b> {@link Response}
     */
    public static Response serverError() {
        return Response
            .serverError()
            .build();
    }

    /**
     * A <b>502_BAD_GATEWAY</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where it is explicitly an external service (such as the Folding@Home API) that is
     * unavailable, rather than the more generic {@link #serviceUnavailable()} {@link Response}.
     *
     * @return the <b>502_BAD_GATEWAY</b> {@link Response}
     */
    public static Response badGateway() {
        return Response
            .status(Response.Status.BAD_GATEWAY)
            .build();
    }

    /**
     * A <b>503_SERVICE_UNAVAILABLE</b> {@link Response}.
     *
     * <p>
     * Generally used for cases where either this service is unavailable due to the {@link me.zodac.folding.api.SystemState},
     * or if an external service (such as the Folding@Home API) is unavailable.
     *
     * @return the <b>503_SERVICE_UNAVAILABLE</b> {@link Response}
     */
    public static Response serviceUnavailable() {
        return Response
            .status(Response.Status.SERVICE_UNAVAILABLE)
            .build();
    }
}
