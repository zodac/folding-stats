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

package me.zodac.folding.test.integration.util.rest.request;

import static me.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.ADMIN_USER;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.api.tc.lars.LarsGpu;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.util.RestUtilConstants;
import me.zodac.folding.test.integration.util.TestConstants;

/**
 * Utility class for LARS-based tests.
 */
public final class LarsUtils {

    private LarsUtils() {

    }

    /**
     * Executes a manual update of {@link me.zodac.folding.api.tc.Hardware} from the LARS DB.
     *
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void manualLarsUpdate() throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(TestConstants.FOLDING_URL + "/debug/lars"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        try {
            final HttpResponse<Void> response = RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode())
                .as("Expected a 200_OK")
                .isEqualTo(HttpURLConnection.HTTP_OK);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to add LARS GPUs", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to add LARS GPUs", e);
        }
    }

    /**
     * Adds the provided {@link LarsGpu}s to the stubbed endpoint to be retrieved for LARS tests.
     *
     * @param larsGpus the {@link LarsGpu}s to be added
     * @throws FoldingRestException thrown if an error occurs adding the {@link LarsGpu}s
     */
    public static void addGpusToLarsDb(final LarsGpu... larsGpus) throws FoldingRestException {
        for (final LarsGpu larsGpu : larsGpus) {
            addGpuToLarsDb(larsGpu);
        }
    }

    private static void addGpuToLarsDb(final LarsGpu larsGpu) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(larsGpu)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/api/gpu_ppd"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            final HttpResponse<String> response = RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpURLConnection.HTTP_CREATED) {
                throw new FoldingRestException(
                    String.format("Invalid response (%s) when adding LARS GPUs: %s", response.statusCode(), response.body()));
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to add LARS GPUs", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to add LARS GPUs", e);
        }
    }

    /**
     * Deletes all {@link LarsGpu}s from the stubbed endpoint to be retrieved for LARS tests.
     *
     * @throws FoldingRestException thrown if an error occurs deleting the {@link LarsGpu}s
     */
    public static void deleteAllGpusFromLarsDb() throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(TestConstants.FOLDING_URL + "/api/gpu_ppd"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        try {
            final HttpResponse<String> response = RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new FoldingRestException(
                    String.format("Invalid response (%s) when deleting LARS GPUs: %s", response.statusCode(), response.body()));
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to delete LARS GPUs", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to delete LARS GPUs", e);
        }
    }
}
