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
import me.zodac.folding.rest.util.RestUtilConstants;

/**
 * Convenience class to send HTTP requests to the {@link me.zodac.folding.api.tc.User} REST endpoint.
 */
public record UserRequestSender(String usersUrl) {

    /**
     * Create an instance of {@link UserRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link UserRequestSender}
     */
    public static UserRequestSender createWithUrl(final String foldingUrl) {
        final String usersUrl = foldingUrl + "/users";
        return new UserRequestSender(usersUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.User}s in the system.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll(String)
     */
    public HttpResponse<String> getAll() throws FoldingRestException {
        return getAll(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.User}s in the system.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.User} based on the <code>ETag</code>, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve cached {@link me.zodac.folding.api.tc.User}s
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll()
     */
    public HttpResponse<String> getAll(final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(usersUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

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
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.User} based on the <code>ETag</code>, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId    the ID of the {@link me.zodac.folding.api.tc.User} to be retrieved
     * @param entityTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link me.zodac.folding.api.tc.User}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int)
     */
    public HttpResponse<String> get(final int userId, final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(usersUrl + '/' + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

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
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.User} based on the <code>ETag</code>, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId    the ID of the {@link me.zodac.folding.api.tc.User} to be retrieved
     * @param entityTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link me.zodac.folding.api.tc.User}
     * @param userName  the username
     * @param password  the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getWithPasskey(int, String, String)
     */
    public HttpResponse<String> getWithPasskey(final int userId, final String entityTag, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(usersUrl + '/' + userId + "/passkey"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

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
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final UserRequest user, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(user)))
            .uri(URI.create(usersUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNeitherBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

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
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} to update
     * @param user   the {@link UserRequest} to update
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final int userId, final UserRequest user) throws FoldingRestException {
        return update(userId, user, null, null);
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
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(user)))
            .uri(URI.create(usersUrl + '/' + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNeitherBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

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
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} to remove
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int userId) throws FoldingRestException {
        return delete(userId, null, null);
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
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(usersUrl + '/' + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNeitherBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

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
