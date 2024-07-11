/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.lars;

import static me.zodac.folding.rest.api.util.RestUtilConstants.GSON;
import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.stream.Collectors;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.lars.LarsGpu;
import me.zodac.folding.api.tc.lars.LarsGpuResponse;
import me.zodac.folding.api.tc.lars.LarsRetriever;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation of {@link LarsRetriever} that retrieves {@link Hardware} from the LARS PPD database.
 *
 * @see <a href="https://folding.lar.systems/api/gpu_ppd/gpu_rank_list.json">LARS GPU PPD database</a>
 */
public final class HttpLarsRetriever implements LarsRetriever {

    private static final Logger LARS_LOGGER = LogManager.getLogger(LoggerName.LARS.get());

    private HttpLarsRetriever() {

    }

    /**
     * Creates an instance of {@link HttpLarsRetriever}.
     *
     * @return the created {@link HttpLarsRetriever}
     */
    public static HttpLarsRetriever create() {
        return new HttpLarsRetriever();
    }

    @Override
    public Set<Hardware> retrieveGpus(final String gpuApiUrl) {
        LARS_LOGGER.debug("Retrieving LARS GPU data from: '{}'", gpuApiUrl);

        try {
            return retrieveResponseAndParseGpus(gpuApiUrl);
        } catch (final ExternalConnectionException e) {
            LARS_LOGGER.warn("Error retrieving data from LARS GPU DB", e);
            return Set.of();
        }
    }

    private static Set<Hardware> retrieveResponseAndParseGpus(final String gpuDatabaseUrl) throws ExternalConnectionException {
        final HttpResponse<String> response = sendHttpRequest(gpuDatabaseUrl);
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new ExternalConnectionException(response.uri().toString(),
                String.format("Invalid response (status code: %s): %s", response.statusCode(), response.body()));
        }

        LARS_LOGGER.debug("JSON response: {}", response.body());
        final LarsGpuResponse larsGpuResponse = GSON.fromJson(response.body(), LarsGpuResponse.class);

        return larsGpuResponse.rankedGpus()
            .stream()
            .filter(HttpLarsRetriever::isValidLarsGpu)
            .map(HttpLarsRetriever::toHardware)
            .collect(Collectors.toSet());
    }

    private static boolean isValidLarsGpu(final LarsGpu larsGpu) {
        final boolean isValid = StringUtils.isNotBlank(larsGpu.detailedName())
            && StringUtils.isNotBlank(larsGpu.name())
            && HardwareMake.get(larsGpu.make()) != HardwareMake.INVALID
            && larsGpu.multiplier() > 0.0D
            && larsGpu.ppdAverageOverall() > 0L;

        if (!isValid) {
            LARS_LOGGER.warn("Invalid {}: {}", LarsGpu.class.getSimpleName(), larsGpu);
        }

        return isValid;
    }

    private static Hardware toHardware(final LarsGpu larsGpu) {
        return Hardware.create(
            Hardware.EMPTY_HARDWARE_ID,
            larsGpu.detailedName(),
            larsGpu.name(),
            HardwareMake.get(larsGpu.make()),
            HardwareType.GPU,
            larsGpu.multiplier(),
            larsGpu.ppdAverageOverall()
        );
    }

    private static HttpResponse<String> sendHttpRequest(final String requestUrl) throws ExternalConnectionException {
        try {
            final HttpRequest request = createHttpRequest(requestUrl);
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final ConnectException e) {
            LARS_LOGGER.debug("Connection error retrieving LARS GPU data", e);
            LARS_LOGGER.warn("Connection error retrieving LARS GPU data");
            throw new ExternalConnectionException(requestUrl, "Unable to connect to LARS PPD DB API", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalConnectionException(requestUrl, "Unable to send HTTP request LARS PPD DB API", e);
        } catch (final IOException e) {
            throw new ExternalConnectionException(requestUrl, "Unable to send HTTP request LARS PPD DB API", e);
        } catch (final ClassCastException e) {
            throw new ExternalConnectionException(requestUrl, "Unable to parse HTTP response from LARS PPD DB API correctly", e);
        } catch (final Exception e) {
            LARS_LOGGER.warn("Unexpected error retrieving LARS GPU data", e);
            throw new ExternalConnectionException(requestUrl, "Unexpected error retrieving LARS GPU data", e);
        }
    }

    private static HttpRequest createHttpRequest(final String requestUrl) {
        return HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(requestUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();
    }
}
