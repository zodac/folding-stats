/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.stats;

import static me.zodac.folding.stats.http.request.StatsRequestSender.sendFoldingRequest;
import static me.zodac.folding.stats.http.response.StatsResponseParser.getPointsFromResponse;
import static me.zodac.folding.stats.http.response.StatsResponseParser.getUnitsFromResponse;

import java.net.http.HttpResponse;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.stats.http.request.PointsUrlBuilder;
import me.zodac.folding.stats.http.request.StatsRequestUrl;
import me.zodac.folding.stats.http.request.UnitsUrlBuilder;
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
        LOGGER.debug("Getting stats for username/passkey '{}/{}'", foldingStatsDetails.foldingUserName(), foldingStatsDetails.passkey());
        final long userPoints = getPoints(foldingStatsDetails);
        final int userUnits = getUnits(foldingStatsDetails);
        return Stats.create(userPoints, userUnits);
    }

    @Override
    public UserStats getTotalStats(final User user) throws ExternalConnectionException {
        final Stats userStats = getStats(FoldingStatsDetails.createFromUser(user));
        return UserStats.createNow(user.id(), userStats.points(), userStats.units());
    }

    private static long getPoints(final FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException {
        final StatsRequestUrl pointsRequestUrl = new PointsUrlBuilder()
            .forUser(foldingStatsDetails.foldingUserName())
            .withPasskey(foldingStatsDetails.passkey())
            .build();

        LOGGER.debug("Sending points request to: {}", pointsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(pointsRequestUrl);
        LOGGER.debug("Points response: {}", response.body());
        return getPointsFromResponse(response);
    }

    private static int getUnits(final FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException {
        final StatsRequestUrl unitsRequestUrl = new UnitsUrlBuilder()
            .forUser(foldingStatsDetails.foldingUserName())
            .withPasskey(foldingStatsDetails.passkey())
            .build();

        LOGGER.debug("Sending units request to: {}", unitsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(unitsRequestUrl);
        LOGGER.debug("Units response: {}", response.body());
        return getUnitsFromResponse(foldingStatsDetails, response);
    }
}
