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

package me.zodac.folding.cache;

import me.zodac.folding.rest.api.tc.CompetitionSummary;

/**
 * Cache for the {@link CompetitionSummary}.
 *
 * <p>
 * <b>key:</b> {@link #COMPETITION_SUMMARY_ID}
 *
 * <p>
 * <b>value:</b> {@link CompetitionSummary}
 */
public final class CompetitionSummaryCache extends BaseCache<CompetitionSummary> {

    /**
     * While we extend {@link BaseCache}, we don't actually have multiple {@link CompetitionSummary}s, so we reuse the same ID.
     */
    public static final int COMPETITION_SUMMARY_ID = 1;

    private static final CompetitionSummaryCache INSTANCE = new CompetitionSummaryCache();

    private CompetitionSummaryCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link CompetitionSummaryCache}.
     *
     * @return the {@link CompetitionSummaryCache}
     */
    public static CompetitionSummaryCache getInstance() {
        return INSTANCE;
    }
}
