/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.configuration;

import static me.zodac.folding.rest.api.util.RestUtilConstants.GSON;

import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.UsedByException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.rest.exception.ForbiddenException;
import me.zodac.folding.rest.exception.InvalidDayException;
import me.zodac.folding.rest.exception.InvalidLoginCredentialsException;
import me.zodac.folding.rest.exception.InvalidMonthException;
import me.zodac.folding.rest.exception.InvalidStateException;
import me.zodac.folding.rest.exception.InvalidYearException;
import me.zodac.folding.rest.exception.NotFoundException;
import me.zodac.folding.rest.exception.OutOfRangeDayException;
import me.zodac.folding.rest.exception.ServiceUnavailableException;
import me.zodac.folding.rest.exception.UnauthorizedException;
import me.zodac.folding.rest.response.ErrorResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * {@link ControllerAdvice} used to return responses for propagated exceptions. These exceptions are to be propagated out of the REST endpoints and
 * handled here, rather than having TRY/CATCH blocks for each method.
 *
 * <p>
 * We try to use custom exceptions where possible to have more control over the flow, though sometimes must handle spring exceptions
 * (like {@link HttpMessageNotReadableException}), and also handle the generic {@link Exception}.
 */
@ControllerAdvice
public class PropagatedExceptionHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Returned when an object requested through a REST endpoint cannot be found.
     *
     * <p>
     * Returns a <b>404_NOT_FOUND</b> response with no response body.
     *
     * @param e the {@link NotFoundException}
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public void notFound(final NotFoundException e) {
        LOGGER.error(e.getMessage());
    }

    /**
     * Returned when the user making a request through a REST endpoint is not authorized for the request.
     *
     * <p>
     * Returns a <b>403_FORBIDDEN</b> response with no response body.
     *
     * @param e the {@link ForbiddenException}
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(ForbiddenException.class)
    public void forbidden(final ForbiddenException e) {
        LOGGER.error(e.getMessage());
    }

    /**
     * Returned when the user making a request through a REST endpoint cannot be authenticated for the request.
     *
     * <p>
     * Returns a <b>401_UNAUTHORIZED</b> response with no response body.
     *
     * @param e the {@link UnauthorizedException}
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public void unauthorized(final UnauthorizedException e) {
        LOGGER.error(e.getMessage());
    }

    /**
     * Returned when a service is unavailable to fulfil a request made through a REST endpoint.
     *
     * <p>
     * Returns a <b>503_SERVICE_UNAVAILABLE</b> response with no response body.
     *
     * @param e the {@link ServiceUnavailableException}
     */
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ServiceUnavailableException.class)
    public void serviceUnavailable(final ServiceUnavailableException e) {
        LOGGER.error(e.getMessage());
    }

    /**
     * Returned when a request made to a REST endpoint has an invalid parameter type.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an invalid input error message body.
     *
     * @param e the {@link MethodArgumentTypeMismatchException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String invalidId(final MethodArgumentTypeMismatchException e) {
        final String errorMessage = String.format("The input is not a valid format: %s", e.getMessage());
        LOGGER.error(errorMessage);
        return GSON.toJson(ErrorResponse.create(errorMessage));
    }

    /**
     * Returned when a request made to a REST endpoint has a null/empty request body.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with a 'payload is null' error message body.
     *
     * @param e the {@link HttpMessageNotReadableException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public String invalidBody(final HttpMessageNotReadableException e) {
        LOGGER.debug("Payload is null", e);
        LOGGER.error("Payload is null");
        return GSON.toJson(ErrorResponse.create("Payload is null"));
    }

    /**
     * Returned when a request made to a REST endpoint has a missing request parameter
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with the error message body.
     *
     * @param e the {@link MissingServletRequestParameterException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String missingParameter(final MissingServletRequestParameterException e) {
        LOGGER.error("Missing parameter", e);
        return GSON.toJson(ErrorResponse.create(e.getMessage()));
    }

    /**
     * Returned when a request made to a REST endpoint has an invalid year.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an 'invalid year' error message body.
     *
     * @param e the {@link InvalidYearException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidYearException.class)
    public String invalidYear(final InvalidYearException e) {
        final String errorMessage = String.format("The year '%s' is not a valid format", e.getYear());
        LOGGER.error(errorMessage);
        return GSON.toJson(ErrorResponse.create(errorMessage));
    }

    /**
     * Returned when a request made to a REST endpoint has an invalid month.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an 'invalid month' error message body.
     *
     * @param e the {@link InvalidMonthException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidMonthException.class)
    public String invalidMonth(final InvalidMonthException e) {
        final String errorMessage = String.format("The month '%s' is not a valid format", e.getMonth());
        LOGGER.error(errorMessage);
        return GSON.toJson(ErrorResponse.create(errorMessage));
    }

    /**
     * Returned when a request made to a REST endpoint has an invalid day.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an 'invalid day' error message body.
     *
     * @param e the {@link InvalidDayException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidDayException.class)
    public String invalidDay(final InvalidDayException e) {
        final String errorMessage = String.format("The day '%s' is not a valid format", e.getDay());
        LOGGER.error(errorMessage);
        return GSON.toJson(ErrorResponse.create(errorMessage));
    }

    /**
     * Returned when a {@link me.zodac.folding.rest.api.LoginCredentials} request made to a REST endpoint has invalid credentials.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an 'invalid day' error message body.
     *
     * @param e the {@link InvalidDayException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidLoginCredentialsException.class)
    public String invalidLoginCredentials(final InvalidLoginCredentialsException e) {
        final String errorMessage = String.format("Invalid login credentials: '%s'", e.getLoginCredentials());
        return GSON.toJson(ErrorResponse.create(errorMessage));
    }

    /**
     * Returned when a request made to a REST endpoint has an out of range day.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an 'out of range day' error message body.
     *
     * @param e the {@link OutOfRangeDayException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OutOfRangeDayException.class)
    public String dayOutOfRange(final OutOfRangeDayException e) {
        final String errorMessage = String.format("The day '%s' is not a valid day for %s/%s", e.getDay(), e.getYear(), e.getMonth());
        LOGGER.error(errorMessage);
        return GSON.toJson(ErrorResponse.create(errorMessage));
    }

    /**
     * Returned when an invalid request is made to update a {@link UserChangeState}.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an 'invalid ID' error message body.
     *
     * @param e the {@link InvalidStateException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidStateException.class)
    public String invalidState(final InvalidStateException e) {
        final String errorMessage = String.format("%s '%s' cannot be updated to '%s'", UserChangeState.class.getSimpleName(), e.getFromState(),
            e.getToState());
        LOGGER.error(errorMessage);
        return GSON.toJson(ErrorResponse.create(errorMessage));
    }

    /**
     * Returned when an input object fails validation.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with the validation failures as the error message body.
     *
     * @param e the {@link ValidationException}
     * @return the {@link ValidationException.ValidationFailure} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public String validationFailure(final ValidationException e) {
        LOGGER.error("Object failed validation: {}", e.getValidationFailure());
        return GSON.toJson(e.getValidationFailure());
    }

    /**
     * Returned when an input object conflicts with an existing object.
     *
     * <p>
     * Returns a <b>409_CONFLICT</b> response with an error message body.
     *
     * @param e the {@link ConflictException}
     * @return the {@link ConflictException.ConflictFailure} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public String conflictedObject(final ConflictException e) {
        LOGGER.error("Object conflicts with an existing object: {}", e.getConflictFailure());
        return GSON.toJson(e.getConflictFailure());
    }

    /**
     * Returned when an object to be deleted is in use by another existing object.
     *
     * <p>
     * Returns a <b>409_CONFLICT</b> response with an error message body.
     *
     * @param e the {@link UsedByException}
     * @return the {@link UsedByException.UsedByFailure} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UsedByException.class)
    public String inUse(final UsedByException e) {
        LOGGER.error("Object is used by an existing object: {}", e.getUsedByFailure());
        return GSON.toJson(e.getUsedByFailure());
    }

    /**
     * Returned when a request made to a REST endpoint has an invalid 'Content-Type' header.
     *
     * <p>
     * Returns a <b>415_UNSUPPORTED_MEDIA_TYPE</b> response with the invalid 'Content-Type' as the error message body.
     *
     * @param e the {@link HttpMediaTypeNotSupportedException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public String invalidContentType(final HttpMediaTypeNotSupportedException e) {
        LOGGER.error(e.getMessage());
        return GSON.toJson(ErrorResponse.create(e.getMessage()));
    }

    /**
     * Returned when a request made to a REST endpoint has an unexpected error.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an 'unexpected' error message body.
     *
     * @param e the {@link Exception}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String serverError(final Exception e) {
        LOGGER.error("Unhandled exception occurred", e);
        return GSON.toJson(ErrorResponse.create("Unhandled exception occurred, please contact admin"));
    }
}
