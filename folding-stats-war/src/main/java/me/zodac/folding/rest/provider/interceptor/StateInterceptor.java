package me.zodac.folding.rest.provider.interceptor;

import static me.zodac.folding.rest.response.Responses.serviceUnavailable;

import java.lang.reflect.Method;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.state.OperationType;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.WriteRequired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Provider} that intercepts all requests and verifies that the system is in a valid {@link me.zodac.folding.api.state.SystemState} to allow
 * {@link OperationType#READ} or {@link OperationType#WRITE} requests.
 *
 * <p>
 * If a function is annotated with {@link ReadRequired} and the current {@link me.zodac.folding.api.state.SystemState} does not allow
 * {@link OperationType#READ} operations, or is annotated with {@link WriteRequired} and the current
 * {@link me.zodac.folding.api.state.SystemState} does not allow {@link OperationType#WRITE} operations, we return a
 * <b>503_SERVICE_UNAVAILABLE</b> {@link javax.ws.rs.core.Response}.
 *
 * @see me.zodac.folding.SystemStateManager
 */
@Provider
public class StateInterceptor implements ContainerRequestFilter {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        LOGGER.debug("Checking system state for REST request at '{}'", requestContext.getUriInfo().getAbsolutePath());

        try {
            validateSystemState(requestContext);
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error validating REST request at '{}'", requestContext.getUriInfo().getAbsolutePath(), e);
        }
    }

    private void validateSystemState(final ContainerRequestContext requestContext) {
        final Method method = resourceInfo.getResourceMethod();
        LOGGER.trace("Access requested to: #{}()", method.getName());

        if (method.isAnnotationPresent(ReadRequired.class) && SystemStateManager.current().isReadBlocked()) {
            LOGGER.warn("System state {} does not allow read requests", SystemStateManager.current());
            requestContext.abortWith(serviceUnavailable());
            return;
        }

        if (method.isAnnotationPresent(WriteRequired.class) && SystemStateManager.current().isWriteBlocked()) {
            LOGGER.warn("System state {} does not allow write requests", SystemStateManager.current());
            requestContext.abortWith(serviceUnavailable());
            return;
        }

        LOGGER.trace("Request permitted");
    }
}