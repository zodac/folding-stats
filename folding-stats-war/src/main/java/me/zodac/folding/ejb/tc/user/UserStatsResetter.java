package me.zodac.folding.ejb.tc.user;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.ProcessingType;
import me.zodac.folding.cache.RetiredTcStatsCache;
import me.zodac.folding.cache.TcStatsCache;
import me.zodac.folding.cache.TotalStatsCache;
import me.zodac.folding.ejb.api.FoldingStatsCore;
import me.zodac.folding.ejb.tc.scheduled.StatsScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Resets the stats for the <code>Team Competition</code>.
 */
@Singleton
public class UserStatsResetter {

    private static final Logger LOGGER = LogManager.getLogger();

    @EJB
    private FoldingStatsCore foldingStatsCore;

    @EJB
    private StatsScheduler statsScheduler;

    /**
     * Resets the <code>Team Competition</code> stats for all {@link User}s.
     *
     * <p>
     * Actions performed:
     * <ol>
     * <li>Retrieves the current stats for all {@link User}s and updates their initial stats to these current values</li>
     * <li>Remove offset stats for all users</li>
     * <li>Delete all retired user stats</li>
     * <li>Invalidate all stats caches ({@link TcStatsCache}/{@link TotalStatsCache}/{@link RetiredTcStatsCache})</li>
     * <li>Execute a new stats update to set all values to <b>0</b></li>
     * </ol>
     *
     * @see FoldingStatsCore#resetAllTeamCompetitionUserStats()
     * @see StatsScheduler#manualTeamCompetitionStatsParsing(ProcessingType)
     */
    public void resetTeamCompetitionStats() {
        try {
            // Pull stats one more time to get the latest values
            statsScheduler.manualTeamCompetitionStatsParsing(ProcessingType.SYNCHRONOUS);

            LOGGER.info("Resetting Team Competition stats");
            foldingStatsCore.resetAllTeamCompetitionUserStats();

            // Pull stats for new month
            statsScheduler.manualTeamCompetitionStatsParsing(ProcessingType.SYNCHRONOUS);
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error manually resetting TC stats", e);
        }
    }
}
