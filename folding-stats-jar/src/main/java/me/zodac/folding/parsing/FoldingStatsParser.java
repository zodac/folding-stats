package me.zodac.folding.parsing;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.utils.TimeUtils;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.StatsCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.parsing.http.request.PointsUrlBuilder;
import me.zodac.folding.parsing.http.request.UnitsUrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;
import static me.zodac.folding.parsing.http.request.RequestSender.sendFoldingRequest;
import static me.zodac.folding.parsing.http.response.ResponseParser.getPointsFromResponse;
import static me.zodac.folding.parsing.http.response.ResponseParser.getUnitsFromResponse;

// TODO: [zodac] Should not go straight to TC stats caches or PostgresDB, use StorageFacade to call parsing logic, then do persistence from caller
public class FoldingStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoldingStatsParser.class);

    // TODO: [zodac] This shouldn't be here anymore. Keep this class as simple logic, move this function elsewhere since it has TC-specific logic
    public static void parseTcStatsForAllUsers(final List<User> users) {
        final Timestamp currentUtcTime = TimeUtils.getCurrentUtcTimestamp();
        final List<UserStats> stats = new ArrayList<>(users.size());

        for (final User user : users) {
            if (StringUtils.isBlank(user.getPasskey())) {
                LOGGER.warn("Not parsing TC stats for user, missing passkey: {}", user);
                continue;
            }

            try {
                final Hardware hardware = HardwareCache.get().get(user.getHardwareId());
                final Stats totalStatsForUser = getStatsForUser(user.getFoldingUserName(), user.getPasskey(), user.getFoldingTeamNumber(), hardware.getMultiplier());
                stats.add(new UserStats(user.getId(), totalStatsForUser, currentUtcTime));
                LOGGER.info("{}: {} points | {} unmultiplied points | {} units", user.getFoldingUserName(), formatWithCommas(totalStatsForUser.getPoints()), formatWithCommas(totalStatsForUser.getUnmultipliedPoints()), formatWithCommas(totalStatsForUser.getUnits()));

                StatsCache.get().addCurrentStats(user.getId(), totalStatsForUser);
            } catch (final NotFoundException e) {
                LOGGER.warn("Unable to find hardware multiplier for user '{}/{}/{}'", user.getFoldingUserName(), user.getPasskey(), user.getFoldingTeamNumber(), e);
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get stats for user '{}/{}/{}'", user.getFoldingUserName(), user.getPasskey(), user.getFoldingTeamNumber(), e.getCause());
            }
        }

        try {
            DbManagerRetriever.get().persistHourlyUserStats(stats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting stats", e.getCause());
        }
    }


    public static Stats getStatsForUser(final String userName, final String passkey, final int foldingTeamNumber, final double multiplier) throws FoldingException {
        LOGGER.debug("Getting stats for username/passkey '{}/{}' at team {}", userName, passkey, foldingTeamNumber);
        final long userPoints = getPointsForUser(userName, passkey, foldingTeamNumber);
        final int userUnits = getUnitsForUser(userName, passkey);

        return new Stats(userPoints, userUnits, multiplier);
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
