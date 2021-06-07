package me.zodac.folding.rest.provider;

import me.zodac.folding.api.db.SystemUserAuthentication;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.utils.EncodingUtils;
import me.zodac.folding.ejb.BusinessLogic;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.util.response.Responses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static me.zodac.folding.rest.util.response.Responses.forbidden;
import static me.zodac.folding.rest.util.response.Responses.unauthorized;

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
 *                 If unsuccessful, or invalid authentication headers have been provided, we return a <b>401_UNAUTHORIZED</b> {@link javax.ws.rs.core.Response}
 *                 using {@link ContainerRequestContext#abortWith(Response)} and {@link Responses#unauthorized()}.</li>
 *           <li>
 *               If successful, we retrieve the user's roles, and compare them to {@link RolesAllowed#value()}. If there
 *               is a match, we accept the request, or else we return a <b>403_FORBIDDEN</b> {@link javax.ws.rs.core.Response}
 *               using {@link ContainerRequestContext#abortWith(Response)} and {@link Responses#forbidden()}.
 *           </li>
 *         </ul>
 *     </li>
 * </ul>
 */
@Provider
public class SecurityInterceptor implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityInterceptor.class);

    @Context
    private transient ResourceInfo resourceInfo;

    @EJB
    private transient BusinessLogic businessLogic;

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        LOGGER.trace("Validating REST request at '{}'", requestContext.getUriInfo().getAbsolutePath());
        try {
            validateRequest(requestContext);
        } catch (final FoldingException e) {
            LOGGER.warn("Error validating REST request at '{}'", requestContext.getUriInfo().getAbsolutePath());
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error validating REST request at '{}'", requestContext.getUriInfo().getAbsolutePath());
        }
    }

    private void validateRequest(final ContainerRequestContext requestContext) throws FoldingException {
        final Method method = resourceInfo.getResourceMethod();

        if (method.isAnnotationPresent(DenyAll.class)) {
            LOGGER.warn("All access to '#{}()' at '{}' is denied", method.getName(), requestContext.getUriInfo().getAbsolutePath());
            requestContext.abortWith(forbidden());
            return;
        }

        if (method.isAnnotationPresent(PermitAll.class) || !method.isAnnotationPresent(RolesAllowed.class)) {
            LOGGER.trace("All access to '#{}()' at '{}' is permitted", method.getName(), requestContext.getUriInfo().getAbsolutePath());
            return;
        }

        final String authorizationProperty = requestContext.getHeaderString(RestHeader.AUTHORIZATION.headerName());
        if (EncodingUtils.isNotBasicAuthentication(authorizationProperty)) {
            LOGGER.warn("Invalid {} value provided at '{}': '{}'", RestHeader.AUTHORIZATION.headerName(), requestContext.getUriInfo().getAbsolutePath(), authorizationProperty);
            requestContext.abortWith(unauthorized());
            return;
        }

        final Map<String, String> decodedUserNameAndPassword = EncodingUtils.decodeBasicAuthentication(authorizationProperty);
        final String userName = decodedUserNameAndPassword.get(EncodingUtils.DECODED_USERNAME_KEY);
        final String password = decodedUserNameAndPassword.get(EncodingUtils.DECODED_PASSWORD_KEY);

        final SystemUserAuthentication systemUserAuthentication = businessLogic.authenticateSystemUser(userName, password);

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
        LOGGER.trace("Permitted roles: {}", permittedRoles);

        if (containsNoMatches(userRoles, permittedRoles)) {
            LOGGER.warn("User '{}' has roles {}, must be one of: {}", userName, userRoles, permittedRoles);
            requestContext.abortWith(forbidden());
            return;
        }

        LOGGER.trace("Request permitted");
    }

    private static <V> boolean containsNoMatches(final Collection<V> first, final Collection<V> second) {
        return Collections.disjoint(first, second);
    }
}