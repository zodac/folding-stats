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

package me.zodac.folding.rest;

import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.rest.response.Responses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@link RestController} used to handle invalid URL requests. Redirects to the URL defined in environment variable <b>REDIRECT_URL</b>.
 */
@RestController
@RequestMapping("/error") // Must be on the RestController to override built-in Spring handler for '/error'
public class InvalidUrlRedirecter {

    private static final String REDIRECT_URL = EnvironmentVariableUtils.getOrDefault("REDIRECT_URL", "https://etf.axihub.ca/");

    /**
     * {@link GetMapping} for the {@code /error} URL (any request with an invalid URL will be sent to this endpoint by Spring).
     *
     * @return {@link HttpStatus#SEE_OTHER} {@link ResponseEntity} with the <b>Location</b> header to the URL defined by <b>REDIRECT_URL</b>
     */
    @GetMapping
    public ResponseEntity<String> redirectOnError() {
        return Responses.redirect(REDIRECT_URL);
    }
}