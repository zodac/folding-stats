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
import java.time.Month;
import java.time.Year;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.util.DateTimeUtils;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.util.RestUtilConstants;

/**
 * Convenience class to send HTTP requests to the {@link me.zodac.folding.api.tc.result.MonthlyResult} REST endpoint.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MonthlyResultRequestSender {

    private final String monthlyResultUrl;

    /**
     * Create an instance of {@link MonthlyResultRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link MonthlyResultRequestSender}
     */
    public static MonthlyResultRequestSender createWithUrl(final String foldingUrl) {
        final String monthlyResultUrl = foldingUrl + "/results";
        return new MonthlyResultRequestSender(monthlyResultUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve {@link me.zodac.folding.api.tc.result.MonthlyResult} for the current year/month.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.api.tc.result.MonthlyResult} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyResult(Year, Month, String)
     */
    public HttpResponse<String> getCurrentMonthlyResult() throws FoldingRestException {
        return getCurrentMonthlyResult(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve {@link me.zodac.folding.api.tc.result.MonthlyResult} for the current year/month.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.api.tc.result.MonthlyResult} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached  {@link me.zodac.folding.api.tc.result.MonthlyResult} based on the <code>ETag</code>, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link me.zodac.folding.api.tc.result.MonthlyResult}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyResult(Year, Month, String)
     */
    public HttpResponse<String> getCurrentMonthlyResult(final String entityTag) throws FoldingRestException {
        return getMonthlyResult(DateTimeUtils.currentUtcYear(), DateTimeUtils.currentUtcMonth(), entityTag);
    }

    /**
     * Send a <b>GET</b> request to retrieve {@link me.zodac.folding.api.tc.result.MonthlyResult} for the given {@code year}/{@code month}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.api.tc.result.MonthlyResult} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * @param year  the {@link Year} of the {@link me.zodac.folding.api.tc.result.MonthlyResult}
     * @param month the {@link Month} of the {@link me.zodac.folding.api.tc.result.MonthlyResult}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyResult(Year, Month, String)
     */
    public HttpResponse<String> getMonthlyResult(final Year year, final Month month) throws FoldingRestException {
        return getMonthlyResult(year, month, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve {@link me.zodac.folding.api.tc.result.MonthlyResult} for the given {@code year}/{@code month}.
     *
     * <p>
     * <b>NOTE:</b> The {@link me.zodac.folding.api.tc.result.MonthlyResult} are based on {@link java.time.ZoneOffset#UTC}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached  {@link me.zodac.folding.api.tc.result.MonthlyResult} based on the <code>ETag</code>, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param year      the {@link Year} of the {@link me.zodac.folding.api.tc.result.MonthlyResult}
     * @param month     the {@link Month} of the {@link me.zodac.folding.api.tc.result.MonthlyResult}
     * @param entityTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached
     *                  {@link me.zodac.folding.api.tc.result.MonthlyResult}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getMonthlyResult(Year, Month)
     */
    public HttpResponse<String> getMonthlyResult(final Year year, final Month month, final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(monthlyResultUrl + "/result/" + year.getValue() + '/' + month.getValue()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

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
     * Sends a <b>POST</b> request to manually save the result of the <code>Team Competition</code> for the current month.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualSave() throws FoldingRestException {
        return manualSave(null, null);
    }

    /**
     * Sends a <b>POST</b> request to manually save the result of the <code>Team Competition</code> for the current month.
     *
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> manualSave(final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(monthlyResultUrl + "/manual/save"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNeitherBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

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
