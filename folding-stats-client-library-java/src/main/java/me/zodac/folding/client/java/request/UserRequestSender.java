package me.zodac.folding.client.java.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetStats;
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
 * Convenience class to send HTTP requests to the {@link User} REST endpoint.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserRequestSender {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String foldingUrl;

    /**
     * Create an instance of {@link UserRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link UserRequestSender}
     */
    public static UserRequestSender create(final String foldingUrl) {
        return new UserRequestSender(foldingUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link User}s in the system.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll(String)
     */
    public HttpResponse<String> getAll() throws FoldingRestException {
        return getAll(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link User}s in the system.
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link User} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param eTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve cached {@link User}s
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll()
     */
    public HttpResponse<String> getAll(final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(foldingUrl + "/users"))
                .header("Content-Type", "application/json");

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header("If-None-Match", eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get all users", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link User} with the given {@code userId}.
     *
     * @param userId the ID of the {@link User} to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int, String)
     */
    public HttpResponse<String> get(final int userId) throws FoldingRestException {
        return get(userId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link User} with the given {@code userId}.
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link User} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param userId the ID of the {@link User} to be retrieved
     * @param eTag   the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link User}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int)
     */
    public HttpResponse<String> get(final int userId, final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(foldingUrl + "/users/" + userId))
                .header("Content-Type", "application/json");

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header("If-None-Match", eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get user", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link User} in the system.
     *
     * @param user the {@link User} to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final User user) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(user)))
                .uri(URI.create(foldingUrl + "/users"))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create user", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link User}s in the system.
     *
     * @param batchOfUsers the {@link List} of {@link User}s to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> createBatchOf(final List<User> batchOfUsers) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(batchOfUsers)))
                .uri(URI.create(foldingUrl + "/users/batch"))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create batch of users", e);
        }
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link User} in the system.
     *
     * @param user the {@link User} to update
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final User user) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(user)))
                .uri(URI.create(foldingUrl + "/users/" + user.getId()))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to update user", e);
        }
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link User} with the given {@code userId}.
     *
     * @param userId the ID of the {@link User} to remove
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int userId) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(foldingUrl + "/users/" + userId))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to delete user", e);
        }
    }

    /**
     * Send a <b>PATCH</b> request to retrieve update {@link User}s with the given {@code userId} with a points/unit offset.
     * <p>
     * <b>NOTE:</b> If either the {@code pointsOffset} or {@code multipliedPointsOffset} are set to <b>0</b>, then it will be calculated
     * based on the hardware multiplier of the {@link User}.
     *
     * @param userId                 the ID of the {@link User} to update
     * @param pointsOffset           the additional (unmultiplied) points to add to the {@link User}
     * @param multipliedPointsOffset the additional (multiplied) points to add to the {@link User}
     * @param unitsOffset            the additional units to add to the {@link User}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> offset(final int userId, final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset) throws FoldingRestException {
        final OffsetStats offsetStats = OffsetStats.create(pointsOffset, multipliedPointsOffset, unitsOffset);

        final HttpRequest request = HttpRequest.newBuilder()
                .method("PATCH", HttpRequest.BodyPublishers.ofString(GSON.toJson(offsetStats)))
                .uri(URI.create(foldingUrl + "/users/" + userId))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to offset user stats", e);
        }
    }
}
