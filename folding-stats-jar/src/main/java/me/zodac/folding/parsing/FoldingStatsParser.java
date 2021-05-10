package me.zodac.folding.parsing;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.utils.DateTimeUtils;
import me.zodac.folding.parsing.http.request.PointsUrlBuilder;
import me.zodac.folding.parsing.http.request.UnitsUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.sql.Timestamp;

import static me.zodac.folding.parsing.http.request.RequestSender.sendFoldingRequest;
import static me.zodac.folding.parsing.http.response.ResponseParser.getPointsFromResponse;
import static me.zodac.folding.parsing.http.response.ResponseParser.getUnitsFromResponse;

public class FoldingStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoldingStatsParser.class);

    public static UserStats getTotalStatsForUser(final User user) throws FoldingException, FoldingExternalServiceException {
        LOGGER.debug("Getting stats for username/passkey '{}/{}'", user.getFoldingUserName(), user.getPasskey());
        final Timestamp currentUtcTime = DateTimeUtils.getCurrentUtcTimestamp();
        final long userPoints = getPointsForUser(user.getFoldingUserName(), user.getPasskey());
        final int userUnits = getUnitsForUser(user.getFoldingUserName(), user.getPasskey());
        return UserStats.create(user.getId(), currentUtcTime, Stats.create(userPoints, userUnits));
    }

    public static long getPointsForUser(final String userName, final String passkey) throws FoldingException, FoldingExternalServiceException {
        final String pointsRequestUrl = new PointsUrlBuilder()
                .forUser(userName)
                .withPasskey(passkey)
                .build();

        LOGGER.debug("Sending points request to: {}", pointsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(pointsRequestUrl);
        LOGGER.debug("Points response: {}", response.body());

        return getPointsFromResponse(response);
    }

    public static int getUnitsForUser(final String userName, final String passkey) throws FoldingException, FoldingExternalServiceException {
        final String unitsRequestUrl = new UnitsUrlBuilder()
                .forUser(userName)
                .withPasskey(passkey)
                .build();

        LOGGER.debug("Sending units request to: {}", unitsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(unitsRequestUrl);
        LOGGER.debug("Units response: {}", response.body());

        return getUnitsFromResponse(userName, passkey, response);
    }
}
