package me.zodac.folding.ejb;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.Optional;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

/**
 * Class that parses {@link Stats} for <code>Team Competition</code> {@link User}s.
 */
@LocalBean
@Stateless
public class UserTeamCompetitionStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTeamCompetitionStatsParser.class);
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    @EJB
    private transient BusinessLogic businessLogic;

    /**
     * Parses the latest TC stats for the given {@link User}.
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
        if (StringUtils.isBlank(user.getPasskey())) {
            LOGGER.warn("Not parsing TC stats for user, missing passkey: {}", user);
            return;
        }

        final Stats initialStats = getInitialStatsForUserOrEmpty(user);
        if (initialStats.isEmpty()) {
            LOGGER.warn("Retrieved empty initial stats for user: {}", user);
            return;
        }

        final OffsetStats offsetStats = getOffsetStatsForUserOrEmpty(user);
        if (offsetStats.isEmpty()) {
            LOGGER.trace("Retrieved empty stat offset for user: {}", user);
        } else {
            LOGGER.debug("{}: {} offset points | {} offset units", user.getDisplayName(), formatWithCommas(offsetStats.getMultipliedPointsOffset()), formatWithCommas(offsetStats.getUnitsOffset()));
        }

        final UserStats totalStats = getTotalStatsForUserOrEmpty(user);
        if (totalStats.isEmpty()) {
            LOGGER.warn("Retrieved empty total stats for user: {}", user);
            return;
        }

        try {
            businessLogic.persistTotalStatsForUser(totalStats);
        } catch (final FoldingException e) {
            LOGGER.warn("Error persisting total user stats", e.getCause());
            return;
        }

        final Optional<Hardware> hardware = businessLogic.getHardwareForUser(user);
        if (hardware.isEmpty()) {
            LOGGER.warn("Unable to find hardware multiplier for user: {}", user);
            return;
        }

        calculateAndPersistTcStats(user, initialStats, offsetStats, totalStats, hardware.get());
    }

    private void calculateAndPersistTcStats(final User user, final Stats initialStats, final OffsetStats offsetStats, final UserStats totalStats, final Hardware hardware) {
        final double hardwareMultiplier = hardware.getMultiplier();
        final long points = Math.max(0, totalStats.getPoints() - initialStats.getPoints());
        final long multipliedPoints = Math.round(points * hardwareMultiplier);
        final int units = Math.max(0, totalStats.getUnits() - initialStats.getUnits());
        final UserTcStats statsBeforeOffset = UserTcStats.create(user.getId(), totalStats.getTimestamp(), points, multipliedPoints, units);

        final UserTcStats hourlyUserTcStats = UserTcStats.updateWithOffsets(statsBeforeOffset, offsetStats);
        LOGGER.debug("{}: {} total points (unmultiplied) | {} total units", user.getDisplayName(), formatWithCommas(totalStats.getPoints()), formatWithCommas(totalStats.getUnits()));
        LOGGER.debug("{}: {} TC points (pre-offset) | {} TC units (pre-offset)", user.getDisplayName(), formatWithCommas(multipliedPoints), formatWithCommas(units));
        LOGGER.info("{}: {} TC points | {} TC units", user.getDisplayName(), formatWithCommas(hourlyUserTcStats.getMultipliedPoints()), formatWithCommas(hourlyUserTcStats.getUnits()));

        try {
            businessLogic.persistHourlyTcStatsForUser(hourlyUserTcStats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting hourly TC stats", e.getCause());
        }
    }

    private static UserStats getTotalStatsForUserOrEmpty(final User user) {
        try {
            return FOLDING_STATS_RETRIEVER.getTotalStats(user);
        } catch (final FoldingExternalServiceException e) {
            LOGGER.warn("Error connecting to Folding@Home API at '{}'", e.getUrl(), e);
        } catch (final FoldingException e) {
            LOGGER.warn("Error sending request to Folding@Home API", e.getCause());
        }

        return UserStats.empty();
    }

    private Stats getInitialStatsForUserOrEmpty(final User user) {
        try {
            return businessLogic.getInitialStatsForUser(user.getId());
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting initial user stats for user: {}", user, e.getCause());
        }

        return Stats.empty();
    }

    private OffsetStats getOffsetStatsForUserOrEmpty(final User user) {
        try {
            return businessLogic.getOffsetStatsForUser(user.getId());
        } catch (final FoldingException e) {
            LOGGER.warn("Error getting user offset stats for user: {}", user, e.getCause());
        }

        return OffsetStats.empty();
    }
}
