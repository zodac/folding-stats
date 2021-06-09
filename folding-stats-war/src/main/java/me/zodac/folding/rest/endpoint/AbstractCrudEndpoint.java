package me.zodac.folding.rest.endpoint;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.exception.TeamNotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.api.validator.ValidationResult;
import me.zodac.folding.ejb.OldFacade;
import me.zodac.folding.rest.parse.IntegerParser;
import me.zodac.folding.rest.parse.ParseResult;
import me.zodac.folding.rest.response.BatchCreateResponse;
import org.slf4j.Logger;

import javax.ejb.EJB;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static me.zodac.folding.api.utils.DateTimeUtils.untilNextMonthUtc;
import static me.zodac.folding.rest.response.Responses.badGateway;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.conflict;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.nullRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.okBuilder;
import static me.zodac.folding.rest.response.Responses.serverError;
import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

// TODO: [zodac] Decorator around REST methods, so we can catch generic exceptions in a single place?
abstract class AbstractCrudEndpoint<I extends RequestPojo, O extends ResponsePojo> {

    // Expecting most changes to be at the monthly reset, so counting number of seconds until then
    private static final int CACHE_EXPIRATION_TIME = untilNextMonthUtc(ChronoUnit.SECONDS);

    @Context
    protected transient UriInfo uriContext;

    @EJB
    protected BusinessLogic businessLogic;

    @EJB
    protected OldFacade oldFacade;

    protected abstract Logger getLogger();

    protected abstract String elementType();

    protected abstract O createElement(final O element) throws NotFoundException, ExternalConnectionException;

    protected abstract Collection<O> getAllElements();

    protected abstract ValidationResponse<O> validateCreateAndConvert(final I inputRequest);

    protected abstract ValidationResponse<O> validateUpdateAndConvert(final I inputRequest, final O existingElement);

    protected abstract ValidationResponse<O> validateDeleteAndConvert(final O element);

    protected abstract Optional<O> getElementById(final int elementId);

    protected abstract O updateElementById(final int elementId, final O element, final O existingElement) throws NotFoundException, ExternalConnectionException;

    protected abstract void deleteElementById(final int elementId) throws UserNotFoundException, TeamNotFoundException;

