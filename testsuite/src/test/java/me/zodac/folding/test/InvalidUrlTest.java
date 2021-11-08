package me.zodac.folding.test;

import static me.zodac.folding.rest.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.util.TestConstants.FOLDING_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import org.junit.jupiter.api.Test;

/**
 * Test to verify that an invalid URL will redirect to the <code>Team Competition</code> home page.
 */
class InvalidUrlTest {

    @Test
    void whenLoadingPage_andInvalidRestUrlIsUsed_thenResponseRedirectsToHomepage() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/invalid"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .build();

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode())
            .as("Expected invalid URL to redirect: " + response.body() + ", " + response.headers())
            .isEqualTo(HttpURLConnection.HTTP_SEE_OTHER);

        final String redirectLocation = response.headers()
            .firstValue("location")
            .orElse("no location header");

        assertThat(redirectLocation)
            .as("Expected redirect to go to home page: " + response.body() + ", " + response.headers())
            .isEqualTo("http://frontend_dev/");
    }
}
