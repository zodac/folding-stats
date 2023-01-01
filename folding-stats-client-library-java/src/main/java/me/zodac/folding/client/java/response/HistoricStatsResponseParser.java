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

package me.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.rest.api.util.RestUtilConstants;

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
        final Type collectionType = HistoricStatsCollectionType.getInstance().getType();
        return RestUtilConstants.GSON.fromJson(responseBody, collectionType);
    }

    /**
     * Private class defining the {@link Collection} for {@link HistoricStats}.
     */
    private static final class HistoricStatsCollectionType extends TypeToken<Collection<HistoricStats>> {

        private static final HistoricStatsCollectionType INSTANCE = new HistoricStatsCollectionType();

        /**
         * Retrieve a singleton instance of {@link HistoricStatsCollectionType}.
         *
         * @return {@link HistoricStatsCollectionType} instance.
         */
        static HistoricStatsCollectionType getInstance() {
            return INSTANCE;
        }
    }
}
