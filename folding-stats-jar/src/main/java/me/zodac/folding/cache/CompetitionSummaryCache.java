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
