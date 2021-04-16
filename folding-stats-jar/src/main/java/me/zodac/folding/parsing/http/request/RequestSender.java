package me.zodac.folding.parsing.http.request;

import me.zodac.folding.api.exception.FoldingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class RequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestSender.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private RequestSender() {

    }

    // The Folding@Home API seems to be caching the username+passkey stats. The first request will have the same result as the previous hour
    // To get around this, we send a request, ignore it, then send another request and parse that one
    public static HttpResponse<String> sendFoldingRequest(final String requestUrl) throws FoldingException {
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(requestUrl))
                    .header("Content-Type", "application/json")
                    .build();

            HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            // All user searches return a 200 response, even if the user/passkey is invalid, we will need to parse and check it later
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new FoldingException(String.format("Invalid response: %s", response));
            }

            return response;
        } catch (final IOException | InterruptedException e) {
            throw new FoldingException("Unable to send HTTP request to Folding@Home API", e);
        } catch (final ClassCastException e) {
            throw new FoldingException("Unable to parse HTTP response from Folding@Home API correctly", e);
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error retrieving stats for user", e);
            throw e;
        }
    }
}

