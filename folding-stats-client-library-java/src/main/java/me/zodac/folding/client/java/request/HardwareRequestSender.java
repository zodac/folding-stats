package me.zodac.folding.client.java.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Convenience class to send HTTP requests to the {@link Hardware} REST endpoint.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HardwareRequestSender {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String foldingUrl;

    /**
     * Create an instance of {@link HardwareRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link HardwareRequestSender}
     */
    public static HardwareRequestSender create(final String foldingUrl) {
        return new HardwareRequestSender(foldingUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link Hardware}s in the system.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll(String)
     */
    public HttpResponse<String> getAll() throws FoldingRestException {
        return getAll(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link Hardware}s in the system.
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link Hardware} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param eTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve cached {@link Hardware}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll()
     */
    public HttpResponse<String> getAll(final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(foldingUrl + "/hardware"))
                .header("Content-Type", "application/json");

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header("If-None-Match", eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get all hardware", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link Hardware} with the given {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link Hardware} to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int, String)
     */
    public HttpResponse<String> get(final int hardwareId) throws FoldingRestException {
        return get(hardwareId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link Hardware} with the given {@code hardwareId}.
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link Hardware} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param hardwareId the ID of the {@link Hardware} to be retrieved
     * @param eTag       the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link Hardware}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int)
     */
    public HttpResponse<String> get(final int hardwareId, final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(foldingUrl + "/hardware/" + hardwareId))
                .header("Content-Type", "application/json");

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header("If-None-Match", eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get hardware", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link Hardware} in the system.
     *
     * @param hardware the {@link Hardware} to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final Hardware hardware) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(hardware)))
                .uri(URI.create(foldingUrl + "/hardware"))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create hardware", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link Hardware}s in the system.
     *
     * @param batchOfHardware the {@link List} of {@link Hardware}s to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> createBatchOf(final List<Hardware> batchOfHardware) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(batchOfHardware)))
                .uri(URI.create(foldingUrl + "/hardware/batch"))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create batch of hardware", e);
        }
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link Hardware} in the system.
     *
     * @param hardware the {@link Hardware} to update
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final Hardware hardware) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(hardware)))
                .uri(URI.create(foldingUrl + "/hardware/" + hardware.getId()))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to update hardware", e);
        }
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link Hardware} with the given {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link Hardware} to remove
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int hardwareId) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(foldingUrl + "/hardware/" + hardwareId))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to delete hardware", e);
        }
    }
}
