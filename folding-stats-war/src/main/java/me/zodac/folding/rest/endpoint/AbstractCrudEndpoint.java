package me.zodac.folding.rest.endpoint;

import static me.zodac.folding.api.util.DateTimeUtils.untilNextMonthUtc;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.nullRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.ejb.EJB;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.rest.endpoint.util.IdResult;
import me.zodac.folding.rest.endpoint.util.IntegerParser;
import me.zodac.folding.rest.response.BatchCreateResponse;
import me.zodac.folding.rest.validator.ValidationFailure;
import me.zodac.folding.rest.validator.ValidationResult;
import org.apache.logging.log4j.Logger;

// TODO: [zodac] Decorator around REST methods, so we can catch generic exceptions in a single place?
abstract class AbstractCrudEndpoint<I extends RequestPojo, O extends ResponsePojo> {

    @Context
    protected UriInfo uriContext;

    @EJB
    protected BusinessLogic businessLogic;

    protected abstract Logger getLogger();

    protected abstract String elementType();

    protected abstract O createElement(final O element);

    protected abstract Collection<O> getAllElements();

    protected abstract ValidationResult<O> validateCreateAndConvert(final I inputRequest);

    protected abstract ValidationResult<O> validateUpdateAndConvert(final I inputRequest, final O existingElement);

    protected abstract ValidationResult<O> validateDeleteAndConvert(final O element);

    protected abstract Optional<O> getElementById(final int elementId);

    protected abstract O updateElementById(final O element, final O existingElement);

    protected abstract void deleteElement(final O element);

    protected Response create(final I inputRequest) {
        getLogger().debug("POST request received to create {} at '{}' with request: {}", this::elementType, uriContext::getAbsolutePath,
            () -> inputRequest);

        final ValidationResult<O> validationResult = validateCreateAndConvert(inputRequest);
        if (validationResult.isFailure()) {
            return validationResult.getFailureResponse();
        }
        final O validatedElement = validationResult.getOutput();

        try {
            final O elementWithId = createElement(validatedElement);

            final UriBuilder elementLocationBuilder = uriContext
                .getRequestUriBuilder()
                .path(String.valueOf(elementWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            getLogger().info("Created {} with ID {}", elementType(), elementWithId.getId());
            return created(elementWithId, elementLocationBuilder);
        } catch (final Exception e) {
            getLogger().error("Unexpected error creating {}: {}", elementType(), inputRequest, e);
            return serverError();
        }
    }

    protected Response createBatchOf(final Collection<I> batchOfInputRequests) {
        getLogger().debug("POST request received to create {} {}s at '{}' with request: {}", batchOfInputRequests::size, this::elementType,
            uriContext::getAbsolutePath, () -> batchOfInputRequests);

        final Collection<O> validElements = new ArrayList<>(batchOfInputRequests.size() / 2);
        final Collection<ValidationFailure> failedValidationRespons = new ArrayList<>(batchOfInputRequests.size() / 2);

        for (final I inputRequest : batchOfInputRequests) {
            final ValidationResult<O> validationResult = validateCreateAndConvert(inputRequest);

            if (validationResult.isFailure()) {
                getLogger().error("Found validation error for {}: {}", inputRequest, validationResult);
                failedValidationRespons.add(validationResult.getValidationFailure());
            } else {
                validElements.add(validationResult.getOutput());
            }
        }

        if (validElements.isEmpty()) {
            getLogger().error("All {}s contain validation errors: {}", elementType(), failedValidationRespons);
            return badRequest(failedValidationRespons);
        }

        final List<Object> successful = new ArrayList<>();
        final List<Object> unsuccessful = new ArrayList<>(failedValidationRespons);

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
            getLogger()
                .error("{} {}s successfully created, {} {}s unsuccessful", successful.size(), elementType(), unsuccessful.size(), elementType());
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok(batchCreateResponse);
        }

        getLogger().info("{} {}s successfully created", successful::size, this::elementType);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        return ok(batchCreateResponse.getSuccessful());
    }

    protected Response getAll(final Request request) {
        getLogger().debug("GET request received for all {}s at '{}'", this::elementType, uriContext::getAbsolutePath);

        try {
            final Collection<O> elements = getAllElements();
            return cachedOk(elements, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            getLogger().error("Unexpected error getting all {}s", elementType(), e);
            return serverError();
        }
    }

    protected Response getById(final String elementId, final Request request) {
        getLogger().debug("GET request for {} received at '{}'", this::elementType, uriContext::getAbsolutePath);

        try {
            final IdResult idResult = IntegerParser.parsePositive(elementId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<O> optionalElement = getElementById(parsedId);
            if (optionalElement.isEmpty()) {
                getLogger().error("Error getting {} with ID {}", elementType(), elementId);
                return notFound();
            }

            final O element = optionalElement.get();
            return cachedOk(element, request, untilNextMonthUtc(ChronoUnit.SECONDS));
        } catch (final Exception e) {
            getLogger().error("Unexpected error getting {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }

    @SuppressWarnings("PMD.NPathComplexity") // Better than breaking into smaller functions
    protected Response updateById(final String elementId, final I inputRequest) {
        getLogger().debug("PUT request for {} received at '{}'", this::elementType, uriContext::getAbsolutePath);

        if (inputRequest == null) {
            getLogger().error("No payload provided");
            return nullRequest();
        }

        try {
            final IdResult idResult = IntegerParser.parsePositive(elementId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

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

            final ValidationResult<O> validationResult = validateUpdateAndConvert(inputRequest, existingElement);
            if (validationResult.isFailure()) {
                return validationResult.getFailureResponse();
            }
            final O validatedElement = validationResult.getOutput();

            final O updatedElementWithId = updateElementById(validatedElement, existingElement);

            final UriBuilder elementLocationBuilder = uriContext
                .getRequestUriBuilder()
                .path(String.valueOf(updatedElementWithId.getId()));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            getLogger().info("Updated {} with ID {}", elementType(), updatedElementWithId.getId());
            return ok(updatedElementWithId, elementLocationBuilder);
        } catch (final Exception e) {
            getLogger().error("Unexpected error updating {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }

    protected Response deleteById(final String elementId) {
        getLogger().debug("DELETE request for {} received at '{}'", this::elementType, uriContext::getAbsolutePath);

        try {
            final IdResult idResult = IntegerParser.parsePositive(elementId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<O> optionalElement = getElementById(parsedId);
            if (optionalElement.isEmpty()) {
                getLogger().error("Error getting {} with ID {}", elementType(), elementId);
                return notFound();
            }
            final O element = optionalElement.get();

            final ValidationResult<O> validationResult = validateDeleteAndConvert(element);
            if (validationResult.isFailure()) {
                return validationResult.getFailureResponse();
            }
            final O validatedElement = validationResult.getOutput();

            deleteElement(validatedElement);
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            getLogger().info("Deleted {} with ID {}", elementType(), elementId);
            return ok();
        } catch (final Exception e) {
            getLogger().error("Unexpected error deleting {} with ID: {}", elementType(), elementId, e);
            return serverError();
        }
    }
}
