package me.zodac.folding.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.zodac.folding.api.Identifiable;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.validator.ValidationResponse;
import org.slf4j.Logger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

// TODO: [zodac] POST with List<V> elements?
abstract class AbstractIdentifiableCrudEndpoint<V extends Identifiable> {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Context
    private UriInfo uriContext;

    protected abstract Logger getLogger();

    protected abstract String elementType();

    protected abstract ValidationResponse validate(final V element);

    protected abstract V createElement(final V element) throws FoldingException, NotFoundException;

    protected abstract List<V> getAllElements() throws FoldingException;

    protected abstract V getElementById(final int elementId) throws FoldingException, NotFoundException;

    protected abstract V updateElementById(final int elementId, final V element) throws FoldingException, NotFoundException;

    protected abstract void deleteElementById(final int elementId) throws FoldingConflictException, FoldingException;

    public Response create(final V element) {
        getLogger().info("POST request received to create {} at '{}' with request: {}", elementType(), uriContext.getAbsolutePath(), element);

        final ValidationResponse validationResponse = validate(element);
        if (!validationResponse.isValid()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(validationResponse))
                    .build();
        }

        try {
            final V elementWithId = createElement(element);

            final UriBuilder builder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(elementWithId.getId()));
            return Response
                    .created(builder.build())
                    .entity(GSON.toJson(elementWithId))
                    .build();
        } catch (final FoldingException e) {
            getLogger().error("Error creating {}: {}", elementType(), element, e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            getLogger().error("Unexpected error creating {}: {}", elementType(), element, e);
            return Response
                    .serverError()
                    .build();
        }
    }

    public Response getAll() {
        getLogger().info("GET request received for all {}s at '{}'", elementType(), uriContext.getAbsolutePath());

        try {
            final List<V> elements = getAllElements();
            getLogger().info("Found {} {}s", elements.size(), elementType());
            return Response
                    .ok()
                    .entity(GSON.toJson(elements))
                    .build();
        } catch (final FoldingException e) {
            getLogger().error("Error getting all {}s", elementType(), e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            getLogger().error("Unexpected error getting all {}s", elementType(), e);
            return Response
                    .serverError()
                    .build();
        }
    }

    public Response getById(final String elementId) {
        getLogger().info("GET request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        try {
            final V element = getElementById(Integer.parseInt(elementId));
            return Response
                    .ok()
                    .entity(element)
                    .build();
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), elementId);

            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(new ErrorObject(errorMessage), ErrorObject.class))
                    .build();
        } catch (final NotFoundException e) {
            getLogger().debug("No {} found with ID: {}", elementType(), elementId, e);
            getLogger().error("No {} found with ID: {}", elementType(), elementId);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        } catch (final FoldingException e) {
            getLogger().error("Error getting {} with ID: {}", elementType(), elementId, e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            getLogger().error("Unexpected error getting {} with ID: {}", elementType(), elementId, e);
            return Response
                    .serverError()
                    .build();
        }
    }

    public Response updateById(final String elementId, final V element) {
        getLogger().info("PUT request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        final ValidationResponse validationResponse = validate(element);
        if (!validationResponse.isValid()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(validationResponse))
                    .build();
        }

        try {
            // We want to make sure the payload is not trying to change the ID of the element
            // If no ID is provided, the POJO will default to a value of 0, which is acceptable
            if (Integer.parseInt(elementId) != element.getId() && element.getId() != 0) {
                final String errorMessage = String.format("Path ID '%s' does not match ID '%s' of payload", elementId, element.getId());

                getLogger().error(errorMessage);
                return Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(GSON.toJson(new ErrorObject(errorMessage), ErrorObject.class))
                        .build();
            }

            final V existingElement = getElementById(Integer.parseInt(elementId));

            if (existingElement.equals(element)) {
                getLogger().debug("No change necessary");
                return Response
                        .noContent()
                        .build();
            }

            final V updatedElementWithId = updateElementById(Integer.parseInt(elementId), element);

            final UriBuilder builder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(element.getId()));
            return Response
                    .ok(builder.build())
                    .entity(GSON.toJson(updatedElementWithId))
                    .build();
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), elementId);

            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(new ErrorObject(errorMessage), ErrorObject.class))
                    .build();
        } catch (final NotFoundException e) {
            getLogger().debug("No {} found with ID: {}", elementType(), elementId, e);
            getLogger().error("No {} found with ID: {}", elementType(), elementId, e);
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .build();
        } catch (final FoldingException e) {
            getLogger().error("Error updating {} with ID: {}", elementType(), elementId, e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            getLogger().error("Unexpected error updating {} with ID: {}", elementType(), elementId, e);
            return Response
                    .serverError()
                    .build();
        }
    }

    public Response deleteById(final String elementId) {
        getLogger().info("DELETE request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        try {
            deleteElementById(Integer.parseInt(elementId));
            return Response
                    .noContent()
                    .build();
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), elementId);

            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(new ErrorObject(errorMessage), ErrorObject.class))
                    .build();
        } catch (final FoldingConflictException e) {
            final String errorMessage = String.format("The %s ID '%s' is in use, remove all usages before deleting", elementType(), elementId);

            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(GSON.toJson(new ErrorObject(errorMessage), ErrorObject.class))
                    .build();
        } catch (final FoldingException e) {
            getLogger().error("Error deleting {} with ID: {}", elementType(), elementId, e.getCause());
            return Response
                    .serverError()
                    .build();
        } catch (final Exception e) {
            getLogger().error("Unexpected error deleting {} with ID: {}", elementType(), elementId, e);
            return Response
                    .serverError()
                    .build();
        }
    }
}
