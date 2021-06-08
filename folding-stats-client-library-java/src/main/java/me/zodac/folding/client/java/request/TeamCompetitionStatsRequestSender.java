package me.zodac.folding.client.java.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static me.zodac.folding.api.utils.EncodingUtils.encodeBasicAuthentication;

/**
 * Convenience class to send HTTP requests to the <code>Team Competition</code> stats REST endpoint.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamCompetitionStatsRequestSender {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String statsUrl;

    /**
     * Create an instance of {@link TeamCompetitionStatsRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link TeamCompetitionStatsRequestSender}
     */
    public static TeamCompetitionStatsRequestSender create(final String foldingUrl) {
        final String statsUrl = foldingUrl + "/stats";
        return new TeamCompetitionStatsRequestSender(statsUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve the overall <code>Team Competition</code> {@link CompetitionSummary}.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats(String)
     */
    public HttpResponse<String> getStats() throws FoldingRestException {
        return getStats(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the overall <code>Team Competition</code> {@link CompetitionSummary}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link CompetitionSummary} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param eTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link CompetitionSummary}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats()
     */
    public HttpResponse<String> getStats(final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(statsUrl))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get TC stats", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve the <code>Team Competition</code> {@link UserSummary} for a {@link me.zodac.folding.api.tc.User}.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} whose {@link UserSummary} is to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats(String)
     */
    public HttpResponse<String> getStatsForUser(final int userId) throws FoldingRestException {
        return getStatsForUser(userId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the <code>Team Competition</code> {@link UserSummary} for a {@link me.zodac.folding.api.tc.User}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link UserSummary} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} whose {@link UserSummary} is to be retrieved
     * @param eTag   the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link UserSummary}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getStats()
     */
    public HttpResponse<String> getStatsForUser(final int userId, final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(statsUrl + "/users/" + userId))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get TC stats", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve the overall <code>Team Competition</code> {@link me.zodac.folding.api.tc.Team} leaderboard.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getTeamLeaderboard(String)
     */
    public HttpResponse<String> getTeamLeaderboard() throws FoldingRestException {
        return getTeamLeaderboard(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the overall <code>Team Competition</code> {@link me.zodac.folding.api.tc.Team} leaderboard.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link HttpResponse} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param eTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link HttpResponse}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getTeamLeaderboard()
     */
    public HttpResponse<String> getTeamLeaderboard(final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(statsUrl + "/leaderboard"))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get TC team leaderboard", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve the <code>Team Competition</code> category leaderboard.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getCategoryLeaderboard(String)
     */
    public HttpResponse<String> getCategoryLeaderboard() throws FoldingRestException {
        return getCategoryLeaderboard(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve the <code>Team Competition</code> category leaderboard.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link HttpResponse} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param eTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached{@link HttpResponse}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getCategoryLeaderboard()
     */
    public HttpResponse<String> getCategoryLeaderboard(final String eTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(statsUrl + "/category"))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(eTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), eTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get TC category leaderboard", e);
        }
    }

    /**
     * Sends a <b>GET</b> request to manually trigger an update of the <code>Team Competition</code> stats for all {@link me.zodac.folding.api.tc.User}s and {@link me.zodac.folding.api.tc.Team}s.
     * <p>
     * Request will be sent and only return when the update is complete. If an asynchronous update is required, look at {@link #manualUpdate(boolean, String, String)}.
     *
     * @param userName the user name
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualUpdate(final String userName, final String password) throws FoldingRestException {
        return manualUpdate(false, userName, password);
    }

    /**
     * Sends a <b>GET</b> request to manually trigger an update of the <code>Team Competition</code> stats for all {@link me.zodac.folding.api.tc.User}s and {@link me.zodac.folding.api.tc.Team}s.
     *
     * @param async    should the update be performed asynchronously, or wait for the result
     * @param userName the user name
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualUpdate(final boolean async, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(statsUrl + "/manual/update?async=" + async))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

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
        return manualReset(null, null);
    }

    /**
     * Sends a <b>GET</b> request to manually reset the <code>Team Competition</code> stats for all {@link me.zodac.folding.api.tc.User}s and {@link me.zodac.folding.api.tc.Team}s.
     *
     * @param userName the user name
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualReset(final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(statsUrl + "/manual/reset"))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to manually trigger monthly reset of TC stats", e);
        }
    }


    /**
     * Send a <b>PATCH</b> request to retrieve update {@link me.zodac.folding.api.tc.User}s with the given {@code userId} with a points/unit offset.
     * <p>
     * <b>NOTE:</b> If either the {@code pointsOffset} or {@code multipliedPointsOffset} are set to <b>0</b>, then it will be calculated
     * based on the hardware multiplier of the {@link me.zodac.folding.api.tc.User}.
     *
     * @param userId                 the ID of the {@link me.zodac.folding.api.tc.User} to update
     * @param pointsOffset           the additional (unmultiplied) points to add to the {@link me.zodac.folding.api.tc.User}
     * @param multipliedPointsOffset the additional (multiplied) points to add to the {@link me.zodac.folding.api.tc.User}
     * @param unitsOffset            the additional units to add to the {@link me.zodac.folding.api.tc.User}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> offset(final int userId, final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset) throws FoldingRestException {
        return offset(userId, pointsOffset, multipliedPointsOffset, unitsOffset, null, null);
    }

    /**
     * Send a <b>PATCH</b> request to retrieve update {@link me.zodac.folding.api.tc.User}s with the given {@code userId} with a points/unit offset.
     * <p>
     * <b>NOTE:</b> If either the {@code pointsOffset} or {@code multipliedPointsOffset} are set to <b>0</b>, then it will be calculated
     * based on the hardware multiplier of the {@link me.zodac.folding.api.tc.User}.
     *
     * @param userId                 the ID of the {@link me.zodac.folding.api.tc.User} to update
     * @param pointsOffset           the additional (unmultiplied) points to add to the {@link me.zodac.folding.api.tc.User}
     * @param multipliedPointsOffset the additional (multiplied) points to add to the {@link me.zodac.folding.api.tc.User}
     * @param unitsOffset            the additional units to add to the {@link me.zodac.folding.api.tc.User}
     * @param userName               the user name
     * @param password               the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> offset(final int userId, final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset, final String userName, final String password) throws FoldingRestException {
        final OffsetStats offsetStats = OffsetStats.create(pointsOffset, multipliedPointsOffset, unitsOffset);

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .method("PATCH", HttpRequest.BodyPublishers.ofString(GSON.toJson(offsetStats)))
                .uri(URI.create(statsUrl + "/users/" + userId))
                .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to offset user stats", e);
        }
    }
}
