/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.bean.stats;

import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link FoldingStatsRetriever} which essentially wraps {@link HttpFoldingStatsRetriever}. Used so we can inject an instance
 * instead of creating an instance of {@link HttpFoldingStatsRetriever}.
 */
@Component
public class FoldingStatsRetrieverImpl implements FoldingStatsRetriever {

    private static final FoldingStatsRetriever HTTP_FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    @Override
    public Stats getStats(final FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException {
        return HTTP_FOLDING_STATS_RETRIEVER.getStats(foldingStatsDetails);
    }

    @Override
    public UserStats getTotalStats(final User user) throws ExternalConnectionException {
        return HTTP_FOLDING_STATS_RETRIEVER.getTotalStats(user);
    }
}
