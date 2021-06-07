package me.zodac.folding.stats;

import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.utils.DateTimeUtils;
import me.zodac.folding.stats.http.request.PointsUrlBuilder;
import me.zodac.folding.stats.http.request.StatsRequestUrl;
import me.zodac.folding.stats.http.request.UnitsUrlBuilder;
import me.zodac.folding.stats.http.response.StatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.sql.Timestamp;

import static me.zodac.folding.stats.http.request.StatsRequestSender.sendFoldingRequest;
import static me.zodac.folding.stats.http.response.StatsResponseParser.getPointsFromResponse;
import static me.zodac.folding.stats.http.response.StatsResponseParser.getUnitsFromResponse;

/**
 * Concrete implementation of {@link FoldingStatsRetriever} that retrieves {@link UserStats} through HTTP calls to the Folding@Home REST API.
 *
 * @see <a href="https://api2.foldingathome.org/">Folding@Home REST API</a>
 */
public final class HttpFoldingStatsRetriever implements FoldingStatsRetriever {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpFoldingStatsRetriever.class);

    private HttpFoldingStatsRetriever() {

    }

    public static HttpFoldingStatsRetriever create() {
        return new HttpFoldingStatsRetriever();
    }

    @Override
    public Stats getStats(final FoldingStatsDetails foldingStatsDetails) throws FoldingExternalServiceException {
        LOGGER.debug(""); // Line break to differentiate different users
        LOGGER.debug("Getting stats for username/passkey '{}/{}'", foldingStatsDetails.getFoldingUserName(), foldingStatsDetails.getPasskey());
        final long userPoints = getPoints(foldingStatsDetails);
        final int userUnits = getUnits(foldingStatsDetails);
        return Stats.create(userPoints, userUnits);
    }


    @Override
    public UserStats getTotalStats(final User user) throws FoldingExternalServiceException {
        final Timestamp currentUtcTime = DateTimeUtils.currentUtcTimestamp();
        final Stats userStats = getStats(FoldingStatsDetails.createFromUser(user));
        return UserStats.create(user.getId(), currentUtcTime, userStats.getPoints(), userStats.getUnits());
    }

    @Override
    public long getPoints(final FoldingStatsDetails foldingStatsDetails) throws FoldingExternalServiceException {
        final StatsRequestUrl pointsRequestUrl = new PointsUrlBuilder()
                .forUser(foldingStatsDetails.getFoldingUserName())
                .withPasskey(foldingStatsDetails.getPasskey())
                .build();

        LOGGER.debug("Sending points request to: {}", pointsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(pointsRequestUrl);
        LOGGER.debug("Points response: {}", response.body());

        final StatsResponse statsResponse = StatsResponse.create(response);
        return getPointsFromResponse(statsResponse);
    }

    @Override
    public int getUnits(final FoldingStatsDetails foldingStatsDetails) throws FoldingExternalServiceException {
        final StatsRequestUrl unitsRequestUrl = new UnitsUrlBuilder()
                .forUser(foldingStatsDetails.getFoldingUserName())
                .withPasskey(foldingStatsDetails.getPasskey())
                .build();

        LOGGER.debug("Sending units request to: {}", unitsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(unitsRequestUrl);
        LOGGER.debug("Units response: {}", response.body());

        final StatsResponse statsResponse = StatsResponse.create(response);
        return getUnitsFromResponse(foldingStatsDetails, statsResponse);
    }
}
