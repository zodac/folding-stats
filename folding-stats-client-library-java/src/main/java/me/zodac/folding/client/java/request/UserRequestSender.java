package me.zodac.folding.client.java.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;

import static me.zodac.folding.api.utils.EncodingUtils.encodeBasicAuthentication;

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

    private final String usersUrl;

    /**
     * Create an instance of {@link UserRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link UserRequestSender}
     */
    public static UserRequestSender create(final String foldingUrl) {
        final String usersUrl = foldingUrl + "/users";
        return new UserRequestSender(usersUrl);
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
                .uri(URI.create(usersUrl))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), eTag);
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
                .uri(URI.create(usersUrl + '/' + userId))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get user", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link UserRequest} in the system.
     *
     * @param user the {@link UserRequest} to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final UserRequest user) throws FoldingRestException {
        return create(user, null, null);
    }

    /**
     * Send a <b>POST</b> request to create the given {@link UserRequest} in the system.
     *
     * @param user     the {@link UserRequest} to create
     * @param userName the user name
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final UserRequest user, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(user)))
                .uri(URI.create(usersUrl))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create user", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link UserRequest}s in the system.
     *
     * @param batchOfUsers the {@link Collection} of {@link UserRequest}s to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> createBatchOf(final Collection<UserRequest> batchOfUsers) throws FoldingRestException {
        return createBatchOf(batchOfUsers, null, null);
    }

    /**
     * Send a <b>POST</b> request to create the given {@link UserRequest}s in the system.
     *
     * @param batchOfUsers the {@link Collection} of {@link UserRequest}s to create
     * @param userName     the user name
     * @param password     the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> createBatchOf(final Collection<UserRequest> batchOfUsers, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(batchOfUsers)))
                .uri(URI.create(usersUrl + "/batch"))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create batch of users", e);
        }
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link UserRequest} in the system.
     *
     * @param user the {@link UserRequest} to update
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final UserRequest user) throws FoldingRestException {
        return update(user, null, null);
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link UserRequest} in the system.
     *
     * @param user     the {@link UserRequest} to update
     * @param userName the user name
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final UserRequest user, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(user)))
                .uri(URI.create(usersUrl + '/' + user.getId()))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

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
        return delete(userId, null, null);
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link User} with the given {@code userId}.
     *
     * @param userId   the ID of the {@link User} to remove
     * @param userName the user name
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int userId, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(usersUrl + '/' + userId))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to delete user", e);
        }
    }
}
