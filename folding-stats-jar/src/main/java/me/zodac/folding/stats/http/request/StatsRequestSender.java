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

package me.zodac.folding.stats.http.request;

import static me.zodac.folding.api.util.EnvironmentVariableUtils.getIntOrDefault;
import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.stats.http.response.StatsResponseParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to send REST requests to the Stanford Folding@Home REST endpoints.
 *
 * <p>
 * The Folding@Home API, however, does seem to be caching responses for requests sent later. The {@link RestHeader#CACHE_CONTROL}
 * header is used to try and reduce the caching done on to the Folding@Home API, but is not guaranteed to work.
 * In addition to using this header, we keep a cache of the most recent response for each request URL, since the URL
 * will contain a unique username/passkey for each request. We will compare the response to this cached version, and if
 * there is no change, we will send REST requests to the server. The number of requests is defined by the environment variable
 * <b>MAXIMUM_HTTP_REQUEST_ATTEMPTS</b>.
 *
 * <p>
 * While not ideal, I'm not sure of any other way of forcing an update since it seems to be a server-side decision. This
 * should at least help in reducing the number of duplicate requests that need to be sent.
 */
public final class StatsRequestSender {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int HTTP_TOO_MANY_REQUESTS_STATUS_CODE = 429;
    private static final int MAX_RETRY_ATTEMPTS = getIntOrDefault("MAXIMUM_HTTP_REQUEST_ATTEMPTS", 2);
    private static final long SECONDS_BETWEEN_ATTEMPTS = getIntOrDefault("SECONDS_BETWEEN_HTTP_REQUEST_ATTEMPTS", 20);
    private static final long MINIMUM_REQUESTS_TO_FLUSH_EXTERNAL_CACHE = 1;

    private StatsRequestSender() {

    }

    /**
     * Sends a {@link HttpRequest.Builder#GET()} request to the provided URL, generated by one of:
     * <ul>
     *     <li>{@link PointsUrlBuilder}</li>
     *     <li>{@link UnitsUrlBuilder}</li>
     * </ul>
     *
     * <p>
     * Includes the headers:
     * <ul>
     *     <li>{@link RestHeader#CONTENT_TYPE}: {@link ContentType#JSON}</li>
     *     <li>{@link RestHeader#CACHE_CONTROL}: {@link CacheControl#NO_CACHE} {@link CacheControl#NO_STORE}</li>
     * </ul>
     *
     * @param statsRequestUrl the URL the stats request should be sent to
     * @return the {@link HttpResponse} to be parsed by {@link StatsResponseParser}
     * @throws ExternalConnectionException thrown if an error occurs connecting to the external URL
     */
    public static HttpResponse<String> sendFoldingRequest(final StatsRequestUrl statsRequestUrl) throws ExternalConnectionException {
        final String requestUrl = statsRequestUrl.url();
        return sendRequestWithRetries(requestUrl);
    }

    private static HttpResponse<String> sendRequestWithRetries(final String requestUrl) throws ExternalConnectionException {
        // In case the user has set the env variable less than 1, we will always do at least 1 attempt
        final int maximumHttpRequests = Math.max(MAX_RETRY_ATTEMPTS, 1);

        // Continue making requests as long as we have not hit max attempts, or a response is found
        for (int requestCount = 1; requestCount <= maximumHttpRequests; requestCount++) {
            LOGGER.debug("Sending request #{}", requestCount);
            final HttpResponse<String> response = sendHttpRequest(requestUrl);

            // Possible 429 response if too many requests are sent at once, so we sleep when this occurs
            if (response.statusCode() == HTTP_TOO_MANY_REQUESTS_STATUS_CODE) {
                try {
                    LOGGER.debug("Received 'too many requests' response for request #{} to {}, sleeping for {}s", requestCount, requestUrl,
                        SECONDS_BETWEEN_ATTEMPTS);
                    Thread.sleep(TimeUnit.SECONDS.toMillis(SECONDS_BETWEEN_ATTEMPTS));
                } catch (final InterruptedException e) {
                    LOGGER.debug("Unexpected interrupt", e);
                    Thread.currentThread().interrupt();
                }

                // After sleeping, continue the FOR loop for the next HTTP request, no need to parse the response any futher
                continue;
            }

            validateFoldingResponse(response);

            // We don't want to return the first response, since it is often a cached value from the Stanford Folding@Home API
            // Since the returned value from the Stanford Folding@Home API is often cached, we will ignore requests until we hit the minumum
            // number of requests, and only then return a subsequent valid response (or else go into error handling).
            // However, if we are only allowing one HTTP request, we will return the potentially cached response
            if (requestCount > MINIMUM_REQUESTS_TO_FLUSH_EXTERNAL_CACHE && maximumHttpRequests != 1) {
                return response;
            }
        }

        throw new ExternalConnectionException(requestUrl,
            String.format("'Too many requests' response returned after %s attempts", maximumHttpRequests));
    }

    private static void validateFoldingResponse(final HttpResponse<String> response) throws ExternalConnectionException {
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new ExternalConnectionException(response.uri().toString(),
                String.format("Invalid response (status code: %s): %s", response.statusCode(), response.body()));
        }

        if (StringUtils.isBlank(response.body())) {
            throw new ExternalConnectionException(response.uri().toString(), "Empty Folding@Home stats response");
        }
    }

    private static HttpResponse<String> sendHttpRequest(final String requestUrl) throws ExternalConnectionException {
        try {
            final HttpRequest request = createHttpRequest(requestUrl);
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final ConnectException e) {
            LOGGER.debug("Connection error retrieving stats for user", e);
            LOGGER.warn("Connection error retrieving stats for user");
            throw new ExternalConnectionException(requestUrl, "Unable to connect to Folding@Home API", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalConnectionException(requestUrl, "Unable to send HTTP request to Folding@Home API", e);
        } catch (final IOException e) {
            throw new ExternalConnectionException(requestUrl, "Unable to send HTTP request to Folding@Home API", e);
        } catch (final ClassCastException e) {
            throw new ExternalConnectionException(requestUrl, "Unable to parse HTTP response from Folding@Home API correctly", e);
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error retrieving stats for user", e);
            throw new ExternalConnectionException(requestUrl, "Unexpected error retrieving stats for user", e);
        }
    }

    private static HttpRequest createHttpRequest(final String requestUrl) {
        return HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(requestUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.CACHE_CONTROL.headerName(), CacheControl.NO_CACHE.headerValue() + " " + CacheControl.NO_STORE.headerValue())
            .build();
    }
}


