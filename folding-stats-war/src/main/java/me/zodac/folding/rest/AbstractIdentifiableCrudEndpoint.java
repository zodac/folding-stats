package me.zodac.folding.rest;

import me.zodac.folding.api.Identifiable;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.exception.FoldingIdInvalidException;
import me.zodac.folding.api.exception.FoldingIdOutOfRangeException;
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

import static me.zodac.folding.rest.response.Responses.badGateway;
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

    protected abstract V createElement(final V element) throws FoldingException, NotFoundException, FoldingConflictException, FoldingExternalServiceException;

    protected abstract List<V> getAllElements() throws FoldingException;

    protected abstract V getElementById(final int elementId) throws FoldingException, NotFoundException;

    protected abstract V updateElementById(final int elementId, final V element) throws FoldingException, NotFoundException, FoldingConflictException, FoldingExternalServiceException;

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
        } catch (final FoldingExternalServiceException e) {
            final String errorMessage = String.format("Error connecting to external service: %s", e.getMessage());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badGateway();
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

    protected Response createBatchOf(final List<V> batchOfElements) {
        getLogger().info("POST request received to create {} {}s at '{}' with request: {}", batchOfElements.size(), elementType(), uriContext.getAbsolutePath(), batchOfElements);

        final List<V> validElements = new ArrayList<>(batchOfElements.size() / 2);
        final List<ValidationResponse> failedValidationResponses = new ArrayList<>(batchOfElements.size() / 2);

        for (final V element : batchOfElements) {
            final ValidationResponse validationResponse = validate(element);
            if (validationResponse.isInvalid()) {
                getLogger().error("Found validation error for {}: {}", element, validationResponse);
                failedValidationResponses.add(validationResponse);
            } else {
                validElements.add(element);
            }
        }

        if (validElements.isEmpty()) {
            getLogger().error("All {}s contain validation errors: {}", elementType(), failedValidationResponses);
            return badRequest(failedValidationResponses);
        }

        final List<V> successful = new ArrayList<>(batchOfElements.size() / 2);
        final List<V> unsuccessful = new ArrayList<>(batchOfElements.size() / 2);

        for (final V element : validElements) {
            try {
                final V elementWithId = createElement(element);
                successful.add(elementWithId);
            } catch (final FoldingConflictException | FoldingException | FoldingExternalServiceException e) {
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
            final V element = getElementById(parseId(elementId));
            return ok(element);
        } catch (final FoldingIdInvalidException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingIdOutOfRangeException e) {
            final String errorMessage = String.format("The %s ID '%s' is out of range", elementType(), e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NotFoundException e) {
            getLogger().debug("Error getting {}, could not find {} with ID {}", elementType(), e.getType(), e.getId(), e);
            getLogger().error("Error getting {}, could not find {} with ID {}", elementType(), e.getType(), e.getId());
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
            final int parsedId = parseId(elementId);
            // We want to make sure the payload is not trying to change the ID of the element
            // If no ID is provided, the POJO will default to a value of 0, which is acceptable
            if (parsedId != element.getId() && element.getId() != 0) {
                final String errorMessage = String.format("Path ID '%s' does not match ID '%s' of payload", elementId, element.getId());
                getLogger().error(errorMessage);
                return badRequest(errorMessage);
            }

            final V existingElement = getElementById(parsedId);

            if (existingElement.equals(element)) {
                getLogger().debug("No change necessary");
                return noContent();
            }

            final V updatedElementWithId = updateElementById(parsedId, element);

            final UriBuilder elementLocationBuilder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(element.getId()));
            return ok(updatedElementWithId, elementLocationBuilder);
        } catch (final FoldingIdInvalidException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingIdOutOfRangeException e) {
            final String errorMessage = String.format("The %s ID '%s' is out of range", elementType(), e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingConflictException e) {
            final String errorMessage = String.format("The %1$s conflicts with an existing %1$s", elementType());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return conflict(errorMessage);
        } catch (final FoldingExternalServiceException e) {
            final String errorMessage = String.format("Error connecting to external service: %s", e.getMessage());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badGateway();
        } catch (final NotFoundException e) {
            getLogger().debug("Error updating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId(), e);
            getLogger().error("Error updating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId());
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
            final int parsedId = parseId(elementId);
            getElementById(parsedId);
            deleteElementById(parsedId);
            return noContent();
        } catch (final FoldingIdInvalidException e) {
            final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final FoldingIdOutOfRangeException e) {
            final String errorMessage = String.format("The %s ID '%s' is out of range", elementType(), e.getId());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badRequest(errorMessage);
        } catch (final NotFoundException e) {
            getLogger().debug("Error deleting {}, could not find {} with ID {}", elementType(), e.getType(), e.getId(), e);
            getLogger().error("Error deleting {}, could not find {} with ID {}", elementType(), e.getType(), e.getId());
            return noContent();
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

    protected int parseId(final String id) throws FoldingIdInvalidException, FoldingIdOutOfRangeException {
        try {
            final int parsedId = Integer.parseInt(id);
            if (parsedId < 0) {
                throw new FoldingIdOutOfRangeException(parsedId);
            }
            return parsedId;
        } catch (final NumberFormatException e) {
            throw new FoldingIdInvalidException(id, e);
        }
    }
}
