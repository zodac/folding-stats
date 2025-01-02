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

package me.zodac.folding.test.integration;

import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.DummyAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.integration.util.DummyAuthenticationData.READ_ONLY_USER;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.client.java.request.LoginRequestSender;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.test.integration.util.TestConstants;
import org.junit.jupiter.api.Test;

/**
 * Tests for the login REST endpoint at {@code /folding/login}.
 */
class LoginTest {

    private static final LoginRequestSender LOGIN_REQUEST_SENDER = LoginRequestSender.createWithUrl(TestConstants.FOLDING_URL);

    @Test
    void whenLoggingIn_givenCredentialsAreCorrect_andUserIsAdmin_thenResponseHas200Status() throws FoldingRestException {
        final HttpResponse<Void> response = LOGIN_REQUEST_SENDER.loginAsAdmin(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    void whenLoggingIn_givenCredentialsAreCorrect_andUserIsNotAdmin_thenResponseHas403Status() throws FoldingRestException {
        final HttpResponse<Void> response = LOGIN_REQUEST_SENDER.loginAsAdmin(READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 403_FORBIDDEN HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void whenLoggingIn_givenCredentialsAreIncorrect_thenResponseHas401Status() throws FoldingRestException {
        final HttpResponse<Void> response = LOGIN_REQUEST_SENDER.loginAsAdmin(INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenLoggingIn_givenInvalidCredentials_thenResponseHas400Status() throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(TestConstants.FOLDING_URL + "/login/admin"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to login as admin", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to login as admin", e);
        }
    }
}
