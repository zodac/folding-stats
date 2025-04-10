/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.bean.tc.validation.retriever;

import net.zodac.folding.api.exception.ExternalConnectionException;
import net.zodac.folding.api.stats.FoldingStatsDetails;
import net.zodac.folding.api.stats.FoldingStatsRetriever;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.api.tc.stats.Stats;
import net.zodac.folding.api.tc.stats.UserStats;

/**
 * A test implementation of {@link FoldingStatsRetriever} that simulates an external connection issue.
 */
public class ExternalConnectionFoldingStatsRetriever implements FoldingStatsRetriever {

    @Override
    public Stats getStats(final FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException {
        throw new ExternalConnectionException("https://www.google.com", "Error connecting");
    }

    @Override
    public UserStats getTotalStats(final User user) throws ExternalConnectionException {
        throw new ExternalConnectionException("https://www.google.com", "Error connecting");
    }
}
