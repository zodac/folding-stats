/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

package me.zodac.folding.rest.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import me.zodac.folding.rest.exception.ServiceUnavailableException;
import me.zodac.folding.rest.util.ReadRequired;
import me.zodac.folding.rest.util.WriteRequired;
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
        LOGGER.debug("Checking system state for REST request at '{}', current state: {}", request.getRequestURI(), SystemStateManager.current());

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
        LOGGER.trace("State access requested to: #{}()", method.getName());

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
