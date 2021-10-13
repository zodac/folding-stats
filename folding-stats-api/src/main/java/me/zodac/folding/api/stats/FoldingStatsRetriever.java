package me.zodac.folding.api.stats;

import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;

/**
 * Interface defining a class that can retrieve Folding@Home stats.
 */
public interface FoldingStatsRetriever {

    /**
     * Gets the {@link Stats} for the given {@link FoldingStatsDetails}.
     *
     * @param foldingStatsDetails the {@link FoldingStatsDetails} to use in stats retrieval
     * @return the {@link Stats} for the {@link FoldingStatsDetails}
     * @throws ExternalConnectionException thrown if an error occurs connecting to an external service
     */
    Stats getStats(final FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException;

    /**
     * Gets the total {@link UserStats} for the given {@link User}.
     *
     * @param user the {@link User} to use in stats retrieval
     * @return the {@link UserStats} for the {@link User}
     * @throws ExternalConnectionException thrown if an error occurs connecting to an external service
     */
    UserStats getTotalStats(final User user) throws ExternalConnectionException;
}
