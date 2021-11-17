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
 */

package me.zodac.folding.test.endpoint;

import static me.zodac.folding.rest.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.client.java.request.LoginRequestSender;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

/**
 * Tests for the login REST endpoint at <code>/folding/login</code>.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginTest {

    @LocalServerPort
    private int randomPort;

    private String getFoldingUrl() {
        return "http://127.0.0.1:" + randomPort + "/folding";
    }

    private LoginRequestSender getLoginRequestSender() {
        final String foldingUrl = getFoldingUrl();
        return LoginRequestSender.createWithUrl(foldingUrl);
    }

    @Test
    void whenLoggingIn_givenCredentialsAreCorrect_andUserIsAdmin_thenResponseHas200Status() throws FoldingRestException {
        final HttpResponse<Void> response = getLoginRequestSender().loginAsAdmin(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }

//    @Test
//    void whenLoggingIn_givenCredentialsAreCorrect_andUserIsNotAdmin_thenResponseHas403Status() throws FoldingRestException {
//        final HttpResponse<Void> response = getLoginRequestSender().loginAsAdmin(READ_ONLY_USER.userName(), READ_ONLY_USER.password());
//        assertThat(response.statusCode())
//            .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
//            .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
//    }
//
//    @Test
//    void whenLoggingIn_givenCredentialsAreIncorrect_thenResponseHas401Status() throws FoldingRestException {
//        final HttpResponse<Void> response = getLoginRequestSender().loginAsAdmin(INVALID_USERNAME.userName(), INVALID_USERNAME.password());
//        assertThat(response.statusCode())
//            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
//            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
//    }

    @Test
    void whenLoggingIn_givenInvalidCredentials_thenResponseHas400Status() throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(getFoldingUrl() + "/login/admin"))
            .header("Content-Type", "application/json")
            .build();

        try {
            final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to login as admin", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to login as admin", e);
        }
    }
}
