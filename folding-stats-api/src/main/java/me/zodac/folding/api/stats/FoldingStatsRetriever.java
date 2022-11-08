/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
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
