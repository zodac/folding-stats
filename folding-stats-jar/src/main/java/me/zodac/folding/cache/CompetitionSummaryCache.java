package me.zodac.folding.cache;

import java.util.Optional;
import me.zodac.folding.rest.api.tc.CompetitionSummary;

/**
 * Cache for the {@link CompetitionSummary}. There should only be a single {@link CompetitionSummary}, so this will cache the latest, or none.
 */
public final class CompetitionSummaryCache {

    private static final CompetitionSummaryCache INSTANCE = new CompetitionSummaryCache();

    private transient CompetitionSummary competitionSummary;

    private CompetitionSummaryCache() {

    }

    /**
     * Returns a singleton instance of {@link RetiredTcStatsCache}.
     *
     * @return the {@link RetiredTcStatsCache}
     */
    public static CompetitionSummaryCache getInstance() {
        return INSTANCE;
    }

    /**
     * Checks whether any {@link CompetitionSummary} has been cached.
     *
     * @return <code>true</code> is there is a cached {@link CompetitionSummary}
     */
    public boolean hasCachedResult() {
        return competitionSummary != null;
    }

    /**
     * Adds a {@link CompetitionSummary} to the cache, replacing any existing one.
     *
     * @param competitionSummary the {@link CompetitionSummary} to cache
     */
    public void add(final CompetitionSummary competitionSummary) {
        this.competitionSummary = competitionSummary;
    }

    /**
     * Retrieves the cached {@link CompetitionSummary}.
     *
     * <p>
     * Should check whether any {@link CompetitionSummary} is already cached by using {@link #hasCachedResult()}
     *
     * @return an {@link Optional} of the cached {@link CompetitionSummary}
     */
    public Optional<CompetitionSummary> get() {
        return Optional.ofNullable(competitionSummary);
    }
}
