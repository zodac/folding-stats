package me.zodac.folding.rest.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class Responses {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String DEFAULT_ACCESS_CONTROL_ORIGIN = "*";
    private static final String DEFAULT_ACCESS_CONTROL_METHODS = "GET, PUT, POST, PATCH, DELETE, HEAD";

    private Responses() {

    }

    public static Response notImplemented() {
        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .header("Access-Control-Allow-Origin", DEFAULT_ACCESS_CONTROL_ORIGIN)
                .header("Access-Control-Allow-Methods", DEFAULT_ACCESS_CONTROL_METHODS)
                .build();
    }

    public static Response serverError() {
        return Response
                .serverError()
                .build();
    }

    public static Response notFound() {
        return Response
                .status(Response.Status.NOT_FOUND)
                .build();
    }

    public static Response noContent() {
        return Response
                .noContent()
                .header("Access-Control-Allow-Origin", DEFAULT_ACCESS_CONTROL_ORIGIN)
                .header("Access-Control-Allow-Methods", DEFAULT_ACCESS_CONTROL_METHODS)
                .build();
    }

    public static Response badRequest(final String errorMessage) {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
                .build();
    }

    public static Response conflict(final String errorMessage) {
        return Response
                .status(Response.Status.CONFLICT)
                .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
                .build();
    }

    public static Response badRequest(final Object entity) {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(GSON.toJson(entity))
                .build();
    }

    public static Response ok() {
        return Response
                .ok()
                .header("Access-Control-Allow-Origin", DEFAULT_ACCESS_CONTROL_ORIGIN)
                .header("Access-Control-Allow-Methods", DEFAULT_ACCESS_CONTROL_METHODS)
                .build();
    }

    public static Response ok(final Object entity) {
        return Response
                .ok()
                .header("Access-Control-Allow-Origin", DEFAULT_ACCESS_CONTROL_ORIGIN)
                .header("Access-Control-Allow-Methods", DEFAULT_ACCESS_CONTROL_METHODS)
                .entity(GSON.toJson(entity))
                .build();
    }

    public static Response ok(final Object entity, final UriBuilder entityLocationBuilder) {
        return Response
                .ok(entityLocationBuilder.build())
                .header("Access-Control-Allow-Origin", DEFAULT_ACCESS_CONTROL_ORIGIN)
                .header("Access-Control-Allow-Methods", DEFAULT_ACCESS_CONTROL_METHODS)
                .entity(GSON.toJson(entity))
                .build();
    }

    public static Response created(final Object entity, final UriBuilder entityLocationBuilder) {
        return Response
                .created(entityLocationBuilder.build())
                .header("Access-Control-Allow-Origin", DEFAULT_ACCESS_CONTROL_ORIGIN)
                .header("Access-Control-Allow-Methods", DEFAULT_ACCESS_CONTROL_METHODS)
                .entity(GSON.toJson(entity))
                .build();
    }
}
