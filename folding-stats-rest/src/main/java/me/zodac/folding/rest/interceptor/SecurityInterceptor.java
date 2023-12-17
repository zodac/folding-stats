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

package me.zodac.folding.rest.interceptor;

import static java.util.stream.Collectors.toSet;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.util.CollectionUtils;
import me.zodac.folding.api.util.DecodedLoginCredentials;
import me.zodac.folding.api.util.EncodingUtils;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.exception.ForbiddenException;
import me.zodac.folding.rest.exception.UnauthorizedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * {@link HandlerInterceptor} that intercepts all requests and verifies that the request is authorized and authenticated. Each request
 * should be one of the following cases:
 * <ul>
 *     <li>
 *         Method is annotated with {@link DenyAll}. All requests are rejected, and a {@link ForbiddenException} is thrown.
 *     </li>
 *     <li>
 *         Method is annotated with {@link PermitAll}, or otherwise does not specify the {@link RolesAllowed} annotation. All requests
 *         are accepted.
 *     </li>
 *     <li>
 *         Method is annotated with {@link RolesAllowed}, which specifies the valid roles for the REST endpoint. In this case, the
 *         user authorization should be supplied in the {@link HttpServletRequest} using basic authentication. The user/password
 *         should be encoded using {@link java.util.Base64}. We decode and then authenticate against the DB.
 *         <ul>
 *             <li>
 *                 If unsuccessful, or invalid authentication headers have been provided, an {@link UnauthorizedException} is thrown.
 *             </li>
 *             <li>
 *                 If successful, we retrieve the user's roles, and compare them to {@link RolesAllowed#value()}. If there is a match, we accept the
 *                 request, or else a {@link ForbiddenException} is thrown.
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @see FoldingRepository#authenticateSystemUser(DecodedLoginCredentials)
 */
@Component
public final class SecurityInterceptor implements HandlerInterceptor {

    private static final Logger SECURITY_LOGGER = LogManager.getLogger(LoggerName.SECURITY.get());

    private final FoldingRepository foldingRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     */
    @Autowired
    public SecurityInterceptor(final FoldingRepository foldingRepository) {
        this.foldingRepository = foldingRepository;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        SECURITY_LOGGER.debug("Validating REST request at '{}'", request::getRequestURI);

        if (!(handler instanceof HandlerMethod)) {
            if (isPreflightRequest(handler)) {
                SECURITY_LOGGER.debug("Preflight request, no need to validate: {}", handler.getClass());
                return true;
            } else {
                SECURITY_LOGGER.warn("Unable to validate, handler is type: {}", handler.getClass());
                throw new UnauthorizedException();
            }
        }

        try {
            validateRequest(request, (HandlerMethod) handler);
        } catch (final ForbiddenException | UnauthorizedException e) {
            SECURITY_LOGGER.debug("Handling exception: {}", e.getClass().getSimpleName());
            throw e;
        } catch (final Exception e) {
            SECURITY_LOGGER.warn("Unexpected error validating REST request at '{}'", request.getRequestURI(), e);
            throw new UnauthorizedException(e);
        }

        return true;
    }

    // 'PreFlightHandler' is a private class in 'AbstractHandlerMapping' so we need to check for the classes it extends/implements
    private static boolean isPreflightRequest(final Object handler) {
        return handler instanceof CorsConfigurationSource && handler instanceof HttpRequestHandler;
    }

    private void validateRequest(final HttpServletRequest request, final HandlerMethod handlerMethod) {
        final Method method = handlerMethod.getMethod();
        SECURITY_LOGGER.debug("Security access requested to: #{}()", method.getName());

        if (method.isAnnotationPresent(PermitAll.class) || !method.isAnnotationPresent(RolesAllowed.class)) {
            SECURITY_LOGGER.debug("All access to '#{}()' at '{}' is permitted", method.getName(), request.getRequestURI());
            return;
        }

        if (method.isAnnotationPresent(DenyAll.class)) {
            SECURITY_LOGGER.warn("All access to '#{}()' at '{}' is denied", method.getName(), request.getRequestURI());
            throw new ForbiddenException();
        }

        final DecodedLoginCredentials decodedLoginCredentials = extractDecodedCredentials(request);

        final UserAuthenticationResult userAuthenticationResult = foldingRepository.authenticateSystemUser(decodedLoginCredentials);
        validateUserAuthentication(userAuthenticationResult, decodedLoginCredentials.username());
        validateRoles(userAuthenticationResult, method, decodedLoginCredentials.username());
        SECURITY_LOGGER.debug("Request permitted");
    }

    private static DecodedLoginCredentials extractDecodedCredentials(final HttpServletRequest request) {
        final String authorizationProperty = request.getHeader(RestHeader.AUTHORIZATION.headerName());
        if (authorizationProperty == null || EncodingUtils.isInvalidBasicAuthentication(authorizationProperty)) {
            SECURITY_LOGGER.warn("Invalid {} value provided at '{}': '{}'", RestHeader.AUTHORIZATION.headerName(),
                request.getRequestURI(), authorizationProperty);
            throw new UnauthorizedException();
        }

        return EncodingUtils.decodeBasicAuthentication(authorizationProperty);
    }

    private static void validateUserAuthentication(final UserAuthenticationResult userAuthenticationResult, final String userName) {
        if (!userAuthenticationResult.userExists()) {
            SECURITY_LOGGER.warn("User '{}' does not exist", userName);
            throw new UnauthorizedException();
        }

        if (!userAuthenticationResult.passwordMatch()) {
            SECURITY_LOGGER.warn("Invalid password supplied for user '{}'", userName);
            throw new UnauthorizedException();
        }
    }

    private static void validateRoles(final UserAuthenticationResult userAuthenticationResult, final Method method, final String userName) {
        final Set<String> userRoles = userAuthenticationResult.userRoles()
            .stream()
            .map(s -> s.toLowerCase(Locale.UK))
            .collect(toSet());

        final RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
        final Set<String> permittedRoles = Arrays.stream(rolesAnnotation.value())
            .map(s -> s.toLowerCase(Locale.UK))
            .collect(toSet());
        SECURITY_LOGGER.debug("Permitted roles: {}", permittedRoles);

        if (CollectionUtils.containsNoMatches(userRoles, permittedRoles)) {
            SECURITY_LOGGER.warn("User '{}' has roles {}, must be one of: {}", userName, userRoles, permittedRoles);
            throw new ForbiddenException();
        }
    }
}
