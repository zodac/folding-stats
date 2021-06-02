package me.zodac.folding.rest.endpoint;

import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.SystemState;
import me.zodac.folding.api.db.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.tc.exception.FoldingIdInvalidException;
import me.zodac.folding.api.tc.exception.FoldingIdOutOfRangeException;
import me.zodac.folding.api.tc.exception.NoStatsAvailableException;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.api.tc.exception.TeamNotFoundException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.BusinessLogic;
import me.zodac.folding.rest.util.IdentityParser;
import me.zodac.folding.rest.util.response.BatchCreateResponse;
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

import static me.zodac.folding.api.utils.DateTimeUtils.untilNextMonthUtc;
import static me.zodac.folding.rest.util.response.Responses.badGateway;
import static me.zodac.folding.rest.util.response.Responses.badRequest;
import static me.zodac.folding.rest.util.response.Responses.conflict;
import static me.zodac.folding.rest.util.response.Responses.created;
import static me.zodac.folding.rest.util.response.Responses.notFound;
import static me.zodac.folding.rest.util.response.Responses.nullRequest;
import static me.zodac.folding.rest.util.response.Responses.ok;
import static me.zodac.folding.rest.util.response.Responses.okBuilder;
import static me.zodac.folding.rest.util.response.Responses.serverError;
import static me.zodac.folding.rest.util.response.Responses.serviceUnavailable;

abstract class AbstractCrudEndpoint<I extends RequestPojo, O extends ResponsePojo> {

    // Expecting most changes to be at the monthly reset, so counting number of seconds until then
    private static final int CACHE_EXPIRATION_TIME = untilNextMonthUtc(ChronoUnit.SECONDS);

    @Context
    protected transient UriInfo uriContext;

    @EJB
    protected BusinessLogic businessLogic;

    protected abstract Logger getLogger();

    protected abstract String elementType();

    protected abstract O createElement(final O element) throws FoldingException, NotFoundException, FoldingConflictException, FoldingExternalServiceException;

    protected abstract Collection<O> getAllElements() throws FoldingException;

    protected abstract ValidationResponse<O> validateAndConvert(final I inputRequest);

    protected abstract O getElementById(final int elementId) throws FoldingException, NotFoundException;

    protected abstract O updateElementById(final int elementId, final O element) throws FoldingException, NotFoundException, FoldingConflictException, FoldingExternalServiceException;

    protected abstract void deleteElementById(final int elementId) throws FoldingConflictException, FoldingException, UserNotFoundException, NoStatsAvailableException, TeamNotFoundException;

    protected Response create(final I inputRequest) {
        getLogger().debug("POST request received to create {} at '{}' with request: {}", elementType(), uriContext.getAbsolutePath(), inputRequest);

        if (SystemStateManager.current().isWriteBlocked()) {
            getLogger().warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        final ValidationResponse<O> validationResponse = validateAndConvert(inputRequest);
        if (validationResponse.isInvalid()) {
            return badRequest(validationResponse);
        }

        try {
            final O elementToCreate = validationResponse.getOutput();
            final O elementWithId = createElement(elementToCreate);

            final UriBuilder elementLocationBuilder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(elementWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return created(elementWithId, elementLocationBuilder);
        } catch (final FoldingConflictException e) {
            // TODO: [zodac] For conflict exceptions, return the ID conflicted against
            final String errorMessage = String.format("The %1$s conflicts with an existing %1$s", elementType());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return conflict(errorMessage);
        } catch (final FoldingExternalServiceException e) {
            final String errorMessage = String.format("Error connecting to external service at '%s': %s", e.getUrl(), e.getMessage());
            getLogger().debug(errorMessage, e);
            getLogger().error(errorMessage);
            return badGateway();
        } catch (final NotFoundException e) {
            getLogger().debug("Error creating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId(), e);
            getLogger().error("Error creating {}, could not find {} with ID {}", elementType(), e.getType(), e.getId());
            return serverError();
        } catch (final FoldingException e) {
            getLogger().error("Error creating {}: {}", elementType(), inputRequest, e.getCause());
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
            final ValidationResponse<O> validationResponse = validateAndConvert(inputRequest);
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
            } catch (final FoldingConflictException | FoldingException | FoldingExternalServiceException e) {
                getLogger().error("Error creating {}: {}", elementType(), element, e.getCause());
                unsuccessful.add(element);
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
        } catch (final FoldingException e) {
            getLogger().error("Error getting all {}s", elementType(), e.getCause());
            return serverError();
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
            final O element = getElementById(IdentityParser.parse(elementId));

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
            getLogger().debug("Error getting {} with ID {}", e.getType(), e.getId(), e);
            getLogger().error("Error getting {} with ID {}", e.getType(), e.getId());
            return notFound();
        } catch (final FoldingException e) {
            getLogger().error("Error getting {} with ID: {}", elementType(), elementId, e.getCause());
            return serverError();
        } catch (final Exception e) {
            getLogger().error("Unexpected error getting {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }

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
            final int parsedId = IdentityParser.parse(elementId);
            // We want to make sure the payload is not trying to change the ID of the element
            // If no ID is provided, the POJO will default to a value of 0, which is acceptable
            if (parsedId != inputRequest.getId() && inputRequest.getId() != 0) {
                final String errorMessage = String.format("Path ID '%s' does not match ID '%s' of payload", elementId, inputRequest.getId());
                getLogger().error(errorMessage);
                return badRequest(errorMessage);
            }

            final O existingElement = getElementById(parsedId);

            if (existingElement.isEqualRequest(inputRequest)) {
                getLogger().debug("No change necessary");
                return ok(existingElement);
            }

            final ValidationResponse<O> validationResponse = validateAndConvert(inputRequest);
            if (validationResponse.isInvalid()) {
                return badRequest(validationResponse);
            }

            final O updatedElementWithId = updateElementById(parsedId, validationResponse.getOutput());

            final UriBuilder elementLocationBuilder = uriContext
                    .getRequestUriBuilder()
                    .path(String.valueOf(inputRequest.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
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
            final String errorMessage = String.format("Error connecting to external service at '%s': %s", e.getUrl(), e.getMessage());
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
        getLogger().debug("DELETE request for {} received at '{}'", elementType(), uriContext.getAbsolutePath());

        if (SystemStateManager.current().isWriteBlocked()) {
            getLogger().warn("System state {} does not allow write requests", SystemStateManager.current());
            return serviceUnavailable();
        }

        try {
            final int parsedId = IdentityParser.parse(elementId);
            getElementById(parsedId); // We call this so if the value does not exist, we can fail with a NOT_FOUND response
            deleteElementById(parsedId);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok();
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
            return notFound();
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
