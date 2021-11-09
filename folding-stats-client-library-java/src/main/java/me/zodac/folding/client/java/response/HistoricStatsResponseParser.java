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

package me.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.rest.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender}.
 */
public final class HistoricStatsResponseParser {

    private HistoricStatsResponseParser() {

    }

    /**
     * Returns the {@link HistoricStats} retrieved by
     * {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getHourlyUserStats(int, Year, Month, int, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getHourlyUserStats(final HttpResponse<String> response) {
        return convertHistoricStats(response.body());
    }

    /**
     * Returns the {@link HistoricStats} retrieved by
     * {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getDailyUserStats(int, Year, Month, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getDailyUserStats(final HttpResponse<String> response) {
        return convertHistoricStats(response.body());
    }

    /**
     * Returns the {@link HistoricStats} retrieved by
     * {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getMonthlyUserStats(int, Year, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getMonthlyUserStats(final HttpResponse<String> response) {
        return convertHistoricStats(response.body());
    }

    /**
     * Returns the {@link HistoricStats} retrieved by
     * {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getHourlyTeamStats(int, Year, Month, int, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getHourlyTeamStats(final HttpResponse<String> response) {
        return convertHistoricStats(response.body());
    }

    /**
     * Returns the {@link HistoricStats} retrieved by
     * {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getDailyTeamStats(int, Year, Month, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getDailyTeamStats(final HttpResponse<String> response) {
        return convertHistoricStats(response.body());
    }

    /**
     * Returns the {@link HistoricStats} retrieved by
     * {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getMonthlyTeamStats(int, Year, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getMonthlyTeamStats(final HttpResponse<String> response) {
        return convertHistoricStats(response.body());
    }

    private static Collection<HistoricStats> convertHistoricStats(final String responseBody) {
        final Type collectionType = new TypeToken<Collection<HistoricStats>>() {
        }.getType();
        return RestUtilConstants.GSON.fromJson(responseBody, collectionType);
    }
}