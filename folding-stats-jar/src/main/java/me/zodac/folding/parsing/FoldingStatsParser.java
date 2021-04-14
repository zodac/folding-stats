package me.zodac.folding.parsing;

import me.zodac.folding.api.FoldingStats;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.cache.tc.TcStatsCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.parsing.http.request.PointsUrlBuilder;
import me.zodac.folding.parsing.http.request.UnitsUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;
import static me.zodac.folding.parsing.http.request.RequestSender.sendFoldingRequest;
import static me.zodac.folding.parsing.http.response.ResponseParser.getPointsFromResponse;
import static me.zodac.folding.parsing.http.response.ResponseParser.getUnitsFromResponse;

// TODO: [zodac] Should not go straight to TC stats caches or PostgresDB, use StorageFacade to call parsing logic, then do persistence from caller
public class FoldingStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoldingStatsParser.class);

    // TODO: [zodac] This shouldn't be here anymore? Keep this class simple logic, move this elsewhere
    public static void parseStatsForAllUsers(final List<FoldingUser> foldingUsers) {
        final Timestamp currentUtcTime = new Timestamp(OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli());
        final List<FoldingStats> stats = new ArrayList<>(foldingUsers.size());

        for (final FoldingUser foldingUser : foldingUsers) {
            try {
                final UserStats totalStatsForUser = getStatsForUser(foldingUser.getFoldingUserName(), foldingUser.getPasskey(), foldingUser.getFoldingTeamNumber());
                final FoldingStats foldingStats = new FoldingStats(foldingUser.getId(), totalStatsForUser, currentUtcTime);
                stats.add(foldingStats);
                LOGGER.info("{}: {} points, {} units", foldingUser.getFoldingUserName(), formatWithCommas(totalStatsForUser.getPoints()), formatWithCommas(totalStatsForUser.getUnits()));

                TcStatsCache.get().addCurrentStats(foldingUser.getId(), totalStatsForUser);
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get stats for user '{}/{}/{}'", foldingUser.getFoldingUserName(), foldingUser.getPasskey(), foldingUser.getFoldingTeamNumber(), e.getCause());
            }
        }

        try {
            DbManagerRetriever.get().persistTcStats(stats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting stats", e.getCause());
        }
    }


    public static UserStats getStatsForUser(final String userName, final String passkey, final int foldingTeamNumber) throws FoldingException {
        LOGGER.debug("Getting stats for username/passkey '{}/{}' at team {}", userName, passkey, foldingTeamNumber);
        final long userPoints = getPointsForUser(userName, passkey, foldingTeamNumber);
        final int userUnits = getUnitsForUser(userName, passkey);

        return new UserStats(userPoints, userUnits);
    }

    public static long getPointsForUser(final String userName, final String passkey, final int foldingTeamNumber) throws FoldingException {
        final String pointsRequestUrl = new PointsUrlBuilder()
                .forUser(userName)
                .withPasskey(passkey)
                .atTeam(foldingTeamNumber)
                .build();

        LOGGER.debug("Sending points request to: {}", pointsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(pointsRequestUrl);
        LOGGER.debug("Points response: {}", response.body());

        return getPointsFromResponse(response);
    }

    public static int getUnitsForUser(final String userName, final String passkey) throws FoldingException {
        final String unitsRequestUrl = new UnitsUrlBuilder()
                .forUser(userName)
                .withPasskey(passkey)
                .build();

        LOGGER.debug("Sending units request to: {}", unitsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(unitsRequestUrl);
        LOGGER.debug("Units response: {}", response.body());

        return getUnitsFromResponse(response);
    }
}
