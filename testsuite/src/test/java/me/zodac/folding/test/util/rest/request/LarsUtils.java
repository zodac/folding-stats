package me.zodac.folding.test.util.rest.request;

import static me.zodac.folding.test.util.TestConstants.TEST_SERVICE_URL;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import me.zodac.folding.api.tc.lars.LarsGpu;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.util.RestUtilConstants;

/**
 * Utility class for LARS-based tests.
 */
public final class LarsUtils {

    private LarsUtils() {

    }

    /**
     * Adds the provided {@link LarsGpu}s to the stubbed endpoint to be retrieved for LARS tests.
     *
     * @param larsGpus the {@link LarsGpu}s to be added
     * @throws FoldingRestException thrown if an error occurs adding the {@link LarsGpu}s
     */
    public static void addGpus(final LarsGpu... larsGpus) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(
                Set.of(larsGpus)
            )))
            .uri(URI.create(TEST_SERVICE_URL + "/gpu_ppd/overall_ranks"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
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
}
