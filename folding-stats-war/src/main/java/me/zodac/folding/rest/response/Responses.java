package me.zodac.folding.rest.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.zodac.folding.api.Identifiable;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

public class Responses {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT_VALUE = "*";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_DEFAULT_VALUE = "GET, PUT, POST, PATCH, DELETE, HEAD";

    private Responses() {

    }

    public static Response notImplemented() {
        return Response
                .status(Response.Status.NOT_IMPLEMENTED)
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME, ACCESS_CONTROL_ALLOW_METHODS_DEFAULT_VALUE)
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
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME, ACCESS_CONTROL_ALLOW_METHODS_DEFAULT_VALUE)
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
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME, ACCESS_CONTROL_ALLOW_METHODS_DEFAULT_VALUE)
                .build();
    }

    public static Response ok(final List<? extends Identifiable> entities) {
        return Response
                .ok()
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME, ACCESS_CONTROL_ALLOW_METHODS_DEFAULT_VALUE)
                .header("X-Total-Count", entities.size())
                .entity(GSON.toJson(entities))
                .build();
    }

    public static Response ok(final Object entity) {
        return Response
                .ok()
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME, ACCESS_CONTROL_ALLOW_METHODS_DEFAULT_VALUE)
                .entity(GSON.toJson(entity))
                .build();
    }

    public static Response ok(final Object entity, final UriBuilder entityLocationBuilder) {
        return Response
                .ok(entityLocationBuilder.build())
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME, ACCESS_CONTROL_ALLOW_METHODS_DEFAULT_VALUE)
                .entity(GSON.toJson(entity))
                .build();
    }

    public static Response created(final Object entity, final UriBuilder entityLocationBuilder) {
        return Response
                .created(entityLocationBuilder.build())
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_DEFAULT_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME, ACCESS_CONTROL_ALLOW_METHODS_DEFAULT_VALUE)
                .entity(GSON.toJson(entity))
                .build();
    }
}
