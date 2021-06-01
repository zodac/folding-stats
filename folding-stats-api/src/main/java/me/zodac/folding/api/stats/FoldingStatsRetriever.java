package me.zodac.folding.api.stats;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
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
     * @throws FoldingException                thrown if an error occurs retrieving {@link Stats}
     * @throws FoldingExternalServiceException thrown if an error occurs connecting to an external service
     */
    Stats getStats(final FoldingStatsDetails foldingStatsDetails) throws FoldingException, FoldingExternalServiceException;

    /**
     * Gets the total {@link UserStats} for the given {@link User}.
     *
     * @param user the {@link User} to use in stats retrieval
     * @return the {@link UserStats} for the {@link User}
     * @throws FoldingException                thrown if an error occurs retrieving {@link User}
     * @throws FoldingExternalServiceException thrown if an error occurs connecting to an external service
     */
    UserStats getTotalStats(final User user) throws FoldingException, FoldingExternalServiceException;

    /**
     * Gets the total points for the given {@link FoldingStatsDetails}.
     *
     * @param foldingStatsDetails the {@link FoldingStatsDetails} to use in stats retrieval
     * @return the points for the {@link FoldingStatsDetails}
     * @throws FoldingException                thrown if an error occurs retrieving {@link UserStats}
     * @throws FoldingExternalServiceException thrown if an error occurs connecting to an external service
     */
    long getPoints(final FoldingStatsDetails foldingStatsDetails) throws FoldingException, FoldingExternalServiceException;

    /**
     * Gets the total Work Units for the given {@link FoldingStatsDetails}.
     *
     * @param foldingStatsDetails the {@link FoldingStatsDetails} to use in stats retrieval
     * @return the units for the {@link FoldingStatsDetails}
     * @throws FoldingException                thrown if an error occurs retrieving {@link UserStats}
     * @throws FoldingExternalServiceException thrown if an error occurs connecting to an external service
     */
    int getUnits(final FoldingStatsDetails foldingStatsDetails) throws FoldingException, FoldingExternalServiceException;
}
