/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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
import me.zodac.folding.rest.api.tc.AllTeamsSummary;
import me.zodac.folding.rest.api.tc.request.OffsetTcStatsRequest;
import me.zodac.folding.rest.api.util.RestUtilConstants;

/**
 * Convenience class to send HTTP requests to the {@code Team Competition} stats REST endpoint.
 *
 * @param statsUrl the URL to the {@code Team Competition} stats REST endpoint
 */
public record TeamCompetitionStatsRequestSender(String statsUrl) {

    /**
     * Create an instance of {@link TeamCompetitionStatsRequestSender}.
     *
     * @param foldingUrl the root URL of the {@code /folding} endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link TeamCompetitionStatsRequestSender}
     */
    public static TeamCompetitionStatsRequestSender createWithUrl(final String foldingUrl) {
        final String statsUrl = foldingUrl + "/stats";
        return new TeamCompetitionStatsRequestSender(statsUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link AllTeamsSummary}.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats(String)
     */
    public HttpResponse<String> getStats() throws FoldingRestException {
        return getStats(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link AllTeamsSummary}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link AllTeamsSummary} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link AllTeamsSummary}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats()
     */
    public HttpResponse<String> getStats(final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(statsUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get TC stats", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get TC stats", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link AllTeamsSummary}.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getOverallStats(String)
     */
    public HttpResponse<String> getOverallStats() throws FoldingRestException {
        return getOverallStats(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link AllTeamsSummary}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link AllTeamsSummary} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link AllTeamsSummary}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getOverallStats()
     */
    public HttpResponse<String> getOverallStats(final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(statsUrl + "/overall"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get overall TC stats", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get overall TC stats", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link me.zodac.folding.rest.api.tc.UserSummary} for a
     * {@link me.zodac.folding.api.tc.User}.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} whose {@link me.zodac.folding.rest.api.tc.UserSummary} is to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats(String)
     */
    public HttpResponse<String> getStatsForUser(final int userId) throws FoldingRestException {
        return getStatsForUser(userId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link me.zodac.folding.rest.api.tc.UserSummary} for a
     * {@link me.zodac.folding.api.tc.User}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.UserSummary} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId    the ID of the {@link me.zodac.folding.api.tc.User} whose {@link me.zodac.folding.rest.api.tc.UserSummary} is to be retrieved
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link me.zodac.folding.rest.api.tc.UserSummary}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats()
     */
    public HttpResponse<String> getStatsForUser(final int userId, final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(statsUrl + "/users/" + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get TC stats", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get TC stats", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve the overall {@code Team Competition} {@link me.zodac.folding.api.tc.Team} leaderboard.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getTeamLeaderboard(String)
     */
    public HttpResponse<String> getTeamLeaderboard() throws FoldingRestException {
        return getTeamLeaderboard(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the overall {@code Team Competition} {@link me.zodac.folding.api.tc.Team} leaderboard.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link HttpResponse} based on the {@code ETag}, an empty {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached {@link HttpResponse}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getTeamLeaderboard()
     */
    public HttpResponse<String> getTeamLeaderboard(final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(statsUrl + "/leaderboard"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get TC team leaderboard", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get TC team leaderboard", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} category leaderboard.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getCategoryLeaderboard(String)
     */
    public HttpResponse<String> getCategoryLeaderboard() throws FoldingRestException {
        return getCategoryLeaderboard(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} category leaderboard.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link HttpResponse} based on the {@code ETag}, an empty {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached{@link HttpResponse}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getCategoryLeaderboard()
     */
    public HttpResponse<String> getCategoryLeaderboard(final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(statsUrl + "/category"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get TC category leaderboard", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get TC category leaderboard", e);
        }
    }

    /**
     * Sends a <b>POST</b> request to manually trigger an update of the {@code Team Competition} stats for all
     * {@link me.zodac.folding.api.tc.User}s and {@link me.zodac.folding.api.tc.Team}s.
     *
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualUpdate(final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(statsUrl + "/manual/update"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to manually trigger update of TC stats", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to manually trigger update of TC stats", e);
        }
    }

    /**
     * Sends a <b>POST</b> request to manually reset the {@code Team Competition} stats for all {@link me.zodac.folding.api.tc.User}s and
     * {@link me.zodac.folding.api.tc.Team}s.
     *
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualReset(final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(statsUrl + "/manual/reset"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to manually trigger monthly reset of TC stats", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to manually trigger monthly reset of TC stats", e);
        }
    }

    /**
     * Send a <b>PATCH</b> request to retrieve update {@link me.zodac.folding.api.tc.User}s with the given {@code userId} with a points/unit offset.
     *
     * <p>
     * <b>NOTE:</b> If either the {@code pointsOffset} or {@code multipliedPointsOffset} are set to <b>0</b>, then it will be calculated
     * based on the hardware multiplier of the {@link me.zodac.folding.api.tc.User}.
     *
     * @param userId                 the ID of the {@link me.zodac.folding.api.tc.User} to update
     * @param pointsOffset           the additional (unmultiplied) points to add to the {@link me.zodac.folding.api.tc.User}
     * @param multipliedPointsOffset the additional (multiplied) points to add to the {@link me.zodac.folding.api.tc.User}
     * @param unitsOffset            the additional units to add to the {@link me.zodac.folding.api.tc.User}
     * @param userName               the username
     * @param password               the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> offset(final int userId, final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset,
                                     final String userName, final String password) throws FoldingRestException {
        final OffsetTcStatsRequest offsetTcStatsRequest = OffsetTcStatsRequest.builder()
            .pointsOffset(pointsOffset)
            .multipliedPointsOffset(multipliedPointsOffset)
            .unitsOffset(unitsOffset)
            .build();

        final HttpRequest request = HttpRequest.newBuilder()
            .method("PATCH", HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(offsetTcStatsRequest)))
            .uri(URI.create(statsUrl + "/users/" + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to offset user stats", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to offset user stats", e);
        }
    }
}
