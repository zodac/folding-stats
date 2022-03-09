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
