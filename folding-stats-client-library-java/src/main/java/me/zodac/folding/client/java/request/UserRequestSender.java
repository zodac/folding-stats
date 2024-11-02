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

package me.zodac.folding.client.java.request;

import static me.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.rest.api.util.RestUtilConstants;
import org.jspecify.annotations.Nullable;

/**
 * Convenience class to send HTTP requests to the {@link me.zodac.folding.api.tc.User} REST endpoint.
 *
 * @param usersUrl the URL to the {@link me.zodac.folding.api.tc.User} REST endpoint
 */
public record UserRequestSender(String usersUrl) {

    /**
     * Create an instance of {@link UserRequestSender}.
     *
     * @param foldingUrl the root URL of the {@code /folding} endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link UserRequestSender}
     */
    public static UserRequestSender createWithUrl(final String foldingUrl) {
        final String usersUrl = foldingUrl + "/users";
        return new UserRequestSender(usersUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.User}s in the system, with passkeys hidden.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> getAllWithoutPasskeys() throws FoldingRestException {
        return getAllWithoutPasskeys(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.User}s in the system, with passkeys hidden.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.User} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve cached {@link me.zodac.folding.api.tc.User}s
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> getAllWithoutPasskeys(final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(usersUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get all users", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get all users", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.User}s in the system, with passkeys shown.
     *
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> getAllWithPasskeys(final String userName, final String password) throws FoldingRestException {
        return getAllWithPasskeys(null, userName, password);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.User}s in the system, with passkeys shown.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.User} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve cached {@link me.zodac.folding.api.tc.User}s
     * @param userName  the username
     * @param password  the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> getAllWithPasskeys(final @Nullable String entityTag, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(usersUrl + "/all/passkey"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get all users", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get all users", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.User} with the given {@code userId}.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int, String)
     */
    public HttpResponse<String> get(final int userId) throws FoldingRestException {
        return get(userId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.User} with the given {@code userId}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.User} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId    the ID of the {@link me.zodac.folding.api.tc.User} to be retrieved
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached {@link me.zodac.folding.api.tc.User}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int)
     */
    public HttpResponse<String> get(final int userId, final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(usersUrl + '/' + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get user with passkey", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get user with passkey", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.User} with passkey with the given {@code userId}.
     *
     * @param userId   the ID of the {@link me.zodac.folding.api.tc.User} to be retrieved
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getWithPasskey(int, String, String, String)
     */
    public HttpResponse<String> getWithPasskey(final int userId, final String userName, final String password) throws FoldingRestException {
        return getWithPasskey(userId, null, userName, password);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.User} with passkey with the given {@code userId}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.User} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId    the ID of the {@link me.zodac.folding.api.tc.User} to be retrieved
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached {@link me.zodac.folding.api.tc.User}
     * @param userName  the username
     * @param password  the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getWithPasskey(int, String, String)
     */
    public HttpResponse<String> getWithPasskey(final int userId, final @Nullable String entityTag, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(usersUrl + '/' + userId + "/passkey"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        if (StringUtils.isNeitherBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get user with passkey", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get user with passkey", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link UserRequest} in the system.
     *
     * @param user     the {@link UserRequest} to create
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final UserRequest user, final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(user)))
            .uri(URI.create(usersUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password)).build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to create user", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to create user", e);
        }
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link UserRequest} in the system.
     *
     * @param userId   the ID of the {@link me.zodac.folding.api.tc.User} to update
     * @param user     the {@link UserRequest} to update
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final int userId, final UserRequest user, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(user)))
            .uri(URI.create(usersUrl + '/' + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to update user", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to update user", e);
        }
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link me.zodac.folding.api.tc.User} with the given {@code userId}.
     *
     * @param userId   the ID of the {@link me.zodac.folding.api.tc.User} to remove
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int userId, final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(usersUrl + '/' + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to delete user", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to delete user", e);
        }
    }
}
