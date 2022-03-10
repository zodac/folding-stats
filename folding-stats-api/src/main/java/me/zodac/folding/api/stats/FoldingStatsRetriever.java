/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
    Stats getStats(FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException;

    /**
     * Gets the total {@link UserStats} for the given {@link User}.
     *
     * @param user the {@link User} to use in stats retrieval
     * @return the {@link UserStats} for the {@link User}
     * @throws ExternalConnectionException thrown if an error occurs connecting to an external service
     */
    UserStats getTotalStats(User user) throws ExternalConnectionException;
}
