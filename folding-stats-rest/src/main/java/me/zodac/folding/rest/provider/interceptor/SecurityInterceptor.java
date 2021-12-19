/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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
 *
 */

package me.zodac.folding.rest.provider.interceptor;

import static java.util.stream.Collectors.toSet;
import static me.zodac.folding.api.util.CollectionUtils.containsNoMatches;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.util.EncodingUtils;
import me.zodac.folding.rest.api.FoldingStatsService;
import me.zodac.folding.rest.api.header.RestHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 *                 If unsuccessful, or invalid authentication headers have been provided, a {@link UnauthorizedException} is thrown.
 *             </li>
 *             <li>
 *                 If successful, we retrieve the user's roles, and compare them to {@link RolesAllowed#value()}. If there is a match, we accept the
 *                 request, or else a {@link ForbiddenException} is thrown.
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @see FoldingStatsService#authenticateSystemUser(String, String)
 */
// TODO: [zodac] Sort out the logging
public class SecurityInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FoldingStatsService foldingStatsService;

    private SecurityInterceptor(final FoldingStatsService foldingStatsService) {
        this.foldingStatsService = foldingStatsService;
    }

    /**
     * Creates an instance of {@link SecurityInterceptor}.
     *
     * <p>
     * Since we must instantiate a new object to register in {@link me.zodac.folding.InterceptorRegister}, we cannot use an
     * {@link org.springframework.beans.factory.annotation.Autowired} {@link FoldingStatsService}. Instead, the
     * {@link me.zodac.folding.InterceptorRegister} will pass in its own injected {@link FoldingStatsService}.
     *
     * @param foldingStatsService the {@link FoldingStatsService}
     * @return the created {@link SecurityInterceptor}
     */
    public static SecurityInterceptor create(final FoldingStatsService foldingStatsService) {
        return new SecurityInterceptor(foldingStatsService);
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        LOGGER.debug("Validating REST request at '{}'", request::getRequestURI);

        try {
            if (handler instanceof HandlerMethod) {
                validateRequest(request, (HandlerMethod) handler);
            } else {
                LOGGER.warn("Unable to validate, handler is type: {}", handler.getClass());
                throw new UnauthorizedException();
            }
        } catch (final ForbiddenException | UnauthorizedException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error validating REST request at '{}'", request.getRequestURI(), e);
            throw new UnauthorizedException();
        }

        return true;
    }

    private void validateRequest(final HttpServletRequest request, final HandlerMethod handlerMethod) {
        final Method method = handlerMethod.getMethod();
        LOGGER.debug("Access requested to: #{}()", method.getName());

        if (method.isAnnotationPresent(DenyAll.class)) {
            LOGGER.warn("All access to '#{}()' at '{}' is denied", method.getName(), request.getRequestURI());
            throw new ForbiddenException();
        }

        if (method.isAnnotationPresent(PermitAll.class) || !method.isAnnotationPresent(RolesAllowed.class)) {
            LOGGER.debug("All access to '#{}()' at '{}' is permitted", method.getName(), request.getRequestURI());
            return;
        }

        final String authorizationProperty = request.getHeader(RestHeader.AUTHORIZATION.headerName());
        if (EncodingUtils.isInvalidBasicAuthentication(authorizationProperty)) {
            LOGGER.warn("Invalid {} value provided at '{}': '{}'", RestHeader.AUTHORIZATION.headerName(),
                request.getRequestURI(), authorizationProperty);
            throw new UnauthorizedException();
        }

        final Map<String, String> decodedUserNameAndPassword = EncodingUtils.decodeBasicAuthentication(authorizationProperty);
        final String userName = decodedUserNameAndPassword.get(EncodingUtils.DECODED_USERNAME_KEY);
        final String password = decodedUserNameAndPassword.get(EncodingUtils.DECODED_PASSWORD_KEY);

        final UserAuthenticationResult userAuthenticationResult = foldingStatsService.authenticateSystemUser(userName, password);

        if (!userAuthenticationResult.isUserExists()) {
            LOGGER.warn("User '{}' does not exist", userName);
            throw new UnauthorizedException();
        }

        if (!userAuthenticationResult.isPasswordMatch()) {
            LOGGER.warn("Invalid password supplied for user '{}'", userName);
            throw new UnauthorizedException();
        }

        final Set<String> userRoles = userAuthenticationResult.getUserRoles()
            .stream()
            .map(s -> s.toLowerCase(Locale.UK))
            .collect(toSet());

        final RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
        final Set<String> permittedRoles = Arrays.stream(rolesAnnotation.value())
            .map(s -> s.toLowerCase(Locale.UK))
            .collect(toSet());
        LOGGER.debug("Permitted roles: {}", permittedRoles);

        if (containsNoMatches(userRoles, permittedRoles)) {
            LOGGER.warn("User '{}' has roles {}, must be one of: {}", userName, userRoles, permittedRoles);
            throw new ForbiddenException();
        }

        LOGGER.debug("Request permitted");
    }
}
