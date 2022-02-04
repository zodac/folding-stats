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

package me.zodac.folding.stats;

import static me.zodac.folding.stats.http.request.StatsRequestSender.sendFoldingRequest;
import static me.zodac.folding.stats.http.response.StatsResponseParser.getPointsFromResponse;
import static me.zodac.folding.stats.http.response.StatsResponseParser.getUnitsFromResponse;

import java.net.http.HttpResponse;
import java.sql.Timestamp;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.util.DateTimeUtils;
import me.zodac.folding.stats.http.request.PointsUrlBuilder;
import me.zodac.folding.stats.http.request.StatsRequestUrl;
import me.zodac.folding.stats.http.request.UnitsUrlBuilder;
import me.zodac.folding.stats.http.response.StatsResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete implementation of {@link FoldingStatsRetriever} that retrieves {@link UserStats} through HTTP calls to the Folding@Home REST API.
 *
 * @see <a href="https://api2.foldingathome.org/">Folding@Home REST API</a>
 */
public final class HttpFoldingStatsRetriever implements FoldingStatsRetriever {

    private static final Logger LOGGER = LogManager.getLogger();

    private HttpFoldingStatsRetriever() {

    }

    /**
     * Creates an instance of {@link HttpFoldingStatsRetriever}.
     *
     * @return the created {@link HttpFoldingStatsRetriever}
     */
    public static HttpFoldingStatsRetriever create() {
        return new HttpFoldingStatsRetriever();
    }

    @Override
    public Stats getStats(final FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException {
        LOGGER.debug(""); // Line-break to differentiate different users
        LOGGER.debug("Getting stats for username/passkey '{}/{}'", foldingStatsDetails::foldingUserName, foldingStatsDetails::passkey);
        final long userPoints = getPoints(foldingStatsDetails);
        final int userUnits = getUnits(foldingStatsDetails);
        return Stats.create(userPoints, userUnits);
    }

    @Override
    public UserStats getTotalStats(final User user) throws ExternalConnectionException {
        final Timestamp currentUtcTime = DateTimeUtils.currentUtcTimestamp();
        final Stats userStats = getStats(FoldingStatsDetails.createFromUser(user));
        return UserStats.create(user.getId(), currentUtcTime, userStats.getPoints(), userStats.getUnits());
    }

    private static long getPoints(final FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException {
        final StatsRequestUrl pointsRequestUrl = new PointsUrlBuilder()
            .forUser(foldingStatsDetails.foldingUserName())
            .withPasskey(foldingStatsDetails.passkey())
            .build();

        LOGGER.debug("Sending points request to: {}", pointsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(pointsRequestUrl);
        LOGGER.debug("Points response: {}", response::body);

        final StatsResponse statsResponse = StatsResponse.create(response);
        return getPointsFromResponse(statsResponse);
    }

    private static int getUnits(final FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException {
        final StatsRequestUrl unitsRequestUrl = new UnitsUrlBuilder()
            .forUser(foldingStatsDetails.foldingUserName())
            .withPasskey(foldingStatsDetails.passkey())
            .build();

        LOGGER.debug("Sending units request to: {}", unitsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(unitsRequestUrl);
        LOGGER.debug("Units response: {}", response::body);

        final StatsResponse statsResponse = StatsResponse.create(response);
        return getUnitsFromResponse(foldingStatsDetails, statsResponse);
    }
}
