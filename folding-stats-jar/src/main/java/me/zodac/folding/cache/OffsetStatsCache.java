package me.zodac.folding.cache;

import me.zodac.folding.api.tc.stats.OffsetStats;

/**
 * Implementation of {@link BaseCache} for {@link OffsetStats}s.
 *
 * <p>
 * <b>key:</b> the {@link me.zodac.folding.api.tc.User} ID
 *
 * <p>
 * <b>value:</b> {@link OffsetStats}
 */
public final class OffsetStatsCache extends BaseCache<OffsetStats> {

    private static final OffsetStatsCache INSTANCE = new OffsetStatsCache();

    private OffsetStatsCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link OffsetStatsCache}.
     *
     * @return the {@link OffsetStatsCache}
     */
    public static OffsetStatsCache getInstance() {
        return INSTANCE;
    }
}
