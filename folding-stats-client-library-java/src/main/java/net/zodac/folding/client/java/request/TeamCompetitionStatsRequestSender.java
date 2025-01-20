/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.client.java.request;

import static net.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.api.util.StringUtils;
import net.zodac.folding.rest.api.exception.FoldingRestException;
import net.zodac.folding.rest.api.header.ContentType;
import net.zodac.folding.rest.api.header.RestHeader;
import net.zodac.folding.rest.api.tc.AllTeamsSummary;
import net.zodac.folding.rest.api.tc.UserSummary;
import net.zodac.folding.rest.api.tc.request.OffsetTcStatsRequest;
import net.zodac.folding.rest.api.util.RestUtilConstants;
import org.jspecify.annotations.Nullable;

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
        final String statsUrl = foldingUrl + RestUri.REST_URI_PATH_SEPARATOR + "stats";
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
    public HttpResponse<String> getStats(final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(statsUrl))
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
     * @see #getSummaryStats(String)
     */
    public HttpResponse<String> getSummaryStats() throws FoldingRestException {
        return getSummaryStats(null);
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
     * @see #getSummaryStats()
     */
    public HttpResponse<String> getSummaryStats(final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(statsUrl, "summary"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get summary TC stats", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get summary TC stats", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link UserSummary} for a
     * {@link User}.
     *
     * @param userId the ID of the {@link User} whose {@link UserSummary} is to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats(String)
     */
    public HttpResponse<String> getStatsForUser(final int userId) throws FoldingRestException {
        return getStatsForUser(userId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link UserSummary} for a
     * {@link User}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link UserSummary} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId    the ID of the {@link User} whose {@link UserSummary} is to be retrieved
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link UserSummary}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats()
     */
    public HttpResponse<String> getStatsForUser(final int userId, final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(statsUrl, "users", userId))
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
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link Team} leaderboard.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getTeamLeaderboard(String)
     */
    public HttpResponse<String> getTeamLeaderboard() throws FoldingRestException {
        return getTeamLeaderboard(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the {@code Team Competition} {@link Team} leaderboard.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link HttpResponse} based on the {@code ETag}, an empty {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached {@link HttpResponse}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getTeamLeaderboard()
     */
    public HttpResponse<String> getTeamLeaderboard(final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(statsUrl, "leaderboard"))
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
    public HttpResponse<String> getCategoryLeaderboard(final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(statsUrl, "category"))
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
     * {@link User}s and {@link Team}s.
     *
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualUpdate(final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(RestUri.create(statsUrl, "manual", "update"))
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
     * Sends a <b>POST</b> request to manually reset the {@code Team Competition} stats for all {@link User}s and
     * {@link Team}s.
     *
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualReset(final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(RestUri.create(statsUrl, "manual", "reset"))
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
     * Send a <b>PATCH</b> request to retrieve update {@link User}s with the given {@code userId} with a points/unit offset.
     *
     * <p>
     * <b>NOTE:</b> If either the {@code pointsOffset} or {@code multipliedPointsOffset} are set to <b>0</b>, then it will be calculated
     * based on the hardware multiplier of the {@link User}.
     *
     * @param userId                 the ID of the {@link User} to update
     * @param pointsOffset           the additional (unmultiplied) points to add to the {@link User}
     * @param multipliedPointsOffset the additional (multiplied) points to add to the {@link User}
     * @param unitsOffset            the additional units to add to the {@link User}
     * @param userName               the username
     * @param password               the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> offset(final int userId, final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset,
                                     final String userName, final String password) throws FoldingRestException {
        final OffsetTcStatsRequest offsetTcStatsRequest = new OffsetTcStatsRequest(pointsOffset, multipliedPointsOffset, unitsOffset);

        final HttpRequest request = HttpRequest.newBuilder()
            .method("PATCH", HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(offsetTcStatsRequest)))
            .uri(RestUri.create(statsUrl, "users", userId))
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
