package me.zodac.folding.api.stats;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.UserStats;

/**
 * Interface defining a class that can retrieve Folding@Home stats.
 */
public interface FoldingStatsRetriever {

    /**
     * Gets the total {@link UserStats} for the given {@link User}
     *
     * @param user the {@link User} whose {@link UserStats} are to be retrieved
     * @return the {@link UserStats} for the {@link User}
     * @throws FoldingException                thrown if an error occurs retrieving {@link UserStats}
     * @throws FoldingExternalServiceException thrown if an error occurs connecting to an external service
     */
    UserStats getTotalStats(final User user) throws FoldingException, FoldingExternalServiceException;

    /**
     * Gets the total points for the given {@link User}
     *
     * @param user the {@link User} whose points are to be retrieved
     * @return the points for the {@link User}
     * @throws FoldingException                thrown if an error occurs retrieving {@link UserStats}
     * @throws FoldingExternalServiceException thrown if an error occurs connecting to an external service
     */
    long getPoints(final User user) throws FoldingException, FoldingExternalServiceException;

    /**
     * Gets the total Work Units for the given {@link User}
     *
     * @param user the {@link User} whose units are to be retrieved
     * @return the units for the {@link User}
     * @throws FoldingException                thrown if an error occurs retrieving {@link UserStats}
     * @throws FoldingExternalServiceException thrown if an error occurs connecting to an external service
     */
    int getUnits(final User user) throws FoldingException, FoldingExternalServiceException;
}
