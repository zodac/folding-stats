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
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.test.integration.util.TestConstants;
import org.junit.jupiter.api.Test;

/**
 * Test to verify that an invalid URL will redirect to the {@code Team Competition} home page.
 */
class InvalidUrlTest {

    @Test
    void whenLoadingPage_andInvalidRestUrlIsUsed_thenResponseRedirectsToHomepage() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(TestConstants.FOLDING_URL + "/invalid"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode())
            .as("Expected invalid URL to redirect: %s, %s", response.body(), response.headers())
            .isEqualTo(HttpURLConnection.HTTP_SEE_OTHER);

        final String redirectLocation = response.headers()
            .firstValue("Location")
            .orElseThrow(() -> new AssertionError("no location header"));

        assertThat(redirectLocation)
            .as("Expected redirect to go to home page: %s, %s", response.body(), response.headers())
            .isEqualTo("https://127.0.0.1:81");
    }
}
