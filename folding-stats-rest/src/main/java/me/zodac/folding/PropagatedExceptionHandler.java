/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding;

import static me.zodac.folding.rest.util.RestUtilConstants.GSON;

import me.zodac.folding.rest.exception.ForbiddenException;
import me.zodac.folding.rest.exception.InvalidDayException;
import me.zodac.folding.rest.exception.InvalidIdException;
import me.zodac.folding.rest.exception.InvalidLoginCredentialsException;
import me.zodac.folding.rest.exception.InvalidMonthException;
import me.zodac.folding.rest.exception.InvalidYearException;
import me.zodac.folding.rest.exception.NotFoundException;
import me.zodac.folding.rest.exception.OutOfRangeDayException;
import me.zodac.folding.rest.exception.OutOfRangeIdException;
import me.zodac.folding.rest.exception.ServiceUnavailableException;
import me.zodac.folding.rest.exception.UnauthorizedException;
import me.zodac.folding.rest.response.ErrorResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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
     * Returns a <b>404_NOT_FOUND</b> response with no body.
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
     * Returns a <b>403_FORBIDDEN</b> response with no body.
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
     * Returns a <b>401_UNAUTHORIZED</b> response with no body.
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
     * Returns a <b>503_SERVICE_UNAVAILABLE</b> response with no body.
     *
     * @param e the {@link ServiceUnavailableException}
     */
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ServiceUnavailableException.class)
    public void serviceUnavailable(final ServiceUnavailableException e) {
        LOGGER.error(e.getMessage());
    }

    /**
     * Returned when a request made to a REST endpoint has an out of range ID.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an 'out of range ID' error message body.
     *
     * @param e the {@link OutOfRangeIdException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OutOfRangeIdException.class)
    public String idOutOfRange(final OutOfRangeIdException e) {
        final String errorMessage = String.format("The ID '%s' is out of range", e.getId());
        LOGGER.error(errorMessage);
        return GSON.toJson(ErrorResponse.create(errorMessage));
    }

    /**
     * Returned when a request made to a REST endpoint has an invalid ID.
     *
     * <p>
     * Returns a <b>400_BAD_REQUEST</b> response with an 'invalid ID' error message body.
     *
     * @param e the {@link InvalidIdException}
     * @return the {@link ErrorResponse} body
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidIdException.class)
    public String invalidId(final InvalidIdException e) {
        final String errorMessage = String.format("The ID '%s' is not a valid format", e.getId());
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
     * Returned when a request made to a REST endpoint has an invalid 'Content-Type' header.
     *
     * <p>
     * Returns a <b>415_UNSUPPORTED_MEDIA_TYPE</b> response with a the invalid 'Content-Type' as the error message body.
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