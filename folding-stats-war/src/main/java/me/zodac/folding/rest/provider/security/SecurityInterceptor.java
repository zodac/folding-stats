package me.zodac.folding.rest.provider.security;

import static java.util.stream.Collectors.toSet;
import static me.zodac.folding.api.utils.CollectionUtils.containsNoMatches;
import static me.zodac.folding.rest.response.Responses.forbidden;
import static me.zodac.folding.rest.response.Responses.unauthorized;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import me.zodac.folding.api.SystemUserAuthentication;
import me.zodac.folding.api.utils.EncodingUtils;
import me.zodac.folding.ejb.OldFacade;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.response.Responses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Provider} that intercepts all requests and verifies that the request is authorized and authenticated. Each request
 * should be one of the following cases:
 * <ul>
 *     <li>
 *         Method is annotated with {@link DenyAll}. All requests are rejected, and a <b>403_FORBIDDEN</b> {@link javax.ws.rs.core.Response}
 *         is returned using {@link ContainerRequestContext#abortWith(Response)} and {@link Responses#forbidden()}.
 *     </li>
 *     <li>
 *         Method is annotated with {@link PermitAll}, or otherwise does not specify the {@link RolesAllowed} annotation. All requests
 *         are accepted.
 *     </li>
 *     <li>
 *         Method is annotated with {@link RolesAllowed}, which specifies the valid roles for the REST endpoint. In this case, the
 *         user authorization should be supplied in the {@link ContainerRequestContext} using basic authentication. The user/password
 *         should be encoded using {@link Base64}. We decode and then authenticate against the DB.
 *         <ul>
 *             <li>
 *                 If unsuccessful, or invalid authentication headers have been provided, we return a <b>401_UNAUTHORIZED</b>
 *                 {@link javax.ws.rs.core.Response}using {@link ContainerRequestContext#abortWith(Response)} and {@link Responses#unauthorized()}.
 *             </li>
 *             <li>
 *                 If successful, we retrieve the user's roles, and compare them to {@link RolesAllowed#value()}. If there is a match, we accept the
 *                 request, or else we return a <b>403_FORBIDDEN</b> {@link javax.ws.rs.core.Response} using
 *                 {@link ContainerRequestContext#abortWith(Response)} and {@link Responses#forbidden()}.
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 */
@Provider
public class SecurityInterceptor implements ContainerRequestFilter {

    private static final Logger LOGGER = LogManager.getLogger();

    @Context
    private ResourceInfo resourceInfo;

    @EJB
    private OldFacade oldFacade;

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        LOGGER.info("Validating REST request at '{}'", requestContext.getUriInfo().getAbsolutePath());
        try {
            validateRequest(requestContext);
        } catch (final Exception e) {
            LOGGER.debug("Unexpected error validating REST request at '{}'", requestContext.getUriInfo().getAbsolutePath(), e);
            LOGGER.warn("Unexpected error validating REST request at '{}'", requestContext.getUriInfo().getAbsolutePath());
        }
    }

    private void validateRequest(final ContainerRequestContext requestContext) {
        final Method method = resourceInfo.getResourceMethod();
        LOGGER.info("Access requested to: #{}()", method.getName());

        if (method.isAnnotationPresent(DenyAll.class)) {
            LOGGER.warn("All access to '#{}()' at '{}' is denied", method.getName(), requestContext.getUriInfo().getAbsolutePath());
            requestContext.abortWith(forbidden());
            return;
        }

        if (method.isAnnotationPresent(PermitAll.class) || !method.isAnnotationPresent(RolesAllowed.class)) {
            LOGGER.info("All access to '#{}()' at '{}' is permitted", method.getName(), requestContext.getUriInfo().getAbsolutePath());
            return;
        }

        final String authorizationProperty = requestContext.getHeaderString(RestHeader.AUTHORIZATION.headerName());
        if (EncodingUtils.isNotBasicAuthentication(authorizationProperty)) {
            LOGGER.warn("Invalid {} value provided at '{}': '{}'", RestHeader.AUTHORIZATION.headerName(),
                requestContext.getUriInfo().getAbsolutePath(), authorizationProperty);
            requestContext.abortWith(unauthorized());
            return;
        }

        final Map<String, String> decodedUserNameAndPassword = EncodingUtils.decodeBasicAuthentication(authorizationProperty);
        final String userName = decodedUserNameAndPassword.get(EncodingUtils.DECODED_USERNAME_KEY);
        final String password = decodedUserNameAndPassword.get(EncodingUtils.DECODED_PASSWORD_KEY);

        final SystemUserAuthentication systemUserAuthentication = oldFacade.authenticateSystemUser(userName, password);

        if (!systemUserAuthentication.isUserExists()) {
            LOGGER.warn("User '{}' does not exist", userName);
            requestContext.abortWith(unauthorized());
            return;
        }

        if (!systemUserAuthentication.isPasswordMatch()) {
            LOGGER.warn("Invalid password supplied for user '{}'", userName);
            requestContext.abortWith(unauthorized());
            return;
        }

        final Set<String> userRoles = systemUserAuthentication.getUserRoles().stream().map(String::toLowerCase).collect(toSet());

        final RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
        final Set<String> permittedRoles = Arrays.stream(rolesAnnotation.value()).map(String::toLowerCase).collect(toSet());
        LOGGER.info("Permitted roles: {}", permittedRoles);

        if (containsNoMatches(userRoles, permittedRoles)) {
            LOGGER.warn("User '{}' has roles {}, must be one of: {}", userName, userRoles, permittedRoles);
            requestContext.abortWith(forbidden());
            return;
        }

        LOGGER.info("Request permitted");
    }
}