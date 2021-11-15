/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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
import java.util.Collection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.rest.util.RestUtilConstants;

/**
 * Convenience class to send HTTP requests to the {@link me.zodac.folding.api.tc.Team} REST endpoint.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamRequestSender {

    private final String teamsUrl;

    /**
     * Create an instance of {@link TeamRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link TeamRequestSender}
     */
    public static TeamRequestSender createWithUrl(final String foldingUrl) {
        final String teamsUrl = foldingUrl + "/teams";
        return new TeamRequestSender(teamsUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.Team}s in the system.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll(String)
     */
    public HttpResponse<String> getAll() throws FoldingRestException {
        return getAll(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.Team}s in the system.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.Team} based on the <code>ETag</code>, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve cached {@link me.zodac.folding.api.tc.Team}s
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll()
     */
    public HttpResponse<String> getAll(final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(teamsUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get all teams", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get all teams", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.Team} with the given {@code teamId}.
     *
     * @param teamId the ID of the {@link me.zodac.folding.api.tc.Team} to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int, String)
     */
    public HttpResponse<String> get(final int teamId) throws FoldingRestException {
        return get(teamId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.Team} with the given {@code teamId}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.Team} based on the <code>ETag</code>, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param teamId    the ID of the {@link me.zodac.folding.api.tc.Team} to be retrieved
     * @param entityTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link me.zodac.folding.api.tc.Team}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int)
     */
    public HttpResponse<String> get(final int teamId, final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(teamsUrl + '/' + teamId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get team", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get team", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.Team} with the given {@code teamName}.
     *
     * @param teamName the {@code teamName} of the {@link me.zodac.folding.api.tc.Team} to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(String, String)
     */
    public HttpResponse<String> get(final String teamName) throws FoldingRestException {
        return get(teamName, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.Team} with the given {@code teamName}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.Team} based on the <code>ETag</code>, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param teamName  the {@code teamName} of the {@link me.zodac.folding.api.tc.Team} to be retrieved
     * @param entityTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link me.zodac.folding.api.tc.Team}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(String)
     */
    public HttpResponse<String> get(final String teamName, final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(teamsUrl + "/fields?teamName=" + teamName))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get team", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get team", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link TeamRequest} in the system.
     *
     * @param team the {@link TeamRequest} to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final TeamRequest team) throws FoldingRestException {
        return create(team, null, null);
    }

    /**
     * Send a <b>POST</b> request to create the given {@link TeamRequest} in the system.
     *
     * @param team     the {@link TeamRequest} to create
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final TeamRequest team, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(team)))
            .uri(URI.create(teamsUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNeitherBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to create team", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to create team", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link me.zodac.folding.api.tc.Team}s in the system.
     *
     * @param batchOfTeams the {@link Collection} of {@link me.zodac.folding.api.tc.Team}s to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> createBatchOf(final Collection<TeamRequest> batchOfTeams) throws FoldingRestException {
        return createBatchOf(batchOfTeams, null, null);
    }

    /**
     * Send a <b>POST</b> request to create the given {@link me.zodac.folding.api.tc.Team}s in the system.
     *
     * @param batchOfTeams the {@link Collection} of {@link me.zodac.folding.api.tc.Team}s to create
     * @param userName     the username
     * @param password     the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> createBatchOf(final Collection<TeamRequest> batchOfTeams, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(batchOfTeams)))
            .uri(URI.create(teamsUrl + "/batch"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNeitherBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to create batch of teams", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to create batch of teams", e);
        }
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link TeamRequest} in the system.
     *
     * @param teamId the ID of the {@link me.zodac.folding.api.tc.Team} to update
     * @param team   the {@link TeamRequest} to update
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final int teamId, final TeamRequest team) throws FoldingRestException {
        return update(teamId, team, null, null);
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link TeamRequest} in the system.
     *
     * @param teamId   the ID of the {@link me.zodac.folding.api.tc.Team} to update
     * @param team     the {@link TeamRequest} to update
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final int teamId, final TeamRequest team, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(team)))
            .uri(URI.create(teamsUrl + '/' + teamId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNeitherBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to update team", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to update team", e);
        }
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link me.zodac.folding.api.tc.Team} with the given {@code teamId}.
     *
     * @param teamId the ID of the {@link me.zodac.folding.api.tc.Team} to remove
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int teamId) throws FoldingRestException {
        return delete(teamId, null, null);
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link me.zodac.folding.api.tc.Team} with the given {@code teamId}.
     *
     * @param teamId   the ID of the {@link me.zodac.folding.api.tc.Team} to remove
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int teamId, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(teamsUrl + '/' + teamId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNeitherBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to delete team", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to delete team", e);
        }
    }
}
