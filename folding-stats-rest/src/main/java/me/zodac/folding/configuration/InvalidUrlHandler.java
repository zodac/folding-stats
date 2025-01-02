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

package me.zodac.folding.configuration;

import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.rest.response.Responses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * {@link ControllerAdvice} used to handle invalid URL requests. Redirects to the URL defined in environment variable <b>REDIRECT_URL</b>.
 *
 * <p>
 * Marked as {@link Ordered#HIGHEST_PRECEDENCE} so it takes precedence over {@link GlobalExceptionHandler}.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InvalidUrlHandler {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());
    private static final String REDIRECT_URL = EnvironmentVariableUtils.getOrDefault("REDIRECT_URL", "https://etf.axihub.ca/");

    /**
     * {@link ExceptionHandler} for when an invalid URL is requested.
     *
     * <p>
     * # Returns a <b>303_SEE_OTHER</b> response with no response body.
     *
     * @param e the {@link NoResourceFoundException}
     * @return {@link HttpStatus#SEE_OTHER} {@link ResponseEntity} with the <b>Location</b> header to the URL defined by <b>REDIRECT_URL</b>
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> redirect(final NoResourceFoundException e) {
        AUDIT_LOGGER.debug("Invalid request received, redirecting to '{}", REDIRECT_URL, e);
        return Responses.redirect(REDIRECT_URL);
    }
}