    protected Response create(final I inputRequest) {
        getLogger().debug("POST request received to create {} at '{}' with request: {}", elementType(), uriContext.getAbsolutePath(), inputRequest);

        if (SystemStateManager.current().isWriteBlocked()) {
            getLogger().warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        final ValidationResponse<O> validationResponse = validateCreateAndConvert(inputRequest);
        if (validationResponse.getValidationResult() == ValidationResult.FAILURE_ON_VALIDATION) {
            return badRequest(validationResponse);
        } else if (validationResponse.getValidationResult() == ValidationResult.FAILURE_DUE_TO_CONFLICT) {
            return conflict(validationResponse);
        }

        try {
            final O elementToCreate = validationResponse.getOutput();
            final O elementWithId = createElement(elementToCreate);

            final UriBuilder elementLocationBuilder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(elementWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return created(elementWithId, elementLocationBuilder);
        } catch (final ExternalConnectionException e) {
            final String errorMessage = String.format("Error connecting to external service at '%s': %s", e.getUrl(), e.getMessage());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badGateway();
        } catch (final NotFoundException e) {
            getLogger().debug("Error creating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId(), e);
            getLogger().error("Error creating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId());
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error creating {}: {}", elementType(), inputRequest, e);
            return serverError();
        }
    }

    protected Response createBatchOf(final Collection<I> batchOfInputRequests) {
        getLogger().debug("POST request received to create {} {}s at '{}' with request: {}", batchOfInputRequests.size(), elementType(), uriContext.getAbsolutePath(), batchOfInputRequests);

        if (SystemStateManager.current().isWriteBlocked()) {
            getLogger().warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        final Collection<O> validElements = new ArrayList<>(batchOfInputRequests.size() / 2);
        final Collection<ValidationResponse<O>> failedValidationResponses = new ArrayList<>(batchOfInputRequests.size() / 2);

        for (final I inputRequest : batchOfInputRequests) {
            final ValidationResponse<O> validationResponse = validateCreateAndConvert(inputRequest);

            if (validationResponse.isInvalid()) {
                getLogger().error("Found validation error for {}: {}", inputRequest, validationResponse);
                failedValidationResponses.add(validationResponse);
            } else {
                validElements.add(validationResponse.getOutput());
            }
        }

        if (validElements.isEmpty()) {
            getLogger().error("All {}s contain validation errors: {}", elementType(), failedValidationResponses);
            return badRequest(failedValidationResponses);
        }

        final List<Object> successful = new ArrayList<>();
        final List<Object> unsuccessful = new ArrayList<>(failedValidationResponses);

        for (final O element : validElements) {
            try {
                final O elementWithId = createElement(element);
                successful.add(elementWithId);
            } catch (final Exception e) {
                getLogger().error("Unexpected error creating {}: {}", elementType(), element, e);
                unsuccessful.add(element);
            }
        }

        final BatchCreateResponse batchCreateResponse = BatchCreateResponse.create(successful, unsuccessful);

        if (successful.isEmpty()) {
            return badRequest(batchCreateResponse);
        }

        if (!unsuccessful.isEmpty()) {
            getLogger().error("{} {}s successfully created, {} {}s unsuccessful", successful.size(), elementType(), unsuccessful.size(), elementType());
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok(batchCreateResponse);
        }

        getLogger().debug("{} {}s successfully created", successful.size(), elementType());
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        return ok(batchCreateResponse.getSuccessful());
    }

    protected Response getAll(final Request request) {
        getLogger().debug("GET request received for all {}s at '{}'", elementType(), uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            getLogger().warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final Collection<O> elements = getAllElements();

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(CACHE_EXPIRATION_TIME);

            final EntityTag entityTag = new EntityTag(String.valueOf(elements.stream().mapToInt(O::hashCode).sum()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);

            if (builder == null) {
                getLogger().debug("Cached resources have changed");
                builder = okBuilder(elements);
                builder.tag(entityTag);
            }

            builder.cacheControl(cacheControl);
            return builder.build();
        } catch (final Exception e) {
            getLogger().error("Unexpected error getting all {}s", elementType(), e);
            return serverError();
        }
    }

    protected Response getById(final String elementId, final Request request) {
        getLogger().debug("GET request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        if (SystemStateManager.current().isReadBlocked()) {
            getLogger().warn("System state {} does not allow read requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final ParseResult parseResult = IntegerParser.parsePositive(elementId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), elementId);
                getLogger().error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The %s ID '%s' is out of range", elementType(), elementId);
                getLogger().error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();

            final Optional<O> optionalElement = getElementById(parsedId);
            if (optionalElement.isEmpty()) {
                getLogger().error("Error getting {} with ID {}", elementType(), elementId);
                return notFound();
            }
            final O element = optionalElement.get();

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge(CACHE_EXPIRATION_TIME);

            final EntityTag entityTag = new EntityTag(String.valueOf(element.hashCode()));
            Response.ResponseBuilder builder = request.evaluatePreconditions(entityTag);

            if (builder == null) {
                getLogger().debug("Cached resource has changed");
                builder = okBuilder(element);
                builder.tag(entityTag);
            }

            builder.cacheControl(cacheControl);
            return builder.build();
        } catch (final Exception e) {
            getLogger().error("Unexpected error getting {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }

    @SuppressWarnings("PMD.NPathComplexity") // Better than breaking into smaller functions
    protected Response updateById(final String elementId, final I inputRequest) {
        getLogger().debug("PUT request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        if (SystemStateManager.current().isWriteBlocked()) {
            getLogger().warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        if (inputRequest == null) {
            getLogger().error("No payload provided");
            return nullRequest();
        }

        try {
            final ParseResult parseResult = IntegerParser.parsePositive(elementId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), elementId);
                getLogger().error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The %s ID '%s' is out of range", elementType(), elementId);
                getLogger().error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();

            final Optional<O> optionalElement = getElementById(parsedId);
            if (optionalElement.isEmpty()) {
                getLogger().error("Error getting {} with ID {}", elementType(), elementId);
                return notFound();
            }
            final O existingElement = optionalElement.get();

            if (existingElement.isEqualRequest(inputRequest)) {
                getLogger().debug("No change necessary");
                return ok(existingElement);
            }

            final ValidationResponse<O> validationResponse = validateUpdateAndConvert(inputRequest, existingElement);
            if (validationResponse.isInvalid()) {
                return badRequest(validationResponse);
            }

            final O updatedElementWithId = updateElementById(parsedId, validationResponse.getOutput(), existingElement);

            final UriBuilder elementLocationBuilder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(updatedElementWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok(updatedElementWithId, elementLocationBuilder);
        } catch (final ExternalConnectionException e) {
            final String errorMessage = String.format("Error connecting to external service at '%s': %s", e.getUrl(), e.getMessage());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badGateway();
        } catch (final NotFoundException e) {
            getLogger().debug("Error updating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId(), e);
            getLogger().error("Error updating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId());
            return notFound();
        } catch (final Exception e) {
            getLogger().error("Unexpected error updating {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }

    protected Response deleteById(final String elementId) {
        getLogger().debug("DELETE request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        if (SystemStateManager.current().isWriteBlocked()) {
            getLogger().warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }


        try {
            final ParseResult parseResult = IntegerParser.parsePositive(elementId);
            if (parseResult.isBadFormat()) {
                final String errorMessage = String.format("The %s ID '%s' is not a valid format", elementType(), elementId);
                getLogger().error(errorMessage);
                return badRequest(errorMessage);
            } else if (parseResult.isOutOfRange()) {
                final String errorMessage = String.format("The %s ID '%s' is out of range", elementType(), elementId);
                getLogger().error(errorMessage);
                return badRequest(errorMessage);
            }
            final int parsedId = parseResult.getId();

            final Optional<O> optionalElement = getElementById(parsedId);
            if (optionalElement.isEmpty()) {
                getLogger().error("Error getting {} with ID {}", elementType(), elementId);
                return notFound();
            }
            final O element = optionalElement.get();

            final ValidationResponse<O> validationResponse = validateDeleteAndConvert(element);
            if (validationResponse.getValidationResult() == ValidationResult.FAILURE_ON_VALIDATION) {
                return badRequest(validationResponse);
            } else if (validationResponse.getValidationResult() == ValidationResult.FAILURE_DUE_TO_CONFLICT) {
                return conflict(validationResponse);
            }

            deleteElementById(parsedId);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok();
        } catch (final NotFoundException e) {
            getLogger().debug("Error deleting {}, could not find {} with ID {}", elementType(), e.getType(), e.getId(), e);
            getLogger().error("Error deleting {}, could not find {} with ID {}", elementType(), e.getType(), e.getId());
            return notFound();
        } catch (final Exception e) {
            getLogger().error("Unexpected error deleting {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }
}
