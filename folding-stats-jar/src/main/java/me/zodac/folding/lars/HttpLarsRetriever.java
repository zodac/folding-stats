/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.lars;

import static me.zodac.folding.rest.api.util.RestUtilConstants.GSON;
import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
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
        } catch (final Exception e) {
            LARS_LOGGER.warn("Error retrieving data from LARS GPU DB", e);
            return Collections.emptySet();
        }
    }

    private static Set<Hardware> retrieveResponseAndParseGpus(final String gpuDatabaseUrl) throws ExternalConnectionException {
        final HttpResponse<String> response = sendHttpRequest(gpuDatabaseUrl);
        if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            throw new ExternalConnectionException(response.uri().toString(),
                String.format("Invalid response (status code: %s): %s", response.statusCode(), response.body()));
        }

        final LarsGpuResponse larsGpuResponse = GSON.fromJson(response.body(), LarsGpuResponse.class);

        return larsGpuResponse.getRankedGpus()
            .stream()
            .filter(HttpLarsRetriever::isValidLarsGpu)
            .map(HttpLarsRetriever::toHardware)
            .collect(Collectors.toSet());
    }

    private static boolean isValidLarsGpu(final LarsGpu larsGpu) {
        return StringUtils.isNotBlank(larsGpu.getDetailedName())
            && StringUtils.isNotBlank(larsGpu.getName())
            && HardwareMake.get(larsGpu.getMake()) != HardwareMake.INVALID
            && larsGpu.getMultiplier() > 0.0D
            && larsGpu.getPpdAverageOverall() > 0L;
    }

    private static Hardware toHardware(final LarsGpu larsGpu) {
        return Hardware.create(
            Hardware.EMPTY_HARDWARE_ID,
            larsGpu.getDetailedName(),
            larsGpu.getName(),
            HardwareMake.get(larsGpu.getMake()),
            HardwareType.GPU,
            larsGpu.getMultiplier(),
            larsGpu.getPpdAverageOverall()
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
