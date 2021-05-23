package me.zodac.folding.test;

import me.zodac.folding.client.java.request.LoginRequestSender;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;

import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.utils.TestAuthenticationData.READ_ONLY_USER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the login REST endpoint at <code>/folding/login</code>.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginTest {

    private static final LoginRequestSender LOGIN_REQUEST_SENDER = LoginRequestSender.create("http://192.168.99.100:8081/folding");

    @Test
    public void whenLoggingIn_givenCredentialsAreCorrect_andUserIsAdmin_thenResponseHasA200StatusCode() throws FoldingRestException {
        final HttpResponse<Void> response = LOGIN_REQUEST_SENDER.loginAsAdmin(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    public void whenLoggingIn_givenCredentialsAreCorrect_andUserIsNotAdmin_thenResponseHasA403StatusCode() throws FoldingRestException {
        final HttpResponse<Void> response = LOGIN_REQUEST_SENDER.loginAsAdmin(READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    public void whenLoggingIn_givenCredentialsAreIncorrect_thenResponseHasA401StatusCode() throws FoldingRestException {
        final HttpResponse<Void> response = LOGIN_REQUEST_SENDER.loginAsAdmin(INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }
}
