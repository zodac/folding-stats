/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Month;
import java.time.Year;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.util.RestUtilConstants;
import org.checkerframework.nullaway.checker.nullness.qual.Nullable;

/**
 * Convenience class to send HTTP requests to the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} REST endpoint.
 *
 * @param historicStatsUrl the URL to the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} REST endpoint
 */
public record HistoricStatsRequestSender(String historicStatsUrl) {

    /**
     * Create an instance of {@link HistoricStatsRequestSender}.
     *
     * @param foldingUrl the root URL of the {@code /folding} endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link HistoricStatsRequestSender}
     */
    public static HistoricStatsRequestSender createWithUrl(final String foldingUrl) {
        final String historicStatsUrl = foldingUrl + "/historic";
        return new HistoricStatsRequestSender(historicStatsUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve hourly {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.User} for the given {@code year}/{@code month}/{@code day}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *               retrieved
     * @param year   the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param month  the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param day    the day of the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getHourlyUserStats(int, Year, Month, int, String)
     */
    public HttpResponse<String> getHourlyUserStats(final int userId, final Year year, final Month month, final int day) throws FoldingRestException {
        return getHourlyStats(HistoricStatsType.USER, userId, year, month, day, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve hourly {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.User} for the given {@code year}/{@code month}/{@code day}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId    the ID of the {@link me.zodac.folding.api.tc.User} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *                  retrieved
     * @param year      the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param month     the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param day       the day of the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getHourlyUserStats(int, Year, Month, int)
     */
    public HttpResponse<String> getHourlyUserStats(final int userId, final Year year, final Month month, final int day, final String entityTag)
        throws FoldingRestException {
        return getHourlyStats(HistoricStatsType.USER, userId, year, month, day, entityTag);
    }

    /**
     * Send a <b>GET</b> request to retrieve hourly {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.Team}  for the given {@code year}/{@code month}/{@code day}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * @param teamId the ID of the {@link me.zodac.folding.api.tc.Team} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *               retrieved
     * @param year   the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param month  the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param day    the day of the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getHourlyTeamStats(int, Year, Month, int, String)
     */
    public HttpResponse<String> getHourlyTeamStats(final int teamId, final Year year, final Month month, final int day) throws FoldingRestException {
        return getHourlyStats(HistoricStatsType.TEAM, teamId, year, month, day, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve hourly {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.Team} for the given {@code year}/{@code month}/{@code day}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param teamId    the ID of the {@link me.zodac.folding.api.tc.Team} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *                  retrieved
     * @param year      the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param month     the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param day       the day of the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getHourlyTeamStats(int, Year, Month, int)
     */
    public HttpResponse<String> getHourlyTeamStats(final int teamId, final Year year, final Month month, final int day, final String entityTag)
        throws FoldingRestException {
        return getHourlyStats(HistoricStatsType.TEAM, teamId, year, month, day, entityTag);
    }

    private HttpResponse<String> getHourlyStats(final HistoricStatsType historicStatsType, final int id, final Year year, final Month month,
                                                final int day, final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(
                historicStatsUrl + '/' + historicStatsType.endpointUrl + '/' + id + '/' + year.getValue() + '/' + month.getValue() + '/' + day))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get hourly stats for " + historicStatsType.endpointUrl, e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get hourly stats for " + historicStatsType.endpointUrl, e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve daily {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.User} for the given {@code year}/{@code month}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *               retrieved
     * @param year   the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param month  the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getDailyUserStats(int, Year, Month, String)
     */
    public HttpResponse<String> getDailyUserStats(final int userId, final Year year, final Month month) throws FoldingRestException {
        return getDailyStats(HistoricStatsType.USER, userId, year, month, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve daily {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.User} for the given {@code year}/{@code month}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId    the ID of the {@link me.zodac.folding.api.tc.User} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *                  retrieved
     * @param year      the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param month     the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getDailyUserStats(int, Year, Month)
     */
    public HttpResponse<String> getDailyUserStats(final int userId, final Year year, final Month month, final String entityTag)
        throws FoldingRestException {
        return getDailyStats(HistoricStatsType.USER, userId, year, month, entityTag);
    }

    /**
     * Send a <b>GET</b> request to retrieve daily {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.Team} for the given {@code year}/{@code month}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param teamId the ID of the {@link me.zodac.folding.api.tc.Team} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *               retrieved
     * @param year   the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param month  the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getDailyTeamStats(int, Year, Month, String)
     */
    public HttpResponse<String> getDailyTeamStats(final int teamId, final Year year, final Month month) throws FoldingRestException {
        return getDailyStats(HistoricStatsType.TEAM, teamId, year, month, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve daily {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.Team} for the given {@code year}/{@code month}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param teamId    the ID of the {@link me.zodac.folding.api.tc.Team} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *                  retrieved
     * @param year      the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param month     the {@link Month} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getDailyTeamStats(int, Year, Month)
     */
    public HttpResponse<String> getDailyTeamStats(final int teamId, final Year year, final Month month, final String entityTag)
        throws FoldingRestException {
        return getDailyStats(HistoricStatsType.TEAM, teamId, year, month, entityTag);
    }

    private HttpResponse<String> getDailyStats(final HistoricStatsType historicStatsType, final int id, final Year year, final Month month,
                                               final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(historicStatsUrl + '/' + historicStatsType.endpointUrl + '/' + id + '/' + year.getValue() + '/' + month.getValue()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get daily stats for " + historicStatsType.endpointUrl, e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get daily stats for " + historicStatsType.endpointUrl, e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve monthly {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.User} for the given {@code year}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *               retrieved
     * @param year   the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyUserStats(int, Year, String)
     */
    public HttpResponse<String> getMonthlyUserStats(final int userId, final Year year) throws FoldingRestException {
        return getMonthlyStats(HistoricStatsType.USER, userId, year, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve monthly {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.User} for the given {@code year}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param userId    the ID of the {@link me.zodac.folding.api.tc.User} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *                  retrieved
     * @param year      the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyUserStats(int, Year)
     */
    public HttpResponse<String> getMonthlyUserStats(final int userId, final Year year, final String entityTag) throws FoldingRestException {
        return getMonthlyStats(HistoricStatsType.USER, userId, year, entityTag);
    }

    /**
     * Send a <b>GET</b> request to retrieve monthly {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.Team} for the given {@code year}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param teamId the ID of the {@link me.zodac.folding.api.tc.Team} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *               retrieved
     * @param year   the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyTeamStats(int, Year, String)
     */
    public HttpResponse<String> getMonthlyTeamStats(final int teamId, final Year year) throws FoldingRestException {
        return getMonthlyStats(HistoricStatsType.TEAM, teamId, year, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve monthly {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} for a
     * {@link me.zodac.folding.api.tc.Team} for the given {@code year}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param teamId    the ID of the {@link me.zodac.folding.api.tc.Team} whose {@link me.zodac.folding.rest.api.tc.historic.HistoricStats} are to be
     *                  retrieved
     * @param year      the {@link Year} of the {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link me.zodac.folding.rest.api.tc.historic.HistoricStats}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyTeamStats(int, Year)
     */
    public HttpResponse<String> getMonthlyTeamStats(final int teamId, final Year year, final String entityTag) throws FoldingRestException {
        return getMonthlyStats(HistoricStatsType.TEAM, teamId, year, entityTag);
    }

    private HttpResponse<String> getMonthlyStats(final HistoricStatsType historicStatsType, final int id, final Year year,
                                                 final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(historicStatsUrl + '/' + historicStatsType.endpointUrl + '/' + id + '/' + year.getValue()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get monthly stats for " + historicStatsType.endpointUrl, e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get monthly stats for " + historicStatsType.endpointUrl, e);
        }
    }

    /**
     * Defines the type of historic stats we want to retrieve.
     */
    private enum HistoricStatsType {

        /**
         * Historic stats for a {@link me.zodac.folding.api.tc.Team}.
         */
        TEAM("teams"),

        /**
         * Historic stats for a {@link me.zodac.folding.api.tc.User}.
         */
        USER("users");

        private final String endpointUrl;

        /**
         * Constructs a {@link HistoricStatsType}.
         *
         * @param endpointUrl the value of the {@link HistoricStatsType}
         */
        HistoricStatsType(final String endpointUrl) {
            this.endpointUrl = endpointUrl;
        }
    }
}
