package me.zodac.folding.client.java.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Convenience class to send HTTP requests to the <code>Team Competition</code> REST endpoint.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamCompetitionRequestSender {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String foldingUrl;

    /**
     * Create an instance of {@link TeamCompetitionRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link TeamCompetitionRequestSender}
     */
    public static TeamCompetitionRequestSender create(final String foldingUrl) {
        return new TeamCompetitionRequestSender(foldingUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve the overall <code>Team Competition</code> {@link me.zodac.folding.rest.api.tc.CompetitionResult}.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(String)
     */
    public HttpResponse<String> get() throws FoldingRestException {
        return get(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the overall <code>Team Competition</code> {@link me.zodac.folding.rest.api.tc.CompetitionResult}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.CompetitionResult} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param eTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link me.zodac.folding.rest.api.tc.CompetitionResult}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get()
     */
    public HttpResponse<String> get(final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(foldingUrl + "/tc_stats"))
                .header("Content-Type", "application/json");

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header("If-None-Match", eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get TC stats", e);
        }
    }

    /**
     * Sends a <b>GET</b> request to manually trigger an update of the <code>Team Competition</code> stats for all {@link me.zodac.folding.api.tc.User}s and {@link me.zodac.folding.api.tc.Team}s.
     * <p>
     * Request will be sent and only return when the update is complete. If an asynchronous update is required, look at {@link #manualUpdate(boolean)}.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualUpdate() throws FoldingRestException {
        return manualUpdate(false);
    }

    /**
     * Sends a <b>GET</b> request to manually trigger an update of the <code>Team Competition</code> stats for all {@link me.zodac.folding.api.tc.User}s and {@link me.zodac.folding.api.tc.Team}s.
     *
     * @param async should the update be performed asynchronously, or wait for the result
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualUpdate(final boolean async) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(foldingUrl + "/tc_stats/manual?async=" + async))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to manually trigger update of TC stats", e);
        }
    }

    /**
     * Sends a <b>GET</b> request to manually reset the <code>Team Competition</code> stats for all {@link me.zodac.folding.api.tc.User}s and {@link me.zodac.folding.api.tc.Team}s.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualReset() throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(foldingUrl + "/tc_stats/reset"))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to manually trigger monthly reset of TC stats", e);
        }
    }
}
