package me.zodac.folding.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.zodac.folding.api.Identifiable;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.rest.response.BulkCreateResponse;
import me.zodac.folding.rest.response.ErrorResponse;
import me.zodac.folding.validator.ValidationResponse;
import org.slf4j.Logger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

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

    protected Response create(final V element) {
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

    protected Response createBatchOf(final List<V> elements) {
        getLogger().info("POST request received to create {} {}s at '{}' with request: {}", elements.size(), elementType(), uriContext.getAbsolutePath(), elements);

        final List<ValidationResponse> failedValidationResponses = new ArrayList<>(elements.size() / 2);

        for (final V element : elements) {
            final ValidationResponse validationResponse = validate(element);
            if (!validationResponse.isValid()) {
                failedValidationResponses.add(validationResponse);
            }
        }

        if (!failedValidationResponses.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(failedValidationResponses))
                    .build();
        }

        final List<Identifiable> successful = new ArrayList<>(elements.size() / 2);
        final List<Identifiable> unsuccessful = new ArrayList<>(elements.size() / 2);

        for (final V element : elements) {
            try {
                final V elementWithId = createElement(element);
                successful.add(elementWithId);
            } catch (final FoldingException e) {
                getLogger().error("Error creating {}: {}", elementType(), element, e.getCause());
                unsuccessful.add(element);
            } catch (final Exception e) {
                getLogger().error("Unexpected error creating {}: {}", elementType(), element, e);
                unsuccessful.add(element);
            }
        }

        final BulkCreateResponse bulkCreateResponse = BulkCreateResponse.create(successful, unsuccessful);

        if (successful.isEmpty()) {
            getLogger().error("No {}s successfully created", elementType());
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(GSON.toJson(bulkCreateResponse))
                    .build();
        }

        if (!unsuccessful.isEmpty()) {
            getLogger().error("{} {}s successfully created, {} {}s unsuccessful", successful.size(), elementType(), unsuccessful.size(), elementType());
            return Response
                    .ok()
                    .entity(GSON.toJson(bulkCreateResponse))
                    .build();
        }

        getLogger().debug("{} {}s successfully created", successful.size(), elementType());
        return Response
                .ok()
                .entity(GSON.toJson(bulkCreateResponse.getSuccessful()))
                .build();
    }

    protected Response getAll() {
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

    protected Response getById(final String elementId) {
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
                    .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
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

    protected Response updateById(final String elementId, final V element) {
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
                        .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
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
                    .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
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

    protected Response deleteById(final String elementId) {
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
                    .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
                    .build();
        } catch (final FoldingConflictException e) {
            final String errorMessage = String.format("The %s ID '%s' is in use, remove all usages before deleting", elementType(), elementId);

            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity(GSON.toJson(ErrorResponse.create(errorMessage), ErrorResponse.class))
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
