package me.zodac.folding.client.java.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.tc.Team;
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
 * Convenience class to send HTTP requests to the {@link Team} REST endpoint.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamRequestSender {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String foldingUrl;

    /**
     * Create an instance of {@link TeamRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link TeamRequestSender}
     */
    public static TeamRequestSender create(final String foldingUrl) {
        return new TeamRequestSender(foldingUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link Team}s in the system.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll(String)
     */
    public HttpResponse<String> getAll() throws FoldingRestException {
        return getAll(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link Team}s in the system.
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link Team} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param eTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve cached {@link Team}s
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll()
     */
    public HttpResponse<String> getAll(final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(foldingUrl + "/teams"))
                .header("Content-Type", "application/json");

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header("If-None-Match", eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get all teams", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link Team} with the given {@code teamId}.
     *
     * @param teamId the ID of the {@link Team} to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int, String)
     */
    public HttpResponse<String> get(final int teamId) throws FoldingRestException {
        return get(teamId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link Team} with the given {@code teamId}.
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link Team} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param teamId the ID of the {@link Team} to be retrieved
     * @param eTag   the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link Team}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int)
     */
    public HttpResponse<String> get(final int teamId, final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(foldingUrl + "/teams/" + teamId))
                .header("Content-Type", "application/json");

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header("If-None-Match", eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get team", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link Team} in the system.
     *
     * @param team the {@link Team} to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final Team team) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(team)))
                .uri(URI.create(foldingUrl + "/teams"))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create team", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link Team}s in the system.
     *
     * @param batchOfTeams the {@link List} of {@link Team}s to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> createBatchOf(final List<Team> batchOfTeams) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(batchOfTeams)))
                .uri(URI.create(foldingUrl + "/teams/batch"))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create batch of teams", e);
        }
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link Team} in the system.
     *
     * @param team the {@link Team} to update
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final Team team) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(team)))
                .uri(URI.create(foldingUrl + "/teams/" + team.getId()))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to update team", e);
        }
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link Team} with the given {@code teamId}.
     *
     * @param teamId the ID of the {@link Team} to remove
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int teamId) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(foldingUrl + "/teams/" + teamId))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to delete team", e);
        }
    }

    /**
     * Send a <b>PATCH</b> request to retire a {@link me.zodac.folding.api.tc.User} from a {@link Team}.
     *
     * @param teamId the ID of the {@link Team}
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} to retire
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> retireUser(final int teamId, final int userId) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(foldingUrl + "/teams/" + teamId + "/retire/" + userId))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to retire user", e);
        }
    }

    /**
     * Send a <b>PATCH</b> request to un-retire a {@link me.zodac.folding.api.tc.User} to a {@link Team}.
     *
     * @param teamId        the ID of the {@link Team} to un-retire the {@link me.zodac.folding.api.tc.User} to
     * @param retiredUserId the ID of the retired {@link me.zodac.folding.api.tc.User} to un-retire
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> unretireUser(final int teamId, final int retiredUserId) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(foldingUrl + "/teams/" + teamId + "/unretire/" + retiredUserId))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to unretire user", e);
        }
    }
}
