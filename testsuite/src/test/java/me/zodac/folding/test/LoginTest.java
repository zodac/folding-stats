package me.zodac.folding.test;

import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.utils.TestAuthenticationData.READ_ONLY_USER;
import static me.zodac.folding.test.utils.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.utils.TestConstants.HTTP_CLIENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.client.java.request.LoginRequestSender;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for the login REST endpoint at <code>/folding/login</code>.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginTest {

    private static final LoginRequestSender LOGIN_REQUEST_SENDER = LoginRequestSender.createWithUrl(FOLDING_URL);

    @Test
    void whenLoggingIn_givenCredentialsAreCorrect_andUserIsAdmin_thenResponseHas200StatusCode() throws FoldingRestException {
        final HttpResponse<Void> response = LOGIN_REQUEST_SENDER.loginAsAdmin(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    void whenLoggingIn_givenCredentialsAreCorrect_andUserIsNotAdmin_thenResponseHas403StatusCode() throws FoldingRestException {
        final HttpResponse<Void> response = LOGIN_REQUEST_SENDER.loginAsAdmin(READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void whenLoggingIn_givenCredentialsAreIncorrect_thenResponseHas401StatusCode() throws FoldingRestException {
        final HttpResponse<Void> response = LOGIN_REQUEST_SENDER.loginAsAdmin(INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenLoggingIn_givenInvalidCredentials_thenResponseHas400StatusCode() throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/login/admin"))
            .header("Content-Type", "application/json")
            .build();

        try {
            final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to login as admin", e);
        }
    }
}
