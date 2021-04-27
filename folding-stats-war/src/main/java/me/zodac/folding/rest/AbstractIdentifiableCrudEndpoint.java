package me.zodac.folding.rest;

import me.zodac.folding.api.Identifiable;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.rest.response.BulkCreateResponse;
import me.zodac.folding.validator.ValidationResponse;
import org.slf4j.Logger;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.conflict;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.noContent;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

abstract class AbstractIdentifiableCrudEndpoint<V extends Identifiable> {

    @Context
    protected UriInfo uriContext;

    protected abstract Logger getLogger();

    protected abstract String elementType();

    protected abstract ValidationResponse validate(final V element);

    protected abstract V createElement(final V element) throws FoldingException, NotFoundException, FoldingConflictException;

    protected abstract List<V> getAllElements() throws FoldingException;

    protected abstract V getElementById(final int elementId) throws FoldingException, NotFoundException;

    protected abstract V updateElementById(final int elementId, final V element) throws FoldingException, NotFoundException, FoldingConflictException;

    protected abstract void deleteElementById(final int elementId) throws FoldingConflictException, FoldingException;

    protected Response create(final V element) {
        getLogger().info("POST request received to create {} at '{}' with request: {}", elementType(), uriContext.getAbsolutePath(), element);

        final ValidationResponse validationResponse = validate(element);
        if (validationResponse.isInvalid()) {
            return badRequest(validationResponse);
        }

        try {
            final V elementWithId = createElement(element);

            final UriBuilder elementLocationBuilder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(elementWithId.getId()));
            return created(elementWithId, elementLocationBuilder);
        } catch (final FoldingConflictException e) {
            final String errorMessage = String.format("The %1$s conflicts with an existing %1$s", elementType());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return conflict(errorMessage);
        } catch (final NotFoundException e) {
            getLogger().debug("Error creating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId(), e);
            getLogger().error("Error creating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId());
            return serverError();
        } catch (final FoldingException e) {
            getLogger().error("Error creating {}: {}", elementType(), element, e.getCause());
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error creating {}: {}", elementType(), element, e);
            return serverError();
        }
    }

    protected Response createBatchOf(final List<V> elements) {
        getLogger().info("POST request received to create {} {}s at '{}' with request: {}", elements.size(), elementType(), uriContext.getAbsolutePath(), elements);

        final List<ValidationResponse> failedValidationResponses = new ArrayList<>(elements.size() / 2);

        for (final V element : elements) {
            final ValidationResponse validationResponse = validate(element);
            if (validationResponse.isInvalid()) {
                failedValidationResponses.add(validationResponse);
            }
        }

        if (!failedValidationResponses.isEmpty()) {
            return badRequest(failedValidationResponses);
        }

        final List<Identifiable> successful = new ArrayList<>(elements.size() / 2);
        final List<Identifiable> unsuccessful = new ArrayList<>(elements.size() / 2);

        for (final V element : elements) {
            try {
                final V elementWithId = createElement(element);
                successful.add(elementWithId);
            } catch (final FoldingConflictException | FoldingException e) {
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
            return badRequest(bulkCreateResponse);
        }

        if (!unsuccessful.isEmpty()) {
            getLogger().error("{} {}s successfully created, {} {}s unsuccessful", successful.size(), elementType(), unsuccessful.size(), elementType());
            return ok(bulkCreateResponse);
        }

        getLogger().debug("{} {}s successfully created", successful.size(), elementType());
        return ok(bulkCreateResponse.getSuccessful());
    }

    protected Response getAll() {
        getLogger().info("GET request received for all {}s at '{}'", elementType(), uriContext.getAbsolutePath());

        try {
            final List<V> elements = getAllElements();
            getLogger().info("Found {} {}s", elements.size(), elementType());
            return ok(elements);
        } catch (final FoldingException e) {
            getLogger().error("Error getting all {}s", elementType(), e.getCause());
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error getting all {}s", elementType(), e);
            return serverError();
        }
    }

    protected Response getById(final String elementId) {
        getLogger().info("GET request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        try {
            final V element = getElementById(Integer.parseInt(elementId));
            return ok(element);
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), elementId);
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NotFoundException e) {
            getLogger().debug("No {} found with ID: {}", elementType(), elementId, e);
            getLogger().error("No {} found with ID: {}", elementType(), elementId);
            return notFound();
        } catch (final FoldingException e) {
            getLogger().error("Error getting {} with ID: {}", elementType(), elementId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error getting {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }

    protected Response updateById(final String elementId, final V element) {
        getLogger().info("PUT request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        final ValidationResponse validationResponse = validate(element);
        if (validationResponse.isInvalid()) {
            return badRequest(validationResponse);
        }

        try {
            // We want to make sure the payload is not trying to change the ID of the element
            // If no ID is provided, the POJO will default to a value of 0, which is acceptable
            if (Integer.parseInt(elementId) != element.getId() && element.getId() != 0) {
                final String errorMessage = String.format("Path ID '%s' does not match ID '%s' of payload", elementId, element.getId());
                getLogger().error(errorMessage);
                return badRequest(errorMessage);
            }

            final V existingElement = getElementById(Integer.parseInt(elementId));

            if (existingElement.equals(element)) {
                getLogger().debug("No change necessary");
                return noContent();
            }

            final V updatedElementWithId = updateElementById(Integer.parseInt(elementId), element);

            final UriBuilder elementLocationBuilder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(element.getId()));
            return ok(updatedElementWithId, elementLocationBuilder);
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), elementId);
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingConflictException e) {
            final String errorMessage = String.format("The %1$s conflicts with an existing %1$s", elementType());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return conflict(errorMessage);
        } catch (final NotFoundException e) {
            getLogger().debug("No {} found with ID: {}", elementType(), elementId, e);
            getLogger().error("No {} found with ID: {}", elementType(), elementId, e);
            return notFound();
        } catch (final FoldingException e) {
            getLogger().error("Error updating {} with ID: {}", elementType(), elementId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error updating {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }

    protected Response deleteById(final String elementId) {
        getLogger().info("DELETE request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        try {
            deleteElementById(Integer.parseInt(elementId));
            return noContent();
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), elementId);
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingConflictException e) {
            final String errorMessage = String.format("The %s ID '%s' is in use, remove all usages before deleting", elementType(), elementId);
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return conflict(errorMessage);
        } catch (final FoldingException e) {
            getLogger().error("Error deleting {} with ID: {}", elementType(), elementId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error deleting {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }
}
