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

package me.zodac.folding.stats.http.request;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.rest.api.header.CacheControl;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.stats.http.response.StatsResponseParser;
import org.apache.commons.lang3.StringUtils;
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
 * there is no change, we will send a second REST request to the server.
 *
 * <p>
 * While not ideal, I'm not sure of any other way of forcing an update since it seems to be a server-side decision. This
 * should at least help in reducing the number of duplicate requests that need to be sent.
 */
public final class StatsRequestSender {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private static final Map<String, String> CACHED_RESPONSE_BODIES = new HashMap<>();

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
     *     <li>{@link RestHeader#CACHE_CONTROL}: {@link CacheControl#NO_CACHE}</li>
     * </ul>
     *
     * @param statsRequestUrl the URL the stats request should be sent to
     * @return the {@link HttpResponse} to be parsed by {@link StatsResponseParser}
     * @throws ExternalConnectionException thrown if an error occurs connecting to the external URL
     */
    public static HttpResponse<String> sendFoldingRequest(final StatsRequestUrl statsRequestUrl) throws ExternalConnectionException {
        final String requestUrl = statsRequestUrl.getUrl();

        try {
            final HttpResponse<String> response = sendRequest(requestUrl);

            // All user searches 'should' return a 200 response, even if the user/passkey is invalid
            // The response should be parsed and validated, which we do later
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new ExternalConnectionException(response.uri().toString(), String.format("Invalid response: %s", response));
            }

            if (StringUtils.isBlank(response.body())) {
                throw new ExternalConnectionException(response.uri().toString(), "Empty Folding points response");
            }

            return response;
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

    private static HttpResponse<String> sendRequest(final String requestUrl) throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(requestUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .header(RestHeader.CACHE_CONTROL.headerName(), CacheControl.NO_CACHE.cacheControl())
            .build();

        final String cachedResponseBody = CACHED_RESPONSE_BODIES.get(requestUrl);

        final HttpResponse<String> firstResponse = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        LOGGER.trace("First response: {}", firstResponse::body);

        if (firstResponse.body().equalsIgnoreCase(cachedResponseBody)) {
            final HttpResponse<String> secondResponse = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("Initial request returned a response equal to response in the cache, sent second request to Stanford: '{}' vs '{}'",
                firstResponse::body, secondResponse::body);

            CACHED_RESPONSE_BODIES.put(requestUrl, secondResponse.body());
            return secondResponse;
        }

        CACHED_RESPONSE_BODIES.put(requestUrl, firstResponse.body());
        return firstResponse;
    }
}


