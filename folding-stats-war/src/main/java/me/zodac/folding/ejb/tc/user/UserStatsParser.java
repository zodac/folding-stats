package me.zodac.folding.ejb.tc.user;

import static me.zodac.folding.api.util.NumberUtils.formatWithCommas;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class that parses {@link Stats} for <code>Team Competition</code> {@link User}s.
 */
@LocalBean
@Stateless
public class UserStatsParser {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    @EJB
    private FoldingStatsCore foldingStatsCore;

    /**
     * Parses the latest TC stats for the given {@link User}.
     *
     * <p>
     * Method blocks until the stats have been parsed, calculated and persisted.
     *
     * @param user the {@link User} whose TC stats are to be parsed
     */
    public void parseTcStatsForUserAndWait(final User user) {
        parseTcStatsForUser(user);
    }

    /**
     * Parses the latest TC stats for the given {@link User}.
     *
     * <p>
     * Method runs {@link Asynchronous}, so returns immediately and processes the {@link User} stats in a separate/background thread.
     *
     * @param user the {@link User} whose TC stats are to be parsed
     */
    @Asynchronous
    public void parseTcStatsForUser(final User user) {
        updateTcStatsForUser(user);
    }

    private void updateTcStatsForUser(final User user) {
        LOGGER.debug("Updating stats for '{}': {}", user.getDisplayName(), user);
        if (StringUtils.isBlank(user.getPasskey())) {
            LOGGER.warn("Not parsing TC stats for user, missing passkey: {}", user);
            return;
        }

        final Stats initialStats = foldingStatsCore.getInitialStats(user);
        if (initialStats.isEmpty()) {
            LOGGER.warn("Retrieved empty initial stats for user: {}", user);
            return;
        }

        final OffsetTcStats offsetTcStats = foldingStatsCore.getOffsetStats(user);
        if (offsetTcStats.isEmpty()) {
            LOGGER.trace("Retrieved empty stats offset for user: {}", () -> user);
        } else {
            LOGGER.debug("{}: {} offset points | {} offset units", user::getDisplayName,
                () -> formatWithCommas(offsetTcStats.getMultipliedPointsOffset()), () -> formatWithCommas(offsetTcStats.getUnitsOffset()));
        }

        final UserStats totalStats = getTotalStatsForUserOrEmpty(user);
        if (totalStats.isEmpty()) {
            LOGGER.warn("Retrieved empty total stats for user: {}", user);
            return;
        }

        final UserStats createdTotalStats = foldingStatsCore.createTotalStats(totalStats);
        calculateAndPersistTcStats(user, initialStats, offsetTcStats, createdTotalStats);
    }

    private static UserStats getTotalStatsForUserOrEmpty(final User user) {
        try {
            return FOLDING_STATS_RETRIEVER.getTotalStats(user);
        } catch (final ExternalConnectionException e) {
            LOGGER.warn("Error connecting to Folding@Home API at '{}'", e.getUrl(), e);
        }

        return UserStats.empty();
    }

    private void calculateAndPersistTcStats(final User user,
                                            final Stats initialStats,
                                            final OffsetTcStats offsetTcStats,
                                            final UserStats totalStats) {
        final double hardwareMultiplier = user.getHardware().getMultiplier();
        final long points = Math.max(0, totalStats.getPoints() - initialStats.getPoints());
        final long multipliedPoints = Math.round(points * hardwareMultiplier);
        final int units = Math.max(0, totalStats.getUnits() - initialStats.getUnits());

        final UserTcStats statsBeforeOffset = UserTcStats.create(user.getId(), totalStats.getTimestamp(), points, multipliedPoints, units);
        final UserTcStats hourlyUserTcStats = statsBeforeOffset.updateWithOffsets(offsetTcStats);

        LOGGER.debug("{} (ID: {}): {} total points (unmultiplied) | {} total units", user::getDisplayName, user::getId,
            () -> formatWithCommas(totalStats.getPoints()), () -> formatWithCommas(totalStats.getUnits()));
        LOGGER.debug("{} (ID: {}): {} TC multiplied points (pre-offset) | {} TC units (pre-offset)", user::getDisplayName, user::getId,
            () -> formatWithCommas(multipliedPoints), () -> formatWithCommas(units));

        final UserTcStats createdHourlyTcStats = foldingStatsCore.createHourlyTcStats(hourlyUserTcStats);
        LOGGER.info("{} (ID: {}): {} TC points | {} TC units", user.getDisplayName(), user.getId(),
            formatWithCommas(createdHourlyTcStats.getMultipliedPoints()), formatWithCommas(createdHourlyTcStats.getUnits()));
    }
}
