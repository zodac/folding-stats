/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.rest.controller;

import static me.zodac.folding.rest.response.Responses.redirect;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.api.util.LoggerName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RestController} used to handle invalid URL requests. Redirects to the URL defined in environment variable <b>REDIRECT_URL</b>.
 */
@Hidden
@RestController
@RequestMapping("/error") // Must be on the RestController to override built-in Spring handler for '/error'
public class ErrorHandlerController implements ErrorController {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());
    private static final String REDIRECT_URL = EnvironmentVariableUtils.getOrDefault("REDIRECT_URL", "https://etf.axihub.ca/");

    /**
     * {@link GetMapping} for the {@code /error} URL (any request with an invalid URL will be sent to this endpoint by Spring).
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link HttpStatus#SEE_OTHER} {@link ResponseEntity} with the <b>Location</b> header to the URL defined by <b>REDIRECT_URL</b>
     */
    @GetMapping
    public ResponseEntity<Void> redirectOnError(final HttpServletRequest request) {
        AUDIT_LOGGER.debug("Invalid request found at '{}', redirecting to '{}", request::getRequestURI, () -> REDIRECT_URL);
        return redirect(REDIRECT_URL);
    }
}
