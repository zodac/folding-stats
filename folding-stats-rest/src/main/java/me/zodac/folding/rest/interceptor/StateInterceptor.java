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

package me.zodac.folding.rest.interceptor;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.rest.exception.ServiceUnavailableException;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * {@link HandlerInterceptor} that intercepts all requests and verifies that the system is in a valid {@link me.zodac.folding.api.state.SystemState}
 * to allow {@link me.zodac.folding.api.state.OperationType#READ} or {@link me.zodac.folding.api.state.OperationType#WRITE} requests.
 *
 * <p>
 * If a function is annotated with {@link ReadRequired} and the current {@link me.zodac.folding.api.state.SystemState} does not allow
 * {@link me.zodac.folding.api.state.OperationType#READ} operations, or is annotated with {@link WriteRequired} and the current
 * {@link me.zodac.folding.api.state.SystemState} does not allow {@link me.zodac.folding.api.state.OperationType#WRITE} operations, a
 * {@link ServiceUnavailableException} is thrown.
 *
 * @see SystemStateManager
 */
@Component
public class StateInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        LOGGER.debug("Checking system state for REST request at '{}'", request.getRequestURI());

        if (!(handler instanceof HandlerMethod)) {
            if (isPreflightRequest(handler)) {
                LOGGER.debug("Preflight request, no need to validate: {}", handler.getClass());
                return true;
            } else {
                LOGGER.warn("Unable to validate, handler is type: {}", handler.getClass());
                throw new ServiceUnavailableException();
            }
        }

        try {
            validateSystemState((HandlerMethod) handler);
        } catch (final ServiceUnavailableException e) {
            LOGGER.debug("Handling exception: {}", e.getClass().getSimpleName());
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error validating REST request at '{}'", request.getRequestURI(), e);
            throw new ServiceUnavailableException(e);
        }

        return true;
    }

    // 'PreFlightHandler' is a private class in 'AbstractHandlerMapping' so we need to check for the classes it extends/implements
    private static boolean isPreflightRequest(final Object handler) {
        return handler instanceof CorsConfigurationSource && handler instanceof HttpRequestHandler;
    }

    private static void validateSystemState(final HandlerMethod handlerMethod) {
        final Method method = handlerMethod.getMethod();
        LOGGER.trace("Access requested to: #{}()", method.getName());

        if (method.isAnnotationPresent(ReadRequired.class) && SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            throw new ServiceUnavailableException();
        }

        if (method.isAnnotationPresent(WriteRequired.class) && SystemStateManager.current().isWriteBlocked()) {
            LOGGER.warn("System state {} does not allow write requests", SystemStateManager.current());
            throw new ServiceUnavailableException();
        }

        LOGGER.trace("Request permitted");
    }
}
