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
import java.time.Month;
import java.time.Year;
import net.zodac.folding.api.tc.result.MonthlyResult;
import net.zodac.folding.api.util.DateTimeUtils;
import net.zodac.folding.api.util.StringUtils;
import net.zodac.folding.rest.api.exception.FoldingRestException;
import net.zodac.folding.rest.api.header.ContentType;
import net.zodac.folding.rest.api.header.RestHeader;
import net.zodac.folding.rest.api.util.RestUtilConstants;
import org.jspecify.annotations.Nullable;

/**
 * Convenience class to send HTTP requests to the {@link MonthlyResult} REST endpoint.
 *
 * @param monthlyResultUrl the URL to the {@link MonthlyResult} REST endpoint
 */
public record MonthlyResultRequestSender(String monthlyResultUrl) {

    private static final DateTimeUtils DATE_TIME_UTILS = DateTimeUtils.create();

    /**
     * Create an instance of {@link MonthlyResultRequestSender}.
     *
     * @param foldingUrl the root URL of the {@code /folding} endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link MonthlyResultRequestSender}
     */
    public static MonthlyResultRequestSender createWithUrl(final String foldingUrl) {
        final String monthlyResultUrl = foldingUrl + RestUri.REST_URI_PATH_SEPARATOR + "results";
        return new MonthlyResultRequestSender(monthlyResultUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve {@link MonthlyResult} for the current year/month.
     *
     * <p>
     * <b>NOTE:</b> The {@link MonthlyResult} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyResult(Year, Month, String)
     */
    public HttpResponse<String> getCurrentMonthlyResult() throws FoldingRestException {
        return getCurrentMonthlyResult(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve {@link MonthlyResult} for the current year/month.
     *
     * <p>
     * <b>NOTE:</b> The {@link MonthlyResult} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link MonthlyResult} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link MonthlyResult}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyResult(Year, Month)
     */
    public HttpResponse<String> getCurrentMonthlyResult(final @Nullable String entityTag) throws FoldingRestException {
        return getMonthlyResult(DATE_TIME_UTILS.currentUtcYear(), DATE_TIME_UTILS.currentUtcMonth(), entityTag);
    }

    /**
     * Send a <b>GET</b> request to retrieve {@link MonthlyResult} for the given {@code year}/{@code month}.
     *
     * <p>
     * <b>NOTE:</b> The {@link MonthlyResult} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * @param year  the {@link Year} of the {@link MonthlyResult}
     * @param month the {@link Month} of the {@link MonthlyResult}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyResult(Year, Month, String)
     */
    public HttpResponse<String> getMonthlyResult(final Year year, final Month month) throws FoldingRestException {
        return getMonthlyResult(year, month, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve {@link MonthlyResult} for the given {@code year}/{@code month}.
     *
     * <p>
     * <b>NOTE:</b> The {@link MonthlyResult} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link MonthlyResult} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param year      the {@link Year} of the {@link MonthlyResult}
     * @param month     the {@link Month} of the {@link MonthlyResult}
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link MonthlyResult}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyResult(Year, Month)
     */
    public HttpResponse<String> getMonthlyResult(final Year year, final Month month, final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(monthlyResultUrl, "result", year.getValue(), month.getValue()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get monthly result", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get monthly result", e);
        }
    }

    /**
     * Sends a <b>POST</b> request to manually save the result of the {@code Team Competition} for the current month.
     *
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualSave(final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(RestUri.create(monthlyResultUrl, "manual", "save"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to manually save the monthly result of TC stats", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to manually save the monthly result of TC stats", e);
        }
    }
}
